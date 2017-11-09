package com.zx.sms.common;

import io.netty.util.AttributeKey;

import java.nio.charset.Charset;

import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.handler.cmpp.BlackHoleHandler;
import com.zx.sms.handler.cmpp.CmppActiveTestRequestMessageHandler;
import com.zx.sms.handler.cmpp.CmppActiveTestResponseMessageHandler;
import com.zx.sms.handler.cmpp.CmppServerIdleStateHandler;
import com.zx.sms.handler.cmpp.CmppTerminateRequestMessageHandler;
import com.zx.sms.handler.cmpp.CmppTerminateResponseMessageHandler;
import com.zx.sms.handler.smpp.SmppServerIdleStateHandler;
import com.zx.sms.session.cmpp.SessionState;

public interface GlobalConstance {
	public final static int MaxMsgLength = 140;
	public final static String emptyString = "";
	public final static byte[] emptyBytes= new byte[0];
	public final static String[] emptyStringArray= new String[0];
  
    public static final Charset defaultTransportCharset = Charset.forName(PropertiesUtils.getdefaultTransportCharset());

    public final static  CmppActiveTestRequestMessageHandler activeTestHandler =  new CmppActiveTestRequestMessageHandler();
    public final static  CmppActiveTestResponseMessageHandler activeTestRespHandler =  new CmppActiveTestResponseMessageHandler();
    public final static  CmppTerminateRequestMessageHandler terminateHandler =  new CmppTerminateRequestMessageHandler();
    public final static  CmppTerminateResponseMessageHandler terminateRespHandler = new CmppTerminateResponseMessageHandler();
    public final static  CmppServerIdleStateHandler idleHandler = new CmppServerIdleStateHandler();
    public final static  SmppServerIdleStateHandler smppidleHandler = new SmppServerIdleStateHandler();
    public final static AttributeKey<SessionState> attributeKey = AttributeKey.newInstance(SessionState.Connect.name());
    public final static BlackHoleHandler blackhole = new BlackHoleHandler();
    public final static String IdleCheckerHandlerName = "IdleStateHandler";
    public final static boolean isSupportLongMsg = PropertiesUtils.getisSupportLongMsg();
    
    public final static String loggerNamePrefix = "entity.%s";
    public final static String codecName = "codecName";
}
