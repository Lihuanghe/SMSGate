package com.zx.sms.codec.cmpp.wap;

import java.io.Serializable;

import org.marre.sms.AbstractSmsDcs;

import com.zx.sms.common.GlobalConstance;

import io.netty.buffer.ByteBufUtil;

/**
 * MT,MO长短信
 **/
public class LongMessageFrame implements Serializable{
	private static final long serialVersionUID = -8554060199834235624L;
	
	private short pkseq = 0;
	private byte pktotal = 1;
	private byte pknumber = 1;
	private short tppid = 0;// 0是普通GSM 类型，点到点方式 ,127 :写sim卡
	private short tpudhi = 0; // 0:msgcontent不带协议头。1:带有协议头
	private AbstractSmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	// encode septet
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;

	private long sequence;
	
	public short getPkseq() {
		return pkseq;
	}

	public void setPkseq(short pkseq) {
		this.pkseq = pkseq;
	}

	/**
	 * @return the pktotal
	 */
	public byte getPktotal() {
		return pktotal;
	}

	/**
	 * @param pktotal
	 *            the pktotal to set
	 */
	public void setPktotal(byte pktotal) {
		this.pktotal = pktotal;
	}

	/**
	 * @return the pknumber
	 */
	public byte getPknumber() {
		return pknumber;
	}

	/**
	 * @param pknumber
	 *            the pknumber to set
	 */
	public void setPknumber(byte pknumber) {
		this.pknumber = pknumber;
	}

	/**
	 * 该字段只为处理 (U)SIM Toolkit Securit 用于远程写卡,因此关闭public 避免其它地方误用
	 */
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

	@Override
	public String toString() {
		return "LongMessageFrame [pkseq=" + pkseq + ", pktotal=" + pktotal + ", pknumber=" + pknumber + ", tppid="
				+ tppid + ", tpudhi=" + tpudhi + ", msgfmt=" + msgfmt + ", msgContentBytes="
				+ ByteBufUtil.hexDump(msgContentBytes) + ", sequence=" + sequence + "]";
	}
	
	
}
