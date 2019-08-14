package com.zx.sms.codec.smgp.msg;


public class SMGPActiveTestMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5589548347716994701L;

	public SMGPActiveTestMessage() {
		this.commandId = SMGPConstants.SMGP_ACTIVE_TEST;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPActiveTestMessage:[sequenceNumber=")
			  .append(sequenceString()).append("]");
		return buffer.toString();
	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		return new byte[0];
	}

	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}