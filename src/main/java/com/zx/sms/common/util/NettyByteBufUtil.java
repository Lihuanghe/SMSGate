package com.zx.sms.common.util;

import io.netty.buffer.ByteBuf;

public final class NettyByteBufUtil {

	public static byte[] toArray(ByteBuf buf){
		if(buf.hasArray())
			return buf.array();
		
		 byte[] result = new byte[buf.readableBytes()];
		 buf.readBytes(result);
		 return result;
	}
}
