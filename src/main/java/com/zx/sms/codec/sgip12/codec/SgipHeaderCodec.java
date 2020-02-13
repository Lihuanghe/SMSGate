package com.zx.sms.codec.sgip12.codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.sgip12.packet.SgipHead;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.SequenceNumber;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipHeaderCodec extends MessageToMessageCodec<ByteBuf, Message> {
	private final Logger logger = LoggerFactory.getLogger(SgipHeaderCodec.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
		// 此时已处理过粘包和断包了，bytebuf里是完整的一帧
		Message message = new DefaultMessage();

		Header header = new DefaultHeader();
		header.setPacketLength(bytebuf.readUnsignedInt());
		header.setCommandId(bytebuf.readInt());
		byte[] seqbytes = new byte[SgipHead.SEQUENCENUMBER.getLength()];
		bytebuf.readBytes(seqbytes);
		SequenceNumber seq = DefaultSequenceNumberUtil.bytes2SequenceN(seqbytes);
		
		message.setTimestamp(seq.getTimestamp());
		header.setSequenceId(seq.getSequenceId());
		header.setNodeId(seq.getNodeIds());
		header.setHeadLength(SgipHead.COMMANDID.getHeadLength());
		header.setBodyLength(header.getPacketLength() - header.getHeadLength());

		if (header.getBodyLength() > 0) {
			message.setBodyBuffer(new byte[(int) header.getBodyLength()]);

			assert (header.getBodyLength() == bytebuf.readableBytes());

			bytebuf.readBytes(message.getBodyBuffer());
		} else {
			message.setBodyBuffer(GlobalConstance.emptyBytes);
		}
		
		message.setHeader(header);
		list.add(message);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, List<Object> list) throws Exception {

		int headerLength = SgipHead.COMMANDID.getHeadLength();
		int packetLength = message.getBodyBuffer().length + headerLength;

		// buf由netty写channel的时候释放
		ByteBuf buf = ctx.alloc().buffer(packetLength);
		buf.writeInt(packetLength);
		buf.writeInt((int) message.getHeader().getCommandId());
		String timeString = DateFormatUtils.format(message.getTimestamp(), "MMddHHmmss");
		buf.writeInt((int) message.getHeader().getNodeId());
		buf.writeInt(Integer.parseInt(timeString));
		buf.writeInt((int) message.getHeader().getSequenceId());
		if (packetLength > headerLength) {
			buf.writeBytes(message.getBodyBuffer());
		}
		list.add(buf);
	}

}
