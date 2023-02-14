package com.zx.sms.handler.smpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class AddZeroByteHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(AddZeroByteHandler.class);
	public AddZeroByteHandler(EndpointEntity entity) {
		setEndpointEntity(entity);
	}
	
	@Override
	public String name() {
		return "AddZeroByteHandler";
	}

	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		if(getEndpointEntity() instanceof SMPPEndpointEntity) {
    		SMPPEndpointEntity smppendpoit = (SMPPEndpointEntity) getEndpointEntity();
    		if(smppendpoit.isAddZeroByte()) {
    		    //因为中文的SMPP协议文件翻译有误，造成有部分SMPP网关在接收 Submit 及 Deliver消息时 
    		    // short_message 字段是以 0 结尾的字节
    		    //为兼容此问题，增加这样一个配置。当 配置为true时，接收到的short_message字段会自动去尾部的0
    			if (msg instanceof DeliverSm  || msg instanceof SubmitSm) {
    				BaseSm basemsg = (BaseSm)msg;
    				int l = basemsg.getMsglength();
    				if(l > 0) {
    					byte[] newbyte = new byte[l-1];
    					if(newbyte.length > 0) {
    						System.arraycopy(basemsg.getShortMessage(), 0, newbyte, 0, l-1);
    					}
        				basemsg.setShortMessage(newbyte);
        				basemsg.setMsglength((short)newbyte.length);
    				}
    			}
    		}
    	}
		ctx.fireChannelRead(msg);
		
	}

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	if(getEndpointEntity() instanceof SMPPEndpointEntity) {
    		SMPPEndpointEntity smppendpoit = (SMPPEndpointEntity) getEndpointEntity();
    		if(smppendpoit.isAddZeroByte()) {
    		    //因为中文的SMPP协议文件翻译有误，造成有部分SMPP网关在发送Submit 及 Deliver消息时要求 
    		    // short_message 字段必须是为 0 结尾的字节
    		    //为兼容此问题，增加这样一个配置。当 配置为true时，发送给网关的short_message字段会自动拼0
    			if (msg instanceof DeliverSm  || msg instanceof SubmitSm) {
    				BaseSm basemsg = (BaseSm)msg;
    				int l = basemsg.getMsglength();
    			
    				if(l<255) {
    				
    					byte[] newbyte = new byte[l+1];
        				System.arraycopy(basemsg.getShortMessage(), 0, newbyte, 0, l);
        				newbyte[l] = 0;
        				basemsg.setShortMessage(newbyte);
        				basemsg.setMsglength((short)newbyte.length);
    				}else {
    					basemsg.getShortMessage()[l-1] = 0;
    				}
    			}
    		}
    	}
    	ctx.write(msg, promise);
    	
    }
    
}
