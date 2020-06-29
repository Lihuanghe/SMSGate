package com.zx.sms.codec.cmpp.wap;

import org.marre.sms.AbstractSmsDcs;

import com.zx.sms.common.GlobalConstance;

/**
 * MT,MO长短信
 **/
public class LongMessageFrame {
	private static final long serialVersionUID = -8554060199834235624L;
	private short pktotal = 1;
	private short pknumber = 1;
	private short tppid = 0;// 0是普通GSM 类型，点到点方式 ,127 :写sim卡
	private short tpudhi = 0; // 0:msgcontent不带协议头。1:带有协议头
	private AbstractSmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	// encode septet
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;

	private String contentPart;
	
	private long sequence;
	
	/**
	 * @return the pktotal
	 */
	public short getPktotal() {
		return pktotal;
	}

	/**
	 * @param pktotal
	 *            the pktotal to set
	 */
	public void setPktotal(short pktotal) {
		this.pktotal = pktotal;
	}

	/**
	 * @return the pknumber
	 */
	public short getPknumber() {
		return pknumber;
	}

	/**
	 * @param pknumber
	 *            the pknumber to set
	 */
	public void setPknumber(short pknumber) {
		this.pknumber = pknumber;
	}

	short getTppid() {
		return tppid;
	}

	public void setTppid(short tppid) {
		this.tppid = tppid;
	}

	/**
	 * @return the tpudhi
	 */
	public short getTpudhi() {
		return tpudhi;
	}

	/**
	 * @param tpudhi
	 *            the tpudhi to set
	 */
	public void setTpudhi(short tpudhi) {
		this.tpudhi = tpudhi;
	}

	/**
	 * @return the msgfmt
	 */
	public AbstractSmsDcs getMsgfmt() {
		return msgfmt;
	}

	/**
	 * @param msgfmt
	 *            the msgfmt to set
	 */
	public void setMsgfmt(AbstractSmsDcs msgfmt) {
		this.msgfmt = msgfmt;
	}

	/**
	 * @return the msgLength
	 */
	public short getMsgLength() {
		return (short)msgContentBytes.length;
	}

	public void setMsgLength(short msgLength) {
		
	}

	/**
	 * @return the msgContentBytes
	 */
	public byte[] getMsgContentBytes() {
		return msgContentBytes;
	}

	/**
	 * @param msgContentBytes
	 *            the msgContentBytes to set
	 */
	public void setMsgContentBytes(byte[] msgContentBytes) {
		this.msgContentBytes = msgContentBytes;
	}

	public String getContentPart() {
		return contentPart;
	}

	public void setContentPart(String contentPart) {
		this.contentPart = contentPart;
	}


	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	// get unencode septet bytes
	public byte[] getPayloadbytes(int udhl) {
		if (udhl > 0) {
			int payloadlength = getMsgLength() - udhl - 1;
			byte[] payload = new byte[payloadlength];
			System.arraycopy(msgContentBytes, udhl + 1, payload, 0, payloadlength);
			return payload;
		} else {
			return msgContentBytes;
		}
	}
}
