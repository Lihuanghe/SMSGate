/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppQueryRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = -7762194632879048169L;
	private String time = String.format("%tY%<tm%<td", CachedMillisecondClock.INS.now());
	private short queryType = 0;
	private String queryCode = GlobalConstance.emptyString;
	private String reserve = GlobalConstance.emptyString;

	public CmppQueryRequestMessage(Header header) {
		super(CmppPacketType.CMPPQUERYREQUEST, header);
	}

	public CmppQueryRequestMessage() {
		super(CmppPacketType.CMPPQUERYREQUEST);
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the queryType
	 */
	public short getQueryType() {
		return queryType;
	}

	/**
	 * @param queryType
	 *            the queryType to set
	 */
	public void setQueryType(short queryType) {
		this.queryType = queryType;
	}

	/**
	 * @return the queryCode
	 */
	public String getQueryCode() {
		return queryCode;
	}

	/**
	 * @param queryCode
	 *            the queryCode to set
	 */
	public void setQueryCode(String queryCode) {
		this.queryCode = queryCode;
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

}
