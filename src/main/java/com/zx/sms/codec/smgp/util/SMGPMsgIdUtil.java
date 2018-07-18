/**
 * 
 */
package com.zx.sms.codec.smgp.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smgp.msg.MsgId;
import com.zx.sms.common.util.ChannelUtil;

/**
 * 
 *
 */
public final class SMGPMsgIdUtil {
	private static final Logger logger = LoggerFactory.getLogger(SMGPMsgIdUtil.class);
	public static byte[] msgId2Bytes(MsgId msgId) {
        
        byte[] bytes;
		try {
			bytes = Hex.decodeHex(msgId.toString().toCharArray());
		} catch (DecoderException e) {
			bytes = new byte[10];
		}
        return bytes;
        
	}
	public static MsgId bytes2MsgId(byte[] bytes) {
		
		assert(bytes.length == 10);
		String str = String.valueOf(Hex.encodeHex(bytes));
		try{
			return new MsgId(str);
		}catch(Exception ex){
			logger.warn("Err MsgID : 0x{}" ,str);
			return new MsgId();
		}
	}
}
