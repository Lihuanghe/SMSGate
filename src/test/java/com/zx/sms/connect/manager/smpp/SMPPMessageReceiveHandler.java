package com.zx.sms.connect.manager.smpp;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.StandardCharsets;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class SMPPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg)  {
		
		if(msg instanceof DeliverSmReceipt) {
			DeliverSmResp res = ((DeliverSm) msg).createResponse();
			res.setMessageId(String.valueOf(System.currentTimeMillis()));
			return ctx.writeAndFlush(res);
			
		}else if (msg instanceof DeliverSm ) {
			DeliverSmResp res = ((DeliverSm) msg).createResponse();
			String msgcontent = ((DeliverSm) msg).getMsgContent();
			res.setMessageId(DigestUtils.md5Hex(msgcontent.getBytes(StandardCharsets.UTF_8)));
			return ctx.writeAndFlush(res);
		} else if (msg instanceof SubmitSm) {
			SubmitSmResp res = ((SubmitSm) msg).createResponse();
			String msgcontent = ((SubmitSm) msg).getMsgContent();
			byte[] receive = msgcontent.getBytes(StandardCharsets.UTF_8);
			res.setMessageId(DigestUtils.md5Hex(receive));
			ChannelFuture future = ctx.writeAndFlush(res);

			List<SubmitSm> frags = ((SubmitSm) msg).getFragments();
			if (frags != null && !frags.isEmpty()) {
				for (SubmitSm fragment : frags) {

					SubmitSmResp fragres = ((SubmitSm) fragment).createResponse();
					res.setMessageId(String.valueOf(System.currentTimeMillis()));
					ctx.writeAndFlush(fragres);

					if (((SubmitSm) msg).getRegisteredDelivery() == 1) {
						DeliverSmReceipt report = new DeliverSmReceipt();
						report.setId(String.valueOf(fragment.getSequenceNumber()));
						report.setSourceAddress(((SubmitSm) msg).getDestAddress());
						report.setDestAddress(((SubmitSm) msg).getSourceAddress());
						report.setStat("DELIVRD");
						report.setText(fragment.getMsgContent());
						report.setSubmit_date(DateFormatUtils.format(new Date(), "yyMMddHHmm"));
						report.setDone_date(DateFormatUtils.format(new Date(), "yyMMddHHmm"));
						ctx.writeAndFlush(report);
					}
				}
			}
			if (((SubmitSm) msg).getRegisteredDelivery() == 1) {
				DeliverSmReceipt report = new DeliverSmReceipt();
				report.setId(String.valueOf(res.getSequenceNumber()));
				report.setSourceAddress(((SubmitSm) msg).getDestAddress());
				report.setDestAddress(((SubmitSm) msg).getSourceAddress());
				report.setStat("DELIVRD");
				report.setText(((SubmitSm) msg).getMsgContent());
				report.setSubmit_date(DateFormatUtils.format(new Date(), "yyMMddHHmm"));
				report.setDone_date(DateFormatUtils.format(new Date(), "yyMMddHHmm"));
				try {
					ChannelUtil.syncWriteLongMsgToEntity(getEndpointEntity(), report);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return future;
		}
		return null;
	}

}
