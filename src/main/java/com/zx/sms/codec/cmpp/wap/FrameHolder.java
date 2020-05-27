package com.zx.sms.codec.cmpp.wap;

import java.util.BitSet;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.marre.sms.AbstractSmsDcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.CachedMillisecondClock;

// 用来保存一条短信的各个片断
/**
 * TP_udhi ：0代表内容体里不含有协议头信息
 * 1代表内容含有协议头信息（长短信，push短信等都是在内容体上含有头内容的）当设置内容体包含协议头
 * ，需要根据协议写入相应的信息，长短信协议头有两种：<br/>
 * 6位协议头格式：05 00 03 XX MM NN<br/>
 * byte 1 : 05, 表示剩余协议头的长度<br/>
 * byte 2 : 00, 这个值在GSM 03.40规范9.2.3.24.1中规定，表示随后的这批超长短信的标识位长度为1（格式中的XX值）。<br/>
 * byte 3 : 03, 这个值表示剩下短信标识的长度<br/>
 * byte 4 : XX，这批短信的唯一标志，事实上，SME(手机或者SP)把消息合并完之后，就重新记录，所以这个标志是否唯 一并不是很 重要。<br/>
 * byte 5 : MM, 这批短信的数量。如果一个超长短信总共5条，这里的值就是5。<br/>
 * byte 6 : NN, 这批短信的数量。如果当前短信是这批短信中的第一条的值是1，第二条的值是2。<br/>
 * 例如：05 00 03 39 02 01 <br/>
 * 
 * 7 位的协议头格式：06 08 04 XX XX MM NN<br/>
 * byte 1 : 06, 表示剩余协议头的长度<br/>
 * byte 2 : 08, 这个值在GSM 03.40规范9.2.3.24.1中规定，表示随后的这批超长短信的标识位长度为2（格式中的XX值）。<br/>
 * byte 3 : 04, 这个值表示剩下短信标识的长度<br/>
 * byte 4-5 : XX
 * XX，这批短信的唯一标志，事实上，SME(手机或者SP)把消息合并完之后，就重新记录，所以这个标志是否唯一并不是很重要。<br/>
 * byte 6 : MM, 这批短信的数量。如果一个超长短信总共5条，这里的值就是5。<br/>
 * byte 7 : NN, 这批短信的数量。如果当前短信是这批短信中的第一条的值是1，第二条的值是2。<br/>
 * 例如：06 08 04 00 39 02 01 <br/>
 **/

class FrameHolder {
	private static final Logger logger = LoggerFactory.getLogger(FrameHolder.class);
	// 这个字段目前只在当分片丢失时方便跟踪
	private String serviceNum;
	private long sequence;
	private long timestamp = CachedMillisecondClock.INS.now();
	/**
	 * 长短信的总分片数量
	 * */
	private int totalLength = 0;
	int frameKey;
	// 保存帧的Map,每帧都有一个唯一码。以这个唯一码做key
	private byte[][] content;

	private int totalbyteLength = 0;

	private BitSet idxBitset;

	private AbstractSmsDcs msgfmt;

	private InformationElement appUDHinfo;
	
	private LongSMSMessage msg ;

	// 用来保存应用类型，如文本短信或者wap短信
	public void setAppUDHinfo(InformationElement appUDHinfo) {
		this.appUDHinfo = appUDHinfo;
	}

	public InformationElement getAppUDHinfo() {
		return this.appUDHinfo;
	}

	public void setServiceNum(String serviceNum) {
		this.serviceNum = serviceNum;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public FrameHolder(int frameKey, int totalLength) {
		this.frameKey = frameKey;
		this.totalLength = totalLength;

		this.content = new byte[totalLength][];
		this.idxBitset = new BitSet(totalLength);
	}

	public synchronized void merge(LongMessageFrame frame ,byte[] content, int idx) throws NotSupportedException {

		if (idxBitset.get(idx)) {
			logger.warn("have received the same index:{} of Message. do not merge this content.{},origin:{},{},{},new content:{}", idx,this.serviceNum,
					LongMessageFrameHolder.buildTextMessage(this.content[idx], msgfmt).getText(), DateFormatUtils.format(getTimestamp(),
							DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()), getSequence(), LongMessageFrameHolder.buildTextMessage(content, msgfmt).getText());
			throw new NotSupportedException("received the same index");
		}
		if (this.content.length <= idx || idx < 0) {
			logger.warn("have received error index:{} of Message content length:{}. do not merge this content.{},{},{},{}", idx, this.content.length,
					this.serviceNum, DateFormatUtils.format(getTimestamp(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()), getSequence(),
					LongMessageFrameHolder.buildTextMessage(content, msgfmt).getText());
			throw new NotSupportedException("have received error index");
		}
		// 设置该短信序号已填冲
		idxBitset.set(idx);
		
		//判断不同分片的msgfmt是否相同，不同的话就当成String进行编码转换
		if(this.msgfmt != null && msgfmt.getValue() != frame.getMsgfmt().getValue()) {
			String txt = new String(content,CMPPCommonUtil.switchCharset(frame.getMsgfmt().getAlphabet()));
			this.content[idx] = txt.getBytes(CMPPCommonUtil.switchCharset(msgfmt.getAlphabet()));
		}else {
			this.content[idx] = content;
		}

		this.totalbyteLength += this.content[idx].length;
	}

	public synchronized boolean isComplete() {
		return totalLength == idxBitset.cardinality();
	}

	public synchronized byte[] mergeAllcontent() {
		if (totalLength == 1) {
			return content[0];
		}
		byte[] ret = new byte[totalbyteLength];
		int idx = 0;
		for (int i = 0; i < totalLength; i++) {
			if (content[i] != null && content[i].length > 0) {
				System.arraycopy(content[i], 0, ret, idx, content[i].length);
				idx += content[i].length;
			}
		}

		return ret;
	}

	public AbstractSmsDcs getMsgfmt() {
		return msgfmt;
	}

	public void setMsgfmt(AbstractSmsDcs msgfmt) {
		this.msgfmt = msgfmt;
	}

	public LongSMSMessage getMsg() {
		return msg;
	}

	public void setMsg(LongSMSMessage msg) {
		this.msg = msg;
	}

}
