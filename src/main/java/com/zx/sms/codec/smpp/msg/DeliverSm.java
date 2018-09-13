package com.zx.sms.codec.smpp.msg;

import java.util.ArrayList;
import java.util.List;

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

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.smpp.SmppConstants;

public class DeliverSm extends BaseSm<DeliverSmResp> implements LongSMSMessage<DeliverSm> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6858655335844462036L;

	public DeliverSm() {
        super(SmppConstants.CMD_ID_DELIVER_SM, "deliver_sm");
    }

    @Override
    public DeliverSmResp createResponse() {
        DeliverSmResp resp = new DeliverSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<DeliverSmResp> getResponseClass() {
        return DeliverSmResp.class;
    }

	@Override
	public LongMessageFrame generateFrame() {
		
		return doGenerateFrame();
	}

	@Override
	public DeliverSm generateMessage(LongMessageFrame frame) {
		try {
			return (DeliverSm)doGenerateMessage(frame);
		} catch (Exception e) {
			return null;
		}
	}
	private List<DeliverSm> fragments = null;
	
	@Override
	public List<DeliverSm> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(DeliverSm fragment) {
		if(fragments==null)
			fragments = new ArrayList<DeliverSm>();
		
		fragments.add(fragment);
	}
}