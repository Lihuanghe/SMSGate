package com.zx.sms.codec.smpp.msg;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

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

public class DataSm extends BaseSm<DataSmResp>  implements LongSMSMessage<DataSm>{
	private static final Logger logger = LoggerFactory.getLogger(DataSm.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = -3066462470955865784L;
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
			logger.error("generate DataSm Message Error",e);
			return null;
		}
	}
	
	private List<DataSm> fragments = null;
	
	@Override
	public List<DataSm> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(DataSm fragment) {
		if(fragments==null)
			fragments = new ArrayList<DataSm>();
		
		fragments.add(fragment);
	}
	
	@Override
	public UniqueLongMsgId getUniqueLongMsgId() {
		return super.getUniqueLongMsgId();
	}

	@Override
	public void setUniqueLongMsgId(UniqueLongMsgId id) {
		super.setUniqueLongMsgId(id);
	}

	@Override
	public boolean needHandleLongMessage() {
		return true;
	}

	@Override
	public String getSrcIdAndDestId() {
		return  this.getDestAddress().getAddress()+this.getSourceAddress().getAddress();
	}
	
}