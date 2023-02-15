package com.zx.sms.codec.cmpp.wap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.chinamobile.cmos.PduParser.GenericPdu;
import com.chinamobile.cmos.PduParser.NotificationInd;
import com.chinamobile.cmos.PduParser.PduHeaders;
import com.chinamobile.cmos.PduParser.PduParser;
import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsConcatMessage;
import com.chinamobile.cmos.sms.SmsException;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsPdu;
import com.chinamobile.cmos.sms.SmsPduUtil;
import com.chinamobile.cmos.sms.SmsPort;
import com.chinamobile.cmos.sms.SmsPortAddressedTextMessage;
import com.chinamobile.cmos.sms.SmsSimTookitSecurityMessage;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.chinamobile.cmos.sms.SmsUdhElement;
import com.chinamobile.cmos.sms.SmsUdhIei;
import com.chinamobile.cmos.sms.SmsUserData;
import com.chinamobile.cmos.wap.mms.MmsConstants;
import com.chinamobile.cmos.wap.push.SmsMmsNotificationMessage;
import com.chinamobile.cmos.wap.push.SmsWapPushMessage;
import com.chinamobile.cmos.wap.push.WapSIPush;
import com.chinamobile.cmos.wap.push.WapSLPush;
import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.LongMessageFrameCache;
import com.zx.sms.codec.LongMessageFrameProvider;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.StandardCharsets;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;

import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.stream.WbXmlInputFactory;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;


//短信片断持久化需要集中保存，因为同一短信的不同分片会从不同的连接发送。可能不在同一台主机。
//可以使用 Redis.Memcached等。

public enum LongMessageFrameHolder {
	INS;
	private static final Logger logger = LoggerFactory.getLogger(LongMessageFrameHolder.class);

	private static LongMessageFrameProvider provider;
	
	//长短信合并的集群缓存
	private static LongMessageFrameCache clusterMap ;
	
	//长短信合并的JVM缓存
	private static LongMessageFrameCache jvmMap = (new LongMessageFrameProviderInner()).create();
	
	//使用SPI机制，通过ServiceLoader加载序号最大的类，做为长短信合并的缓存Map
	static {
		ServiceLoader<LongMessageFrameProvider> p = ServiceLoader.load(LongMessageFrameProvider.class);
		for(LongMessageFrameProvider i  : p) {
			
			logger.info("LongMessageFrameProvider search ... found {}. order : {}" ,i.getClass(),i.order());
			//选取序号最大的生效
			if(provider == null  || provider.order() < i.order())
				provider = i;
		}
		
		if(provider == null || LongMessageFrameProviderInner.class.equals(provider.getClass())) {
		
			clusterMap = jvmMap;
			logger.warn("not found other LongMessageFrameProvider.class Implementation class . use LongMessageFrameProviderInner.class" );
		}else {
			logger.info("would use {} for cluster Merge.", provider.getClass() );
			
			clusterMap = provider.create();
		}
	}
	
	public final static boolean hasClusterLongMessageFrameProvider  = provider != null && !LongMessageFrameProviderInner.class.equals(provider.getClass());

	private SmsMessage generatorSmsMessage(FrameHolder fh, LongMessageFrame frame) throws NotSupportedException {
		byte[] contents = fh.mergeAllcontent();
		InformationElement udheader = fh.getAppUDHinfo();
		// udh为空表示文本短信
		if (udheader == null) {
			return buildTextMessage(contents, fh.getMsgfmt());
		} else {
			if (SmsUdhIei.APP_PORT_16BIT.equals(udheader.udhIei)) {
				// 2948 wap_push 0x0B84
				int destport = (((udheader.infoEleData[0] & 0xff) << 8) | (udheader.infoEleData[1] & 0xff)) & 0x0ffff;
				// 9200 wap-wsp 0x23f0
				int srcport = (((udheader.infoEleData[2] & 0xff) << 8) | (udheader.infoEleData[3] & 0xff)) & 0x0ffff;
				if (destport == SmsPort.WAP_PUSH.getPort() && srcport == SmsPort.WAP_WSP.getPort()) {
					return parseWapPdu(contents);
				} else if (destport == SmsPort.NOKIA_MULTIPART_MESSAGE.getPort() && srcport == SmsPort.ZERO.getPort()) {
					// Nokia手机支持的OTA图片格式
					throw new NotSupportedException("Nokia手机支持的OTA图片格式,无法解析");
				} else if (destport == SmsPort.OTA_SETTINGS_BROWSER.getPort()) {
					// Nokia手机支持的OTA浏览器书签
					throw new NotSupportedException("Nokia手机支持的OTA浏览器书签,无法解析");
				}
				if(logger.isWarnEnabled()) {
					logger.warn("UnsupportedportMessage key:{} ,UDH:0x{} udhdata:{},pdu:[{}]", fh.getServiceNum(),ByteBufUtil.hexDump(new byte[] {udheader.udhIei.getValue()}), ByteBufUtil.hexDump(udheader.infoEleData),
							ByteBufUtil.hexDump(contents));
				}
				SmsTextMessage text = buildTextMessage(contents, fh.getMsgfmt());
				return new SmsPortAddressedTextMessage(new SmsPort(destport), new SmsPort(srcport), text);
			} else if(frame.getTppid()==0x7f && (SmsUdhIei.COMMAND_PACKET.equals(udheader.udhIei)||SmsUdhIei.COMMAND_RESPONSE_PACKET.equals(udheader.udhIei))){
				//Tppid()==0x7f sim data download
				//(U)SIM Toolkit Securit 用于远程写卡
				SmsSimTookitSecurityMessage  sts = new SmsSimTookitSecurityMessage(udheader.udhIei.getValue() ,udheader.infoEleData,contents);
				return sts;
			}
			else {
				// 其它都当成文本短信
				logger.warn("Unsupported key:{} ,UDH:0x{} udhdata:{},pdu:[{}]", fh.getServiceNum(),ByteBufUtil.hexDump(new byte[] {udheader.udhIei.getValue()}), ByteBufUtil.hexDump(udheader.infoEleData), ByteBufUtil.hexDump(contents));
				return buildTextMessage(contents, fh.getMsgfmt());
			}
		}
	}
	
	static SmsTextMessage buildTextMessage(byte[] bytes,AbstractSmsDcs msgfmt){
		String text = null;
		switch(msgfmt.getAlphabet()){
		case GSM:
			text = SmsPduUtil.unencodedSeptetsToString(bytes);
			break;
		default:
			text = new String(bytes,CMPPCommonUtil.switchCharset(msgfmt.getAlphabet()));
		}
		return new SmsTextMessage(text, msgfmt);
	}
	
	/**
	 * 获取长短信切分后的短信片断内容
	 * 
	 **/
	public String getPartTextMsg(LongMessageFrame frame) {
		if (!frame.isHasTpudhi()) {
			return buildTextMessage(frame.getPayloadbytes(0), frame.getMsgfmt()).getText();
		} else {
			UserDataHeader header = parseUserDataHeader(frame.getMsgContentBytes());
			byte[] payload = frame.getPayloadbytes(header.headerlength);
			return buildTextMessage(payload, frame.getMsgfmt()).getText();
		}
	}
	
	/**
	 *获取长短信的UDHI字段里的frameKey + pkTol ,以方便长短信关联状态报告时使用
	 */
	public FrameHolder parseFrameKey(LongMessageFrame frame) {
		
		if (frame.isHasTpudhi()) {
			try {
				FrameHolder fh = createFrameHolder("", frame);
				return fh ;
			}catch(NotSupportedException es) {
			}
		}
		return null;
	}
	
	/**
	 * 获取一条完整的长短信，如果长短信组装未完成，返回null
	 **/
	public SmsMessageHolder putAndget(EndpointEntity  entity,String longSmsKey ,LongSMSMessage msg,boolean isRecvLongMsgOnMultiLink) throws NotSupportedException {
		LongMessageFrame frame = msg.generateFrame();
		if(entity != null && entity instanceof SMPPEndpointEntity && msg instanceof BaseSm) {
			BaseSm smppMsg = (BaseSm)msg;
			SMPPEndpointEntity smppSMPPEndpointEntity = (SMPPEndpointEntity)entity;
			//根据entity的默认字符表配置修改Frame的SmsDcs字段
			frame.setMsgfmt(new SmppSmsDcs(smppMsg.getDataCoding(),smppSMPPEndpointEntity.getDefauteSmsAlphabet()));
		}
		
		/**
        1、根据SMPP协议，融合网关收到短信中心上行的esm_class字段（一个字节）是0100 0000，转换成16进制就是0X40 (64), 01XXXXXX表明是一条长短信。
        网关默认透传所有信息，即网关直接透传了0100 0000。所以接收到64是指短信属于长短信。（网关与短信中心采用SMPP协议）
        2.CMPP协议中没有esm_class字段。根据CMPP长短信TP_udhi的标准是0X01 (1)，即0000 0001，但目前所有网关都是配置接收到0X40（64）表明是一条长短信。
        该问题据说是以前一直遗留下来的，没有正式的文档规范说明，所以一直都是发送0X40(64)。 
		 */
		// udhi只取第1个bit和第7个bit同时为0时，表示不包含UDH
		if (!frame.isHasTpudhi()) {
			// 短信内容不带协议头，直接获取短信内容
			SmsTextMessage smsmsg =  buildTextMessage(frame.getPayloadbytes(0), frame.getMsgfmt());
			
			return new SmsMessageHolder(smsmsg,msg);
		} else {

			try {
				//包含UDH，调用createFrameHolder进行UDH解析
				FrameHolder fh = createFrameHolder(longSmsKey, frame);
//
				if(fh == null)
					return null;
//				
				// 判断是否只有一帧
				if (fh.isComplete()) {
					return new SmsMessageHolder(generatorSmsMessage(fh, frame),msg);
				}

				// 超过一帧的，进行长短信合并
				String mapKey = longSmsKey+"."+fh.frameKey+"."+fh.getTotalLength();
				
				//设置短信的接收时间
				frame.setTimestamp(((BaseMessage)msg).getTimestamp());

				//将新收到的分片保存，并获取全部的分片。因为多个分片可能同时从不同连接到达，因此这个方法要线程安全。
				boolean complete = setAndget(msg,mapKey, frame,isRecvLongMsgOnMultiLink);
			
				//判断是否收全了长短信片断 , 接收的分片数可能超过标示分片数，说明存在重复的分片
				if(complete) {
					//多线程、或者多进程处理时，只有唯一一个线程能进入到这里
					
					List<LongMessageFrame> allFrame = getAndDel(mapKey,isRecvLongMsgOnMultiLink);
					//总帧数个数虽然够了，还要再判断是不是所有帧都齐了 ，有可能收到相同帧序号的帧
					//从第一个帧开始偿试合并
					FrameHolder firstF = createFrameHolder(mapKey, allFrame.get(0));
					for(int i = 1; i< allFrame.size() ;i++) {
						try {
							firstF = mergeFrameHolder(firstF, allFrame.get(i));
						}catch(NotSupportedException ex) {
						}
					}
					if (firstF.isComplete()) {
						//合并成功，
						
						//根据分片信息，恢复消息对象，并保存在Fragments 列表中，不包含第一个分片
						//用第一个到达的分片做为 合并后消息的母本

						//恢复UniqueLongMsgId
						LongSMSMessage fullMsg = (LongSMSMessage) msg.generateMessage(allFrame.get(0));
						//恢复消息序列号
						if(fullMsg instanceof BaseMessage) {
							((BaseMessage)fullMsg).setSequenceNo(allFrame.get(0).getSequence());
						}
						if(fullMsg.getUniqueLongMsgId()!=null)
							fullMsg.setUniqueLongMsgId(new UniqueLongMsgId(fullMsg.getUniqueLongMsgId(),allFrame.get(0)));
						
						//其它的分片，作为fragment放入 Fragment List 
						for(int i = 1; i< allFrame.size() ;i++) {
							LongMessageFrame tmp = allFrame.get(i);
							LongSMSMessage  frag = (LongSMSMessage) fullMsg.generateMessage(tmp);
							
							//恢复UniqueLongMsgId
							if(frag.getUniqueLongMsgId()!=null)
								frag.setUniqueLongMsgId(new UniqueLongMsgId(frag.getUniqueLongMsgId(),tmp));
							
							//恢复消息序列号
							if(frag instanceof BaseMessage) {
								((BaseMessage)frag).setSequenceNo(tmp.getSequence());
							}
							fullMsg.addFragment(frag);
						}
						return new SmsMessageHolder(generatorSmsMessage(firstF, frame),fullMsg);
					}else {
						//正常业务不会到这里
						throw new NotSupportedException("not here");
					}
				}
			} catch (Exception ex) {
				logger.warn("Merge Long SMS Error. entity : {} , dump:{}.\n {} ",  entity != null?entity.getId():"null",ByteBufUtil.hexDump(frame.getMsgContentBytes()) ,msg,ex);
				throw new NotSupportedException(ex.getMessage());
			}
		} 
		return null;
	}
	
	private void warning (boolean isMulti) {
		//如果没有提供集群版的长短信合并缓存，要给告警
		if(isMulti && !hasClusterLongMessageFrameProvider)
			logger.warn("you use JVM cache for LongMessageFrameCache .When Long message fragments sent by multiple connections , messages will be lost , Cause memory leak");
	}
	
	//这个方法必须是线程安全的
	private boolean setAndget(LongSMSMessage msg,String key, LongMessageFrame currFrame,boolean isMulti){
		if(isMulti) {
			warning(isMulti);
			return clusterMap.addAndGet(msg, key, currFrame);
		}else {
			return jvmMap.addAndGet(msg, key, currFrame);
		}
	}
	


	Long getUniqueLongMsgId(String cacheKey,boolean isMulti) {
		if(isMulti) {
			return clusterMap.getUniqueLongMsgId(cacheKey);
		}else {
			return jvmMap.getUniqueLongMsgId(cacheKey);
		}
	}


	void clearUniqueLongMsgIdCacheKey(String cacheKey,boolean isMulti) {
		if(isMulti) {
			 clusterMap.clearUniqueLongMsgIdCacheKey(cacheKey);
		}else {
			 jvmMap.clearUniqueLongMsgIdCacheKey(cacheKey);
		}
	}
	
	private List<LongMessageFrame> getAndDel(String key,boolean isMulti) {
		if(isMulti) {
			return clusterMap.getAndDel(key);
		}else {
			return jvmMap.getAndDel(key);
		}
	}
	
	public static <T extends BaseMessage> List<T> splitLongSmsMessage(EndpointEntity e, T msg) throws Exception {
		return splitLongSmsMessage(e,msg,null);
	}
	
	public static <T extends BaseMessage> List<T> splitLongSmsMessage(EndpointEntity e, T msg,Channel ch) throws Exception {
		List<T> msgs = new ArrayList<T>();

		if (msg instanceof LongSMSMessage) {
			LongSMSMessage<T> lmsg = (LongSMSMessage<T>) msg;
			if (lmsg.needHandleLongMessage()) {
				// 长短信拆分
				SmsMessage msgcontent = lmsg.getSmsMessage();
				
				//根据默认的Dcs设置将要发送消息的msgdcs值，可能不同的通道长短信分片的最大长度不同
				if(e != null && msgcontent instanceof SmsTextMessage) {
					SmsTextMessage smsTextMessage = (SmsTextMessage)msgcontent;
					Class defaultDcsclz = e.getDefaultSmsDcs().getClass();
					AbstractSmsDcs msgDcs = smsTextMessage.getDcs();
					//类型不同的，以通道默认的Dcs为准
					if(!msgDcs.getClass().equals(defaultDcsclz)) {
						if(e instanceof SMPPEndpointEntity && msgDcs instanceof SmppSmsDcs) {
							Constructor constructor = defaultDcsclz.getConstructor(byte.class,SmsAlphabet.class);
							AbstractSmsDcs newdcs = (AbstractSmsDcs)constructor.newInstance(msgDcs.getValue(),((SMPPEndpointEntity)e).getDefauteSmsAlphabet());
							smsTextMessage.setText(smsTextMessage.getText(), newdcs);
						}else {
							Constructor constructor = defaultDcsclz.getConstructor(byte.class);
							AbstractSmsDcs newdcs = (AbstractSmsDcs)constructor.newInstance(msgDcs.getValue());
							smsTextMessage.setText(smsTextMessage.getText(), newdcs);
						}
					}
				}

				if (msgcontent instanceof SmsConcatMessage) {
					((SmsConcatMessage) msgcontent).setSeqNoKey(lmsg.getSrcIdAndDestId());
				}

				List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);
				// 生成长短信唯一ID
				UniqueLongMsgId uniqueId = null;
				// 保证同一条长短信，通过同一个tcp连接发送

				for (LongMessageFrame frame : frameList) {
					LongSMSMessage<T> t = null;
					if(e != null && e instanceof SMPPEndpointEntity && msg instanceof BaseSm) {
						//SMPP协议判断SmppSplitType
						t = (LongSMSMessage) ((BaseSm)msg).generateMessage(frame, ((SMPPEndpointEntity)e).getSplitType());
					}else {
						t = (LongSMSMessage) lmsg.generateMessage(frame);
					}
					if (uniqueId == null) {
						if(ch!=null) {
							uniqueId = new UniqueLongMsgId(e, ch, t,DefaultSequenceNumberUtil.getSequenceNo(), false);
						}else {
							uniqueId = new UniqueLongMsgId(e, t);
						}
						t.setUniqueLongMsgId(uniqueId);
					} else {
						frame.setTimestamp(((T)t).getTimestamp());
						frame.setSequence(((T)t).getSequenceNo());
						t.setUniqueLongMsgId(new UniqueLongMsgId(uniqueId, frame));
					}
					msgs.add((T) t);
				}
				return msgs;
			}
		} 
		
		msgs.add(msg);
		
		return msgs;
	}
	
	//用于SMPP协议，以optionalParameter方式发送长短信时，删掉messageContents里的长短信头
	public  byte[] removeConcatUDHie(byte[] msgcontent){

		UserDataHeader header = parseUserDataHeader(msgcontent);
		List<InformationElement> newIE = new ArrayList<InformationElement>();
		if (header.infoElement.size() > 0) {
			for (InformationElement udhi : header.infoElement) {
				if (!SmsUdhIei.CONCATENATED_8BIT.equals(udhi.udhIei) && !SmsUdhIei.CONCATENATED_16BIT.equals(udhi.udhIei)) {
					newIE.add(udhi);
				}
			}
			int udhl = msgcontent[0];
			byte[] realcontents = ArrayUtils.subarray(msgcontent, udhl + 1, msgcontent.length);
			//剩下的UDH的长度
			int lastUDHL = 0;
			for (InformationElement udhi : newIE) {
				lastUDHL += (udhi.infoEleLength+2);
			}
			
			//剩下的UDHData数据
			if(lastUDHL>0) {
				byte[] lashUDHData = new byte[lastUDHL+1];
				int i = 0;
				lashUDHData[i++] = (byte)lastUDHL;
				for (InformationElement udhi : newIE) {
					lashUDHData[i++] = udhi.udhIei.getValue();
					lashUDHData[i++] = (byte)udhi.infoEleLength;
					System.arraycopy(udhi.infoEleData, 0, lashUDHData, i, udhi.infoEleLength);
					i+=udhi.infoEleLength;
				}

				//合并 lashUDHData realcontents返回
				byte[] finalyContent = new byte[lashUDHData.length + realcontents.length];
				System.arraycopy(lashUDHData, 0, finalyContent, 0, lashUDHData.length);
				System.arraycopy(realcontents, 0, finalyContent, lashUDHData.length, realcontents.length);
				return finalyContent;
			}else {
				return realcontents;
			}

		}
		return msgcontent;
	}
	
	public List<LongMessageFrame> splitmsgcontent(SmsMessage content) throws SmsException {

		List<LongMessageFrame> result = new ArrayList<LongMessageFrame>();
		SmsPdu[] pdus = content.getPdus();
		int i = 1;
		for (SmsPdu aMsgPdu : pdus) {
			byte[] udh = aMsgPdu.getUserDataHeaders();
			LongMessageFrame frame = new LongMessageFrame();
			
			SmsUdhElement[] udhe = aMsgPdu.getUdhElements_();
			int pkseq  = 1;
			short pktot  = 1;
			short pknum  = 1;
			if(udhe!=null && udhe.length>0) {
				SmsUdhElement  firstudh = udhe[0];
				
				if(SmsUdhIei.CONCATENATED_8BIT.equals(firstudh.getUdhIei_())) {
					byte[] udhdata = firstudh.getUdhIeiData();
					pkseq = byteToInt(udhdata[0]);
					pktot = (short)byteToInt(udhdata[1]);
					pknum = (short)byteToInt(udhdata[2]);
					frame.setConcat(true);
				}else if(SmsUdhIei.CONCATENATED_16BIT.equals(firstudh.getUdhIei_())) {
					byte[] udhdata = firstudh.getUdhIeiData();
					pkseq = (int)((((udhdata[0] & 0xff )<<8) | (udhdata[1]&0xff)) & 0x0ffff);
					pktot = (short)byteToInt(udhdata[2]);
					pknum = (short)byteToInt(udhdata[3]);
					frame.setConcat(true);
				}
			}
			
			frame.setPkseq(pkseq);
			frame.setPktotal(pktot);
			frame.setPknumber(pknum);
			frame.setMsgfmt(aMsgPdu.getDcs());
			
			frame.setTpudhi(udh != null ? (short) 1: (short) 0);

			ByteArrayOutputStream btos = new ByteArrayOutputStream(200);
			frame.setMsgLength((short) encodeOctetPdu(aMsgPdu, btos));
			frame.setMsgContentBytes(btos.toByteArray());
			result.add(frame);
		}

		return result;
	}

	private FrameHolder mergeFrameHolder(FrameHolder fh, LongMessageFrame frame) throws NotSupportedException {
		byte[] msgcontent = frame.getMsgContentBytes();
		UserDataHeader header = parseUserDataHeader(msgcontent);

		if (header.infoElement.size() > 0) {

			for (InformationElement udhi : header.infoElement) {
				if (SmsUdhIei.CONCATENATED_8BIT.equals(udhi.udhIei)) {
					int idx = byteToInt(udhi.infoEleData[2]);
					fh.merge(frame,frame.getPayloadbytes(header.headerlength), idx - 1);
					break;
				} else if (SmsUdhIei.CONCATENATED_16BIT.equals(udhi.udhIei)) {
					int idx = byteToInt(udhi.infoEleData[3]);
					fh.merge(frame,frame.getPayloadbytes(header.headerlength), idx - 1);
					break;
				}
			}

			return fh;
		}

		throw new NotSupportedException("Not Support LongMsg");
	}

	private int byteToInt(byte b) {
		return (int) (b & 0x0ff);
	}

	private FrameHolder createFrameHolder(String serviceNum, LongMessageFrame frame) throws NotSupportedException {

		byte[] msgcontent = frame.getMsgContentBytes();

		UserDataHeader header = parseUserDataHeader(msgcontent);

		if (header.infoElement.size() > 0) {
			FrameHolder frameholder = null;
			InformationElement appudhinfo = null;
			int i = 0;
			int frameKey = 0;
			short pknumber = 1;
			short pkTotle = 1;
			for (InformationElement udhi : header.infoElement) {
				if (SmsUdhIei.CONCATENATED_8BIT.equals(udhi.udhIei)) {
					frameKey = byteToInt(udhi.infoEleData[i++]);
					pkTotle = (short)byteToInt(udhi.infoEleData[i++]);
					frameholder = new FrameHolder(frameKey, pkTotle);
					pknumber =  (short)(byteToInt(udhi.infoEleData[i++]));
					//设置frame里的分片序列号，唯一ID
					frame.setPkseq(frameKey);
					//设置frame里的总分片数
					frame.setPktotal(pkTotle);
					//设置frame里的分片序号
					frame.setPknumber(pknumber);
					
				} else if (SmsUdhIei.CONCATENATED_16BIT.equals(udhi.udhIei)) {
					frameKey = (int)(((udhi.infoEleData[i] & 0xff) << 8) | (udhi.infoEleData[i + 1] & 0xff) & 0x0ffff);
					i += 2;
					pkTotle = (short)byteToInt(udhi.infoEleData[i++]);
					frameholder = new FrameHolder(frameKey,pkTotle );
					pknumber = (short)byteToInt(udhi.infoEleData[i++]);
					//设置frame里的分片序列号，唯一ID
					frame.setPkseq(frameKey);
					//设置frame里的总分片数
					frame.setPktotal(pkTotle);
					//设置frame里的分片序号
					frame.setPknumber(pknumber);
				} else {
					appudhinfo = udhi;
				}
			}
			// 不是续列短信
			if (frameholder == null) {
				frameholder = new FrameHolder(0, 1);
			}
			// 如果没有app的udh，默认为文本短信

			frameholder.setAppUDHinfo(appudhinfo);
			frameholder.setMsgfmt(frame.getMsgfmt());
			frameholder.setSequence(frame.getSequence());
			frameholder.setServiceNum(serviceNum);
			frameholder.merge(frame,frame.getPayloadbytes(header.headerlength), pknumber - 1 );
			
			return frameholder;
		}

		throw new NotSupportedException("Not Support LongMsg");
	}

	private UserDataHeader parseUserDataHeader(byte[] pdu) {
		UserDataHeader udh = new UserDataHeader();
		udh.headerlength = pdu[0]; // 05
		udh.infoElement = new ArrayList<InformationElement>();

		int i = 1;
		while (i < udh.headerlength) {
			InformationElement t = new InformationElement();
			t.udhIei = SmsUdhIei.valueOf(pdu[i++]); // 00
			t.infoEleLength = pdu[i++]; // 03
			t.infoEleData = new byte[t.infoEleLength];
			if(t.infoEleLength>0) {
				System.arraycopy(pdu, i, t.infoEleData, 0, t.infoEleLength);
				i += t.infoEleLength;
			}
			udh.infoElement.add(t);
		}
		return udh;
	}

	private class UserDataHeader {
		int headerlength;
		List<InformationElement> infoElement;
	}



	/**
	 * Convert a stream of septets read as octets into a byte array containing
	 * the 7-bit values from the octet stream.
	 * 
	 * @param octets
	 * @param setptetCnt
	 *            FIXME pass the septet length in here, so if there is a spare
	 *            septet at the end of the octet, we can ignore that
	 * @return byte arrays
	 */

	public static byte[] octetStream2septetStream(byte[] octets,int setptetCnt) {
		
		byte[] septets = new byte[setptetCnt];
		/*
		for (int newIndex = septets.length - 1; newIndex >= 0; --newIndex) {
			for (int bit = 6; bit >= 0; --bit) {
				int oldBitIndex = ((newIndex * 7) + bit);
				if ((octets[oldBitIndex >>> 3] & (1 << (oldBitIndex & 7))) != 0)
					septets[newIndex] |= (1 << bit);
			}
		}
		*/
		
		int ind = 0;
		septets[ind++]  = (byte)(octets[0] & 0x7f) ;
		for(int i = 1 ;i<octets.length ;i++) {
			int mod = (i + 6 ) % 7 + 1    ;
			//当前字节 左移 N 个 bit
			byte b = (byte)(octets[i] << mod);
			//上一个字节 右移 8-N 个 bit ，剩余最高 N 个bit
			byte a =(byte) ((octets[i-1] & 0xff ) >>> (8 - mod) & 0x7f);
			septets[ind++] = (byte)(( b | a ) & 0x7f);
			if(i % 7 == 0 ) {
				septets[ind++]  = (byte)(octets[i] & 0x7f) ;
			}
		}
		if(octets.length * 8  == setptetCnt * 7 )
			septets[ind++] = (byte)(octets[octets.length-1] >>> 1 & 0x7f);
		
		return septets;
	}

	public static int octetLengthfromseptetsLength(int septetLength) {
		return (int) Math.ceil((septetLength * 7) / 8.0);
	}

	private SmsMessage parseWapPdu(byte[] pdu) {

		int index = 0;
		int transactionId = pdu[index++] & 0xFF;
		int pduType = pdu[index++] & 0xFF;
		int headerLength = 0;

		if ((pduType != WspTypeDecoder.PDU_TYPE_PUSH) && (pduType != WspTypeDecoder.PDU_TYPE_CONFIRMED_PUSH)) {

			return null;
		}

		WspTypeDecoder pduDecoder = new WspTypeDecoder(pdu);

		/**
		 * Parse HeaderLen(unsigned integer). From wap-230-wsp-20010705-a
		 * section 8.1.2 The maximum size of a uintvar is 32 bits. So it will be
		 * encoded in no more than 5 octets.
		 */
		if (!pduDecoder.decodeUintvarInteger(index)) {

			return null;
		}
		headerLength = (int) pduDecoder.getValue32();
		index += pduDecoder.getDecodedDataLength();

		int headerStartIndex = index;

		/**
		 * Parse Content-Type. From wap-230-wsp-20010705-a section 8.4.2.24
		 *
		 * Content-type-value = Constrained-media | Content-general-form
		 * Content-general-form = Value-length Media-type Media-type =
		 * (Well-known-media | Extension-Media) *(Parameter) Value-length =
		 * Short-length | (Length-quote Length) Short-length = <Any octet 0-30>
		 * (octet <= WAP_PDU_SHORT_LENGTH_MAX) Length-quote = <Octet 31>
		 * (WAP_PDU_LENGTH_QUOTE) Length = Uintvar-integer
		 */
		if (!pduDecoder.decodeContentType(index)) {

			return null;
		}
		int binaryContentType;
		String mimeType = pduDecoder.getValueString();
		if (mimeType == null) {
			binaryContentType = (int) pduDecoder.getValue32();
			// TODO we should have more generic way to map binaryContentType
			// code to mimeType.
			switch (binaryContentType) {
			case WspTypeDecoder.CONTENT_TYPE_B_DRM_RIGHTS_XML:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_DRM_RIGHTS_XML;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_DRM_RIGHTS_WBXML:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_DRM_RIGHTS_WBXML;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_PUSH_SI:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_SI;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_PUSH_SL:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_SL;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_PUSH_CO:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_CO;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_MMS:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_MMS;
				break;
			case WspTypeDecoder.CONTENT_TYPE_B_VND_DOCOMO_PF:
				mimeType = WspTypeDecoder.CONTENT_MIME_TYPE_B_VND_DOCOMO_PF;
				break;
			default:
				;
				return null;
			}
		} else {
			if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_DRM_RIGHTS_XML)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_DRM_RIGHTS_XML;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_DRM_RIGHTS_WBXML)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_DRM_RIGHTS_WBXML;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_SI)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_PUSH_SI;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_SL)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_PUSH_SL;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_PUSH_CO)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_PUSH_CO;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_MMS)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_MMS;
			} else if (mimeType.equals(WspTypeDecoder.CONTENT_MIME_TYPE_B_VND_DOCOMO_PF)) {
				binaryContentType = WspTypeDecoder.CONTENT_TYPE_B_VND_DOCOMO_PF;
			} else {

				return null;
			}
		}
		index += pduDecoder.getDecodedDataLength();

		switch (binaryContentType) {
		case WspTypeDecoder.CONTENT_TYPE_B_PUSH_SI:
			return dispatchWapPdu_PushWBXML(pdu, transactionId, pduType, headerStartIndex, headerLength, XMLFact.INS.SIinFact);

		case WspTypeDecoder.CONTENT_TYPE_B_PUSH_SL:
			return dispatchWapPdu_PushWBXML(pdu, transactionId, pduType, headerStartIndex, headerLength, XMLFact.INS.SLinFact);

		case WspTypeDecoder.CONTENT_TYPE_B_MMS:
			return dispatchWapPdu_MMS(pdu, transactionId, pduType, headerStartIndex, headerLength);
		default:
			return null;
		}
	}
	
	private enum XMLFact {
		INS;
		private final static XMLInputFactory SLinFact = createSLinFact();
		private final static XMLInputFactory SIinFact = createSIinFact();
		private static XMLInputFactory createSLinFact() {
			XMLInputFactory inFact = new WbXmlInputFactory();
			inFact.setProperty(WbXmlInputFactory.DEFINITION_PROPERTY, WbXmlInitialization.getDefinitionByName("SL 1.0"));
			return inFact;
		}

		private static XMLInputFactory createSIinFact() {
			XMLInputFactory inFact = new WbXmlInputFactory();
			inFact.setProperty(WbXmlInputFactory.DEFINITION_PROPERTY, WbXmlInitialization.getDefinitionByName("SI 1.0"));
			return inFact;
		}
	}

	
	private SmsMessage dispatchWapPdu_PushWBXML(byte[] pdu, int transactionId, int pduType, int headerStartIndex, int headerLength, XMLInputFactory inFact) {
		byte[] header = new byte[headerLength];
		System.arraycopy(pdu, headerStartIndex, header, 0, header.length);
		int dataIndex = headerStartIndex + headerLength;
		byte[] data;

		data = new byte[pdu.length - dataIndex];
		System.arraycopy(pdu, dataIndex, data, 0, data.length);

		try {
			Document doc = wbxmlStream2Doc(inFact, new ByteArrayInputStream(data), false);
			Node node = doc.getFirstChild(); // si sl
			if ("si".equals(node.getNodeName())) {
				NodeList nl = node.getChildNodes();

				if (nl != null && nl.getLength() > 0) {
					for (int index = 0; index < nl.getLength(); index++) {
						Node indication = nl.item(index);

						if ("indication".equals(indication.getNodeName())) {
							NamedNodeMap attrs = indication.getAttributes();
							if (attrs != null) {
								Node uri = attrs.getNamedItem("href");
								if (uri != null) {
									String uriStr = uri.getNodeValue();
									Node message = indication.getFirstChild();
									String text = message != null ? message.getNodeValue() : ""; // Message
									WapSIPush si = new WapSIPush(uriStr, text);
									return new SmsWapPushMessage(si);
								}
							}
						}
					}

				}

			} else if ("sl".equals(node.getNodeName())) {
				NamedNodeMap attrs = node.getAttributes();
				if (attrs != null) {
					Node uri = attrs.getNamedItem("href");
					if (uri != null) {
						String uriStr = uri.getNodeValue();

						WapSLPush sl = new WapSLPush(uriStr);
						return new SmsWapPushMessage(sl);
					}
				}

			}

		} catch (Exception e) {
			logger.error("pdu = [{}]", ByteBufUtil.hexDump(pdu));
		}

		return null;
	}

	private SmsMessage dispatchWapPdu_MMS(byte[] pdu, int transactionId, int pduType, int headerStartIndex, int headerLength) {
		byte[] header = new byte[headerLength];
		System.arraycopy(pdu, headerStartIndex, header, 0, header.length);
		int dataIndex = headerStartIndex + headerLength;
		byte[] data = new byte[pdu.length - dataIndex];
		System.arraycopy(pdu, dataIndex, data, 0, data.length);
		PduParser parse = new PduParser(data);
		GenericPdu notify = parse.parse();
		if (notify != null && notify instanceof NotificationInd) {
			NotificationInd nind = (NotificationInd) notify;
			SmsMmsNotificationMessage mms = new SmsMmsNotificationMessage(new String(nind.getContentLocation(), StandardCharsets.US_ASCII),
					nind.getMessageSize());
			mms.setExpiry((int) (nind.getExpiry() - CachedMillisecondClock.INS.now() / 1000));
			if (nind.getFrom() != null)
				mms.setFrom(nind.getFrom().getString());
			String msgclass = new String(nind.getMessageClass(), StandardCharsets.UTF_8);

			if (PduHeaders.MESSAGE_CLASS_PERSONAL_STR.equals(msgclass)) {
				mms.setMessageClass(MmsConstants.X_MMS_MESSAGE_CLASS_ID_PERSONAL);
			} else if (PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.equals(msgclass)) {
				mms.setMessageClass(MmsConstants.X_MMS_MESSAGE_CLASS_ID_ADVERTISMENT);
			} else if (PduHeaders.MESSAGE_CLASS_AUTO_STR.equals(msgclass)) {
				mms.setMessageClass(MmsConstants.X_MMS_MESSAGE_CLASS_ID_AUTO);
			} else if (PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.equals(msgclass)) {
				mms.setMessageClass(MmsConstants.X_MMS_MESSAGE_CLASS_ID_INFORMATIONAL);
			}

			if (nind.getSubject() != null)
				mms.setSubject(nind.getSubject().getString());
			if (nind.getTransactionId() != null)
				mms.setTransactionId(new String(nind.getTransactionId()));
			return mms;
		}

		return null;
	}





	protected Document wbxmlStream2Doc(XMLInputFactory inFact, InputStream in, boolean event) throws Exception {
		XMLStreamReader xmlStreamReader = null;
		XMLEventReader xmlEventReader = null;
		try {
			if (event) {
				xmlEventReader = inFact.createXMLEventReader(in);
			} else {
				xmlStreamReader = inFact.createXMLStreamReader(in);
			}
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			StAXSource staxSource = event ? new StAXSource(xmlEventReader) : new StAXSource(xmlStreamReader);
			DOMResult domResult = new DOMResult();
			xformer.transform(staxSource, domResult);
			Document doc = (Document) domResult.getNode();
			doc.normalize();
			return doc;
		} finally {
			if (xmlStreamReader != null) {
				try {
					xmlStreamReader.close();
				} catch (Exception e) {
				}
			}
			if (xmlEventReader != null) {
				try {
					xmlEventReader.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Encodes an octet encoded sms pdu.
	 * 
	 * @param pdu
	 * @param destination
	 * @param sender
	 * @return
	 * @throws SmsException
	 */
	private static int encodeOctetPdu(SmsPdu pdu, OutputStream baos) throws SmsException {
		SmsUserData userData = pdu.getUserData();
		byte[] ud = userData.getData();
		byte[] udh = pdu.getUserDataHeaders();
		int length = 0;
		try {
			int nUdBytes = userData.getLength();
			int nUdhBytes = (udh == null) ? 0 : udh.length;

			// 1 octet/ 7 octets
			// TP-VP - Optional

			// UDH?
			if (nUdhBytes == 0) {
				// 1 Integer
				// TP-UDL
				// UDL includes the length of UDH
				length = nUdBytes;

				// n octets
				// TP-UD
				baos.write(ud);
			} else {

				// TP-UDL includes the length of UDH
				// +1 is for the size header...
				length = nUdBytes + nUdhBytes;
				// TP-UDH (including user data header length)
				baos.write(udh);
				// TP-UD
				baos.write(ud);

			}
			baos.close();
		} catch (IOException ex) {
			throw new SmsException(ex);
		}

		return length;
	}

}
