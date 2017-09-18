package com.zx.sms.common.util;

import io.netty.buffer.ByteBuf;

public final class NettyByteBufUtil {

	public static byte[] toArray(ByteBuf buf,int length){
		 byte[] result = new byte[length];
		 buf.readBytes(result);
		 return result;
	}
}
