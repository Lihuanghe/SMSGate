/**
 * 
 */
package com.zx.sms.codec.sgip12.packet;

import com.zx.sms.codec.cmpp.packet.DataType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum SgipDataType implements DataType {
	
	UNSIGNEDINT(0x1), OCTERSTRING(0x2);
	private int commandId;
	/**
	 * 
	 * @param commandId
	 */
    private SgipDataType(int commandId) {
        this.commandId = commandId;
    }
    public int getCommandId() {
        return commandId;
    }
    
    public int getAllCommandId() {
        int defaultId = 0x0;
        int allCommandId = 0x0;
        for(SgipDataType dataType : SgipDataType.values()) {
            allCommandId |= dataType.commandId;
        }
        return allCommandId ^ defaultId;
    }

}
