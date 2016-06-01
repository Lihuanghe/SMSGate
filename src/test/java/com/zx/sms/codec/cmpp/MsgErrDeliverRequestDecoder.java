package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;

/**
 * 数据短信，无法解析
 * */
public class MsgErrDeliverRequestDecoder extends AbstractTestMessageCodec<CmppDeliverRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x30;
	}

	@Test
	public void testDecode() throws DecoderException {
		byte[][] expecteddata = prepareMsgData();
		for(int i =0;i<expecteddata.length;i++){
			
		byte[] expected = expecteddata[i];
		
		//增加报文头
		int headerLength = CmppHead.COMMANDID.getHeadLength();
		int packetLength = expected.length + headerLength;
		ByteBuf buf = Unpooled.buffer(packetLength);
		buf.writeInt(packetLength);
		buf.writeInt((int)CmppPacketType.CMPPDELIVERREQUEST.getCommandId());
		buf.writeInt(0);
		if (packetLength > headerLength) {
			buf.writeBytes(expected);
		}
		byte[] actuals = new byte[packetLength];
		int index = 0;
		ch.writeInbound(buf);
		CmppDeliverRequestMessage result = null;
		while (null != (result = (CmppDeliverRequestMessage) ch.readInbound())) {
			System.out.println(result);
			ByteBuf bytebuf = Unpooled.copiedBuffer(encode(result));
			int length = bytebuf.readableBytes();
			Assert.assertEquals(expected.length+headerLength, length);
			System.arraycopy(bytebuf.array(), 0, actuals, index,length );
			index = length;
			byte[] deleteheader = new byte[expected.length];
			System.arraycopy(actuals, 12, deleteheader, 0,expected.length);
			Assert.assertArrayEquals(expected, deleteheader);
		}
		}
		
	}

	private byte[][] prepareMsgData() throws DecoderException {
		
		
		return new byte[][]{
				//Hex.decodeHex("38a40100533e03d33130303835000000000000000000000000000000004d434e32323130313033004008383631333831373330383833330000000000000000000000000000000000000000008405000300010167098fc74e0d53bb76847ecf5386ff0c53ea67098d704e0d51fa768481ea5df1ff01603b662f62c55fc38eab8fb94f1a593153bb8c01ff0c53ef621153745fd84e8695eeff0c53c867098c014f1a5bb36015593153bb6211ff1f4eba751fff0c52aa529b4e86300173cd60dc4e86300195ee5fc365e061275c31597dff010000000000000000000000000000000000000000".toCharArray())
				//Hex.decodeHex("3341ab414c75dc5f3130303835000000000000000000000000000000004d434e323231303130330001f53836313437383631393037323900000000000000000000000000000000000000000070060504c34f000001062d1f2b6170706c69636174696f6e2f782d7761702d70726f762e62726f777365722d626f6f6b6d61726b730081ea00010045c67f0187151103000187171103687474703a2f2f7777772e31303038352e636e2f77656238352f68352f48352e68746d6c000101010000000000000000000000000000000000000000".toCharArray()),
				Hex.decodeHex("522a4980002aa6ce313030383531303100000000000000000000000000636d63637a785f736d730000003133353932323631373931000000000000000000000000000000000000000000000147522a4980002aa6cd41463a3400000030303031303130303030303030313031303030303133353932323631373931000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000".toCharArray())
				};
				
	}

}
