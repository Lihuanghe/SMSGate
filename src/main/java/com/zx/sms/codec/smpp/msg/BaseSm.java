package com.zx.sms.codec.smpp.msg;

import org.apache.commons.lang3.ArrayUtils;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsMsgClass;
import com.chinamobile.cmos.sms.SmsPduUtil;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.DefaultSmppSmsDcs;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.SmppInvalidArgumentException;
import com.zx.sms.codec.smpp.SmppSplitType;
import com.zx.sms.codec.smpp.Tlv;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.HexUtil;
import com.zx.sms.common.util.PduUtil;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;

import io.netty.buffer.ByteBuf;

/**
 * Base "short message" PDU as a super class for submit_sm, deliver_sm, and
 * data_sm. Having a common base class they all inherit from makes it easier to
 * work with requests in a standard way, even though data_sm does NOT actually
 * support all of the same parameters.
 * 
 * @author joelauer (twitter: @jjlauer or
 *         <a href="http://twitter.com/jjlauer" target=
 *         window>http://twitter.com/jjlauer</a>)
 */
public abstract class BaseSm<R extends PduResponse> extends PduRequest<R> {

	protected String serviceType;
	protected Address sourceAddress;
	protected Address destAddress;
	protected byte esmClass = 0;
	private byte protocolId = 0; // not present in data_sm
	private byte priority = 0; // not present in data_sm
	private String scheduleDeliveryTime = ""; // not present in data_sm
	private String validityPeriod = ""; // not present in data_sm
	protected byte registeredDelivery = 1;
	private byte replaceIfPresent = 0; // not present in data_sm
	protected byte dataCoding = 0;
	private byte defaultMsgId = 0; // not present in data_sm, not used in
									// deliver_sm
	private byte[] shortMessage; // not present in data_sm
	private short msglength;
	private SmsMessage smsMsg;
	
	private UniqueLongMsgId uniqueLongMsgId;

	public BaseSm(int commandId, String name) {
		super(commandId, name);
	}

	public short getMsglength() {
		return msglength;
	}

	public void setMsglength(short msglength) {
		this.msglength = msglength;
	}

	public byte[] getShortMessage() {
		return this.shortMessage;
	}

	public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
		if (value != null && value.length > 255) {
			throw new SmppInvalidArgumentException("A short message in a PDU can only be a max of 255 bytes [actual=" + value.length
					+ "]; use optional parameter message_payload as an alternative");
		}
		this.shortMessage = value;
	}

	public byte getReplaceIfPresent() {
		return this.replaceIfPresent;
	}

	public void setReplaceIfPresent(byte value) {
		this.replaceIfPresent = value;
	}

	public byte getDataCoding() {
		return this.dataCoding;
	}

	public void setDataCoding(byte value) {
		this.dataCoding = value;
	}

	public byte getDefaultMsgId() {
		return this.defaultMsgId;
	}

	public void setDefaultMsgId(byte value) {
		this.defaultMsgId = value;
	}

	public byte getRegisteredDelivery() {
		return this.registeredDelivery;
	}

	public void setRegisteredDelivery(byte value) {
		this.registeredDelivery = value;
	}

	public String getValidityPeriod() {
		return this.validityPeriod;
	}

	public void setValidityPeriod(String value) {
		this.validityPeriod = value;
	}

	public String getScheduleDeliveryTime() {
		return this.scheduleDeliveryTime;
	}

	public void setScheduleDeliveryTime(String value) {
		this.scheduleDeliveryTime = value;
	}

	public byte getPriority() {
		return this.priority;
	}

	public void setPriority(byte value) {
		this.priority = value;
	}

	public byte getEsmClass() {
		return this.esmClass;
	}

	public void setEsmClass(byte value) {
		this.esmClass = value;
	}

	public byte getProtocolId() {
		return this.protocolId;
	}

	public void setProtocolId(byte value) {
		this.protocolId = value;
	}

	public String getServiceType() {
		return this.serviceType;
	}

	public boolean isReport() {
		return this instanceof DeliverSmReceipt || ((esmClass & 0x3c) == 0x04);
	}

	public void setServiceType(String value) {
		this.serviceType = value;
	}

	public Address getSourceAddress() {
		return this.sourceAddress;
	}

	public void setSourceAddress(Address value) {
		this.sourceAddress = value;
	}

	public Address getDestAddress() {
		return this.destAddress;
	}

	public void setDestAddress(Address value) {
		this.destAddress = value;
	}
	
	protected UniqueLongMsgId getUniqueLongMsgId() {
		return uniqueLongMsgId;
	}

	protected void setUniqueLongMsgId(UniqueLongMsgId uniqueLongMsgId) {
		this.uniqueLongMsgId = uniqueLongMsgId;
	}

	public SmsMessage getSmsMessage() {
		return smsMsg;
	}

	public void setSmsMsg(SmsMessage smsMsg) {
		this.smsMsg = smsMsg;
	}

	public void setSmsMsg(String smsMsg) {
		if (SmsTextMessage.haswidthChar(smsMsg))
			this.smsMsg = new SmsTextMessage(smsMsg, new DefaultSmppSmsDcs(SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN).getValue()));
		else
			this.smsMsg = new SmsTextMessage(smsMsg, new DefaultSmppSmsDcs((byte)0,SmsAlphabet.ASCII));	
	}
	
	public void setSmsMsg(String smsMsg,SmsAlphabet defaultAlp) {
		
		if(SmsAlphabet.GSM == defaultAlp && !SmsPduUtil.hasUnGsmchar(smsMsg)) {
			this.smsMsg = new SmsTextMessage(smsMsg, new DefaultSmppSmsDcs((byte)0,SmsAlphabet.GSM));
		}else {
			if (SmsTextMessage.haswidthChar(smsMsg))
				this.smsMsg = new SmsTextMessage(smsMsg, new DefaultSmppSmsDcs(SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN).getValue()));
			else {
				this.smsMsg = new SmsTextMessage(smsMsg, new DefaultSmppSmsDcs((byte)0,defaultAlp));
			}
		}
	}

	public String getMsgContent() {
		if (smsMsg instanceof SmsMessage) {
			return smsMsg.toString();
		}
		if (shortMessage != null && shortMessage.length > 0) {
			LongMessageFrame frame = doGenerateFrame();
			return LongMessageFrameHolder.INS.getPartTextMsg(frame);
		}
		return "";
	}

	@Override
	public void readBody(ByteBuf buffer,SMPPEndpointEntity entity) throws UnrecoverablePduException, RecoverablePduException {
		this.serviceType = ByteBufUtil.readNullTerminatedString(buffer);
		this.sourceAddress = ByteBufUtil.readAddress(buffer);
		this.destAddress = ByteBufUtil.readAddress(buffer);
		this.esmClass = buffer.readByte();
		this.protocolId = buffer.readByte();
		this.priority = buffer.readByte();
		this.scheduleDeliveryTime = ByteBufUtil.readNullTerminatedString(buffer);
		this.validityPeriod = ByteBufUtil.readNullTerminatedString(buffer);
		this.registeredDelivery = buffer.readByte();
		this.replaceIfPresent = buffer.readByte();
		this.dataCoding = buffer.readByte();
		this.defaultMsgId = buffer.readByte();
		// this is always an unsigned version of the short message length
		this.msglength = buffer.readUnsignedByte();
		
		boolean isNotReport = !isReport();
		//使用7bit 压缩编码,压缩编码的系统很少
		if(entity != null && isNotReport && entity.isUse7bitPack() && entity.buildDefaultSmsDcs(dataCoding).getAlphabet() == SmsAlphabet.GSM) {
			int packedLength = (this.msglength * 7+7)/8 ;
			byte[] packArray = new byte[packedLength];
			buffer.readBytes(packArray);
			
			int udhi  = getTpUdhI();
			int udhl = 0;
			if(udhi != 0) {
				//包含UDH
				udhl = packArray[0];
				int septetOffset =  ((udhl+1)*8+6)/7;
				int septetCount = this.msglength - septetOffset;
				int bitOffset = septetOffset*7-(udhl+1)*8;
				byte[] unencodedSeptets = SmsPduUtil.octetStream2septetStream(packArray,septetOffset-1, septetCount,bitOffset);
				
				this.msglength = (short)((udhl+1) + unencodedSeptets.length);
				this.shortMessage = new byte[msglength];
				System.arraycopy(packArray, 0, this.shortMessage, 0, udhl+1);
				System.arraycopy(unencodedSeptets, 0, this.shortMessage, udhl+1, unencodedSeptets.length);
			}else {
				//没有UDH,直接解码
				byte[] unencodedSeptets = SmsPduUtil.octetStream2septetStream(packArray,0,this.msglength,0);
				this.msglength = (short)unencodedSeptets.length;
				this.shortMessage = unencodedSeptets;
			}
		}else {
			this.shortMessage = new byte[msglength];
			buffer.readBytes(this.shortMessage);
		}

	}

	@Override
	public int calculateByteSizeOfBody() {
		int bodyLength = 0;
		bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.serviceType);
		bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
		bodyLength += PduUtil.calculateByteSizeOfAddress(this.destAddress);
		bodyLength += 3; // esmClass, priority, protocolId
		bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.scheduleDeliveryTime);
		bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.validityPeriod);
		bodyLength += 5; // regDelivery, replace, dataCoding, defaultMsgId,
							// messageLength bytes
		bodyLength += getShortMessage().length;
		return bodyLength;
	}

	@Override
	public void writeBody(ByteBuf buffer,SMPPEndpointEntity entity) throws UnrecoverablePduException, RecoverablePduException {
		ByteBufUtil.writeNullTerminatedString(buffer, this.serviceType);
		ByteBufUtil.writeAddress(buffer, this.sourceAddress);
		ByteBufUtil.writeAddress(buffer, this.destAddress);
		buffer.writeByte(this.esmClass);
		buffer.writeByte(this.protocolId);
		buffer.writeByte(this.priority);
		ByteBufUtil.writeNullTerminatedString(buffer, this.scheduleDeliveryTime);
		ByteBufUtil.writeNullTerminatedString(buffer, this.validityPeriod);
		buffer.writeByte(this.registeredDelivery);
		buffer.writeByte(this.replaceIfPresent);
		buffer.writeByte(this.dataCoding);
		buffer.writeByte(this.defaultMsgId);
		
	
		boolean isNotReport = !isReport();
		//使用7bit 压缩编码,压缩编码的系统很少
		if(entity != null && isNotReport && entity.isUse7bitPack() && entity.buildDefaultSmsDcs(dataCoding).getAlphabet() == SmsAlphabet.GSM) {
			int udhi  = getTpUdhI();
			if(udhi != 0) {
				//包含UDH
				if (this.shortMessage != null) {
					int udhl = this.shortMessage[0];
					int septetOffset =  ((udhl+1)*8+6)/7;
					int bitOffset = septetOffset*7-(udhl+1)*8;
				
					
					int septetCount = getMsglength() - udhl-1;
					byte[] actuallyseptets = new byte[septetCount];
					System.arraycopy(this.shortMessage, udhl+1, actuallyseptets, 0, septetCount);
					byte[] packedseptets = SmsPduUtil.septetStream2octetStream(actuallyseptets,bitOffset);
					byte[] packedUD = new byte[(udhl+1)+packedseptets.length];
					System.arraycopy(this.shortMessage, 0, packedUD, 0, udhl+1);
					System.arraycopy(packedseptets, 0, packedUD, udhl+1, packedseptets.length);
					buffer.writeByte((byte) (septetOffset+septetCount));
					buffer.writeBytes(packedUD);
				}
			}else {
				buffer.writeByte((byte) getMsglength());
				if (this.shortMessage != null) {
					byte[] packedseptets = SmsPduUtil.septetStream2octetStream(this.shortMessage,0);
					buffer.writeBytes(packedseptets);
				}
			}
		}else {
			buffer.writeByte((byte) getMsglength());
			if (this.shortMessage != null) {
				buffer.writeBytes(this.shortMessage);
			}
		}
	}
	
	public BaseSm generateMessage(LongMessageFrame frame,SmppSplitType splitType) throws Exception {
		BaseSm requestMessage = (BaseSm) this.clone();

		byte old = requestMessage.getEsmClass();
		requestMessage.setEsmClass((byte) ((frame.getTpudhi() << 6) | old));
		requestMessage.setDataCoding(frame.getMsgfmt().getValue());
		
		requestMessage.setMsglength(frame.getMsgLength());
		requestMessage.setShortMessage(frame.getMsgContentBytes());
		
		switch(splitType) {
			case UDHPARAM:
				if(frame.isConcat()) { //有UDH的，不一定是拼接短信。比如端口短信
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_MSG_REF_NUM,ByteArrayUtil.toByteArray((short)frame.getPkseq())));
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_TOTAL_SEGMENTS,ByteArrayUtil.toByteArray((byte)frame.getPktotal())));
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_SEGMENT_SEQNUM,ByteArrayUtil.toByteArray((byte)frame.getPknumber())));
					
					//UDH有可能包含多个UDH项目，这里解析出所有项目后，删除concat类型的项目
					byte[] newContent = null;
					if(frame.isConcatOnly()) { //只包含长短信的拼接短信头
						int udhl = frame.getMsgContentBytes()[0];
						newContent = ArrayUtils.subarray(frame.getMsgContentBytes(), udhl + 1, frame.getMsgContentBytes().length);
						requestMessage.setEsmClass((byte) (old & 0xbf)); //没有UDH了，清除UDHI标识
						
					}else {
						//端口短信，长短信，有两或者更多个头
						newContent = LongMessageFrameHolder.INS.removeConcatUDHie(frame.getMsgContentBytes());
					}
					requestMessage.setMsglength((short)newContent.length);
					requestMessage.setShortMessage(newContent);
					break;
				}
			case UDH:
				requestMessage.setMsglength(frame.getMsgLength());
				requestMessage.setShortMessage(frame.getMsgContentBytes());
				break;
			case PAYLOADPARAM:
				if(frame.isConcat()) {
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_MSG_REF_NUM,ByteArrayUtil.toByteArray((short)frame.getPkseq())));
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_TOTAL_SEGMENTS,ByteArrayUtil.toByteArray((byte)frame.getPktotal())));
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_SAR_SEGMENT_SEQNUM,ByteArrayUtil.toByteArray((byte)frame.getPknumber())));
					
					byte[] newContent = null;
					if(frame.isConcatOnly()) {  //只包含长短信的拼接短信头
						int udhl = frame.getMsgContentBytes()[0];
						newContent = ArrayUtils.subarray(frame.getMsgContentBytes(), udhl + 1, frame.getMsgContentBytes().length);
						requestMessage.setEsmClass((byte) (old & 0xbf));
						
					}else {
						//端口短信，长短信，有两或者更多个头
						newContent = LongMessageFrameHolder.INS.removeConcatUDHie(frame.getMsgContentBytes());
					}
					requestMessage.setMsglength((short)0);
					requestMessage.setShortMessage(GlobalConstance.emptyBytes);
					requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD,newContent));
					break;
				}
			case PAYLOAD:
				requestMessage.setMsglength((short)0);
				requestMessage.setShortMessage(GlobalConstance.emptyBytes);
				requestMessage.addOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD,frame.getMsgContentBytes()));
				break;
		}
		if (frame.getPknumber() != 1) {
			requestMessage.setSequenceNumber((int) DefaultSequenceNumberUtil.getSequenceNo());
		}
		
		requestMessage.setSmsMsg((SmsMessage) null);

		return requestMessage;
	}
	protected BaseSm doGenerateMessage(LongMessageFrame frame) throws Exception {
		BaseSm requestMessage = (BaseSm) this.clone();

		byte old = requestMessage.getEsmClass();
		requestMessage.setEsmClass((byte) ((frame.getTpudhi() << 6) | old));
		requestMessage.setDataCoding(frame.getMsgfmt().getValue());
		requestMessage.setMsglength(frame.getMsgLength());
		requestMessage.setShortMessage(frame.getMsgContentBytes());
		if (frame.getPknumber() != 1) {
			requestMessage.setSequenceNumber((int) DefaultSequenceNumberUtil.getSequenceNo());
		}
		
		requestMessage.setSmsMsg((SmsMessage) null);
		return requestMessage;
	}

	private short getTpUdhI() {
		return (short) ((getEsmClass() >> 6) & 0x01);
	}

	protected LongMessageFrame doGenerateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getProtocolId());
		// udhi bit : x1xxxxxx 表示要处理长短信
		frame.setTpudhi(getTpUdhI());
		frame.setMsgfmt(new DefaultSmppSmsDcs(getDataCoding()));
		byte[] messageBytes = getShortMessage()==null?new byte[0]:getShortMessage();
		frame.setMsgContentBytes(messageBytes);
		frame.setMsgLength((short)messageBytes.length);
		
		if(messageBytes.length == 0) {
			//检查message_payload
			Tlv messagePayload = getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD);
			if(messagePayload != null) {
				messageBytes = messagePayload.getValue();
				frame.setMsgContentBytes(messageBytes);
				frame.setMsgLength((short)messageBytes.length);
			}
		}
		
		//检查 SarMsgRefNum SarTotalSegments  SarSegmentSeqnum
		Tlv ref = getOptionalParameter(SmppConstants.TAG_SAR_MSG_REF_NUM);
		Tlv tot = getOptionalParameter(SmppConstants.TAG_SAR_TOTAL_SEGMENTS);
		Tlv seq = getOptionalParameter(SmppConstants.TAG_SAR_SEGMENT_SEQNUM);
		if(ref != null && tot!=null && seq != null && tot.getValue()[0] > 1) {
			
			int startIndex = frame.isHasTpudhi() ? 1 : 0;
			int udhl = frame.isHasTpudhi() ? messageBytes[0] : 0;
			byte[] mergedMessageBytes = new byte[messageBytes.length+7-startIndex];
			//补充成16bit的长短信分片
			mergedMessageBytes[0]=(byte)(udhl + 6);
			mergedMessageBytes[1]=8;
			mergedMessageBytes[2]=4;
			mergedMessageBytes[3]= ref.getValue()[0];
			mergedMessageBytes[4]= ref.getValue()[1];
			mergedMessageBytes[5]= tot.getValue()[0];
			mergedMessageBytes[6]= seq.getValue()[0];
			
			System.arraycopy(messageBytes, startIndex, mergedMessageBytes, 7, messageBytes.length-startIndex);
			frame.setTpudhi((short)1);
			frame.setMsgContentBytes(mergedMessageBytes);
			
			frame.setMsgLength((short) mergedMessageBytes.length);
		}
		
		frame.setSequence(getSequenceNo());
		return frame;
	}
	
	@Override
	public void appendBodyToString(StringBuilder buffer) {
		buffer.append("(serviceType [");
		buffer.append((this.serviceType));
		buffer.append("] sourceAddr [");
		buffer.append((this.sourceAddress));
		buffer.append("] destAddr [");
		buffer.append((this.destAddress));
		buffer.append("] tpPid [");
		buffer.append((this.getProtocolId()));
		buffer.append("] esmCls [0x");
		buffer.append(HexUtil.toHexString(this.esmClass));
		buffer.append("] regDlvry [0x");
		buffer.append(HexUtil.toHexString(this.registeredDelivery));
		// NOTE: skipped protocolId, priority, scheduledDlvryTime,
		// validityPeriod,replace and defaultMsgId
		buffer.append("] dcs [0x");
		buffer.append(HexUtil.toHexString(this.dataCoding));
		buffer.append("] message [");
		buffer.append(getMsgContent());
		buffer.append("])");
	}
}