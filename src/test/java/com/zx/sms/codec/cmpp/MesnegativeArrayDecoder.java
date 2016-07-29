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

public class MesnegativeArrayDecoder extends AbstractTestMessageCodec<CmppDeliverRequestMessage>{
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
				Hex.decodeHex("7e5da8814c7567ae3130303835000000000000000000000000000000004d434e32323130313033000108383631353132313632363936340000000000000000000000000000000000000000008c05000302b9a2653b51fb4e0bff0c80015e08518d4e006b2176845c48670d4e8630020020300030005e9480015e08768472316db253c84e006b216709598255b76cc94e00822c6d8c51faff0c968f845780015e088fd96b2176849ad86f6eff0c621176845fcd80104e5f8fbe52304e8667819650ff0c4e0d4e454e4857289f9f59344e0a611f52305f025e380000000000000000000000000000000000000000".toCharArray())
				//Hex.decodeHex("3341ab414c75dc5f3130303835000000000000000000000000000000004d434e323231303130330001f53836313437383631393037323900000000000000000000000000000000000000000070060504c34f000001062d1f2b6170706c69636174696f6e2f782d7761702d70726f762e62726f777365722d626f6f6b6d61726b730081ea00010045c67f0187151103000187171103687474703a2f2f7777772e31303038352e636e2f77656238352f68352f48352e68746d6c000101010000000000000000000000000000000000000000".toCharArray()),
				//Hex.decodeHex("522a4980002aa6ce313030383531303100000000000000000000000000636d63637a785f736d730000003133353932323631373931000000000000000000000000000000000000000000000147522a4980002aa6cd41463a3400000030303031303130303030303030313031303030303133353932323631373931000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000".toCharArray())
				};
				
	}


}
