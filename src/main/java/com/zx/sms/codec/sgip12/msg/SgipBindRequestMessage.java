/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SgipBindRequestMessage extends SgipDefaultMessage {
	private static final long serialVersionUID = 776190389687326556L;

	/**
	 * 登录类型。 1：SP向SMG建立的连接，用于发送命令 2：SMG向SP建立的连接，用于发送命令 3：SMG之间建立的连接，用于转发命令
	 * 4：SMG向GNS建立的连接，用于路由表的检索和维护 5：GNS向SMG建立的连接，用于路由表的更新
	 * 6：主备GNS之间建立的连接，用于主备路由表的一致性 11：SP与SMG以及SMG之间建立的测试连接，用于跟踪测试 其它：保留
	 */
	private short loginType = 1;
	private String loginName = GlobalConstance.emptyString;
	private String loginPassowrd = GlobalConstance.emptyString;
	private String reserve = GlobalConstance.emptyString;

	public SgipBindRequestMessage(Header header) {
		super(SgipPacketType.BINDREQUEST, header);
	}

	/**
	 * 
	 * @param packetType
	 */
	public SgipBindRequestMessage() {
		super(SgipPacketType.BINDREQUEST);
	}

	/**
	 * @return the loginType
	 */
	public short getLoginType() {
		return loginType;
	}

	/**
	 * @param loginType
	 *            the loginType to set
	 */
	public void setLoginType(short loginType) {
		this.loginType = loginType;
	}

	/**
	 * @return the loginName
	 */
	public String getLoginName() {
		return loginName;
	}

	/**
	 * @param loginName
	 *            the loginName to set
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	/**
	 * @return the loginPassowrd
	 */
	public String getLoginPassowrd() {
		return loginPassowrd;
	}

	/**
	 * @param loginPassowrd
	 *            the loginPassowrd to set
	 */
	public void setLoginPassowrd(String loginPassowrd) {
		this.loginPassowrd = loginPassowrd;
	}

	/**
	 * @return the reserve
	 */
	public String getReserve() {
		return reserve;
	}

	/**
	 * @param reserve
	 *            the reserve to set
	 */
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("BindRequestMessage [loginType=%s, loginName=%s, loginPassowrd=%s, reserve=%s, seq=%s, header=%s]", loginType, loginName,
				loginPassowrd, reserve, getSequenceNumber(),getHeader());
	}

}
