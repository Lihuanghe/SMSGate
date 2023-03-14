package com.zx.sms.common;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsMsgClass;
import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.cmpp.BlackHoleHandler;
import com.zx.sms.handler.cmpp.CmppActiveTestRequestMessageHandler;
import com.zx.sms.handler.cmpp.CmppActiveTestResponseMessageHandler;
import com.zx.sms.handler.cmpp.CmppServerIdleStateHandler;
import com.zx.sms.handler.cmpp.CmppTerminateRequestMessageHandler;
import com.zx.sms.handler.cmpp.CmppTerminateResponseMessageHandler;
import com.zx.sms.handler.sgip.SgipServerIdleStateHandler;
import com.zx.sms.handler.smgp.SMGPServerIdleStateHandler;
import com.zx.sms.handler.smpp.SMPPServerIdleStateHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.AttributeKey;

public final class GlobalConstance {
	private GlobalConstance() {
	}
	public static final int MaxMsgLength = 140;
	public static final String emptyString = "";
	public static final byte[] emptyBytes = new byte[0];
	public static final String[] emptyStringArray = new String[0];
	public static final Charset defaultTransportCharset = Charset.forName(PropertiesUtils.getDefaultTransportCharset());
	public static final SmsDcs defaultmsgfmt = SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.ASCII,
			SmsMsgClass.CLASS_UNKNOWN);
	public static final CmppActiveTestRequestMessageHandler activeTestHandler = new CmppActiveTestRequestMessageHandler();
	public static final CmppActiveTestResponseMessageHandler activeTestRespHandler = new CmppActiveTestResponseMessageHandler();
	public static final CmppTerminateRequestMessageHandler terminateHandler = new CmppTerminateRequestMessageHandler();
	public static final CmppTerminateResponseMessageHandler terminateRespHandler = new CmppTerminateResponseMessageHandler();
	public static final CmppServerIdleStateHandler idleHandler = new CmppServerIdleStateHandler();
	public static final SMPPServerIdleStateHandler smppidleHandler = new SMPPServerIdleStateHandler();
	public static final SgipServerIdleStateHandler sgipidleHandler = new SgipServerIdleStateHandler();
	public static final SMGPServerIdleStateHandler smgpidleHandler = new SMGPServerIdleStateHandler();
	public static final AttributeKey<EndpointEntity> entityPointKey = AttributeKey.newInstance("__EndpointEntity");
	public static final AttributeKey<SessionState> attributeKey = AttributeKey.newInstance(SessionState.Connect.name());
	public static final AttributeKey<HAProxyMessage> proxyProtocolKey = AttributeKey.newInstance("_proxyProtocolKey_");
	
	public static final AttributeKey<AbstractSessionStateManager> sessionKey = AttributeKey
			.newInstance("__sessionStateManager");
	public static final AttributeKey<AtomicInteger> SENDWINDOWKEY = AttributeKey.newInstance("_SendWindow_");
	public static final AttributeKey<Long> channelActiveTime = AttributeKey.newInstance("_Active_ch_Time_");
	public static final BlackHoleHandler blackhole = new BlackHoleHandler();
	public static final String IdleCheckerHandlerName = "IdleStateHandler";
	public static final String loggerNamePrefix = "entity.%s";
	public static final String codecName = "codecName";

	public static final String sessionStateManager = "sessionStateManager";
	
	@Deprecated
	public static final String sessionHandler = sessionStateManager;
	public static final String sessionLoginManager = "sessionLoginManager";
	
	public static final String MixedServerIdleStateHandler = "MixedServerIdleStateHandler";
	public static final String PreLengthFieldBasedFrameDecoder = "PreLengthFieldBasedFrameDecoder";
	
	public static final int MESSAGE_DELAY_USER_DEFINED_WRITABILITY_INDEX = 31;
}
