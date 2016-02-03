/**
 * 
 */
package com.zx.sms.codec.cmpp10.packet;

import io.netty.handler.codec.MessageToMessageCodec;

import com.zx.sms.codec.cmpp.CmppActiveTestRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppActiveTestResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppCancelRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppCancelResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppConnectRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppConnectResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppDeliverRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppDeliverResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppQueryRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppQueryResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppSubmitRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppSubmitResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppTerminateRequestMessageCodec;
import com.zx.sms.codec.cmpp.CmppTerminateResponseMessageCodec;
import com.zx.sms.codec.cmpp.packet.CmppActiveTestRequest;
import com.zx.sms.codec.cmpp.packet.CmppActiveTestResponse;
import com.zx.sms.codec.cmpp.packet.CmppCancelRequest;
import com.zx.sms.codec.cmpp.packet.CmppCancelResponse;
import com.zx.sms.codec.cmpp.packet.CmppConnectRequest;
import com.zx.sms.codec.cmpp.packet.CmppConnectResponse;
import com.zx.sms.codec.cmpp.packet.CmppDeliverRequest;
import com.zx.sms.codec.cmpp.packet.CmppDeliverResponse;
import com.zx.sms.codec.cmpp.packet.CmppQueryRequest;
import com.zx.sms.codec.cmpp.packet.CmppQueryResponse;
import com.zx.sms.codec.cmpp.packet.CmppSubmitRequest;
import com.zx.sms.codec.cmpp.packet.CmppSubmitResponse;
import com.zx.sms.codec.cmpp.packet.CmppTerminateRequest;
import com.zx.sms.codec.cmpp.packet.CmppTerminateResponse;
import com.zx.sms.codec.cmpp.packet.PacketStructure;
import com.zx.sms.codec.cmpp.packet.PacketType;


/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum Cmpp10PacketType implements PacketType {
    CMPPCONNECTREQUEST(0x00000001L, CmppConnectRequest.class,CmppConnectRequestMessageCodec.class),
    CMPPCONNECTRESPONSE(0x80000001L, CmppConnectResponse.class,CmppConnectResponseMessageCodec.class),
    CMPPTERMINATEREQUEST(0x00000002L, CmppTerminateRequest.class,CmppTerminateRequestMessageCodec.class),
    CMPPTERMINATERESPONSE(0x80000002L, CmppTerminateResponse.class,CmppTerminateResponseMessageCodec.class),    
    CMPPSUBMITREQUEST(0x00000004L, CmppSubmitRequest.class,CmppSubmitRequestMessageCodec.class), 
    CMPPSUBMITRESPONSE(0x80000004L, CmppSubmitResponse.class,CmppSubmitResponseMessageCodec.class),
    CMPPDELIVERREQUEST(0x00000005L, CmppDeliverRequest.class,CmppDeliverRequestMessageCodec.class),
    CMPPDELIVERRESPONSE(0x80000005L, CmppDeliverResponse.class,CmppDeliverResponseMessageCodec.class),    
    CMPPQUERYREQUEST(0x00000006L, CmppQueryRequest.class,CmppQueryRequestMessageCodec.class),
    CMPPQUERYRESPONSE(0x80000006L, CmppQueryResponse.class,CmppQueryResponseMessageCodec.class),
    CMPPCANCELREQUEST(0x00000007L, CmppCancelRequest.class,CmppCancelRequestMessageCodec.class),
    CMPPCANCELRESPONSE(0x80000007L, CmppCancelResponse.class,CmppCancelResponseMessageCodec.class),
    CMPPACTIVETESTREQUEST(0x00000008L, CmppActiveTestRequest.class,CmppActiveTestRequestMessageCodec.class),
    CMPPACTIVETESTRESPONSE(0x80000008L, CmppActiveTestResponse.class,CmppActiveTestResponseMessageCodec.class);
    
    private long commandId;
    private Class<? extends PacketStructure> packetStructure;
    private Class<? extends MessageToMessageCodec> codec;
    
    private Cmpp10PacketType(long commandId, Class<? extends PacketStructure> packetStructure,Class<? extends MessageToMessageCodec> codec) {
        this.commandId = commandId;
        this.packetStructure = packetStructure;
        this.codec = codec;
    }
    public long getCommandId() {
        return commandId;
    }
    public PacketStructure[] getPacketStructures() {
    	return packetStructure.getEnumConstants();
    }

    public long getAllCommandId() {
        long defaultId = 0x0;
        long allCommandId = 0x0;
        for(Cmpp10PacketType packetType : Cmpp10PacketType.values()) {
            allCommandId |= packetType.commandId;
        }
        return allCommandId ^ defaultId;
    }
	@Override
	public MessageToMessageCodec getCodec() {
		
		try {
			return codec.newInstance();
		} catch (InstantiationException e) {
			return null;
		}
		catch(  IllegalAccessException e){
			return null;
		}
	}
}
