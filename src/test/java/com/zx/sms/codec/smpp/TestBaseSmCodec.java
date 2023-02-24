package com.zx.sms.codec.smpp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsMsgClass;
import com.chinamobile.cmos.sms.SmsPduUtil;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.chinamobile.cmos.wap.push.SmsWapPushMessage;
import com.chinamobile.cmos.wap.push.WapSLPush;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp.wap.SmsMessageHolder;
import com.zx.sms.codec.smpp.android.gsm.GsmAlphabet;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.HexUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.TestConstants;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class TestBaseSmCodec extends AbstractSMPPTestMessageCodec<BaseSm> {

	protected EndpointEntity buildEndpointEntity() {
		SMPPClientEndpointEntity entity = new SMPPClientEndpointEntity();
		entity.setId("testAllSplitType");
		entity.setSplitType(SmppSplitType.PAYLOADPARAM);
		entity.setInterfaceVersion((byte) 0x33);
		entity.setDefauteSmsAlphabet(SmsAlphabet.GSM);
		entity.setUseHexReceiptedMessageId(true);
		return entity;
	}

	@Test
	public void decodeDeliverSmWithDeliveryReceiptThatFailedFromEndToEnd() throws Exception {
		ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex(
				"000000A2000000050000000000116AD500010134343935313336313932303537000501475442616E6B000400000000010000006E69643A3934323531343330393233207375623A30303120646C7672643A303031207375626D697420646174653A3039313130343031323420646F6E6520646174653A3039313130343031323420737461743A41434345505444206572723A31303720746578743A20323646313032"
						.toCharArray()));

		DeliverSm pdu0 = (DeliverSm) decode(buffer);

		Assert.assertEquals(162, pdu0.getCommandLength());
		Assert.assertEquals(SmppConstants.CMD_ID_DELIVER_SM, pdu0.getCommandId());
		Assert.assertEquals(0, pdu0.getCommandStatus());
		Assert.assertEquals(1141461, pdu0.getSequenceNumber());
		Assert.assertEquals(true, pdu0.isRequest());
		Assert.assertEquals("", pdu0.getServiceType());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getNpi());
		Assert.assertEquals("4495136192057", pdu0.getSourceAddress().getAddress());
		Assert.assertEquals(0x05, pdu0.getDestAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getDestAddress().getNpi());
		Assert.assertEquals("GTBank", pdu0.getDestAddress().getAddress());
		Assert.assertEquals(0x04, pdu0.getEsmClass());
		Assert.assertEquals(0x00, pdu0.getProtocolId());
		Assert.assertEquals(0x00, pdu0.getPriority());
		Assert.assertEquals("", pdu0.getScheduleDeliveryTime());
		Assert.assertEquals("", pdu0.getValidityPeriod());
		Assert.assertEquals(0x01, pdu0.getRegisteredDelivery());
		Assert.assertEquals(0x00, pdu0.getReplaceIfPresent());
		Assert.assertEquals(0x00, pdu0.getDataCoding());
		Assert.assertEquals(0x00, pdu0.getDefaultMsgId());
//        Assert.assertArrayEquals(HexUtil.toByteArray("69643a3934323531343330393233207375623a30303120646c7672643a303031207375626d697420646174653a3039313130343031323420646f6e6520646174653a3039313130343031323420737461743a41434345505444206572723a31303720746578743a20323646313032"), pdu0.getShortMessage());

		Assert.assertEquals(0, pdu0.getOptionalParameterCount());

		// interesting -- this example has optional parameters it happened to skip...
		Assert.assertEquals(0, buffer.readableBytes());
	}

	@Test
	public void decodeDeliveryReceipt0() throws Exception {
		ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex(
				"000000EB00000005000000000000000100010134343935313336313932300000013430343034000400000000000003009069643A3132366538356136656465616331613032303230303939333132343739353634207375623A30303120646C7672643A303031207375626D697420646174653A3130303231393136333020646F6E6520646174653A3130303231393136333020737461743A44454C49565244206572723A30303020546578743A48656C6C6F2020202020202020202020202020200427000102001E0021313236653835613665646561633161303230323030393933313234373935363400"
						.toCharArray()));

		DeliverSm pdu0 = (DeliverSm) decode(buffer);

		Assert.assertEquals(235, pdu0.getCommandLength());
		Assert.assertEquals(SmppConstants.CMD_ID_DELIVER_SM, pdu0.getCommandId());
		Assert.assertEquals(0, pdu0.getCommandStatus());
		Assert.assertEquals(1, pdu0.getSequenceNumber());
		Assert.assertEquals(true, pdu0.isRequest());
		Assert.assertEquals("", pdu0.getServiceType());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getNpi());
		Assert.assertEquals("44951361920", pdu0.getSourceAddress().getAddress());
		Assert.assertEquals(0x00, pdu0.getDestAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getDestAddress().getNpi());
		Assert.assertEquals("40404", pdu0.getDestAddress().getAddress());
		Assert.assertEquals(0x04, pdu0.getEsmClass());
		Assert.assertEquals(0x00, pdu0.getProtocolId());
		Assert.assertEquals(0x00, pdu0.getPriority());
		Assert.assertEquals("", pdu0.getScheduleDeliveryTime());
		Assert.assertEquals("", pdu0.getValidityPeriod());
		Assert.assertEquals(0x00, pdu0.getRegisteredDelivery());
		Assert.assertEquals(0x00, pdu0.getReplaceIfPresent());
		Assert.assertEquals(0x03, pdu0.getDataCoding());
		Assert.assertEquals(0x00, pdu0.getDefaultMsgId());

		// Assert.assertArrayEquals(HexUtil.toByteArray("69643a3934323531343330393233207375623a30303120646c7672643a303031207375626d697420646174653a3039313130343031323420646f6e6520646174653a3039313130343031323420737461743a41434345505444206572723a31303720746578743a20323646313032"),
		// pdu0.getShortMessage());

		Assert.assertEquals(2, pdu0.getOptionalParameterCount());

		System.out.println(pdu0);

		for (Tlv tlv : pdu0.getOptionalParameters()) {
			System.out.println(tlv.getTagName() + ":" + HexUtil.toHexString(tlv.getValue()));
		}
		// interesting -- this example has optional parameters it happened to skip...
		Assert.assertEquals(0, buffer.readableBytes());
	}

	@Test
	public void decodeLargeSequenceNumber() throws Exception {
		ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex(
				"000000400000000500000000A2859F22313030310001013434393531333631393230000001343034303430343034303430343034300000000000000000080000"
						.toCharArray()));

		DeliverSm pdu0 = (DeliverSm) decode(buffer);

		Assert.assertEquals(64, pdu0.getCommandLength());
		Assert.assertEquals(SmppConstants.CMD_ID_DELIVER_SM, pdu0.getCommandId());
		Assert.assertEquals(0, pdu0.getCommandStatus());
		Assert.assertEquals(-1568301278, pdu0.getSequenceNumber());
		Assert.assertEquals(true, pdu0.isRequest());
		Assert.assertEquals("1001", pdu0.getServiceType());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getSourceAddress().getNpi());
		Assert.assertEquals("44951361920", pdu0.getSourceAddress().getAddress());
		Assert.assertEquals(0x00, pdu0.getDestAddress().getTon());
		Assert.assertEquals(0x01, pdu0.getDestAddress().getNpi());
		Assert.assertEquals("4040404040404040", pdu0.getDestAddress().getAddress());
		Assert.assertEquals(0x00, pdu0.getEsmClass());
		Assert.assertEquals(0x00, pdu0.getProtocolId());
		Assert.assertEquals(0x00, pdu0.getPriority());
		Assert.assertEquals("", pdu0.getScheduleDeliveryTime());
		Assert.assertEquals("", pdu0.getValidityPeriod());
		Assert.assertEquals(0x00, pdu0.getRegisteredDelivery());
		Assert.assertEquals(0x00, pdu0.getReplaceIfPresent());
		Assert.assertEquals(0x08, pdu0.getDataCoding());
		Assert.assertEquals(0x00, pdu0.getDefaultMsgId());
		Assert.assertEquals(0, pdu0.getMsglength());
		System.out.println(pdu0);
	}

	@Test
	public void testLongmDeliverSm() {
		DeliverSm pdu = new DeliverSm();
		pdu.setDestAddress(new Address((byte) 0, (byte) 0, "10658987"));
		pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "13800138000"));
		pdu.setSmsMsg(TestConstants.testSmsContent);
		testlongCodec(pdu);
	}

	@Test
	public void testLongmSubmitSm() {

		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 0, (byte) 0, "13800138000"));
		pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "10658987"));
		pdu.setSmsMsg(TestConstants.testSmsContent);
		testlongCodec(pdu);

	}

	@Test
	public void testGSMASCIIcode() {
		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 0, (byte) 0, "13800138000"));
		pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "10658987"));
		pdu.setSmsMsg(new SmsTextMessage(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
				SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
		testlongCodec(pdu);
	}

	@Test
	public void test160GSMASCIIcodeHasExtGSM() throws UnrecoverablePduException, RecoverablePduException {
		for (boolean use8bit : new Boolean[] { Boolean.TRUE, Boolean.FALSE }) {
			System.out.println("================= use8bit : " + use8bit);
			SubmitSm pdu = new SubmitSm();
			pdu.setDestAddress(new Address((byte) 0, (byte) 0, "13800138000"));
			pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "10658987"));
			String text = "1@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà012345678901234567890012^567890";
			Assert.assertEquals(159, text.length()); // 159个字符，有一个扩展GSM字符。编码后正好160字节

			SmppSmsDcs dcs = SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN);
			dcs.setUse8bit(use8bit);
			pdu.setSmsMsg(new SmsTextMessage(text, dcs));

			int count = testlongCodec(pdu);

			// 经过GSM编码，正好组装成一条短信
			Assert.assertEquals(1, count);

			// 长度再加1，将进行长短信分片，GSM编码，分片最大长度159 ,测试内容第一个分片最后边是扩展字符，避免将扩展字符拆成一半，分片长度再减1
			text += "a";
			pdu.setSmsMsg(new SmsTextMessage(text, dcs));

			channel().writeOutbound(pdu);
			ByteBuf buf = (ByteBuf) channel().readOutbound();
			PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext(),(SMPPEndpointEntity)buildEndpointEntity());
			count = 0;
			int udhl = use8bit ? 6 : 7;
			while (buf != null) {
				int length = buf.readableBytes();
				BaseSm msg = (BaseSm) transcoder.decode(buf);
				System.out.println(msg);
				if (count++ == 0) {
					Assert.assertEquals(152, msg.getShortMessage().length - udhl);
					if (use8bit) {
						Assert.assertEquals(158, msg.getShortMessage().length);
					} else {
						Assert.assertEquals(159, msg.getShortMessage().length);
					}
				} else {
					Assert.assertEquals(9, msg.getShortMessage().length - udhl);
					if (use8bit) {
						Assert.assertEquals(15, msg.getShortMessage().length);
					} else {
						Assert.assertEquals(16, msg.getShortMessage().length);
					}
				}
				buf = (ByteBuf) channel().readOutbound();
			}
		}

	}

	@Test
	public void test160GSMASCIIcodeNoGSMEXT() throws UnrecoverablePduException, RecoverablePduException {
		for (boolean use8bit : new Boolean[] { Boolean.TRUE, Boolean.FALSE }) {
			System.out.println("================= use8bit : " + use8bit);
			SubmitSm pdu = new SubmitSm();
			pdu.setDestAddress(new Address((byte) 0, (byte) 0, "13800138000"));
			pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "10658987"));
			String text = "1@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà01234567890123456789001234567890";
			Assert.assertEquals(160, text.length());
			SmppSmsDcs dcs = SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN);
			dcs.setUse8bit(use8bit);
			pdu.setSmsMsg(new SmsTextMessage(text,dcs));
			int count = testlongCodec(pdu);

			// 经过GSM编码，正好组装成一条短信
			Assert.assertEquals(1, count);

			// 长度再加1，将进行长短信分片，GSM编码，分片最大长度159 ,测试内容第一个分片最后边是扩展字符，避免将扩展字符拆成一半，分片长度再减1
			text += "a";
			pdu.setSmsMsg(new SmsTextMessage(text,dcs));

			channel().writeOutbound(pdu);
			ByteBuf buf = (ByteBuf) channel().readOutbound();
			PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext(),(SMPPEndpointEntity)buildEndpointEntity());
			count = 0;
			int udhl = use8bit ? 6 : 7;
			while (buf != null) {
				int length = buf.readableBytes();
				BaseSm msg = (BaseSm) transcoder.decode(buf);
				System.out.println(msg);
				if (count++ == 0) {
					if (use8bit) {
						Assert.assertEquals(159, msg.getShortMessage().length);
					} else {
						Assert.assertEquals(159, msg.getShortMessage().length);
					}
				} else {
					if (use8bit) {
						Assert.assertEquals(14, msg.getShortMessage().length);
					} else {
						Assert.assertEquals(16, msg.getShortMessage().length);
					}
				}
				buf = (ByteBuf) channel().readOutbound();
			}
		}
	}

	@Test
	public void testdefaultcode() {
		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 0, (byte) 0, "13800138000"));
		pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "10658987"));
		pdu.setSmsMsg(SmsPduUtil.gsmstr, SmsAlphabet.GSM);
		testlongCodec(pdu);
	}

	@Test
	public void testGSMencode() throws DecoderException {
		ByteBuf expected = Unpooled.wrappedBuffer(Hex.decodeHex(
				"000102030405060708090a0b0c0d0e0f101112131415161718191a1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f1b0a1b141b281b291b2f1b3c1b3d1b3e1b401b65"
						.toCharArray()));
		ByteBuf bs = Unpooled.wrappedBuffer(SmsPduUtil.stringToUnencodedSeptets(SmsPduUtil.gsmstr));
		System.out.println(ByteBufUtil.prettyHexDump(expected));
		System.out.println(ByteBufUtil.prettyHexDump(bs));
		Assert.assertTrue(ByteBufUtil.equals(expected, bs));
	}

	@Test
	public void testGSMcode() {
		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 0, (byte) 0, "1111"));
		pdu.setSourceAddress(new Address((byte) 0, (byte) 0, "2222"));
		pdu.setSmsMsg(new SmsTextMessage("[@[@[@[@",
				SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
		testlongCodec(pdu);
	}

	@Test
	public void testdeliverSmReceipt() throws SmppInvalidArgumentException, UnsupportedEncodingException {
		DeliverSmReceipt report = new DeliverSmReceipt();
		report.setId(String.valueOf(0x0ffffffffL));
		report.setSourceAddress(new Address((byte) 2, (byte) 1, "13800138000"));
		report.setDestAddress(new Address((byte) 2, (byte) 1, "13800138000"));
		report.setStat("ACCEPTD");
		report.setSubmit_date("0911040124");
		report.setDone_date("0911040124");
		report.setErr("1232");
//		String reportString = "id:94251430923 submit date:0911040124  err:1232 done date:0911040124 stat:ACCEPTD custom:1";
//		report.setShortMessage(reportString.getBytes());
		System.out.println(report);
		channel().writeOutbound(report);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {
			copybuf.writeBytes(buf);
			int length = buf.readableBytes();
			buf = (ByteBuf) channel().readOutbound();
		}

		DeliverSmReceipt result = (DeliverSmReceipt) decode(copybuf);
		System.out.println(result);
		Assert.assertEquals("ffffffff", result.getId());
		Assert.assertEquals("0911040124", result.getSubmit_date());
		Assert.assertEquals("0911040124", result.getDone_date());
		Assert.assertEquals("ACCEPTD", result.getStat());
		Assert.assertEquals("1232", result.getErr());
//	    Assert.assertEquals("1",result.getReportKV("custom"));
	}
	@Test
	public void testdeliverSmReceiptOpt() throws SmppInvalidArgumentException, UnsupportedEncodingException {
		DeliverSmReceipt report = new DeliverSmReceipt();
		report.setSourceAddress(new Address((byte) 2, (byte) 1, "13800138000"));
		report.setDestAddress(new Address((byte) 2, (byte) 1, "13800138000"));
		report.setSubmit_date("0911040124");
		report.setDone_date("0911040124");
		report.setErr("1232");
		report.addOptionalParameter(new Tlv(SmppConstants.TAG_RECEIPTED_MSG_ID,ByteArrayUtil.toCOctetString(String.valueOf(0x0ffffffffL))));
		report.addOptionalParameter(new Tlv(SmppConstants.TAG_MSG_STATE,ByteArrayUtil.toByteArray((byte)6)));
		System.out.println(report);
		channel().writeOutbound(report);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {
			copybuf.writeBytes(buf);
			int length = buf.readableBytes();
			buf = (ByteBuf) channel().readOutbound();
		}

		DeliverSmReceipt result = (DeliverSmReceipt) decode(copybuf);
		System.out.println(result);
		Assert.assertEquals("ffffffff", result.getId());
		Assert.assertEquals("0911040124", result.getSubmit_date());
		Assert.assertEquals("0911040124", result.getDone_date());
		Assert.assertEquals("ACCEPTED", result.getStat());
		Assert.assertEquals("1232", result.getErr());
//	    Assert.assertEquals("1",result.getReportKV("custom"));
	}
	
	@Test
	/**
	 * https://smpp.org/
	 */
	public void testsmpp() {
		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 1, (byte) 1, "447712345678"));
		pdu.setSourceAddress(new Address((byte) 5, (byte) 0, "MelroseLabs"));
		pdu.setSmsMsg(new SmsTextMessage("Hello World €$£",
				SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
		pdu.setRegisteredDelivery((byte) 1);
		testlongCodec(pdu);
	}

	private int testlongCodec(BaseSm msg) {
		channel().writeOutbound(msg);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		int count = 0;
		while (buf != null) {
			copybuf.writeBytes(buf);
			int length = buf.readableBytes();
			buf = (ByteBuf) channel().readOutbound();
			count++;
		}

		BaseSm result = decode(copybuf);

		System.out.println(result);
		Assert.assertNotNull(((LongSMSMessage) result).getUniqueLongMsgId().getId());
		System.out.println(((LongSMSMessage) result).getUniqueLongMsgId());
		Assert.assertEquals(((SmsTextMessage) msg.getSmsMessage()).getText(),
				((SmsTextMessage) result.getSmsMessage()).getText());
		return count;
	}

	@Test
	public void testseptetencode() throws UnsupportedEncodingException, Exception {
		String str = shuffle(SmsPduUtil.gsmstr);
		int septetCount = GsmAlphabet.countGsmSeptets(str, true);
		byte[] bs = SmsPduUtil.stringToUnencodedSeptets(str);
		System.out.println("septetCount:" + bs.length);
		Assert.assertEquals("septetCount:" + bs.length,septetCount, bs.length);
		System.out.println(ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(bs)));
		
		byte[] encode = SmsPduUtil.septetStream2octetStream(bs,0);
		System.out.println(ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(encode)));
		
		byte[] ret = SmsPduUtil.octetStream2septetStream(encode,0, septetCount,0);
		System.out.println(ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(ret)));
		Assert.assertArrayEquals(ret, bs);

		String strRet = SmsPduUtil.readSeptets(encode, septetCount);
		System.out.println(strRet);
		Assert.assertEquals(str, strRet);
	}

	@Test
	public void testseptetAll() throws Exception {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < SmsPduUtil.gsmstr.length(); i++) {
			sb.append(SmsPduUtil.gsmstr.charAt(RandomUtils.nextInt() % SmsPduUtil.gsmstr.length()));
			TestGsmAlphabet(sb.toString());
		}
	}

	@Test
	public void testAllWapSplitType() throws Exception {
		BaseSm pdu = createMsg();
		String origin = "http://www.baidu.com?123这个链接的访问记录，跟普通链ssage(content,SmsDcs.getGeneralDataC"
				+ RandomUtils.nextInt();
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		WapSLPush sl = new WapSLPush(origin);
		SmsMessage wap = new SmsWapPushMessage(sl);

		pdu.setSmsMsg(wap);

		channel().writeOutbound(pdu);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {
			copybuf.writeBytes(buf);
			int length = buf.readableBytes();
			buf = (ByteBuf) channel().readOutbound();
		}

		BaseSm result = decode(copybuf);

		System.out.println(result);
		Assert.assertNotNull(((LongSMSMessage) result).getUniqueLongMsgId().getId());
		System.out.println(((LongSMSMessage) result).getUniqueLongMsgId());
		Assert.assertEquals(origin, ((WapSLPush) ((SmsWapPushMessage) result.getSmsMessage()).getWbxml()).getUri());
		System.out.println();

		for (SmppSplitType type : SmppSplitType.values()) {
			SMPPClientEndpointEntity entity = (SMPPClientEndpointEntity) buildEndpointEntity();
			entity.setSplitType(type);
			List<BaseSm> splitted = LongMessageFrameHolder.INS.splitLongSmsMessage(entity, pdu);
			System.out.println("SlitType :" + type + "     \tsplitted Size :" + splitted.size());
			// 打乱顺序
			Collections.shuffle(splitted);
			// 再合并
			for (BaseSm sm : splitted) {
				SmsMessageHolder merged = LongMessageFrameHolder.INS.putAndget(entity,
						((LongSMSMessage) sm).getSrcIdAndDestId(), (LongSMSMessage) sm, false);
				if (merged != null) {
					// 合并完成
					SmsMessage sms = merged.getSmsMessage();
					System.out.println(sms);
					Assert.assertEquals(origin, ((WapSLPush) ((SmsWapPushMessage) sms).getWbxml()).getUri());
				}
			}
		}
	}

	@Test
	public void testAllTextSplitType() throws Exception {
		BaseSm pdu = createMsg();
		String origin = "123这个链接的访问记录，跟普通链ssage(content,SmsDcs.getGeneralDataC" + RandomUtils.nextInt();
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		pdu.setSmsMsg(new SmsTextMessage(origin,
				SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN)));

		// 测试网络收发
		testlongCodec(pdu);
		System.out.println();
		// 多种
		for (SmppSplitType type : SmppSplitType.values()) {
			SMPPClientEndpointEntity entity = new SMPPClientEndpointEntity();
			entity.setId("testAllSplitType");
			entity.setSplitType(type);
			List<BaseSm> splitted = LongMessageFrameHolder.INS.splitLongSmsMessage(entity, pdu);
			System.out.println("SlitType :" + type + "     \tsplitted Size :" + splitted.size());
			// 打乱顺序
			Collections.shuffle(splitted);
			// 再合并
			for (BaseSm sm : splitted) {
				SmsMessageHolder merged = LongMessageFrameHolder.INS.putAndget(entity,
						((LongSMSMessage) sm).getSrcIdAndDestId(), (LongSMSMessage) sm, false);
				if (merged != null) {
					// 合并完成
					SmsMessage sms = merged.getSmsMessage();
					System.out.println(sms);
					Assert.assertEquals(origin, ((SmsTextMessage) sms).getText());
				}
			}

		}
	}

	private BaseSm createMsg() {
		SubmitSm pdu = new SubmitSm();
		pdu.setDestAddress(new Address((byte) 1, (byte) 1, "447712345678"));
		pdu.setSourceAddress(new Address((byte) 5, (byte) 0, "MelroseLabs"));

		pdu.setRegisteredDelivery((byte) 1);
		return pdu;
	}
	
	
	/**
	 * 从 ： https://techsofar.com/combining-sms-messages/ 找的测试数据，对应Command At命令编码方式
	 * 
	 **/
	@Test
	public void testGSM7bitUnpack() throws Exception {
		StringBuilder sb = new StringBuilder();
		byte[][] datas = new byte[][] { Hex.decodeHex(
				"A0050003000301986F79B90D4AC3E7F53688FC66BFE5A0799A0E0AB7CB741668FC76CFCB637A995E9783C2E4343C3D4F8FD3EE33A8CC4ED359A079990C22BF41E5747DDE7E9341F4721BFE9683D2EE719A9C26D7DD74509D0E6287C56F791954A683C86FF65B5E06B5C36777181466A7E3F5B0AB4A0795DDE936284C06B5D3EE741B642FBBD3E1360B14AFA7E7"
				.toCharArray()),Hex.decodeHex(
						"A005000300030240EEF79C2EAF9341657C593E4ED3C3F4F4DB0DAAB3D9E1F6F80D6287C56F797A0E72A7E769509D0E0AB3D3F17A1A0E2AE341E53068FC6EB7DFE43768FC76CFCBF17A98EE22D6D37350B84E2F83D2F2BABC0C22BFD96F3928ED06C9CB7079195D7693CBF2341D947683EC6F761D4E0FD3CB207B999DA683CAF37919344EB3D9F53688FC66BFE5"
						.toCharArray()),
			Hex.decodeHex(
				"90050003000303CAA0721D64AE9FD3613AC85D67B3C32078589E0ED3EB7257113F2EC3E9E5BA1C344FBBE9A0F7781C2E8FC374D0B80E4F93C3F4301DE47EBB4170F93B4D2EBBE92CD0BCEEA683D26ED0B8CE868741F17A1AF4369BD3E37418442ECFCBF2BA9B0E6ABFD9EC341D1476A7DBA03419549ED341ECB0F82DAFB75D"
				.toCharArray())};
		for(byte[] data : datas ) {
			int length = data[0]&0xff;
			String strRet = SmsPduUtil.unencodedSeptetsToString(SmsPduUtil.octetStream2septetStream(data,7, length-7,1));
			System.out.println(strRet);
			String expect = GsmAlphabet.gsm7BitPackedToString(data, 7, length-7,1);
			System.out.println(expect);
			Assert.assertEquals(expect, strRet);
			sb.append(expect);
//			byte[] encodebyte = GsmAlphabet.stringToGsm7BitPackedWithHeader(expect,new byte[] {0,3,0,3,2});
			byte[] encodebyte2 = SmsPduUtil.septetStream2octetStream(SmsPduUtil.stringToUnencodedSeptets(expect),1);
					
			System.out.println(Hex.encodeHex(encodebyte2));
			Assert.assertArrayEquals(Arrays.copyOfRange(data, 7, data.length), encodebyte2);
		}
		System.out.println("=============");
		System.out.println(sb.toString());
	}

	private void TestGsmAlphabet(String str) throws Exception {

//		String str = shuffle(gsmstr);
		byte[] pdu = GsmAlphabet.stringToGsm7BitPacked(str);
		byte[] unencodeseptet = SmsPduUtil.stringToUnencodedSeptets(str);
		byte[] encode = SmsPduUtil.septetStream2octetStream(unencodeseptet,0);

		//
		byte[] data = Unpooled.wrappedBuffer(pdu).copy(1, pdu.length - 1).array();
		Assert.assertArrayEquals(encode, data);

		int septetCount = GsmAlphabet.countGsmSeptets(str, true);
		String result = GsmAlphabet.gsm7BitPackedToString(data, 0, septetCount);
		String strRet = SmsPduUtil.readSeptets(data, septetCount);
		System.out.println(result);
		Assert.assertEquals(str, result);
		Assert.assertEquals(str, strRet);
	}

	private String shuffle(String str) {
		char[] char_arr = str.toCharArray();
		List<Character> char_list = new ArrayList<Character>();
		for (char t : char_arr)
			char_list.add(t);

		Collections.shuffle(char_list);
		StringBuffer sb = new StringBuffer();
		for (Object c : char_list) {
			sb.append(c.toString());
		}

		return sb.toString();
	}
}
