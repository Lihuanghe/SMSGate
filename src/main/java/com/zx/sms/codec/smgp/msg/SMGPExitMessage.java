package com.zx.sms.codec.smgp.msg;

public class SMGPExitMessage extends SMGPBaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8459840003695284990L;

	public SMGPExitMessage() {
		this.commandId = SMGPConstants.SMGP_EXIT_TEST;
	}
	@Override
	protected byte[] getBody(int version) throws Exception {
		return new byte[0];
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPExitMessage:[sequenceNumber=")
				.append(sequenceString()).append("]");
		return buffer.toString();
	}
	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}