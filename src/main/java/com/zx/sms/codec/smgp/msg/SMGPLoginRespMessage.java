package com.zx.sms.codec.smgp.msg;

import org.apache.commons.codec.binary.Hex;

import com.zx.sms.codec.smgp.util.ByteUtil;

public class SMGPLoginRespMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7921237368321175055L;

	public SMGPLoginRespMessage() {
		this.commandId = SMGPConstants.SMGP_LOGIN_RESP;
	}

	private int status; // 4

	private byte[] serverAuth=new byte[16]; // 16

	private byte version; // 1

	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		int offset = 0;
		
		status = ByteUtil.byte2int(bodyBytes, offset);
		offset += 4;

		serverAuth=new byte[16];
		System.arraycopy(bodyBytes, offset, serverAuth, 0, 16);
		offset += 16;

		this.version = bodyBytes[offset];
		offset += 1;
		return offset;
	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		int len = 4 + 16 + 1;
		int offset = 0;
		byte[] bodyBytes = new byte[len];
		ByteUtil.int2byte(status, bodyBytes, offset);
		offset += 4;

		System.arraycopy(serverAuth, 0, bodyBytes, offset, 16);
		offset += 16;

		bodyBytes[offset] = this.version;
		offset += 1;

		return bodyBytes;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public byte[] getServerAuth() {
		return this.serverAuth;
	}

	public void setServerAuth(byte[] serverAuth) {
		this.serverAuth = serverAuth;
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
		buffer.append("SMGPLoginRespMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("status=").append(status).append(",");
		buffer.append("serverAuth=").append(Hex.encodeHex(serverAuth)).append(",");
		buffer.append("version=").append(version).append("]");

		return buffer.toString();
	}
}