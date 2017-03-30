package com.zx.sms.codec.mms;

import io.netty.buffer.ByteBufUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
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
	public void testMM1() throws MmsException{
		// 测试 Mm1Encoder的彩信编码是否正确。
		//MM1是用来下发彩信内容的。当手机收到彩信通知后，会发送http GET请求来获到彩信内容。 Mm1Encoder就是用来对彩信按MM1协议编码
		MmsHeaders header = new MmsHeaders();
		header.setFrom("10085/TYPE=PLMN");
		header.setMessageType(MmsConstants.X_MMS_MESSAGE_TYPE_ID_M_RETRIEVE_CONF);
		//header.setSubject(subject);			//不设置标题
		header.setTo("13800138000/TYPE=PLMN");
		//header.setTransactionId(transactionId); //自动生成
		//header.setVersion(versionId);			//默认为1.0
		header.setMessageId("abcdefg");
		header.setDate(new Date());
		MimeMultipartMixed mixedpart = new MimeMultipartMixed();
		long random = RandomUtils.nextLong();
		mixedpart.addBodyPart(MimeFactory.createTextBodyPart(String.valueOf(random)));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Mm1Encoder.writeMessageToStream(out, mixedpart, header);
		
		System.out.println(ByteBufUtil.hexDump(out.toByteArray()));
		
		PduParser pduparser = new PduParser(out.toByteArray());
		GenericPdu pdu = pduparser.parse();
		
		RetrieveConf rc = (RetrieveConf)pdu;
		Assert.assertEquals(rc.getDate(),header.getDate().getTime()/1000);
		Assert.assertEquals(rc.getFrom().getString(),"10085");
		Assert.assertEquals(rc.getTo()[0].getString(),"13800138000");
		Assert.assertEquals(new String(rc.getMessageId()),header.getMessageId());
		Assert.assertEquals(new String(rc.getTransactionId()),header.getTransactionId());
		Assert.assertEquals(new String(rc.getBody().getPart(0).getData()),String.valueOf(random));
	}
	@Test
	public void testMMSFromNowSMSFile() throws IOException
	{
		//1.MMS是用NowSMS软件生成的MM1彩信内容。
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("1.MMS");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in,out);
		System.out.println(ByteBufUtil.hexDump(out.toByteArray()));
		PduParser pduparser = new PduParser(out.toByteArray());
		GenericPdu pdu = pduparser.parse();
		RetrieveConf rc = (RetrieveConf)pdu;
	
		Assert.assertEquals(rc.getFrom().getString(),"100851");
		Assert.assertEquals(new String(rc.getMessageId()),"123124411224");
		Assert.assertEquals(new String(rc.getTransactionId()),"sdfdf");
		Assert.assertEquals(rc.getSubject().getString(),"MMS MSG");
		Assert.assertEquals(rc.getBody().getPartsNum(),2);
	}
}
