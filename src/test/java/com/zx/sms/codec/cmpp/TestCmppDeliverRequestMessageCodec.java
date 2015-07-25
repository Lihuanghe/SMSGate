package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.packet.CmppDeliverRequest;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.common.util.MsgId;

public class TestCmppDeliverRequestMessageCodec extends AbstractTestMessageCodec<CmppDeliverRequestMessage>{
	private static final Logger logger = LoggerFactory.getLogger(TestCmppDeliverRequestMessageCodec.class);

	

	@Test
	public void testCodec() {

		CmppDeliverRequestMessage msg = createTestReq();

		test0(msg);
	}

	@Test
	public void testReportCodec() {
		CmppDeliverRequestMessage msg = createTestReq();
		CmppReportRequestMessage reportRequestMessage = new CmppReportRequestMessage();
		reportRequestMessage.setSmscSequence(0x1234L);
		reportRequestMessage.setMsgId(new MsgId());
		reportRequestMessage.setDestterminalId("13800138000");
		reportRequestMessage.setStat("9876");
		msg.setReportRequestMessage(reportRequestMessage);
		msg.setRegisteredDelivery((short) 1);

		test0(msg);

	}

	private void test0(CmppDeliverRequestMessage msg) {

		
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();
		int expectLength = CmppDeliverRequest.DESTID.getBodyLength() + msg.getMsgLength() + CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());


		CmppDeliverRequestMessage result = decode(newbuf);

		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		if (msg.isReport()) {
			Assert.assertEquals(msg.getReportRequestMessage().getSmscSequence(), result.getReportRequestMessage().getSmscSequence());
		} else {
			Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		}
		Assert.assertEquals(msg.getSrcterminalId(), result.getSrcterminalId());
		Assert.assertEquals(msg.getTpudhi(), result.getTpudhi());

	}

	private CmppDeliverRequestMessage createTestReq() {

		Header header = new DefaultHeader();
		// 取时间，用来查看编码解码时间

		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage(header);
		msg.setDestId("13800138000");
		msg.setLinkid("0000");
		// 70个汉字
		msg.setMsgContent("k k k k ");
		msg.setMsgfmt((short) 23);
		msg.setMsgId(new MsgId());
		msg.setRegisteredDelivery((short) 0);
		msg.setServiceid("10086");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		msg.setTppid((short) 3);
		msg.setTpudhi((short) 3);
		header.setSequenceId(System.nanoTime() & 0x7fffffff);
		return msg;
	}
}
