<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../common/taglib.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>活动中心奖励列表</title>

		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<link href="../../css/main.css" rel="stylesheet" type="text/css" />
	</head>
	<script type="text/javascript" src="../../js/ajax.js"></script>
	<script type="text/javascript">
		function reflashconstantcache() {
			var ajaxobj = new Ajax();
			ajaxobj.url="reflashConstantCache?cacheType=<s:property value='@com.dataconfig.constant.ReflashCacheConstant@REFLASH_TYPE_A_REWARD_CONSTANT'/>&number="+Math.random();
			ajaxobj.callback=function(){
		    	var responseMsg = eval('(' + ajaxobj.gettext() + ')');
		    	//alert(responseMsg.erroCodeNum);
		    	if (responseMsg.erroCodeNum == 0) {
					alert('刷新活动中心奖励缓存成功!');
				} else {
					alert(responseMsg.erroDescrip+'刷新活动中心奖励缓存失败!');
				}
		    }
			ajaxobj.send();
		}
	</script>
	<body>
		<input type="button" value="刷新活动中心奖励缓存" class="button" onclick="reflashconstantcache()" />
		<table id="table" cellpadding="3" cellspacing="1" border="0" width="100%" align=center>
			<tr class="border">
				<td class="td_title" colspan="10" align="center">
					<center>
						活动中心奖励列表
					</center>
				</td>
			</tr>
			<tr>
				<td>
					奖励编号
				</td>
				<td>
					预留字段
				</td>
				<td>
					奖励内容
				</td>
				<td>
					奖励类型
				</td>
				<td>
					奖励描述
				</td>

			</tr>
			<s:iterator var="rewardConstant" value="arewardConstantsList">
				<tr>
					<td>
						${rewardConstant.rewrdId}
					</td>
					<td>
						${rewardConstant.targetId}
					</td>
					<td>
						${rewardConstant.rewardContent}
					</td>
					<td>
						${rewardConstant.type}
					</td>
					<td>
						${rewardConstant.rewardDesc}
					</td>
				</tr>
			</s:iterator>
			<tr>
				<td colspan="10">
					<aldtags:pageTag />
				</td>
			</tr>
		</table>
	</body>
</html>