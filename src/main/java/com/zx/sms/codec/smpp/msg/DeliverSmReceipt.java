package com.zx.sms.codec.smpp.msg;

import io.netty.buffer.ByteBuf;

import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppInvalidArgumentException;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

public class DeliverSmReceipt extends DeliverSm {
	
	//不能修改shortMessage字段
	public byte[] getShortMessage() {
       return null;
    }
	
    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
        
    }
	@Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		super.readBody(buffer);
		
    }



    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
    	
    	super.writeBody(buffer);
    	
    }
    
}
