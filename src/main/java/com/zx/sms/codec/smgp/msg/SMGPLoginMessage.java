package com.zx.sms.codec.smgp.msg;

import org.apache.commons.codec.binary.Hex;

import com.zx.sms.codec.smgp.util.ByteUtil;

public class SMGPLoginMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2752058312140181133L;

	public SMGPLoginMessage() {
		this.commandId = SMGPConstants.SMGP_LOGIN;
	}

	private String clientId; // 8

	private byte[] clientAuth=new byte[16]; // 16

	/**
	 * 0 : Send Mode 只发送
	 * 1 : Receive Mode 只接收
	 * 2 : Transmit Mode 收发
	 */
	private byte loginMode; // 1

	private byte version; // 1

	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		int offset = 0;
		byte[] tmp = null;

		tmp = new byte[8];
		System.arraycopy(bodyBytes, offset, tmp, 0, 8);
		clientId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 8;

		System.arraycopy(bodyBytes, offset, clientAuth, 0, 16);
		offset += 16;

		loginMode = bodyBytes[offset];
		offset += 1;

		setTimestamp(ByteUtil.byte2int(bodyBytes, offset));
		offset += 4;

		this.version = bodyBytes[offset];
		offset += 1;
		return offset;
	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		int len = 8 + 16 + 1 + 4 + 1;
		int offset = 0;
		byte[] bodyBytes = new byte[len];
		ByteUtil.rfillBytes(clientId.getBytes(), 8, bodyBytes, offset);
		offset += 8;

		System.arraycopy(clientAuth, 0, bodyBytes, offset, 16);
		offset += 16;

		bodyBytes[offset] = loginMode;
		offset += 1;

		ByteUtil.int2byte((int)getTimestamp(), bodyBytes, offset);
		offset += 4;

		bodyBytes[offset] = this.version;
		offset += 1;

		return bodyBytes;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public byte[] getClientAuth() {
		return this.clientAuth;
	}

	public void setClientAuth(byte[] clientAuth) {
		this.clientAuth = clientAuth;
	}

	public byte getLoginMode() {
		return this.loginMode;
	}

	public void setLoginMode(byte loginMode) {
		this.loginMode = loginMode;
	}

	public byte getVersion() {
		return this.version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPLoginMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("clientId=").append(clientId).append(",");
		buffer.append("clientAuth=").append(Hex.encodeHex(clientAuth)).append(",");
		buffer.append("loginMode=").append(loginMode).append(",");
		buffer.append("timestamp=").append(getTimestamp()).append(",");
		buffer.append("version=").append(version).append("]");
		return buffer.toString();
	}
}