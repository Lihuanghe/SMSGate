package com.zx.sms.codec.smgp.msg;


public class SMGPActiveTestRespMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5971146145881161402L;

	public SMGPActiveTestRespMessage() {
		this.commandId = SMGPConstants.SMGP_ACTIVE_TEST_RESP;
	}
	@Override
	protected byte[] getBody(int version) throws Exception {
		return new byte[0];
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPActiveTestRespMessage:[sequenceNumber=").append(
				sequenceString()).append("]");
		
		return buffer.toString();
	}
	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}