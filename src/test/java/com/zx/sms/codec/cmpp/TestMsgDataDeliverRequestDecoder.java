package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;

import static com.zx.sms.common.util.NettyByteBufUtil.*;
public class TestMsgDataDeliverRequestDecoder extends AbstractTestMessageCodec<CmppDeliverRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x20;
	}

	@Test
	public void testDecode() {
		byte[] expected = prepareMsgData();
		byte[] actuals = new byte[expected.length];
		ByteBuf buf = Unpooled.wrappedBuffer(expected);
		int index = 0;
		ch.writeInbound(buf);
		CmppDeliverRequestMessage result = null;
		while (null != (result = (CmppDeliverRequestMessage) ch.readInbound())) {
			System.out.println(result);
			ByteBuf bytebuf = Unpooled.copiedBuffer(encode(result));
			int length = bytebuf.readableBytes();
			Assert.assertEquals(expected.length, length);
			System.arraycopy(toArray(bytebuf,bytebuf.readableBytes()), 0, actuals, index,length );
			index = length;
			Assert.assertArrayEquals(expected, actuals);
			Assert.assertEquals("r", result.getMsgContent());
		}
		
	}

	// 下面数据截取自现网10085的报文。cmppSubmit2.0协议
	private byte[] prepareMsgData() {
		return new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x56,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x0a,(byte)0x27,(byte)0xa5,(byte)0x15,(byte)0x7f,(byte)0xd9,(byte)0x13,(byte)0xc1
				,(byte)0x2d,(byte)0x34,(byte)0xb9,(byte)0xc9,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x38,(byte)0x35,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x4d,(byte)0x43,(byte)0x4e,(byte)0x32,(byte)0x32,(byte)0x31,(byte)0x30
				,(byte)0x31,(byte)0x30,(byte)0x31,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x38,(byte)0x36,(byte)0x31,(byte)0x38,(byte)0x37,(byte)0x37,(byte)0x35,(byte)0x36,(byte)0x34,(byte)0x38
				,(byte)0x31,(byte)0x34,(byte)0x31,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x72,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
	}

}
