package com.fantingame.game.gamecenter.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fantingame.game.gamecenter.bo.UserToken;
import com.fantingame.game.gamecenter.constant.OrderStatus;
import com.fantingame.game.gamecenter.constant.PartenerEnum;
import com.fantingame.game.gamecenter.constant.WebApiCode;
import com.fantingame.game.gamecenter.dao.PaymentOrderDao;
import com.fantingame.game.gamecenter.dao.ServerListDao;
import com.fantingame.game.gamecenter.exception.ServiceException;
import com.fantingame.game.gamecenter.model.GameServer;
import com.fantingame.game.gamecenter.model.PaymentOrder;
import com.fantingame.game.gamecenter.partener.PaymentObj;
import com.fantingame.game.gamecenter.partener.apple.AppleParkSdk;
import com.fantingame.game.gamecenter.partener.fantin.util.TradeInfo;
import com.fantingame.game.gamecenter.partener.ppassistant.PPAssistantPaymentObj;
import com.fantingame.game.gamecenter.sdk.GameApiSdk;
import com.fantingame.game.gamecenter.service.BasePartnerService;
import com.fantingame.game.gamecenter.service.PartnerService;
import com.fantingame.game.gamecenter.util.json.Json;

public class AppleParkServiceImpl extends BasePartnerService {

	public static Logger logger = Logger.getLogger(AppleParkServiceImpl.class);

	@Autowired
	private PaymentOrderDao paymentOrderDao;
	@Autowired
	private ServerListDao serverListDao;
	@Override
	public UserToken login(String token, String partnerId, String serverId, long timestamp, String sign, Map<String, String> params) {
		if (StringUtils.isBlank(token) || StringUtils.isBlank(partnerId) || StringUtils.isBlank(serverId) || timestamp == 0 || StringUtils.isBlank(sign)) {
			throw new ServiceException(WebApiCode.PARAM_ERROR, "参数不正确");
		}

		checkSign(token, partnerId, serverId, timestamp, sign);

		try {
			String info = AppleParkSdk.instance().verifySession(token);
			if(StringUtils.isNotBlank(info)){
				info = "{"+info+"}";
				JSONObject jsonObject = JSON.parseObject(info);
				if(jsonObject.containsKey("userid")){
					GameServer gameServer = serverListDao.getServerByServerIdAndPartenerId(serverId, partnerId);

					return GameApiSdk.getInstance().loadUserToken(jsonObject.getString("userid"), partnerId, serverId,"0",gameServer, params);
				}
			}
		} catch (Exception e) {
			throw new ServiceException(WebApiCode.LOGIN_VALID_FAILD, "登录验证失败");
		}
		throw new ServiceException(PartnerService.LOGIN_VALID_FAILD, "登录验证失败");

	}

	@Override
	public boolean doPayment(String orderId, String partnerUserId, boolean success, String partnerOrderId, BigDecimal finishAmount, Map<String, String> reqInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doPayment(PaymentObj paymentObj) {
		if (paymentObj == null) {
			logger.error("paymentObj为空");
			return false;
		}

		PPAssistantPaymentObj cb = (PPAssistantPaymentObj) paymentObj;

		if (!AppleParkSdk.instance().checkPayCallbackSign(cb)) {
			logger.error("签名不正确" + Json.toJson(paymentObj));
			return false;
		}
		logger.info("game id:" + cb.getBillno());
		PaymentOrder order = paymentOrderDao.get(cb.getBillno());
		logger.info("应用订单：" + Json.toJson(order));
		if (order == null) {
			logger.error("订单为空：" + Json.toJson(cb));
			return false;
		}
		if (order.getStatus() == OrderStatus.STATUS_FINISH) {
			logger.error("订单已经完成" + Json.toJson(cb));
			return true;
		}
		BigDecimal finishAmount = new BigDecimal(cb.getAmount());
		if (!cb.getStatus().equals("0")) {
			logger.error("充值失败：" + Json.toJson(cb));
			this.paymentOrderDao.updateStatus(order.getOrderId(), OrderStatus.STATUS_ERROR, cb.getOrder_id(), finishAmount, "");
			return false;
		}
		int gold = (int) (finishAmount.doubleValue() * 10);
		// 更新订单状态
		if (this.paymentOrderDao.updateStatus(order.getOrderId(), OrderStatus.STATUS_FINISH, cb.getOrder_id(), finishAmount, "")) {
			GameServer gameServer = serverListDao.getServerByServerIdAndPartenerId(order.getServerId(), order.getPartnerId());
			// 请求游戏服，发放游戏货币
			if (!GameApiSdk.getInstance().doPayment(order.getPartnerId(), order.getServerId(), order.getPartnerUserId(), "", order.getOrderId(), finishAmount, gold, "", "",gameServer)) {
				// 如果失败，要把订单置为未支付
				this.paymentOrderDao.updateStatus(order.getOrderId(), OrderStatus.STATUS_NOT_PAY, cb.getOrder_id(), finishAmount, "");
				logger.error("发货失败：" + Json.toJson(cb));
				return false;
			} else {
				logger.info("支付成功：" + Json.toJson(cb));
				return true;
			}
		}
		this.paymentOrderDao.updateStatus(order.getOrderId(), OrderStatus.STATUS_ERROR, cb.getOrder_id(), finishAmount, "");
		logger.error("充值失败：" + Json.toJson(cb));
		return false;
	}

	@Override
	public String createOrder(String partnerId, String serverId, String partnerUserId, BigDecimal amount, String tradeName, String qn) {
		TradeInfo info = createOrderInfo(partnerId, serverId, partnerUserId, amount, tradeName);
		return Json.toJson(info);
	}

	@Override
	public PartenerEnum getPatenerEnum() {
		return PartenerEnum.ApplePark;
	}
}
