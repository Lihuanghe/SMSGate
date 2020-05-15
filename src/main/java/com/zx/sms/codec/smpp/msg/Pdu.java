package com.zx.sms.codec.smpp.msg;

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

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.smpp.PduTranscoderContext;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.Tlv;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.HexUtil;

public abstract class Pdu implements BaseMessage,Cloneable{
    
    private final String name;
    private final boolean isRequest;
    private Integer commandLength;          // we'll know the size not calculated yet if null
    private final int commandId;
    private int commandStatus;
    private Integer sequenceNumber;         // we'll know its not assigned yet if null
    // optional parameters (there aren't many, no need for a map)
    private ArrayList<Tlv> optionalParameters;
    // a reference object that a caller can attach to this pdu
    private Object referenceObject;
    
	private long timestamp = CachedMillisecondClock.INS.now();
	//消息的生命周期，单位秒, 0表示永不过期
	private long lifeTime=0;

    public Pdu(int commandId, String name, boolean isRequest) {
        this.name = name;
        this.isRequest = isRequest;
        this.commandLength = null;
        this.commandId = commandId;
        this.sequenceNumber = DefaultSequenceNumberUtil.getSequenceNo();
        this.referenceObject = null;
    }

    public void setReferenceObject(Object value) {
        this.referenceObject = value;
    }

    public Object getReferenceObject() {
        return this.referenceObject;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequest() {
        return this.isRequest;
    }

    public boolean isResponse() {
        return !this.isRequest;
    }

    public boolean hasCommandLengthCalculated() {
        return (this.commandLength != null);
    }

    public void removeCommandLength() {
        this.commandLength = null;
    }

    public void setCommandLength(int value) {
        this.commandLength = value;
    }

    public int getCommandLength() {
        if (this.commandLength == null) {
            return 0;
        } else {
            return this.commandLength;
        }
    }

    /**
     * Calculates and sets the commandLength for the PDU based on all currently
     * set values and optional parameters.
     * @return The calculated PDU command length
     */
    public int calculateAndSetCommandLength() {
        int len = SmppConstants.PDU_HEADER_LENGTH + this.calculateByteSizeOfBody() + this.calculateByteSizeOfOptionalParameters();
        this.setCommandLength(len);
        return len;
    }

    public int getCommandId() {
        return this.commandId;
    }

    public void setCommandStatus(int value) {
        this.commandStatus = value;
    }

    public int getCommandStatus() {
        return this.commandStatus;
    }

    public boolean hasSequenceNumberAssigned() {
        return (this.sequenceNumber != null);
    }

    public void removeSequenceNumber() {
        this.sequenceNumber = null;
    }

    public void setSequenceNumber(int value) {
        this.sequenceNumber = value;
    }

    public int getSequenceNumber() {
        if (this.sequenceNumber == null) {
            return 0;
        } else {
            return this.sequenceNumber;
        }
    }
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}
	
	public boolean isTerminated() {
		return lifeTime !=0 && (( timestamp + lifeTime*1000 ) - CachedMillisecondClock.INS.now() < 0L);
	}

    public int getOptionalParameterCount() {
        if (this.optionalParameters == null) {
            return 0;
        }
        return this.optionalParameters.size();
    }

    /**
     * Gets the current list of optional parameters.  If no parameters have been
     * added, this will return null.
     * @return Null if no parameters added yet, or the list of optional parameters.
     */
    public ArrayList<Tlv> getOptionalParameters() {
        return this.optionalParameters;
    }

    /**
     * Adds an optional parameter to this PDU. Does not check if the TLV has
     * already been added (allows duplicates).
     * @param tlv The TLV to add
     * @see Pdu#setOptionalParameter(com.cloudhopper.smpp.tlv.Tlv)
     */
    public void addOptionalParameter(Tlv tlv) {
        if (this.optionalParameters == null) {
            this.optionalParameters = new ArrayList<Tlv>();
        }
        this.optionalParameters.add(tlv);
    }

    /**
     * Removes an optional parameter by tag.  Will only remove the first matching
     * tag.
     * @param tag That tag to remove
     * @return Null if no TLV removed, or the TLV removed.
     */
    public Tlv removeOptionalParameter(short tag) {
        // does this parameter exist?
        int i = this.findOptionalParameter(tag);
        if (i < 0) {
            return null;
        } else {
            return this.optionalParameters.remove(i);
        }
    }

    /**
     * Sets an optional parameter by checking if the tag already exists in our
     * list of optional parameters.  If it already exists, will replace the old
     * value with the new value.
     * @param tlv The TLV to add/set
     * @return Null if no TLV was replaced, or the TLV replaced.
     */
    public Tlv setOptionalParameter(Tlv tlv) {
        // does this parameter already exist?
        int i = this.findOptionalParameter(tlv.getTag());
        if (i < 0) {
            // parameter does not yet exist, add it, not replaced
            this.addOptionalParameter(tlv);
            return null;
        } else {
            // this parameter already exists, replace it, return old
            return this.optionalParameters.set(i, tlv);
        }
    }

    /**
     * Checks if an optional parameter by tag exists.
     * @param tag The TLV to search for
     * @return True if exists, otherwise false
     */
    public boolean hasOptionalParameter(short tag) {
        return (this.findOptionalParameter(tag) >= 0);
    }

    protected int findOptionalParameter(short tag) {
        if (this.optionalParameters == null) {
            return -1;
        }
        int i = 0;
        for (Tlv tlv : this.optionalParameters) {
            if (tlv.getTag() == tag) {
                return i;
            }
            i++;
        }
        // if we get here, we didn't find the parameter by tag
        return -1;
    }

    /**
     * Gets a TLV by tag.
     * @param tag The TLV tag to search for
     * @return The first matching TLV by tag
     */
    public Tlv getOptionalParameter(short tag) {
        if (this.optionalParameters == null) {
            return null;
        }
        // try to find this parameter's index
        int i = this.findOptionalParameter(tag);
        if (i < 0) {
            return null;
        }
        return this.optionalParameters.get(i);
    }

    //
    // read & write pdu body
    //
    
    abstract protected int calculateByteSizeOfBody();

    abstract public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException;

    abstract public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException;

    abstract protected void appendBodyToString(StringBuilder buffer);

    //
    // read & write pdu optional parameters
    //

    protected int calculateByteSizeOfOptionalParameters() {
        if (this.optionalParameters == null) {
            return 0;
        }
        int optParamLength = 0;
        // otherwise, add length of each tlv
        for (Tlv tlv : this.optionalParameters) {
            optParamLength += tlv.calculateByteSize();
        }
        return optParamLength;
    }

    public void readOptionalParameters(ByteBuf buffer, PduTranscoderContext context) throws UnrecoverablePduException, RecoverablePduException {
        // if there is any data left, it's part of an optional parameter
        while (buffer.readableBytes() > 0) {
            Tlv tlv = ByteBufUtil.readTlv(buffer);
            if (tlv.getTagName() == null) {
                tlv.setTagName(context.lookupTlvTagName(tlv.getTag()));
            }
            this.addOptionalParameter(tlv);
        }
    }

    public void writeOptionalParameters(ByteBuf buffer, PduTranscoderContext context) throws UnrecoverablePduException, RecoverablePduException {
        if (this.optionalParameters == null) {
            return;
        }
        for (Tlv tlv : this.optionalParameters) {
            if (tlv.getTagName() == null) {
                tlv.setTagName(context.lookupTlvTagName(tlv.getTag()));
            }
            ByteBufUtil.writeTlv(buffer, tlv);
        }
    }

    protected void appendOptionalParameterToString(StringBuilder buffer) {
        if (this.optionalParameters == null) {
            return;
        }
        int i = 0;
        for (Tlv tlv : this.optionalParameters) {
            if (i != 0) {
                buffer.append(" (");
            } else {
                buffer.append("(");
            }
            // format 0x0000 0x0000 [00..]
            buffer.append(tlv.toString());
            buffer.append(")");
            i++;
        }
    }

    @Override
    public String toString() {
        // our guess of the optimal "toString" buffer size
        StringBuilder buffer = new StringBuilder(65 + 300 + (getOptionalParameterCount()*20));

        // append PDU header
        buffer.append("(");
        buffer.append(this.name);
        buffer.append(": 0x");
        buffer.append(HexUtil.toHexString(getCommandLength()));
        buffer.append(" 0x");
        buffer.append(HexUtil.toHexString(this.commandId));
        buffer.append(" 0x");
        buffer.append(HexUtil.toHexString(this.commandStatus));
        buffer.append(" 0x");
        buffer.append(HexUtil.toHexString(getSequenceNumber()));

        // for "responses", attempt to lookup the command status message
        if (this instanceof PduResponse) {
            PduResponse response = (PduResponse)this;
            String statusMessage = response.getResultMessage();
            if (statusMessage != null) {
                buffer.append(" result: \"");
                buffer.append(statusMessage);
                buffer.append("\"");
            } else {
                buffer.append(" result: <unmapped>");
            }
        }

        buffer.append(")");

        // append PDU body
        buffer.append(" (body: ");
        this.appendBodyToString(buffer);
        
        // append PDU optional parameters
        buffer.append(") (opts: ");
        this.appendOptionalParameterToString(buffer);
        buffer.append(")");

        return buffer.toString();
    }
    
	@Override
	public int getSequenceNo() {
		return getSequenceNumber();
	}
	
	public void setSequenceNo(int seq) {
		setSequenceNumber(seq);
	}
}