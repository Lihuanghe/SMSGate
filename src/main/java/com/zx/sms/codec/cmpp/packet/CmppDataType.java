/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;


/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppDataType implements DataType {

    UNSIGNEDINT(0x1), OCTERSTRING(0x2);
    private int commandId;
    private CmppDataType(int commandId) {
        this.commandId = commandId;
    }
    public int getCommandId() {
        return commandId;
    }
    
    public int getAllCommandId() {
        int defaultId = 0x0;
        int allCommandId = 0x0;
        for(CmppDataType dataType : CmppDataType.values()) {
            allCommandId |= dataType.commandId;
        }
        return allCommandId ^ defaultId;
    }
}
