package com.zx.sms.codec.smpp.msg;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.netty.buffer.ByteBuf;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

public class DataSm extends BaseSm<DataSmResp>  implements LongSMSMessage{

    public DataSm() {
        super(SmppConstants.CMD_ID_DATA_SM, "data_sm");
    }

    @Override
    public DataSmResp createResponse() {
        DataSmResp resp = new DataSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<DataSmResp> getResponseClass() {
        return DataSmResp.class;
    }
    
    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.serviceType = ByteBufUtil.readNullTerminatedString(buffer);
        this.sourceAddress = ByteBufUtil.readAddress(buffer);
        this.destAddress = ByteBufUtil.readAddress(buffer);
        this.esmClass = buffer.readByte();
        this.registeredDelivery = buffer.readByte();
        this.dataCoding = buffer.readByte();
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.serviceType);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.destAddress);
        bodyLength += 3;    // esmClass, regDelivery, dataCoding bytes
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.serviceType);
        ByteBufUtil.writeAddress(buffer, this.sourceAddress);
        ByteBufUtil.writeAddress(buffer, this.destAddress);
        buffer.writeByte(this.esmClass);
        buffer.writeByte(this.registeredDelivery);
        buffer.writeByte(this.dataCoding);
    }
	@Override
	public LongMessageFrame generateFrame() {
		
		return doGenerateFrame();
	}
	@Override
	public DataSm generateMessage(LongMessageFrame frame) {
		try {
			return (DataSm)doGenerateMessage(frame);
		} catch (Exception e) {
			return null;
		}
	}
}