package com.fantingame.game.msgbody.client.hero;

import com.fantingame.game.msgbody.common.io.iface.IXInputStream;
import com.fantingame.game.msgbody.common.io.iface.IXOutStream;
import com.fantingame.game.msgbody.common.model.ICodeAble;
import java.io.IOException;


/**遣散英雄**/
public class HeroAction_disbandReq implements ICodeAble {

		/**用户英雄id**/
	private String userHeroId="";

	
	@Override
	public void encode(IXOutStream outputStream) throws IOException {
		outputStream.writeUTF(userHeroId);


	}

	@Override
	public void decode(IXInputStream inputStream) throws IOException {
		userHeroId = inputStream.readUTF();


	}
	
		/**用户英雄id**/
    public String getUserHeroId() {
		return userHeroId;
	}
	/**用户英雄id**/
    public void setUserHeroId(String userHeroId) {
		this.userHeroId = userHeroId;
	}

	
	
}
