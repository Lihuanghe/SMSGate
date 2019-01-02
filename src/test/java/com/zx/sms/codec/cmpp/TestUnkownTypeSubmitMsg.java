package com.zx.sms.codec.cmpp;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsUnkownTypeMessage;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.FstObjectSerializeUtil;
import com.zx.sms.common.util.NettyByteBufUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class TestUnkownTypeSubmitMsg extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {

	@Test
	public void testDecode() throws Exception {
	
		CmppSubmitRequestMessage submit = new CmppSubmitRequestMessage();
		submit.setDestterminalId("13800138000");
		submit.setTppid((short)0x7f);
		submit.setMsg(new SmsUnkownTypeMessage((byte)0xf6,prepareMsgData()));
		ch.writeOutbound(submit);
	
		ByteBuf resultni = ch.readOutbound();
		System.out.println(ByteBufUtil.hexDump(resultni));
//		Assert.assertArrayEquals(prepareMsgData(), NettyByteBufUtil.toArray(resultni, resultni.readableBytes()));
	}
	
	private byte[] prepareMsgData() throws DecoderException {
		return Hex.decodeHex("070003010301700001161102000005B000F6000000000000C7F6284C010101000B0011201612231630180643040101050101110103120A986810414840903184F106090849456085031038810720".toCharArray());
	}
}
