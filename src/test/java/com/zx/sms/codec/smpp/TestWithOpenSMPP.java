package com.zx.sms.codec.smpp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.TimeoutException;
import org.smpp.WrongSessionStateException;
import org.smpp.pdu.Address;
import org.smpp.pdu.AddressRange;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.UnbindResp;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.util.ByteBuffer;

import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.wap.push.SmsWapPushMessage;
import com.chinamobile.cmos.wap.push.WapSIPush;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.TestConstants;
import com.zx.sms.connect.manager.smpp.SMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;

public class TestWithOpenSMPP {

	static Session session = null;
	
	enum PayloadType {
		UDH,PAYLOAD,SAR
	}

	String systemId = "901782";
	String password = "ICP";
	int port = 2775;
	
	@Before
	public void  chooseport() {
		port = getAvailablePort();
		EndpointManager.INS.removeAll();
	}
	
    private static int getAvailablePort() {
        try {
        	ServerSocket ss = new ServerSocket();
            ss.bind(null);
            ss.close();
            int port = ss.getLocalPort();
            return port;
        } catch (IOException e) {
        	e.printStackTrace();
            return getRandomPort();
        }
    }
    
    private static int getRandomPort() {
        return 30000 + RandomUtils.nextInt(0,10000);
    }

	@Test
	public void test() throws Exception {
		final EndpointManager manager = EndpointManager.INS;
		String testSmsContent = "【中信信用卡】限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请";

		SMPPServerEndpointEntity server = new SMPPServerEndpointEntity();
		server.setId("sms-core-smppserver");
		server.setHost("127.0.0.1");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(false);

		SMPPServerChildEndpointEntity child = new SMPPServerChildEndpointEntity();
		child.setId("opensmppchild");
		child.setSystemId(systemId);
		child.setPassword(password);
		child.setValid(true);
		child.setChannelType(ChannelType.DOWN);
		child.setMaxChannels((short) 3);
		child.setRetryWaitTimeSec((short) 30);
		child.setMaxRetryCnt((short) 3);
		child.setReSendFailMsg(false);
		child.setIdleTimeSec((short) 15);
		// child.setWriteLimit(200);
		// child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SMPPMessageReceiveHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		manager.openEndpoint(server);
		
		Thread.sleep(2000);
		System.out.println("start.....");

		bind();
		String randomStr = String.valueOf(RandomUtils.nextInt(0,10000));
		sendsubmit((byte)0,("@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|" + randomStr).getBytes( Data.ENC_GSM7BIT),Data.ENC_GSM7BIT,PayloadType.UDH);
		sendsubmit((byte)0,(testSmsContent + randomStr).getBytes( Data.ENC_UTF16_BE),Data.ENC_UTF16_BE,PayloadType.UDH);
		
		sendsubmit((byte)0,("@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|" + randomStr).getBytes( Data.ENC_GSM7BIT),Data.ENC_GSM7BIT,PayloadType.PAYLOAD);
		sendsubmit((byte)0,(testSmsContent + randomStr).getBytes( Data.ENC_UTF16_BE),Data.ENC_UTF16_BE,PayloadType.PAYLOAD);
		
		sendsubmit((byte)0,("@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|" + randomStr).getBytes( Data.ENC_GSM7BIT),Data.ENC_GSM7BIT,PayloadType.SAR);
		sendsubmit((byte)0,(testSmsContent + randomStr).getBytes( Data.ENC_UTF16_BE),Data.ENC_UTF16_BE,PayloadType.SAR);
		
		WapSIPush si = new WapSIPush("http://www.baidu.com?", "baidu");
		SmsMessage wap = new SmsWapPushMessage(si);
		
		List<LongMessageFrame> frame = LongMessageFrameHolder.INS.splitmsgcontent(wap);
		sendsubmit((byte)64,frame.get(0).getMsgContentBytes(),Data.ENC_ISO8859_1,PayloadType.SAR);
		sendsubmit((byte)64,frame.get(0).getMsgContentBytes(),Data.ENC_ISO8859_1,PayloadType.UDH);
		sendsubmit((byte)64,frame.get(0).getMsgContentBytes(),Data.ENC_ISO8859_1,PayloadType.PAYLOAD);
		
		Thread.sleep(2000);
		unbind();
	}

	public void sendsubmit(byte emsclass ,byte[] msg,String  charencoding,PayloadType pType) throws Exception {
		SubmitSM request = new SubmitSM();
		request.setServiceType("");
		request.setSourceAddr(new Address((byte) 5, (byte) 0, "10088"));
		request.setDestAddr(new Address((byte) 1, (byte) 1, "13800138001"));
		request.setReplaceIfPresentFlag((byte) 0);
		
		ByteBuffer buf = new ByteBuffer();
		buf.appendBytes(msg);
		switch(pType) {
			case PAYLOAD:
				request.setShortMessage("");
				request.setMessagePayload(buf);
				break;
			case UDH:
				request.setShortMessageData(buf);
				break;
			case SAR:
				request.setShortMessageData(buf);
				request.setSarMsgRefNum((byte) 0);
				request.setSarTotalSegments((byte) 1);
				request.setSarSegmentSeqnum((byte) 1);
				break;
		}
		
		request.setScheduleDeliveryTime("");
		request.setValidityPeriod("");
		request.setEsmClass(emsclass);
		request.setProtocolId((byte) 0);
		request.setPriorityFlag((byte) 0);
		request.setRegisteredDelivery((byte) 0);
		
		request.setDataCoding((byte)(charencoding == Data.ENC_GSM7BIT ? 0 : 8));
		request.setSmDefaultMsgId((byte) 0);

		request.setUserMessageReference((byte) 0);
		request.setSourcePort((byte) 0);
		request.setSourceAddrSubunit((byte) 0);
		request.setDestinationPort((byte) 0);
		request.setDestAddrSubunit((byte) 0);

		request.setPayloadType((byte) 0);
		request.setPrivacyIndicator((byte) 0);
		request.setCallbackNumPresInd((byte) 0);
		// send the request

		request.assignSequenceNumber(true);
		byte[] origin = msg;
		String expected = DigestUtils.md5Hex(origin);
		System.out.println("SubmitSM request " + request.debugString());
		SubmitSMResp response = session.submit(request);
		System.out.println("SubmitSM response " + response.debugString());
		String messageId = response.getMessageId();
		String result = messageId;
		Assert.assertEquals(expected, result);
		
	}

	public void bind() throws ValueNotSetException, TimeoutException, PDUException, WrongSessionStateException, IOException {
		BindRequest request = null;
		BindResponse response = null;

		request = new BindTransmitter();
		TCPIPConnection connection = new TCPIPConnection("127.0.0.1", port);
		connection.setReceiveTimeout(20 * 1000);
		session = new Session(connection);

		// set values
		request.setSystemId(systemId);
		request.setPassword(password);
		request.setSystemType("");
		request.setInterfaceVersion((byte) 0x34);
		AddressRange ar = new AddressRange();
		request.setAddressRange(ar);
		response = session.bind(request);
		System.out.println("Bind response " + response.debugString());
	}

	public void unbind() throws ValueNotSetException, TimeoutException, PDUException, WrongSessionStateException, IOException {
		UnbindResp response = session.unbind();
		System.out.println("Unbind response " + response.debugString());
	}
}
