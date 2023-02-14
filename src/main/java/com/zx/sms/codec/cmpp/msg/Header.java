package com.zx.sms.codec.cmpp.msg;

import java.io.Serializable;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
public interface Header extends Serializable {
     void setHeadLength(long length);
     long getHeadLength();
     void setPacketLength(long length);
     long getPacketLength();
     void setBodyLength(long length);
     long getBodyLength();
     void setCommandId(int commandId);
     int getCommandId();
     void setSequenceId(int transitionId);
     int getSequenceId();
     long getNodeId();
     void setNodeId(long nodeId);
}
