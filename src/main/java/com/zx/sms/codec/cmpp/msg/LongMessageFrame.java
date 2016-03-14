package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.common.GlobalConstance;

/**
 * MT,MO长短信
 **/
public class LongMessageFrame  {
	private static final long serialVersionUID = -8554060199834235624L;
	private short pktotal = 1;
	private short pknumber = 1;
	private short tppid = 0;//0是普通GSM 类型，点到点方式 ,127 :写sim卡
	private short tpudhi = 0; //0:msgcontent不带协议头。1:带有协议头
	private short msgfmt = 8;
	private short msgLength = 140;
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;

	private String contentPart;

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
	/**
	 * @return the tppid
	 */
	public short getTppid() {
		return tppid;
	}

	/**
	 * @param tppid
	 *            the tppid to set
	 */
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
	public short getMsgfmt() {
		return msgfmt;
	}

	/**
	 * @param msgfmt
	 *            the msgfmt to set
	 */
	public void setMsgfmt(short msgfmt) {
		this.msgfmt = msgfmt;
	}

	/**
	 * @return the msgLength
	 */
	public short getMsgLength() {
		return msgLength;
	}
	
	public void setMsgLength(short msgLength) {
		this.msgLength = msgLength;
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
	//如果是7bit编码，需要计算真实的数据长度
	public int getPayloadLength(int udl){
		if(this.msgfmt == 0)
			return LongMessageFrameHolder.octetLengthfromseptetsLength(udl);
		else
			return udl;
	}
	
	public byte[] getPayloadbytes(int udhl){
		if(udhl > 0){
			int payloadlength = msgLength - udhl -1;
			byte[] payload = new byte[payloadlength];
			System.arraycopy(msgContentBytes, udhl+1, payload, 0,payloadlength);
			//如果是7bit编码.先转也8bit编码
			if(this.msgfmt == 0)
				
				return LongMessageFrameHolder.octetStream2septetStream(payload);
			else
				return payload;
		}else{
			if(this.msgfmt == 0)
				return LongMessageFrameHolder.octetStream2septetStream(msgContentBytes);
			else
				return msgContentBytes;
		}
	}
}
