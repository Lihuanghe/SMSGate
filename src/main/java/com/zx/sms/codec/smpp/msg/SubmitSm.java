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

public class SubmitSm extends BaseSm<SubmitSmResp>  implements LongSMSMessage<SubmitSm> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4398064962035428672L;
	public SubmitSm() {
        super(SmppConstants.CMD_ID_SUBMIT_SM, "submit_sm");
    }

    @Override
    public SubmitSmResp createResponse() {
        SubmitSmResp resp = new SubmitSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<SubmitSmResp> getResponseClass() {
        return SubmitSmResp.class;
    }

	@Override
	public LongMessageFrame generateFrame() {
		
		return doGenerateFrame();
	}
	@Override
	public SubmitSm generateMessage(LongMessageFrame frame) {
		try {
			return (SubmitSm)doGenerateMessage(frame);
		} catch (Exception e) {
			return null;
		}
	}
	private List<SubmitSm> fragments = null;
	
	@Override
	public List<SubmitSm> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SubmitSm fragment) {
		if(fragments==null)
			fragments = new ArrayList<SubmitSm>();
		
		fragments.add(fragment);
	}
}