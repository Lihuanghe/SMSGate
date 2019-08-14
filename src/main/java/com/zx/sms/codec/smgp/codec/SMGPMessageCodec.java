package com.zx.sms.codec.smgp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.smgp.msg.SMGPActiveTestMessage;
import com.zx.sms.codec.smgp.msg.SMGPActiveTestRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPBaseMessage;
import com.zx.sms.codec.smgp.msg.SMGPConstants;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPExitMessage;
import com.zx.sms.codec.smgp.msg.SMGPExitRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPLoginMessage;
import com.zx.sms.codec.smgp.msg.SMGPLoginRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPUnknownMessage;
import com.zx.sms.codec.smgp.util.ByteUtil;

public class SMGPMessageCodec extends MessageToMessageCodec<ByteBuf, SMGPBaseMessage> {
	
	private int version;
	
	public SMGPMessageCodec(int version) {
		this.version = version;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, SMGPBaseMessage msg, List<Object> out) throws Exception {
		ByteBuf buf = Unpooled.wrappedBuffer(msg.toBytes(version));
		out.add(buf);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		int length = buf.readableBytes();
		byte[] data = new byte[length];
		buf.readBytes(data);
		SMGPBaseMessage msg = fromBytes(data);
		out.add(msg);
	}
	
	private SMGPBaseMessage fromBytes(byte[] bytes) throws Exception {
		if (bytes == null) {
			return null;
		}
		if (bytes.length < SMGPBaseMessage.SZ_HEADER) {
			return null;
		}

		int commandLength = ByteUtil.byte2int(bytes, 0);
		
		assert bytes.length == commandLength;
		
		int commandId = ByteUtil.byte2int(bytes, 4);

		SMGPBaseMessage baseMsg = null;
		switch (commandId) {
		case SMGPConstants.SMGP_LOGIN:
			baseMsg = new SMGPLoginMessage();
			break;
		case SMGPConstants.SMGP_LOGIN_RESP:
			baseMsg = new SMGPLoginRespMessage();
			break;
		case SMGPConstants.SMGP_SUBMIT:
			baseMsg = new SMGPSubmitMessage();
			break;
		case SMGPConstants.SMGP_SUBMIT_RESP:
			baseMsg = new SMGPSubmitRespMessage();
			break;
		case SMGPConstants.SMGP_DELIVER:
			baseMsg = new SMGPDeliverMessage();
			break;
		case SMGPConstants.SMGP_DELIVER_RESP:
			baseMsg = new SMGPDeliverRespMessage();
			break;
		case SMGPConstants.SMGP_ACTIVE_TEST:
			baseMsg = new SMGPActiveTestMessage();
			break;
		case SMGPConstants.SMGP_ACTIVE_TEST_RESP:
			baseMsg = new SMGPActiveTestRespMessage();
			break;
		case SMGPConstants.SMGP_EXIT_TEST:
			baseMsg = new SMGPExitMessage();
			break;
		case SMGPConstants.SMGP_EXIT_RESP:
			baseMsg = new SMGPExitRespMessage();
			break;
		default:
			baseMsg = new SMGPUnknownMessage(commandId);
			break;
		}
		baseMsg.fromBytes(bytes,version);
		return baseMsg;
	}

}
