package com.zx.sms.codec.cmpp.wap;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsUdhIei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

//短信片断持久化需要集中保存，因为同一短信的不同分片会从不同的连接发送。可能不在同一台主机。
//可以使用 Redis.Memcached等。
public enum LongMessageFrameHolder {
	INS;
	private final Logger logger = LoggerFactory.getLogger(LongMessageFrameHolder.class);
	private final static int UDHILENGTH = 6;
	private final static int MAXLENGTH = GlobalConstance.MaxMsgLength;
	private final static String prefix = "(%d/%d)";

	private final static FrameKeyCreator frameKeyCreator = INS.newFrameKeyCreator();

	/**
	 * 以服务号码+帧唯一码为key
	 */
	private ConcurrentHashMap<String, FrameHolder> map = new ConcurrentHashMap<String, FrameHolder>();

	
	
	private SmsMessage generatorSmsMessage(FrameHolder fh){
		
	}
	
	
	/**
	 * 获取一条完整的长短信，如果长短信组装未完成，返回null
	 **/
	public SmsMessage putAndget(String serviceNum, LongMessageFrame frame) throws NotSupportedException {

		assert (frame.getTppid() == 0);

		// 短信内容不带协议头，直接获取短信内容
		//udhi只取第一个bit
		if ((frame.getTpudhi() & 0x1) == 0) {
			if(frame.getMsgfmt() == 0){
				//TODO 这里还没有经过7bit转码 
				
			}else{
				
			}
			
			return generatorString(frame.getMsgContentBytes(),frame);

		} else if ((frame.getTpudhi() & 0x1) == 1) {

			FrameHolder fh = createFrameHolder(frame);
			// 判断是否只有一帧
			if (fh.isComplete()) {

				return generatorString(fh.mergeAllcontent(),frame);
			}

			// 超过一帧的，进行长短信合并
			String mapKey = new StringBuilder().append(serviceNum).append(".").append(fh.frameKey).toString();

			FrameHolder oldframeHolder = map.putIfAbsent(mapKey, fh);
			
			if (oldframeHolder != null) {

				mergeFrameHolder(oldframeHolder, frame);
			}else{
				oldframeHolder = fh;
			}

			if (oldframeHolder.isComplete()) {
				
				map.remove(mapKey);
				
				return generatorString(oldframeHolder.mergeAllcontent(),frame);
			}

		} else {
			throw new NotSupportedException("Not Support LongMsg.Tpudhi");
		}

		return null;

	}

	public List<LongMessageFrame> splitmsgcontent(String content, boolean isSupportLongMsg) {
		if (content == null)
			content = GlobalConstance.emptyString;

		// 判断是否包含非ASCII字符
		boolean haswidthChar = haswidthChar(content);

		int total = content.length();

		int msgLength = 0;

		// 不包含汉字，并且不支持长短信的，每条短信长度为140
		if (!haswidthChar) {
			if (total <= MAXLENGTH) {
				msgLength = MAXLENGTH;
			} else {
				// 长度大于140,但支持长短信的按65切分，长短信编码必须是ucs2
				if (isSupportLongMsg) {
					msgLength = (MAXLENGTH - UDHILENGTH) / 2;
				} else {
					// 长度大于140,但不支持长短信的按140切分
					msgLength = MAXLENGTH - 5; // 去掉(1/3)的短信头
				}
			}
		} else {
			if (total <= MAXLENGTH / 2) {
				msgLength = MAXLENGTH / 2;
			} else {
				if (isSupportLongMsg) {
					msgLength = (MAXLENGTH - UDHILENGTH) / 2;
				} else {
					msgLength = MAXLENGTH  / 2 - 5;// 去掉(1/3)的短信头
				}
			}
		}

		// 按最大短信长度拆分短信
		List<String> strlist = new ArrayList<String>();
		int i = 0;

		int totalMsgCnt = total % msgLength == 0 ? total / msgLength : (total / msgLength + 1);

		List<LongMessageFrame> result = new ArrayList<LongMessageFrame>();

		// 如果只有一条，按不支持长短信发,如果total为0则按一条算
		if (totalMsgCnt == 1 || totalMsgCnt == 0) {
			if (!haswidthChar) {

				result.add(splitByCharset(content, (short) 0, false, (short) 1, (short) 1,(byte)0));
			} else {

				result.add(splitByCharset(content, (short) 8, false, (short) 1, (short) 1,(byte)0));
			}

		} else {
			int idxMsgcnt = 1;
			while (i + msgLength < total) {
				String split;
				if (isSupportLongMsg) {
					split = new StringBuilder().append(content.substring(i, i + msgLength)).toString();
				} else {
					split = new StringBuilder().append(String.format(prefix, idxMsgcnt, totalMsgCnt)).append(content.substring(i, i + msgLength)).toString();
				}
				i = i + msgLength;
				idxMsgcnt++;
				strlist.add(split);
			}

			if (i < total) {
				
				String split;
				if (isSupportLongMsg) {
					split = new StringBuilder().append(content.substring(i, total)).toString();
				} else {
					split = new StringBuilder().append(String.format(prefix, idxMsgcnt, totalMsgCnt)).append(content.substring(i, total)).toString();
				}
				strlist.add(split);
			}
			byte frameKey = frameKeyCreator.getOne();
			for (int j = 0; j < totalMsgCnt; j++) {
				if ((!haswidthChar) && (!isSupportLongMsg)) {

					result.add(splitByCharset(strlist.get(j), (short) 0, isSupportLongMsg, (short)( j+1), (short) totalMsgCnt ,frameKey));
				} else {

					result.add(splitByCharset(strlist.get(j), (short) 8, isSupportLongMsg, (short)( j+1), (short) totalMsgCnt,frameKey));
				}

			}
		}
		return result;
	}

	private LongMessageFrame splitByCharset(String content, short msgFmt, boolean isSupportLongMsg, short idxMsgcnt, short totalMsgCnt,byte frameKey) {

		byte[] contentBytes = content.getBytes(switchCharset(msgFmt));
		LongMessageFrame frame = new LongMessageFrame();
		frame.setContentPart(content);
		//支持长短信，并且总的短信条数据大于1时，按长短信发
		if (isSupportLongMsg && totalMsgCnt>1) {
			assert (UDHILENGTH + contentBytes.length <= 140);
			byte[] tmp = new byte[UDHILENGTH + contentBytes.length];
			tmp[0] = 5;
			tmp[1] = 0;
			tmp[2] = 3;
			tmp[3] = frameKey ;
			tmp[4] = (byte) totalMsgCnt;
			tmp[5] = (byte) idxMsgcnt ;
			System.arraycopy(contentBytes, 0, tmp, 6, contentBytes.length);

			frame.setMsgfmt(msgFmt);
			frame.setPknumber((short) idxMsgcnt );
			frame.setPktotal(totalMsgCnt);
			frame.setTppid((short) 0);
			frame.setTpudhi((short) 1);
			frame.setMsgContentBytes(tmp);
			return frame;

		} else {
			
			frame.setMsgfmt(msgFmt);
			frame.setPknumber((short) 1);
			frame.setPktotal((short) 1);
			frame.setTppid((short) 0);
			frame.setTpudhi((short) 0);
			frame.setMsgContentBytes(contentBytes);
			return frame;
		}
	}

	private boolean haswidthChar(String content) {
		if (StringUtils.isEmpty(content))
			return false;

		byte[] bytes = content.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			// 判断最高位是否为1
			if ((bytes[i] & (byte) 0x80) == (byte) 0x80) {
				return true;
			}
		}
		return false;
	}

	// 处理GSM协议TP-DCS数据编码方案
	/*
	 * 数据编码方案 TP-DCS（TP-Data-Coding-Scheme）长度1byte
	 * 
	 * Bit No.7 与Bit No.6 :一般设置为00； Bit No.5： 0—文本未压缩， 1—文本用GSM 标准压缩算法压缩 Bit
	 * No.4： 0—表示Bit No.1、Bit No.0 为保留位，不含信息类型信息， 1—表示Bit No.1、Bit No.0 含有信息类型信息
	 * Bit No.3 与Bit No.2： 00—默认的字母表(7bit 编码) 01—8bit， 10—USC2（16bit）编码 11—预留
	 * Bit No.1 与Bit No.0： 00—Class 0， 01—Class 1， 10—Class 2（SIM 卡特定信息），
	 * 11—Class 3//写卡
	 */

	private Charset switchCharset(short type) {
		switch (type) {
		case 0:
			return Charset.forName("US-ASCII");// 7bit编码
		case 3:
			return Charset.forName("US-ASCII");// 7bit编码
		case 4:
			return Charset.forName("US-ASCII");// 8bit编码,通常用于发送数据消息，比如图片和铃声等；
		case 8:
			return Charset.forName("ISO-10646-UCS-2");// 16bit编码
		case 15:
			return Charset.forName("GBK");// 预留
		default:
			return GlobalConstance.defaultTransportCharset;
		}
	}

	private FrameHolder mergeFrameHolder(FrameHolder fh, LongMessageFrame frame) throws NotSupportedException {
		byte[] msgcontent = frame.getMsgContentBytes();
		UserDataHeader header = parseUserDataHeader(msgcontent);
		
		if(header.infoElement.size() > 0){
			
			for(InformationElement udhi : header.infoElement){
				if(SmsUdhIei.CONCATENATED_8BIT.equals(udhi.udhIei)){
					
					 fh.merge(frame.getPayloadbytes(udhi.infoEleLength), udhi.infoEleData[2]-1);
				}else if(SmsUdhIei.CONCATENATED_16BIT.equals(udhi.udhIei)){
					
					 fh.merge(frame.getPayloadbytes(udhi.infoEleLength), udhi.infoEleData[3]-1);
				}
			}
		
			return fh;
		}
		
		throw new NotSupportedException("Not Support LongMsg");
	}

	private FrameHolder createFrameHolder(LongMessageFrame frame) throws NotSupportedException {

		byte[] msgcontent = frame.getMsgContentBytes();
		
		UserDataHeader header = parseUserDataHeader(msgcontent);
		
		if(header.infoElement.size() > 0){
			FrameHolder frameholder = null;
			InformationElement appudhinfo = null;
			int i = 0;
			int frameKey = 0;
			for(InformationElement udhi : header.infoElement){
				if(SmsUdhIei.CONCATENATED_8BIT.equals(udhi.udhIei)){
					 frameKey = udhi.infoEleData[i]; 
					 i++;
					 frameholder =  new FrameHolder(frameKey,udhi.infoEleData[i],frame.getPayloadbytes(udhi.infoEleLength),udhi.infoEleData[i+1]-1);
					
				}else if(SmsUdhIei.CONCATENATED_16BIT.equals(udhi.udhIei)){
					 frameKey = (((int) udhi.infoEleData[i] << 8) | (int) udhi.infoEleData[i+1] & 0xff);
					 i+=2;
					 frameholder =  new FrameHolder(frameKey,udhi.infoEleData[i],frame.getPayloadbytes(udhi.infoEleLength),udhi.infoEleData[i+1]-1);
				}else{
					appudhinfo = udhi;
				}
			}
			//不是续列短信
			if(frameholder==null){
				frameholder =  new FrameHolder(0x0,1,msgcontent,0);
			}
			//如果没有app的udh，默认为文本短信
			if(appudhinfo == null){
				appudhinfo = new InformationElement();
				appudhinfo.udhIei = SmsUdhIei.valueOf((byte)0);
			}
			frameholder.setAppUDHinfo(appudhinfo);
			return frameholder;
		}
		
		throw new NotSupportedException("Not Support LongMsg");
	}
	
	private UserDataHeader parseUserDataHeader(byte[] pdu){
		UserDataHeader udh = new UserDataHeader();
		udh.headerlength = pdu[0]; //05
		udh.infoElement = new ArrayList<InformationElement>();
		
		int i = 1;
		while(i<udh.headerlength){
			InformationElement t = new InformationElement();
			t.udhIei = SmsUdhIei.valueOf(pdu[i++]);  //00
			t.infoEleLength = pdu[i++]; //03
			t.infoEleData = new byte[t.infoEleLength];
			System.arraycopy(pdu, i, t.infoEleData, 0, t.infoEleLength);
			i+=t.infoEleLength;
			udh.infoElement.add(t);
		}
		return udh;
	}
	
	private class UserDataHeader{
		int headerlength;
		List<InformationElement> infoElement;
	}
	
	private class InformationElement{
		
		SmsUdhIei udhIei;
		int infoEleLength;
		byte[] infoEleData;
	}

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
	private class FrameHolder {
		/**
		 * 长短信的总分片数量
		 * */
		private int totalLength = 0;
		private int frameKey;
		// 保存帧的Map,每帧都有一个唯一码。以这个唯一码做key
		private byte[][] content;
		
		private int totalbyteLength = 0;
		
		private BitSet idxBitset ;
		
		private InformationElement appUDHinfo;
		
		//用来保存应用类型，如文本短信或者wap短信
		public void setAppUDHinfo(InformationElement appUDHinfo){
			this.appUDHinfo = appUDHinfo;
		}

		public FrameHolder(int frameKey, int totalLength, byte[] content, int frameIndex) {
			this.frameKey = frameKey;
			this.totalLength = totalLength;
			
			this.content = new byte[totalLength][];
			this.idxBitset = new BitSet(totalLength);
			merge(content, frameIndex);
		}

		
		public synchronized void merge(byte[] content, int idx) {
			
			if(idxBitset.get(idx)){
				logger.warn("have received the same index of Message. do not merge this content. ");
				return;
			}
			if(this.content.length<=idx){
				logger.warn("have received error index:{} of Message content length:{}. do not merge this content. ",idx,this.content.length);
				return;
			}
			//设置该短信序号已填冲
			idxBitset.set(idx);
			
			this.content[idx] = content;
		
			this.totalbyteLength += this.content[idx].length;
		}

		public synchronized boolean isComplete() {
			return totalLength == idxBitset.cardinality();
		}

		public synchronized byte[] mergeAllcontent() {
			if(totalLength ==1){
				return content[0];
			}
			byte[] ret = new byte[totalbyteLength];
			int idx = 0;
			for (int i = 0; i < totalLength; i++) {
				System.arraycopy(content[i], 0, ret, idx, content[i].length);
				idx += content[i].length;
			}

			return ret;
		}
	}

	private FrameKeyCreator newFrameKeyCreator() {
		return new FrameKeyCreator();
	}

	private class FrameKeyCreator {
		public byte getOne() {
			return (byte) ( DefaultSequenceNumberUtil.getSequenceNo() & 0xff);
		}
	}

	
	/**
	 * Convert a stream of septets read as octets into a byte array containing the 7-bit
	 * values from the octet stream.
	 * @param octets
	 * @param bitSkip
	 * FIXME pass the septet length in here, so if there is a spare septet at the end of the octet, we can ignore that
	 * @return
	 */

	public static byte[] octetStream2septetStream(byte[] octets) {
		int septetCount = (8 * octets.length) / 7;
		byte[] septets = new byte[septetCount];
		for(int newIndex=septets.length-1; newIndex>=0; --newIndex) {
			for(int bit=6; bit>=0; --bit) {
				int oldBitIndex = ((newIndex * 7) + bit);
				if((octets[oldBitIndex >>> 3] & (1 << (oldBitIndex & 7))) != 0)
					septets[newIndex] |= (1 << bit);
			}
		}
		
		return septets;		
	}


	
	/**
	 * Convert a list of septet values into an octet stream, with a number of empty bits at the start.
	 * @param septets
	 * @param skipBits
	 * @return
	 */
	public static byte[] septetStream2octetStream(byte[] septets) {
		
		int octetLength = octetLengthfromseptetsLength(septets.length);
		byte[] octets = new byte[octetLength];
		
		for (int i = 0; i < septets.length; i++) {
			for (int j = 0; j < 7; j++) {
				if ((septets[i] & (1 << j)) != 0) {
					int bitIndex = (i * 7) + j ;
					octets[bitIndex >>> 3] |= 1 << (bitIndex & 7);
				}
			}
		}
		return octets;
	}
	
	public static int octetLengthfromseptetsLength(int septetLength)
	{
		return (int) Math.ceil((septetLength * 7 ) / 8.0);
	}
}
