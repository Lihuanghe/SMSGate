package com.zx.sms.codec.mms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.marre.mime.MimeBodyPart;
import org.marre.mime.MimeFactory;
import org.marre.mime.MimeMultipartMixed;
import org.marre.mms.MmsException;
import org.marre.mms.MmsHeaders;
import org.marre.mms.transport.mm1.Mm1Encoder;
import org.marre.wap.mms.MmsConstants;

import PduParser.GenericPdu;
import PduParser.PduParser;
import PduParser.RetrieveConf;

public class TestMMS1RetrieveConfCodec {

	@Test
	public void testMM1() throws MmsException {
		// 测试 Mm1Encoder的彩信编码是否正确。
		// MM1是用来下发彩信内容的。当手机收到彩信通知后，会发送http GET请求来获到彩信内容。
		// Mm1Encoder就是用来对彩信按MM1协议编码
		MmsHeaders header = new MmsHeaders();
		header.setFrom("10085/TYPE=PLMN");
		header.setMessageType(MmsConstants.X_MMS_MESSAGE_TYPE_ID_M_RETRIEVE_CONF);
		// header.setSubject(subject); //不设置标题
		header.setTo("13800138000/TYPE=PLMN");
		// header.setTransactionId(transactionId); //自动生成
		// header.setVersion(versionId); //默认为1.0
		header.setMessageId("abcdefg");
		header.setDate(new Date());
		MimeMultipartMixed mixedpart = new MimeMultipartMixed();

		long random = RandomUtils.nextLong();
		MimeBodyPart part = MimeFactory.createTextBodyPart(String.valueOf(random));
		part.setContentId("<1.txt>");
		part.setContentLocation("1.txt");
		mixedpart.addBodyPart(part);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Mm1Encoder.writeMessageToStream(out, mixedpart, header);

		PduParser pduparser = new PduParser(out.toByteArray());
		GenericPdu pdu = pduparser.parse();

		RetrieveConf rc = (RetrieveConf) pdu;
		Assert.assertEquals(rc.getDate(), header.getDate().getTime() / 1000);
		Assert.assertEquals(rc.getFrom().getString(), "10085");
		Assert.assertEquals(rc.getMmsVersion(), 0x12);
		Assert.assertEquals(new String(rc.getContentType()), mixedpart.getContentType().getValue());
		Assert.assertEquals(rc.getMessageType() & 0x7f, header.getMessageType());
		Assert.assertEquals(rc.getTo()[0].getString(), "13800138000");
		Assert.assertEquals(new String(rc.getMessageId()), header.getMessageId());
		Assert.assertEquals(new String(rc.getTransactionId()), header.getTransactionId());
		Assert.assertEquals(new String(rc.getBody().getPart(0).getContentId()), "<1.txt>");
		Assert.assertEquals(new String(rc.getBody().getPart(0).getContentLocation()), "1.txt");
		Assert.assertEquals(new String(rc.getBody().getPart(0).getData()), String.valueOf(random));
	}

	@Test
	public void testMMSFromNowSMSFile() throws IOException {
		// 1.MMS是用NowSMS软件生成的MM1彩信内容。
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1.MMS");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		PduParser pduparser = new PduParser(out.toByteArray());
		GenericPdu pdu = pduparser.parse();
		RetrieveConf rc = (RetrieveConf) pdu;
		Assert.assertEquals(rc.getMmsVersion(), 0x10);
		Assert.assertEquals(rc.getMessageType() & 0x7f, MmsConstants.X_MMS_MESSAGE_TYPE_ID_M_RETRIEVE_CONF);
		Assert.assertEquals(rc.getFrom().getString(), "100851");
		Assert.assertEquals(new String(rc.getMessageId()), "123124411224");
		Assert.assertEquals(new String(rc.getTransactionId()), "sdfdf");
		Assert.assertEquals(rc.getSubject().getString(), "MMS MSG");
		Assert.assertEquals(new String(rc.getContentType()), "application/vnd.wap.multipart.related");
		Assert.assertEquals(rc.getBody().getPartsNum(), 2);
		Assert.assertEquals(new String(rc.getBody().getPartByContentId("<1.smil>").getContentLocation()), "1.smil");
	}
	@Test
	public void testH() throws DecoderException, InterruptedException{
		byte[] data = Hex.decodeHex("8c84983174696431323530383537303938305f307373777734008d928b696d3966386568663174356739353440772e6d6d732e726f676572732e636f6d00850456f6c93a8918802b31323530363039313636382f545950453d504c4d4e00972b31323530383537303938302f545950453d504c4d4e00966e6f207375626a656374008a808f8186819081841f22b38a223c736d696c2e736d696c3e0089226170706c69636174696f6e2f736d696c00033683151c6170706c69636174696f6e2f736d696c0085736d696c2e736d696c00c0223c736d696c2e736d696c3e008e736d696c2e736d696c003c736d696c3e0a203c686561643e0a20203c6c61796f75743e0a2020203c726f6f742d6c61796f75742077696474683d223132303022206865696768743d223136303022202f3e0a2020203c726567696f6e2069643d2254657874222077696474683d223132303022206865696768743d2231363022206c6566743d22302220746f703d223134343022206669743d226d65657422202f3e0a2020203c726567696f6e2069643d22496d616765222077696474683d223132303022206865696768743d223134343022206c6566743d22302220746f703d223022206669743d226d65657422202f3e0a20203c2f6c61796f75743e0a203c2f686561643e0a203c626f64793e0a20203c706172206475723d22353030306d7322203e0a2020203c74657874207372633d22746578745f302e747874222020726567696f6e3d2254657874222f3e0a2020203c696d67207372633d2232303136303332365f3133333733332e6a7067222020726567696f6e3d22496d616765222f3e0a20203c2f7061723e0a203c2f626f64793e0a3c2f736d696c3e0a2b3a0f8381ea85746578745f302e74787400c0223c746578745f302e7478743e008e746578745f302e747874004d6f7374206f66207468652073746f7665732061726520676c61737320746f702e20436f756c642074686f7320776f726b20666f7220796f753f448dc96d169e8532303136303332365f3133333733332e6a706700c0223c32303136303332365f3133333733332e6a70673e008e32303136303332365f3133333733332e6a706700ffd8ffe000104a46494600010100000100010000ffdb004300080606070605080707070909080a0c140d0c0b0b0c1912130f141d1a1f1e1d1a1c1c20242e2720222c231c1c2837292c30313434341f27393d38323c2e333432ffdb0043010909090c0b0c180d0d1832211c21323232323232323232323232323232323232323232323232323232323232323232323232323232".toCharArray());
		PduParser pduparser = new PduParser(data);
		GenericPdu pdu = pduparser.parse();
		RetrieveConf rc = (RetrieveConf) pdu;
		Assert.assertEquals(new String(rc.getMessageId()), "im9f8ehf1t5g954@w.mms.rogers.com");
		Assert.assertEquals(new String(rc.getTransactionId()), "1tid12508570980_0ssww4");		
	}

}
