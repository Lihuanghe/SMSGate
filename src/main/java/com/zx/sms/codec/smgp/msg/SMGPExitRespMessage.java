package com.zx.sms.codec.smgp.msg;



public class SMGPExitRespMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4438794269709080555L;

	public SMGPExitRespMessage() {
		this.commandId = SMGPConstants.SMGP_EXIT_RESP;
	}
	@Override
	protected byte[] getBody() throws Exception {
		return new byte[0];
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPExitRespMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("]");
		return buffer.toString();
	}
	@Override
	protected int setBody(byte[] bodyBytes) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}