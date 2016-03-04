/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.sms.transport.gsm.commands;

import java.io.IOException;

import org.marre.sms.transport.gsm.GsmComm;
import org.marre.sms.transport.gsm.GsmException;
import org.marre.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PDU mode Send Message Set request (AT+CMGS).
 * 
 * @author Markus
 * @version $Id$
 */
public class PduSendMessageReq
{
    private static final Logger log_ = LoggerFactory.getLogger(PduSendMessageReq.class);
    
    private final byte[] smscPdu_;
    private final byte[] smsPdu_;
    
    /**
     * Send message in PDU mode using default SMSC.
     * 
     * @param smsPdu pdu for the sms data.
     */
    public PduSendMessageReq(byte[] smsPdu) {
        smscPdu_ = new byte[] {0x00};
        smsPdu_ = smsPdu;
    }
    
    /**
     * Send message in PDU mode.
     * 
     * @param smscPdu pdu for the SMSC address.
     * @param smsPdu pdu for the sms data.
     */
    public PduSendMessageReq(byte[] smscPdu, byte[] smsPdu) {
        smscPdu_ = smscPdu;
        smsPdu_ = smsPdu;
    }
    
    /**
     * Sends the command and builds a response object.
     * 
     * @param comm
     * @return
     * @throws GsmException
     * @throws IOException
     */
    public PduSendMessageRsp send(GsmComm comm) throws GsmException, IOException 
    {
        // <length> must indicate the number of octets coded in the TP layer data unit to 
        // be given (i.e. SMSC address octets are excluded)
        log_.debug("Sending AT+CMGS command");
        comm.send("AT+CMGS=" + smsPdu_.length + "\r");
        log_.debug("Read response from AT+CMGS command. Expecting a single '> ' without crlf.");
        readContinue(comm);

        // Build cmgs string.
        String cmgsPduString = StringUtil.bytesToHexString(smscPdu_) + StringUtil.bytesToHexString(smsPdu_);
        
        log_.debug("Send hexcoded PDU.");
        comm.send(cmgsPduString + '\032');
        return readResponse(comm);
    }

    /**
     * Reads the response from the GSM module.
     * 
     * The expected response is:
     * <pre>
     * AT+CMGS...
     * <empty row>
     * OK...
     * </pre>
     * 
     * @param comm
     * @return
     * @throws GsmException
     * @throws IOException
     */
    private PduSendMessageRsp readResponse(GsmComm comm) throws GsmException, IOException 
    {
        log_.debug("Expecting a single +CMGS response");

        String cmgs = comm.readLine();
        if (cmgs.startsWith("+CMGS"))
        {
            log_.debug("Expecting a empty row");
            String empty = comm.readLine();
            if (empty.trim().length() != 0) {
                throw new GsmException("AT+CMGF failed.", empty);
            }
            
            log_.debug("Expecting a OK");
            String ok = comm.readLine();
            log_.debug("Expecting a OK");
            if (! ok.startsWith("OK")) {
                throw new GsmException("AT+CMGF failed.", ok);
            }
            
            // TODO: Parse message reference
            return new PduSendMessageRsp(cmgs);
        } 
        else if (cmgs.startsWith("+CMS ERROR:"))
        {
            throw new GsmException("CMS ERROR", cmgs);
        } 
        else
        {
            throw new GsmException("Unexpected response", cmgs);
        }
    }
    
    private void readContinue(GsmComm comm)
        throws GsmException, IOException
    {
        // Eat "> " that is sent from the device
        String response = comm.readLine("> ");
        
        if (! response.equals("> ")) {
            throw new GsmException("Unexpected response", response);
        }
    }
}
