package com.zx.sms.codec.smgp.msg;

import java.io.Serializable;
import com.zx.sms.common.util.StandardCharsets;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.zx.sms.codec.smgp.util.ByteUtil;
import com.zx.sms.codec.smgp.util.SMGPMsgIdUtil;

public class SMGPReportData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1038868368719047952L;
	private static final Logger logger = LoggerFactory.getLogger(SMGPReportData.class);
	public static final int LENGTH = 10 + 3 + 3 + 10 + 10 + 7 + 3 + 20 + "id: sub: dlvrd: submit date: done date: stat: err: text:".length();

	private MsgId msgId; // 10
	private String sub = "001"; // 3
	private String dlvrd = "001"; // 3
	private String subTime; // 10;
	private String doneTime; // 10;
	private String stat; // 7
	private String err = ""; // 3
	private String txt = ""; // 20

	// id:XXXXXXXXXX sub:000 dlvrd:000 submit date:0901151559 done
	// date:0901151559 stat:DELIVRD err:000 text:
	public boolean fromBytes(byte[] bytes) throws Exception {

		try {
			int offset = 0;
			byte[] tmp = null;

			offset += "id:".length();

			byte[] msgId = new byte[10];
			System.arraycopy(bytes, offset, msgId, 0, 10);
			this.msgId = SMGPMsgIdUtil.bytes2MsgId(msgId);

			offset += 10;

			offset += " sub:".length();

			tmp = new byte[3];
			System.arraycopy(bytes, offset, tmp, 0, 3);
			sub = new String(ByteUtil.rtrimBytes(tmp));
			offset += 3;

			offset += " dlvrd:".length();

			tmp = new byte[3];
			System.arraycopy(bytes, offset, tmp, 0, 3);
			dlvrd = new String(ByteUtil.rtrimBytes(tmp));
			offset += 3;

			offset += " submit date:".length();

			tmp = new byte[10];
			System.arraycopy(bytes, offset, tmp, 0, 10);
			subTime = new String(ByteUtil.rtrimBytes(tmp));
			offset += 10;

			offset += " done date:".length();

			tmp = new byte[10];
			System.arraycopy(bytes, offset, tmp, 0, 10);
			doneTime = new String(ByteUtil.rtrimBytes(tmp));
			offset += 10;

			offset += " stat:".length();

			tmp = new byte[7];
			System.arraycopy(bytes, offset, tmp, 0, 7);
			stat = new String(ByteUtil.rtrimBytes(tmp));
			offset += 7;

			offset += " err:".length();

			tmp = new byte[3];
			System.arraycopy(bytes, offset, tmp, 0, 3);
			err = new String(ByteUtil.rtrimBytes(tmp));
			offset += 3;

			offset += " text:".length();

			tmp = new byte[20];
			System.arraycopy(bytes, offset, tmp, 0, bytes.length-offset);
			byte[] text_length = new byte[] {tmp[0],tmp[1],tmp[2]};
			String t_text_length = StringUtils.trim(new String(text_length,StandardCharsets.US_ASCII));
			if(tmp[0]>0 && tmp[1] > 0 && tmp[2]>0 && isAllOfASCII(text_length) && StringUtils.isNumeric(t_text_length)) {
				
				
				byte[] t_text_txt = new byte[17];
				System.arraycopy(bytes, offset+3, t_text_txt, 0, 17);
				
				if(isAllOfASCII(t_text_txt)) {
					txt = t_text_length+new String(ByteUtil.rtrimBytes(t_text_txt),StandardCharsets.US_ASCII);
				}else {
					txt = t_text_length+new String(ByteUtil.rtrimBytes(t_text_txt),StandardCharsets.UTF_16BE);
				}
			}else {
				if(isAllOfASCII(tmp)) {
					txt = new String(ByteUtil.rtrimBytes(tmp),StandardCharsets.US_ASCII);
				}else {
					txt = new String(ByteUtil.rtrimBytes(tmp),StandardCharsets.UTF_16BE);
				}
			}

			offset += 20;
			return true;
		} catch (Exception ex) {
			logger.warn("parse data err length:{} ; 0x{}",bytes.length,Hex.encodeHexString(bytes));
			return true;
		}
	}
	
	private boolean isAllOfASCII(byte[] seq) {
		//判断是否是ASCII码
		for(byte b : seq) {
			if(b < 0 || b > 0x7f) {
				return false;
			}
		}
		return true;
	}

	public byte[] toBytes() throws Exception {
		int offset = 0;
		byte[] bytes = new byte[LENGTH];

		System.arraycopy("id:".getBytes(), 0, bytes, offset, "id:".length());
		offset += "id:".length();
		byte[] b_msgId = SMGPMsgIdUtil.msgId2Bytes(msgId);
		System.arraycopy(b_msgId, 0, bytes, offset, 10);
		offset += 10;

		System.arraycopy(" sub:".getBytes(), 0, bytes, offset, " sub:".length());
		offset += " sub:".length();

		ByteUtil.rfillBytes(sub.getBytes(), 3, bytes, offset);
		offset += 3;

		System.arraycopy(" dlvrd:".getBytes(), 0, bytes, offset, " dlvrd:".length());
		offset += " dlvrd:".length();

		ByteUtil.rfillBytes(dlvrd.getBytes(), 3, bytes, offset);
		offset += 3;

		System.arraycopy(" submit date:".getBytes(), 0, bytes, offset, " submit date:".length());
		offset += " submit date:".length();

		ByteUtil.rfillBytes(subTime.getBytes(), 10, bytes, offset);
		offset += 10;

		System.arraycopy(" done date:".getBytes(), 0, bytes, offset, " done date:".length());
		offset += " done date:".length();

		ByteUtil.rfillBytes(doneTime.getBytes(), 10, bytes, offset);
		offset += 10;

		System.arraycopy(" stat:".getBytes(), 0, bytes, offset, " stat:".length());
		offset += " stat:".length();

		ByteUtil.rfillBytes(stat.getBytes(), 7, bytes, offset);
		offset += 7;

		System.arraycopy(" err:".getBytes(), 0, bytes, offset, " err:".length());
		offset += " err:".length();

		ByteUtil.rfillBytes(err.getBytes(), 3, bytes, offset);
		offset += 3;

		System.arraycopy(" text:".getBytes(), 0, bytes, offset, " text:".length());
		offset += " text:".length();
		
		if(StringUtils.isNotBlank(txt) && txt.length()>3 && isAllOfASCII(txt.substring(0, 3).getBytes()) && StringUtils.isNumeric(txt.substring(0, 3))) {
			ByteUtil.rfillBytes(txt.substring(0, 3).getBytes(), 3, bytes, offset);
			String txt_content = txt.substring(3);
			if(CharMatcher.ASCII.matchesAllOf(txt_content)) {
				byte[] tmp = txt_content.getBytes(StandardCharsets.US_ASCII);
				ByteUtil.rfillBytes(tmp, 17, bytes, offset+3);
			}else {
				byte[] tmp = txt_content.getBytes(StandardCharsets.UTF_16BE);
				ByteUtil.rfillBytes(tmp, 17, bytes, offset+3);
			}
		}else {
			if(CharMatcher.ASCII.matchesAllOf(txt)) {
				byte[] tmp = txt.getBytes(StandardCharsets.US_ASCII);
				ByteUtil.rfillBytes(tmp, 20, bytes, offset);
			}else {
				byte[] tmp = txt.getBytes(StandardCharsets.UTF_16BE);
				ByteUtil.rfillBytes(tmp, 20, bytes, offset);
			}
		}

		offset += 20;

		return bytes;
	}

	public String getDlvrd() {
		return dlvrd;
	}

	public void setDlvrd(String dlvrd) {
		this.dlvrd = dlvrd;
	}

	public String getDoneTime() {
		return doneTime;
	}

	public void setDoneTime(String doneTime) {
		this.doneTime = doneTime;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public MsgId getMsgId() {
		return msgId;
	}

	public void setMsgId(MsgId msgId) {
		this.msgId = msgId;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getSubTime() {
		return subTime;
	}

	public void setSubTime(String subTime) {
		this.subTime = subTime;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt == null?"":txt;
	}

	private String msgIdString() {
		return msgId.toString();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{msgId=").append(msgIdString()).append(",").append("sub=").append(sub).append(",").append("dlvrd=").append(dlvrd).append(",")
				.append("subTime=").append(subTime).append(",").append("doneTime=").append(doneTime).append(",").append("stat=").append(stat).append(",")
				.append("err=").append(err).append(",").append("text=").append(txt).append("}");
		return buffer.toString();
	}
}
