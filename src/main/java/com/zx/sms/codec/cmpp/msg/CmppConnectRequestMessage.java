package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * 
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppConnectRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = -4852540410843278872L;
	private String sourceAddr = GlobalConstance.emptyString;
	private byte[] authenticatorSource = GlobalConstance.emptyBytes;
	private short version = 0x30;
	private long timestamp = 0L;

	public CmppConnectRequestMessage(Header header) {
		super(CmppPacketType.CMPPCONNECTREQUEST, header);
	}

	public CmppConnectRequestMessage() {
		super(CmppPacketType.CMPPCONNECTREQUEST);
	}

	/**
	 * @return the sourceAddr
	 */
	public String getSourceAddr() {
		return sourceAddr;
	}

	/**
	 * @param SP
	 *            id，maxLength is 6
	 */
	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
	}

	/**
	 * @return the authenticatorSource
	 */
	public byte[] getAuthenticatorSource() {
		return authenticatorSource;
	}

	/**
	 * @param authenticatorSource
	 *            the authenticatorSource to set
	 */
	public void setAuthenticatorSource(byte[] authenticatorSource) {
		this.authenticatorSource = authenticatorSource;
	}

	/**
	 * @return the version
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(short version) {
		this.version = version;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return String.format("CmppConnectRequestMessage [version=%s, sourceAddr=%s, sequenceId=%s]", version, sourceAddr, getHeader().getSequenceId());
	}
}
