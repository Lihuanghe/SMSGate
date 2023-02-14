package com.zx.sms.codec.cmpp.msg;

import org.apache.commons.codec.binary.Hex;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppConnectResponseMessage extends DefaultMessage{
    private static final long serialVersionUID = -5010314567064353091L;
    private long status = 3;
    private byte[] authenticatorISMG = GlobalConstance.emptyBytes;
    private short version = 0x30;
    
    public CmppConnectResponseMessage(Header header ) {
    	super(CmppPacketType.CMPPCONNECTRESPONSE,header);
	}
    public CmppConnectResponseMessage(int sequenceId) {
    	super(CmppPacketType.CMPPCONNECTRESPONSE,sequenceId);
	}
	/**
     * @return the status
     */
    public long getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(long status) {
        this.status = status;
    }

    /**
     * @return the authenticatorISMG
     */
    public byte[] getAuthenticatorISMG() {
        return authenticatorISMG;
    }

    /**
     * @param authenticatorISMG the authenticatorISMG to set
     */
    public void setAuthenticatorISMG(byte[] authenticatorISMG) {
        this.authenticatorISMG = authenticatorISMG;
    }

    /**
     * @return the version
     */
    public short getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(short version) {
        this.version = version;
    }

	@Override
	public String toString() {
		return String.format("CmppConnectResponseMessage [version=%s, status=%s,authenticatorISMG = %s, sequenceId=%s]", version, status,Hex.encodeHexString(authenticatorISMG), getHeader().getSequenceId());
	}
}
