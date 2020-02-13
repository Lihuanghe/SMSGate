package com.zx.sms.codec.smgp.msg;

import java.util.ArrayList;
import java.util.List;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.smgp.tlv.TLV;
import com.zx.sms.codec.smgp.tlv.TLVOctets;
import com.zx.sms.codec.smgp.util.ByteUtil;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

public abstract class SMGPBaseMessage implements BaseMessage ,Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5011411627951305409L;

	public static final int SZ_HEADER = 12;

	protected int commandLength = 0;

	protected int commandId = 0;

	protected int sequenceNumber = DefaultSequenceNumberUtil.getSequenceNo(); 
	
	protected List<TLV> optionalParameters = new ArrayList<TLV>();
	
	private BaseMessage request;
	
	private long timestamp = CachedMillisecondClock.INS.now();
	//消息的生命周期，单位秒, 0表示永不过期
	private long lifeTime=0;

	public boolean fromBytes(byte[] bytes,int version) throws Exception {
		if (bytes == null) {
			return false;
		}
		if (bytes.length < SZ_HEADER) {
			return false;
		}
        int offset=0;
		commandLength = ByteUtil.byte2int(bytes, offset);
		offset+=4;
		commandId = ByteUtil.byte2int(bytes,offset);
		offset+=4;
		sequenceNumber =ByteUtil.byte2int(bytes, offset);
		offset+=4;
		
		byte[] bodyBytes = new byte[commandLength - SZ_HEADER];
		System.arraycopy(bytes, offset, bodyBytes, 0, bodyBytes.length);
		int bodyLength=setBody(bodyBytes,version);
		
		if (bodyLength < bodyBytes.length) {
			byte[] optBytes = new byte[bodyBytes.length - bodyLength];
			System.arraycopy(bodyBytes, bodyLength, optBytes, 0, optBytes.length);
			setOptionalBody(optBytes);
		}
		
		return true;
	}

	public byte[] toBytes(int version) throws Exception {
		byte[] bodyBytes = getBody(version);
		byte[] optBytes = getOptionalBody();
		
		commandLength = SZ_HEADER + bodyBytes.length+optBytes.length;
		int offset=0;
		byte[] bytes = new byte[commandLength];

		ByteUtil.int2byte(commandLength, bytes, offset);
		offset+=4;
		ByteUtil.int2byte(commandId, bytes, offset);
		offset+=4;
		ByteUtil.int2byte(sequenceNumber, bytes, offset);
		offset+=4;
		
		System.arraycopy(bodyBytes, 0, bytes, offset, bodyBytes.length);
		offset += bodyBytes.length;

		System.arraycopy(optBytes, 0, bytes, offset, optBytes.length);
		offset += optBytes.length;
		return bytes;
	}

	abstract protected int setBody(byte[] bodyBytes,int version) throws Exception;

	abstract protected byte[] getBody(int version) throws Exception ;

	
	private void setOptionalBody(byte[] buffer) throws Exception {
		short tag;
		short length;

		int offset = 0;
		TLV tlv = null;
		while (offset < buffer.length) {
			// we prepare buffer with one parameter

			tag = ByteUtil.byte2short(buffer, offset);
			offset += 2;
			tlv = findOptional(tag);
			if(tlv==null) {
				//未知的TLV
				tlv = new TLVOctets(tag); 
			}
			length = ByteUtil.byte2short(buffer, offset);
			offset += 2;
			byte[] valueBytes = new byte[length];
			System.arraycopy(buffer, offset, valueBytes, 0, length);
			offset += length;
			tlv.setValueData(valueBytes);

		}
	}

	private byte[] getOptionalBody() throws Exception {
		int size = optionalParameters.size();
		TLV tlv = null;
		int len = 0;
		for (int i = 0; i < size; i++) {
			tlv = (TLV) optionalParameters.get(i);
			if(tlv.hasValue())
			  len += 4 + tlv.getLength();

		}
		byte[] bytes = new byte[len];
		int offset = 0;
		for (int i = 0; i < size; i++) {
			tlv = (TLV) optionalParameters.get(i);
			offset = tlv.toBytes(bytes, offset);
		}
		if (offset == 0) {
			return new byte[0];
		}
		byte[] result = new byte[offset];
		System.arraycopy(bytes, 0, result, 0, offset);
		return result;
	}

	protected void registerOptional(TLV tlv) {
		if (tlv != null) {
			optionalParameters.add(tlv);
		}
	}

	private TLV findOptional(short tag) {
		int size = optionalParameters.size();
		TLV tlv = null;
		for (int i = 0; i < size; i++) {
			tlv = (TLV) optionalParameters.get(i);
			if (tlv != null) {
				if (tlv.getTag() == tag) {
					return tlv;
				}
			}
		}
		return null;
	}
	
	protected String plus86(String mobile){
		if(mobile==null||mobile.trim().length()==0)return "";
		if(mobile.startsWith("86"))return mobile;
		if(mobile.startsWith("+86"))return mobile.substring(1);
		return "86"+mobile;
	}
	
	protected String minus86(String mobile){
		if(mobile==null||mobile.trim().length()==0)return "";
		if(mobile.startsWith("86"))return mobile.substring(2);
		if(mobile.startsWith("+86"))return mobile.substring(3);
		return mobile;
		
	}
	
	
	public int getCommandId() {
		return commandId;
	}

	public void setCommandId(int commandId) {
		this.commandId = commandId;
	}

	public int getCommandLength() {
		return commandLength;
	}

	public void setCommandLength(int commandLength) {
		this.commandLength = commandLength;
	}




	public String sequenceString(){
		StringBuffer buffer=new StringBuffer();
		int offset=0;
		byte[] seqBytes=new byte[8];
		System.arraycopy(ByteUtil.int2byte(sequenceNumber), offset, seqBytes, 4, 4);
		buffer.append(ByteUtil.byte2long(seqBytes));
		return buffer.toString();
		
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

	@Override
	public boolean isRequest() {
		return (commandId & 0x80000000L) == 0L;
	}

	@Override
	public boolean isResponse() {
		return (commandId & 0x80000000L) == 0x80000000L;
	}

	@Override
	public boolean isTerminated() {
		return lifeTime !=0 && (( timestamp + lifeTime*1000 ) - CachedMillisecondClock.INS.now() < 0L);
	}

	@Override
	public void setRequest(BaseMessage message) {
		this.request = message;
	}

	@Override
	public BaseMessage getRequest() {
		return request;
	}

	@Override
	public int getSequenceNo() {
		return sequenceNumber;
	}
	
	public void setSequenceNo(int seq) {
		 sequenceNumber=seq;
	}

	protected SMGPBaseMessage clone() throws CloneNotSupportedException {
		SMGPBaseMessage msg =  (SMGPBaseMessage) super.clone();
		msg.setSequenceNo(sequenceNumber);
		msg.setCommandId(commandId);
		return msg;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPBaseMessage:[sequenceNumber=").append(sequenceString()).append(",").append(
			"commandId=").append(commandId).append("]");

		return buffer.toString();
	}
}
