package com.zx.sms.codec.smpp;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmppSmsDcs;
import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsPduUtil;
import org.marre.sms.SmsTextMessage;

import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.HexUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class TestBaseSmCodec extends AbstractSMPPTestMessageCodec<BaseSm> {
    
	private String gsmstr = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|€";
	@Test
    public void decodeDeliverSmWithDeliveryReceiptThatFailedFromEndToEnd() throws Exception {
        ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex("000000A2000000050000000000116AD500010134343935313336313932303537000501475442616E6B000400000000010000006E69643A3934323531343330393233207375623A30303120646C7672643A303031207375626D697420646174653A3039313130343031323420646F6E6520646174653A3039313130343031323420737461743A41434345505444206572723A31303720746578743A20323646313032".toCharArray()));

        DeliverSm pdu0 = (DeliverSm)decode(buffer);

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
        ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex("000000EB00000005000000000000000100010134343935313336313932300000013430343034000400000000000003009069643A3132366538356136656465616331613032303230303939333132343739353634207375623A30303120646C7672643A303031207375626D697420646174653A3130303231393136333020646F6E6520646174653A3130303231393136333020737461743A44454C49565244206572723A30303020546578743A48656C6C6F2020202020202020202020202020200427000102001E0021313236653835613665646561633161303230323030393933313234373935363400".toCharArray()));

        DeliverSm pdu0 = (DeliverSm)decode(buffer);

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
        
        //Assert.assertArrayEquals(HexUtil.toByteArray("69643a3934323531343330393233207375623a30303120646c7672643a303031207375626d697420646174653a3039313130343031323420646f6e6520646174653a3039313130343031323420737461743a41434345505444206572723a31303720746578743a20323646313032"), pdu0.getShortMessage());

        Assert.assertEquals(2, pdu0.getOptionalParameterCount());
        
        System.out.println(pdu0);
        
        for(Tlv tlv : pdu0.getOptionalParameters())
        {
        	System.out.println(tlv.getTagName()+":"+HexUtil.toHexString(tlv.getValue()));
        }
        // interesting -- this example has optional parameters it happened to skip...
        Assert.assertEquals(0, buffer.readableBytes());
    }

    @Test
    public void decodeLargeSequenceNumber() throws Exception {
        ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex("000000400000000500000000A2859F22313030310001013434393531333631393230000001343034303430343034303430343034300000000000000000080000".toCharArray()));

        DeliverSm pdu0 = (DeliverSm)decode(buffer);

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
    public void testLongmDeliverSm(){
    	DeliverSm pdu = new DeliverSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"1111"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"2222"));
    	pdu.setSmsMsg("尊敬的客户,您好！您于2016-03-23 14:51:36通过中国移动10085销售专线订购的【一加手机高清防刮保护膜】，请点击支付http://www.10085.cn/web85/page/zyzxpay/wap_order.html?orderId=76DEF9AE1808F506FD4E6CB782E3B8E7EE875E766D3D335C 完成下单。请在60分钟内完成支付，如有疑问，请致电10085咨询，谢谢！中国移动10085");
    	testlongCodec(pdu);
    }
    
    @Test
    public void testLongmSubmitSm(){
    	
    	SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"1111"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"2222"));
    	pdu.setSmsMsg("尊敬的客户,您好！您于2016-03-23 14:51:36通过中国移动10085销售专线订购的【一加手机高清防刮保护膜】，请点击支付http://www.10085.cn/web85/page/zyzxpay/wap_order.html?orderId=76DEF9AE1808F506FD4E6CB782E3B8E7EE875E766D3D335C 完成下单。请在60分钟内完成支付，如有疑问，请致电10085咨询，谢谢！中国移动10085");
 
       	testlongCodec(pdu);
    	
    }
	@Test
	public void testASCIIcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"1111"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"2222"));
    	pdu.setSmsMsg(new SmsTextMessage("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
     	testlongCodec(pdu);
	}
	@Test
	public void testdefaultcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"1111"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"2222"));
    	pdu.setSmsMsg(gsmstr);
     	testlongCodec(pdu);
	}
	
	@Test
	public void testGSMencode() throws DecoderException
	{
		ByteBuf expected = Unpooled.wrappedBuffer(Hex.decodeHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f1b141b281b291b2f1b3c1b3d1b3e1b401b65".toCharArray()));
		ByteBuf bs = Unpooled.wrappedBuffer(SmsPduUtil.stringToUnencodedSeptets(gsmstr));
		System.out.println(ByteBufUtil.prettyHexDump(expected));
		System.out.println(ByteBufUtil.prettyHexDump(bs));
		Assert.assertTrue(ByteBufUtil.equals(expected,bs));
	}
	@Test
	public void testGSMcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"1111"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"2222"));
    	pdu.setSmsMsg(new SmsTextMessage(gsmstr, SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
     	testlongCodec(pdu);
	}
	@Test
	/**
	 * https://smpp.org/
	 */
	public void testsmpp()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)1,(byte)1,"447712345678"));
    	pdu.setSourceAddress(new Address((byte)5,(byte)0,"MelroseLabs"));
    	pdu.setSmsMsg(new SmsTextMessage("Hello World €$£", SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN)));
    	pdu.setRegisteredDelivery((byte)1);
     	testlongCodec(pdu);
	}
	private void testlongCodec(BaseSm msg)
	{
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
	    	copybuf.writeBytes(buf);
			int length = buf.readableBytes();   
			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    BaseSm result = decode(copybuf);
		
		System.out.println(result);
		Assert.assertEquals(((SmsTextMessage)msg.getSmsMessage()).getText(), ((SmsTextMessage)result.getSmsMessage()).getText());
	}
}
