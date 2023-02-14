package com.zx.sms.codec.smgp.msg;


public class SMGPUnknownMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5589548347716994701L;

	public SMGPUnknownMessage(int commandId) {
		this.commandId = commandId;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPUnknowMessage:[sequenceNumber=")
			  .append(sequenceString())
			  .append(" ;commandId=").append(commandId).append("]");
		return buffer.toString();
	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		return new byte[0];
	}

	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		return 0;
	}
}