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

import com.zx.sms.common.util.StandardCharsets;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.smpp.SMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;

public class TestWithOpenSMPP {

	static Session session = null;

	String systemId = "901782";
	String password = "ICP";
	int port = 2775;
	
	@Before
	public void  chooseport() {
		port = getAvailablePort();
	}
	
    private static int getAvailablePort() {
        try {
        	ServerSocket ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        }
    }
    
    private static int getRandomPort() {
        return 30000 + RandomUtils.nextInt(0,10000);
    }

	@Test
	public void test() throws Exception {
		final EndpointManager manager = EndpointManager.INS;

		SMPPServerEndpointEntity server = new SMPPServerEndpointEntity();
		server.setId("sms-core-smppserver");
		server.setHost("127.0.0.1");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(false);

		SMPPServerChildEndpointEntity child = new SMPPServerChildEndpointEntity();
		child.setId("smppchild");
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
		manager.addEndpointEntity(server);
		manager.openAll();
		Thread.sleep(2000);
		System.out.println("start.....");

		bind();
		String randomStr = String.valueOf(RandomUtils.nextInt(0,10000));
		sendsubmit("@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|" + randomStr,Data.ENC_GSM7BIT);
		sendsubmit("尊敬的客户,您好！您于2016-03-23 14:51:36通过中国移动10085销售专线订购的【一加手机高清防刮保护膜】" + randomStr,Data.ENC_UTF16_BE);
		unbind();
	}

	public void sendsubmit(String msg,String  charencoding) throws Exception {
		SubmitSM request = new SubmitSM();
		request.setServiceType("");
		request.setSourceAddr(new Address((byte) 5, (byte) 0, "198332"));
		request.setDestAddr(new Address((byte) 1, (byte) 1, "10086"));
		request.setReplaceIfPresentFlag((byte) 0);

		request.setShortMessage(msg, charencoding);
		request.setScheduleDeliveryTime("");
		request.setValidityPeriod("");
		request.setEsmClass((byte) 0);
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
		request.setSarMsgRefNum((byte) 0);
		request.setSarTotalSegments((byte) 0);
		request.setSarSegmentSeqnum((byte) 0);
		request.setPayloadType((byte) 0);
		request.setPrivacyIndicator((byte) 0);
		request.setCallbackNumPresInd((byte) 0);
		// send the request

		request.assignSequenceNumber(true);
		byte[] origin = msg.getBytes(StandardCharsets.UTF_8);
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
