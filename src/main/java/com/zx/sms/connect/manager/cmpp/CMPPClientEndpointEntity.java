package com.zx.sms.connect.manager.cmpp;

import com.zx.sms.connect.manager.ClientEndpoint;

/**
 *@author Lihuanghe(18852780@qq.com)
 */
public class CMPPClientEndpointEntity extends CMPPEndpointEntity implements ClientEndpoint {

	@Override
	protected CMPPClientEndpointConnector buildConnector() {
		
		return new CMPPClientEndpointConnector(this);
	}
}
