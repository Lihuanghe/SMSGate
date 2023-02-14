package com.zx.sms.codec.cmpp;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.connect.manager.TestConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
public class TestMsgDataSubmitRequestDecoder2 extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x20;
	}
	@Test
	public void testDecodedcsErr() {
		byte[] expected = prepareMsgData();
		//设置fmt字段值
		expected[58] = (byte)0xA8;
		
		byte[] actuals = new byte[expected.length];
		ByteBuf buf = Unpooled.wrappedBuffer(expected);
		int index = 0;
		ch.writeInbound(buf);
		CmppSubmitRequestMessage result = null;
		while (null != (result = (CmppSubmitRequestMessage) ch.readInbound())) {
			System.out.println(result);
			ByteBuf bytebuf = Unpooled.copiedBuffer(encode(result));
			int lenght = bytebuf.readableBytes();
			System.arraycopy(toArray(bytebuf,bytebuf.readableBytes()), 0, actuals, index,lenght );
			index = lenght;
		}
		Assert.assertArrayEquals(expected, actuals);
	}
	@Test
	public void testDecode() {
		byte[] expected = prepareMsgData();
		byte[] actuals = new byte[expected.length];
		ByteBuf buf = Unpooled.wrappedBuffer(expected);
		int index = 0;
		ch.writeInbound(buf);
		CmppSubmitRequestMessage result = null;
		while (null != (result = (CmppSubmitRequestMessage) ch.readInbound())) {
			System.out.println(result);
			ByteBuf bytebuf = Unpooled.copiedBuffer(encode(result));
			int lenght = bytebuf.readableBytes();
			System.arraycopy(toArray(bytebuf,bytebuf.readableBytes()), 0, actuals, index,lenght );
			index = lenght;
		}
		Assert.assertArrayEquals(expected, actuals);
	}

	private byte[] prepareMsgData() {
		return new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xcf,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x04,(byte)0x05,(byte)0x76,(byte)0xbe,(byte)0xfb,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x34,(byte)0x30,(byte)0x30,(byte)0x34,(byte)0x33
				,(byte)0x37,(byte)0x30,(byte)0x31,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x38,(byte)0x35
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x01,(byte)0x31,(byte)0x33,(byte)0x37,(byte)0x32,(byte)0x38,(byte)0x38,(byte)0x30,(byte)0x31,(byte)0x37,(byte)0x35,(byte)0x38,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x30,(byte)0x60,(byte)0xa8,(byte)0x59,(byte)0x7d,(byte)0xff,(byte)0x0c,(byte)0x60,(byte)0xa8,(byte)0x76
				,(byte)0x84,(byte)0x5b,(byte)0x9e,(byte)0x54,(byte)0x0d,(byte)0x52,(byte)0x36,(byte)0x8b,(byte)0xa4,(byte)0x8b,(byte)0xc1,(byte)0x76,(byte)0x7b,(byte)0x96,(byte)0x46,(byte)0x77
				,(byte)0xed,(byte)0x4f,(byte)0xe1,(byte)0x96,(byte)0x8f,(byte)0x67,(byte)0x3a,(byte)0x78,(byte)0x01,(byte)0x4e,(byte)0x3a,(byte)0x00,(byte)0x36,(byte)0x00,(byte)0x30,(byte)0x00
				,(byte)0x31,(byte)0x00,(byte)0x32,(byte)0x00,(byte)0x35,(byte)0x00,(byte)0x30,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0xcf,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x04,(byte)0x05,(byte)0x76,(byte)0xbe,(byte)0xfc,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x08,(byte)0x34,(byte)0x30,(byte)0x30,(byte)0x34,(byte)0x33,(byte)0x37
				,(byte)0x30,(byte)0x31,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x38,(byte)0x35,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01
				,(byte)0x31,(byte)0x35,(byte)0x38,(byte)0x38,(byte)0x38,(byte)0x31,(byte)0x36,(byte)0x35,(byte)0x37,(byte)0x38,(byte)0x39,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
				,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x30,(byte)0x60,(byte)0xa8,(byte)0x59,(byte)0x7d,(byte)0xff,(byte)0x0c,(byte)0x60,(byte)0xa8,(byte)0x76,(byte)0x84
				,(byte)0x5b,(byte)0x9e,(byte)0x54,(byte)0x0d,(byte)0x52,(byte)0x36,(byte)0x8b,(byte)0xa4,(byte)0x8b,(byte)0xc1,(byte)0x76,(byte)0x7b,(byte)0x96,(byte)0x46,(byte)0x77,(byte)0xed
				,(byte)0x4f,(byte)0xe1,(byte)0x96,(byte)0x8f,(byte)0x67,(byte)0x3a,(byte)0x78,(byte)0x01,(byte)0x4e,(byte)0x3a,(byte)0x00,(byte)0x31,(byte)0x00,(byte)0x33,(byte)0x00,(byte)0x37
				,(byte)0x00,(byte)0x32,(byte)0x00,(byte)0x32,(byte)0x00,(byte)0x34,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
	}
	
	@Test
	public void testSameRefNoDiffpkTol() throws DecoderException {
		byte[] expected = prepareSameRefNoData();

		byte[] actuals = new byte[expected.length];
		ByteBuf buf = Unpooled.wrappedBuffer(expected);
		int index = 0;
		ch.writeInbound(buf);
		CmppSubmitRequestMessage result = null;
		String resultStr = "";
		
		while (null != (result = (CmppSubmitRequestMessage) ch.readInbound())) {
			System.out.println(result);
			Assert.assertNotNull(result.getUniqueLongMsgId().getId());
			
			Assert.assertEquals(result.getUniqueLongMsgId().getEntityId(),super.EndPointID);
			Assert.assertEquals(result.getUniqueLongMsgId().getChannelId(),super.ch.id().asShortText());
			System.out.println(result.getUniqueLongMsgId().getId());
			Assert.assertTrue(result.getUniqueLongMsgId().getId().startsWith(super.EndPointID+"."+super.ch.id().asShortText()+"."+result.getSrcIdAndDestId()));
		
			resultStr += result.getMsgContent();
			ByteBuf bytebuf = Unpooled.copiedBuffer(encode(result));
			int lenght = bytebuf.readableBytes();
			System.arraycopy(toArray(bytebuf,bytebuf.readableBytes()), 0, actuals, index,lenght );
			index = lenght;
		}
		Assert.assertNotNull( resultStr);
	}
	
	private byte[] prepareSameRefNoData() throws DecoderException {
		return Hex.decodeHex("0000012b000000043f72b359c7491681f533de86050100093130303836000000000002000000000000000000000000000000000000000000000108000000000000303130303030303000000000000000000000000000000000000000000000000000000000000000000000313030383600000000000000000000000000000000013133383030313338303030000000000000000000008c0500036603015c0a656c76845ba26237002c60a8597dff0160a84e8e0032003000310036002d00300033002d00320033002000310034003a00350031003a00330036901a8fc74e2d56fd79fb52a8003100300030003800359500552e4e137ebf8ba28d2d768430104e0052a0624b673a9ad86e059632522e4fdd62a4819c3011ff0c8bf770b951fb652f4ed800000000000000000000012b000000043f72b35ac7491681f533de86050200093130303836000000000002000000000000000000000000000000000000000000000108000000000000303130303030303000000000000000000000000000000000000000000000000000000000000000000000313030383600000000000000000000000000000000013133383030313338303030000000000000000000008c0500036602020068007400740070003a002f002f007700770077002e0037003800390030003100320033003400350036003700380039003000310032003300340035003600370038003900300031003200330034003500360037003800390030003100320033003400350036003700380039003000310032003300340035003600370038003900300031003200000000000000000000012b000000043f72b35bc7491681f533de86050300093130303836000000000002000000000000000000000000000000000000000000000108000000000000303130303030303000000000000000000000000000000000000000000000000000000000000000000000313030383600000000000000000000000000000000013133383030313338303030000000000000000000008c050003660201003300340035003600370038003900310030003000380035002e0063006e002f00770065006200380035002f0070006100670065002f007a0079007a0078007000610079002f007700610070005f006f0072006400650072002e00680074006d006c003f006f007200640065007200490064003d00370036004400450046003900410045003100000000000000000000012b000000043f72b35cc7491681f533de86050400093130303836000000000002000000000000000000000000000000000000000000000108000000000000303130303030303000000000000000000000000000000000000000000000000000000000000000000000313030383600000000000000000000000000000000013133383030313338303030000000000000000000008c05000366030200380030003800460035003000360046004400340045003600430042003700380032004500330042003800450037004500450038003700350045003700360036004400330044003300330035004300205b8c62104e0b535530028bf75728003600305206949f51855b8c6210652f4ed8ff0c59826709759195eeff0c8bf781f47535003100300000000000000000000000c9000000043f72b35dc7491681f533de86050500093130303836000000000002000000000000000000000000000000000000000000000108000000000000303130303030303000000000000000000000000000000000000000000000000000000000000000000000313030383600000000000000000000000000000000013133383030313338303030000000000000000000002a05000366030300300038003554a88be2ff0c8c228c22ff014e2d56fd79fb52a8003100300030003800350000000000000000".toCharArray());
	}

}
