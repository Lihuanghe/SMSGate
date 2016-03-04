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
 *   Boris von Loesch
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
package org.marre.sms.transport.gsm;

import java.io.IOException;
import java.util.Properties;

import org.marre.sms.*;
import org.marre.sms.transport.SmsTransport;
import org.marre.sms.transport.gsm.commands.MessageFormatSetReq;
import org.marre.sms.transport.gsm.commands.PduSendMessageReq;
import org.marre.sms.transport.gsm.commands.PduSendMessageRsp;
import org.marre.sms.transport.gsm.commands.PingReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SmsTransport that sends the SMS from an GSM phone that is attached
 * to the serial port.
 * <p>
 * This transport supports the following parameters:
 * <br>
 * <pre>
 * <b>sms.gsm.appname</b> - Application name to use when registering the comport
 * <b>sms.gsm.serialport</b> - Serial port where the GSM phone is located. Ex: "COM1"
 * <b>sms.gsm.bitrate</b> - Bits per second
 * <b>sms.gsm.bit</b> - Databits
 * <b>sms.gsm.parity</b> - Parity (NONE, EVEN, ODD, MARK, SPACE)
 * <b>sms.gsm.stopbits</b> - Stopbits (1, 1.5, 2)
 * <b>sms.gsm.echo</b> - Is the device echoing the input?
 * <b>sms.gsm.flowcontrol</b> - FlowControl (XONXOFF, RTSCTS, NONE)
 * <b>sms.gsm.timeout</b> - Timeout to apply when communicating with the device
 * <b>
 * </pre>
 * <p>
 * <i>This transport cannot set the sending "address" to anything else
 * than the sending phone's phonenumber.</i>
 *
 * @author Markus Eriksson, Boris von Loesch
 * @version $Id$
 */
public class GsmTransport implements SmsTransport
{
    private static final Logger log_ = LoggerFactory.getLogger(SerialComm.class);
    
    private static final String DEFAULT_SERIAL_PORT_APP_NAME = "SMSJ";
    
    private static final int RESPONSE_OK = 1;
    private static final int RESPONSE_ERROR = 2;
    private static final int RESPONSE_EMPTY_LINE = 4;
    private static final int RESPONSE_TEXT = 8;
    private static final int RESPONSE_CONTINUE = 16;
    
    private SerialComm serialComm_ = null;
    
    /**
     * Creates a GsmTransport.
     * 
     */
    public GsmTransport()
    {
        // Empty
    }

    /**
     * Initializes this transport.
     * 
     * @param props 
     */
    public void init(Properties props)
    {
        String appName = props.getProperty("sms.gsm.appname", DEFAULT_SERIAL_PORT_APP_NAME); 
        String portName = props.getProperty("sms.gsm.serialport", "COM1");

        serialComm_ = new SerialComm(appName, portName);

        serialComm_.setBitRate(props.getProperty("sms.gsm.bitrate", "19200"));
        serialComm_.setDataBits(props.getProperty("sms.gsm.bit", "8"));
        serialComm_.setStopBits(props.getProperty("sms.gsm.stopbits", "8"));
        serialComm_.setParity(props.getProperty("sms.gsm.parity", "NONE"));
        serialComm_.setFlowControl(props.getProperty("sms.gsm.flowcontrol", "NONE"));
        serialComm_.setTimeout(props.getProperty("sms.gsm.timeout", "0"));
        serialComm_.setEcho(props.getProperty("sms.gsm.echo", "1").equals("1"));
    }
    
    /**
     * Initializes the communication with the GSM phone.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public void connect() 
        throws SmsException, IOException
    {
        try
        {
            log_.debug("Open serial port.");
            serialComm_.open();
            log_.debug("Serial port opened.");
                        
            // AT-ping
            PingReq pingReq = new PingReq();
            pingReq.send(serialComm_);
            pingReq.send(serialComm_);
            pingReq.send(serialComm_);
            
            // Init
            MessageFormatSetReq messageFormatSetReq = new MessageFormatSetReq(MessageFormatSetReq.MODE_PDU);
            messageFormatSetReq.send(serialComm_);
        }
        catch (GsmException e)
        {
            log_.debug("Close serial port.");
            serialComm_.close();
            log_.debug("Serial port closed.");
            
            throw new SmsException("Connect failed: " + e.getMessage() + " Last response:" + e.getResponse(), e);
        }
    }

    /**
     * Sends the SMS message to the given recipients.
     * 
     * Note: The sending address is ignored for the GSM transport.
     *
     * @param msg The message to send
     * @param dest The reciever
     * @param sender The sending address, ignored
     * @return Returns a local message id
     * @throws SmsException Thrown if we fail to send the SMS
     * @throws IOException 
     */
    public String send(SmsMessage msg, SmsAddress dest, SmsAddress sender) throws SmsException, IOException
    {
        if (dest.getTypeOfNumber() == SmsTon.ALPHANUMERIC)
        {
            throw new SmsException("Cannot send SMS to an ALPHANUMERIC address");
        }

        try
        {
            SmsPdu[] msgPdu = msg.getPdus();
            for (SmsPdu aMsgPdu : msgPdu) {
                byte[] data = GsmEncoder.encodePdu(aMsgPdu, dest, sender);
                PduSendMessageReq sendMessageReq = new PduSendMessageReq(data);
                PduSendMessageRsp sendMessageRsp = sendMessageReq.send(serialComm_);
            }
        }
        catch (GsmException e)
        {
            throw new SmsException("Send failed: " + e.getMessage() + " Last response:" + e.getResponse(), e);
        }
        
        // TODO: Return a real message id
        return null;
    }

    /**
     * Sends a "AT" command to keep the connection alive.
     *
     * @throws IOException 
     */
    public void ping()
        throws IOException
    {
        try
        {
            PingReq pingReq = new PingReq();
            pingReq.send(serialComm_);
        }
        catch (GsmException e)
        {
            String msg = "Ping failed: " + e.getMessage() + 
                         " Last response: " + e.getResponse();
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Closes the serial connection to the phone.
     * 
     * @throws IOException 
     */
    public void disconnect()
    {
        serialComm_.close();
    }
}
