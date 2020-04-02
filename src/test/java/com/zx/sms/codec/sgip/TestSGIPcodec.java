package com.zx.sms.codec.sgip;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsDcs;

import com.zx.sms.codec.AbstractSGIPTestMessageCodec;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.common.util.SequenceNumber;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestSGIPcodec extends AbstractSGIPTestMessageCodec<SgipSubmitRequestMessage> {

	@Test
	public void test() throws DecoderException{
		
		System.out.println(new SmsDcs((byte)8).getAlphabet());
		System.out.println(new SequenceNumber());
		ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex("000000a8000000030000000011f333f26ebb10dd31303635353931323530323800000000000000000000000000000000000000000000000000000000000001383631383635373131383633360000000000000000363131313800000000000000000000020000000000000000000000000000090000000000000000000000000000000000000000000000000000000000000000010000000000000004323333610000000000000000".toCharArray()));
		SgipSubmitRequestMessage msg = decode(buffer);
		 Assert.assertNotNull(msg);
		 System.out.println(msg);
		 Assert.assertEquals("106559125028", msg.getSpnumber());
		 Assert.assertEquals("233a", msg.getMsgContent());
	}
}
