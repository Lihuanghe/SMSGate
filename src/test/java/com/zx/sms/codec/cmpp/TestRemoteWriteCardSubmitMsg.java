package com.zx.sms.codec.cmpp;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.FstObjectSerializeUtil;
import com.zx.sms.common.util.NettyByteBufUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class TestRemoteWriteCardSubmitMsg extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {

	@Test
	public void testDecode() throws Exception {
	
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(prepareMsgData());
		ch.writeInbound(buf);
		CmppSubmitRequestMessage submit = (CmppSubmitRequestMessage)ch.readInbound();
		System.out.println(submit);

		byte[] serialdata = FstObjectSerializeUtil.write(submit);
		ch.writeOutbound(FstObjectSerializeUtil.read(serialdata));
		ByteBuf resultni = ch.readOutbound();
		System.out.println(ByteBufUtil.hexDump(resultni));
		Assert.assertArrayEquals(prepareMsgData(), NettyByteBufUtil.toArray(resultni, resultni.readableBytes()));
	}
	
	private byte[] prepareMsgData() throws DecoderException {
		return Hex.decodeHex("0000012400000004000031b0000000000000000001010101574c574b5a584b000000020000000000000000000000000000000000000000007f01f6333730313530303130000000000000000000000000000000000000000000000000000000000000000000000000000000313036343839393337303135300000000000000000013138343932323038303035000000000000000000008502700000801116390505b000b09744ab428232ea9bf25d353f9acdef5c66ceb36c8366a9d4d86ec6197c530e184819e1fe7f6d3331ddf160ad64073cb8765eaa183f561c6582562d4390210cf8dde999bfa47ec52406a51b090c3f9700c14edc4fa621d6f09cc0970d189fb63b88d16d22b15bb30c2892ea6a5fa07eb69d85e23b2e69be690000000000000000".toCharArray());
	}
}
