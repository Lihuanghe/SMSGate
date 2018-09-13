package com.zx.sms.codec.smpp.msg;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.netty.buffer.ByteBuf;

import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsPortAddressedTextMessage;
import org.marre.sms.SmsTextMessage;
import org.marre.util.StringUtil;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;
import org.marre.wap.wbxml.WbxmlDocument;

import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppInvalidArgumentException;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.HexUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * Base "short message" PDU as a super class for submit_sm, deliver_sm, and
 * data_sm.  Having a common base class they all inherit from makes it easier
 * to work with requests in a standard way, even though data_sm does NOT actually
 * support all of the same parameters.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>) 
 */
public abstract class BaseSm<R extends PduResponse> extends PduRequest<R> {

    protected String serviceType;
    protected Address sourceAddress;
    protected Address destAddress;
    protected byte esmClass=0;
    private byte protocolId=0;                    // not present in data_sm
    private byte priority=0;                      // not present in data_sm
    private String scheduleDeliveryTime="";        // not present in data_sm
    private String validityPeriod="";              // not present in data_sm
    protected byte registeredDelivery=1;
    private byte replaceIfPresent=0;              // not present in data_sm
    protected byte dataCoding=0;
    private byte defaultMsgId=0;                  // not present in data_sm, not used in deliver_sm
    private byte[] shortMessage;                // not present in data_sm    
    private SmsMessage smsMsg;

    public BaseSm(int commandId, String name) {
        super(commandId, name);
    }

    public int getShortMessageLength() {
        return (getShortMessage() == null ? 0 :getShortMessage().length);
    }

    public byte[] getShortMessage() {
        return this.shortMessage;
    }

    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
        if (value != null && value.length > 255) {
            throw new SmppInvalidArgumentException("A short message in a PDU can only be a max of 255 bytes [actual=" + value.length + "]; use optional parameter message_payload as an alternative");
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
		return false;
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
    
    public SmsMessage getSmsMessage() {
		return smsMsg;
	}

	public void setSmsMsg(SmsMessage smsMsg) {
		this.smsMsg = smsMsg;
	}
	
	public void setSmsMsg(String smsMsg) {
		this.smsMsg = new SmsTextMessage(smsMsg);
	}
	
    public String getMsgContent() {
		if(smsMsg instanceof SmsTextMessage){
			SmsTextMessage textMsg = (SmsTextMessage) smsMsg;
			return textMsg.getText();
		}else if(smsMsg instanceof SmsPortAddressedTextMessage){
			SmsPortAddressedTextMessage textMsg = (SmsPortAddressedTextMessage) smsMsg;
			return textMsg.getText();
		}else if(smsMsg instanceof SmsMmsNotificationMessage){
			SmsMmsNotificationMessage mms = (SmsMmsNotificationMessage) smsMsg;
			return mms.getContentLocation_();
		}else if(smsMsg instanceof SmsWapPushMessage){
			SmsWapPushMessage wap = (SmsWapPushMessage) smsMsg;
			WbxmlDocument wbxml = wap.getWbxml();
			if(wbxml instanceof WapSIPush){
				return ((WapSIPush)wbxml).getUri();
			}else if(wbxml instanceof WapSLPush){
				return ((WapSLPush)wbxml).getUri();
			}
		}
		return "";
	}

	@Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
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
        short shortMessageLength = buffer.readUnsignedByte();
        this.shortMessage = new byte[shortMessageLength];
        buffer.readBytes(this.shortMessage);
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.serviceType);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.destAddress);
        bodyLength += 3;    // esmClass, priority, protocolId
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.scheduleDeliveryTime);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.validityPeriod);
        bodyLength += 5;    // regDelivery, replace, dataCoding, defaultMsgId, messageLength bytes
        bodyLength += getShortMessageLength();
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
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
        buffer.writeByte((byte)getShortMessageLength());
        if (this.shortMessage != null) {
            buffer.writeBytes(this.shortMessage);
        }
    }

    protected BaseSm doGenerateMessage(LongMessageFrame frame) throws Exception {
		BaseSm requestMessage = (BaseSm)this.clone();
		
		requestMessage.setProtocolId((byte)frame.getTppid());
		byte old = requestMessage.getEsmClass();
		requestMessage.setEsmClass((byte)((frame.getTpudhi()<<6) | old));
		requestMessage.setDataCoding(frame.getMsgfmt().getValue());
		requestMessage.setShortMessage(frame.getMsgContentBytes());
		
		if(frame.getPknumber()!=1){
			requestMessage.setSequenceNumber((int)DefaultSequenceNumberUtil.getSequenceNo());
		}
		requestMessage.setSmsMsg((SmsMessage)null);
		return requestMessage;
	}
    

	protected LongMessageFrame doGenerateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getProtocolId());
		//udhi bit : x1xxxxxx 表示要处理长短信  
		frame.setTpudhi((short)((getEsmClass()>>6) & 0x01));
		frame.setMsgfmt(new SmsDcs(getDataCoding()));
		frame.setMsgContentBytes(getShortMessage());
		frame.setMsgLength((short)getShortMessageLength());
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

        buffer.append("] esmCls [0x");
        buffer.append(HexUtil.toHexString(this.esmClass));
        buffer.append("] regDlvry [0x");
        buffer.append(HexUtil.toHexString(this.registeredDelivery));
        // NOTE: skipped protocolId, priority, scheduledDlvryTime, validityPeriod,replace and defaultMsgId
        buffer.append("] dcs [0x");
        buffer.append(HexUtil.toHexString(this.dataCoding));
        buffer.append("] message [");
        buffer.append(getMsgContent());
        buffer.append("])");
    }
}