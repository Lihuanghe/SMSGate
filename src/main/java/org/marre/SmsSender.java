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
package org.marre;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.marre.sms.*;
import org.marre.sms.MwiType;
import org.marre.sms.SmsMwiMessage;
import org.marre.sms.transport.SmsTransport;
import org.marre.sms.transport.SmsTransportManager;
import org.marre.wap.nokia.NokiaOtaBrowserSettings;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;

/**
 * High level API to the smsj library.
 * 
 * If you only need to send some basic SMS messages than you only have to use
 * this API. Ex:
 * 
 * <pre>
 * try
 * {
 *     // Send SMS with clickatell
 *     SmsSender smsSender = SmsSender.getClickatellSender(&quot;username&quot;, &quot;password&quot;, &quot;apiid&quot;);
 *     String msg = &quot;A sample SMS.&quot;;
 *     // International number to reciever without leading &quot;+&quot;
 *     String reciever = &quot;464545425463&quot;;
 *     // Number of sender (not supported on all transports)
 *     String sender = &quot;46534534535&quot;;
 *     // Connect
 *     smsSender.connect();
 *     // Send message
 *     smsSender.send(&quot;A sample SMS.&quot;, reciever, sender);
 *     // Disconnect
 *     smsSender.disconnect();
 * }
 * catch (IOException ex)
 * {
 *     ex.printStackTrace();
 * }
 * catch (SmsException ex)
 * {
 *     ex.printStackTrace();
 * }
 * </pre>
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class SmsSender
{
    /**
     * The sms transport that is used to send the messages.
     */
    protected SmsTransport transport_;

    /**
     * Creates a SmsSender object by using the given transport and properties.
     * <p>
     * You can also use getClickatellSender(...) to create a SmsSender object
     * 
     * @param transport
     *            Classname of the SmsTransport class
     * @param props
     *            Properties to initialize the transport with
     * @throws SmsException
     */
    public SmsSender(String transport, Properties props) throws SmsException
    {
        transport_ = SmsTransportManager.getTransport(transport, props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with the Clickatell service.
     * 
     * @param username
     *            Clickatell username
     * @param password
     *            Clickatell password
     * @param apiid
     *            Clickatell api-id
     *            
     * @return A SmsSender object that uses the ClickatellTransport to send
     *         messages
     *         
     * @throws SmsException
     */
    public static SmsSender getClickatellSender(String username, String password, String apiid)
            throws SmsException
    {
        Properties props = new Properties();

        props.setProperty("smsj.clickatell.username", username);
        props.setProperty("smsj.clickatell.password", password);
        props.setProperty("smsj.clickatell.apiid", apiid);
        props.setProperty("smsj.clickatell.protocol", "https");

        return new SmsSender("org.marre.sms.transport.clickatell.ClickatellTransport", props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with the Clickatell service.
     * 
     * @param propsFilename
     *            Filename of a properties file containing properties for the
     *            clickatell transport.
     *            
     * @return A SmsSender object that uses the ClickatellTransport to send
     *         messages
     *         
     * @throws SmsException
     * @throws IOException 
     */
    public static SmsSender getClickatellSender(String propsFilename) throws SmsException, IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(propsFilename));
        return new SmsSender("org.marre.sms.transport.clickatell.ClickatellTransport", props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with a GSM phone attached to the serial port on your computer.
     * 
     * @param portName
     *            Serial port where your phone is located. Ex "COM1:"
     *            
     * @return A SmsSender object that uses the GsmTranport to send messages
     * 
     * @throws SmsException
     */
    public static SmsSender getGsmSender(String portName) throws SmsException
    {
        Properties props = new Properties();
        props.setProperty("sms.gsm.serialport", portName);
        return new SmsSender("org.marre.sms.transport.gsm.GsmTransport", props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with a UCP SMSC.
     * 
     * @param address
     *            A string with the ip address or host name of the SMSC
     * @param port
     *            An integer with the ip port on which the SMSC listens
     * @return A SmsSender object that uses the UcpTranport to send messages
     * @throws SmsException
     */
    public static SmsSender getUcpSender(String address, int port) throws SmsException
    {
        //Liquidterm: strict input checking is done in the UcpTransport class
        Properties props = new Properties();
        props.setProperty("smsj.ucp.ip.host", address);
        props.setProperty("smsj.ucp.ip.port", Integer.toString(port));
        return new SmsSender("org.marre.sms.transport.ucp.UcpTransport", props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with a UCP SMSC.
     * 
     * @param address
     *            A string with the ip address or host name of the SMSC
     * @param port
     *            An integer with the ip port on which the SMSC listens
     * @param ucp60Uid
     *            A string containing the UCP60 userid
     * @param ucp60Pwd
     *            A string containing the UCP60 password
     *            
     * @return A SmsSender object that uses the UcpTranport to send messages
     * 
     * @throws SmsException
     */
    public static SmsSender getUcpSender(String address, int port, String ucp60Uid, String ucp60Pwd)
            throws SmsException
    {
        //Liquidterm: strict input checking is done in the UcpTransport class
        Properties props = new Properties();
        props.setProperty("smsj.ucp.ip.host", address);
        props.setProperty("smsj.ucp.ip.port", Integer.toString(port));
        props.setProperty("smsj.ucp.ucp60.uid", ucp60Uid);
        props.setProperty("smsj.ucp.ucp60.password", ucp60Pwd);
        return new SmsSender("org.marre.sms.transport.ucp.UcpTransport", props);
    }

    /**
     * Convenience method to create a SmsSender object that knows how to send
     * messages with a UCP SMSC.
     * 
     * @param propsFilename
     *            A string containt a filename with the serialized Properties
     *            object for the transport
     *            
     * @return A SmsSender object that uses the UcpTranport to send messages
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public static SmsSender getUcpSender(String propsFilename) throws SmsException, IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(propsFilename));
        return new SmsSender("org.marre.sms.transport.ucp.UcpTransport", props);
    }

    /**
     * Sends an ordinary SMS to the given recipient.
     * 
     * There is no limit on the number of concatenated SMS that this message will
     * use. It will send the message with the GSM charset (Max 160 chars/SMS).
     * 
     * @param text Message to send
     * @param dest Destination number (international format without leading +).
     * @param sender Sender number (international format without leading +). Can also be an alphanumerical string like
     *               "SMSJ". This is property is not supported by all transports.
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public String sendTextSms(String text, String dest, String sender) throws SmsException, IOException
    {
        SmsTextMessage textMessage = new SmsTextMessage(text, SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN);
        return sendSms(textMessage, dest, sender);
    }

    /**
     * Sends an ordinary SMS to the given recipient.
     * 
     * There is no limit on the number of concatenated SMS that this message will
     * use. It will send the message with the GSM charset (Max 160 chars/SMS).
     * 
     * @param text Message to send
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public String sendTextSms(String text, String dest) throws SmsException, IOException
    {
        SmsTextMessage textMessage = new SmsTextMessage(text, SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN);
        return sendSms(textMessage, dest, null);
    }
    
    /**
     * Sends an ordinary SMS to the given recipient.
     * 
     * No limit on the number of concatenated SMS that this message will
     * use. Will send the message with the UCS2 charset (MAX 70 chars/SMS).
     * 
     * @param text Message to send
     * @param dest Destination number (international format without leading +).
     * @param sender Sender number (international format without leading +). Can also be an alphanumerical string like
     *               "SMSJ". This is property is not supported by all transports.
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public String sendUnicodeTextSms(String text, String dest, String sender) throws SmsException, IOException
    {
        SmsTextMessage textMessage = new SmsTextMessage(text, SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN);
        return sendSms(textMessage, dest, sender);
    }

    /**
     * Sends an ordinary SMS to the given recipient.
     * 
     * No limit on the number of concatenated SMS that this message will
     * use. Will send the message with the UCS2 charset (MAX 70 chars/SMS).
     * 
     * @param text Message to send
     * @param dest Destination number (international format without leading +).
     * 
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public String sendUnicodeTextSms(String text, String dest) throws SmsException, IOException
    {
        SmsTextMessage textMessage = new SmsTextMessage(text, SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN);
        return sendSms(textMessage, dest, null);
    }
    
    /**
     * 
     * Sends an OTA Bookmark (Nokia specification) to the given recipient
     * 
     * @param title String with the title of the bookmark
     * @param url String with the url referenced by the bookmark
     * @param dest Destination number (international format without leading +).
     * 
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     *
     * @throws SmsException
     * @throws IOException 
     */
    public String sendNokiaBookmark(String title, String url, String dest) throws SmsException, IOException
    {
        NokiaOtaBrowserSettings browserSettings = new NokiaOtaBrowserSettings();
        browserSettings.addBookmark(title, url);
           
        SmsWapPushMessage wapPushMessage = new SmsWapPushMessage(browserSettings, "application/x-wap-prov.browser-bookmarks");
        wapPushMessage.setPorts(new SmsPort(49154), SmsPort.OTA_SETTINGS_BROWSER);
 
        return sendSms(wapPushMessage, dest, null);
    }
    
    /**
     * Sends a Wap Push SI containing to the given recipient.
     *
     * @param text String with the description of the service
     * @param url String with the url referenced by the SI
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     *
     * @throws SmsException
     * @throws IOException 
     */
    public String sendWapSiPushMsg(String url, String text, String dest) throws SmsException, IOException
    {
        WapSIPush siPush = new WapSIPush(url, text);
        
        SmsWapPushMessage wapPushMessage = new SmsWapPushMessage(siPush);
//      wapPushMessage.setXWapApplicationId("x-wap-application:*");
        
        return sendSms(wapPushMessage, dest, null);
    }

    /**
     * Sends a Wap Push SL containing to the given recipient.
     *
     * @param url String with the url referenced by the SL
     * @param dest Destination number in international format
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     *
     * @throws SmsException
     * @throws IOException 
     */
    public String sendWapSlPushMsg(String url, String dest) throws SmsException, IOException
    {
        WapSLPush slPush = new WapSLPush(url);
        
        SmsWapPushMessage wapPushMessage = new SmsWapPushMessage(slPush);
//      wapPushMessage.setXWapApplicationId("x-wap-application:*");
        
        return sendSms(wapPushMessage, dest, null);
    }
    
  
    /**
     * Sends a simple MMS Notification.
     * 
     * @param contentLocation Where the mms pdu can be downloaded from.
     * @param size The size of the message.
     * @param subject The subject of the message.
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     *
     * @throws SmsException
     * @throws IOException 
     */
    public String sendMmsNotification(String contentLocation, long size, String subject, String dest) throws SmsException, IOException
    {
        SmsMmsNotificationMessage mmsNotification = new SmsMmsNotificationMessage(contentLocation, size);
        mmsNotification.setSubject(subject);
        
        return sendSms(mmsNotification, dest, null);
    }
 
    /**
     * Sends a voice message waiting message indication.
     * 
     * @param count Number of messages waiting. Set to 0 to clear the message waiting flag in the phone.
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException
     */
    public String sendMsgWaitingVoice(int count, String dest) throws SmsException, IOException
    {
        return sendMsgWaiting(MwiType.VOICE, count, dest);
    }
    
    /**
     * Sends a fax message waiting message indication.
     * 
     * @param count Number of messages waiting. Set to 0 to clear the message waiting flag in the phone.
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException
     */
    public String sendMsgWaitingFax(int count, String dest) throws SmsException, IOException
    {
        return sendMsgWaiting(MwiType.FAX, count, dest);
    }
    
    /**
     * Sends a email message waiting message indication.
     * 
     * @param count Number of messages waiting. Set to 0 to clear the message waiting flag in the phone.
     * @param dest Destination number (international format without leading +).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException
     */
    public String sendMsgWaitingEmail(int count, String dest) throws SmsException, IOException
    {
        return sendMsgWaiting(MwiType.EMAIL, count, dest);
    }
    
    private String sendMsgWaiting(MwiType type, int count, String dest) throws SmsException, IOException
    {
        SmsMwiMessage mwi = new SmsMwiMessage();
        mwi.addMsgWaiting(type, count);
        
        return sendSms(mwi, dest, null);
    }
    
    /**
     * Sends a SmsMessage.
     * 
     * @param msg The message to send.
     * @param dest
     *            Destination number (international format without leading +)
     *            Ex. 44546754235
     * @param sender
     *            Destination number (international format without leading +).
     *            Can also be an alphanumerical string. Ex "SMSJ". (not
     *            supported by all transports).
     *            
     * @return Returns a local message id for the sent message. It is possible that the message id is null.
     * 
     * @throws SmsException
     * @throws IOException
     */
    public String sendSms(SmsMessage msg, String dest, String sender) throws SmsException, IOException
    {
        SmsAddress destAddress = new SmsAddress(dest);
        SmsAddress senderAddress = null;

        if (sender != null)
        {
            senderAddress = new SmsAddress(sender);
        }
        
        return transport_.send(msg, destAddress, senderAddress);
    }
    
    /**
     * Connect to the server.
     * 
     * Must be called before any send method.
     * 
     * @throws SmsException
     * @throws IOException
     */
    public void connect() throws SmsException, IOException
    {
        transport_.connect();
    }
    
    /**
     * Call this when you are done with the SmsSender object.
     * 
     * It will free any resources that we have used.
     * 
     * @throws SmsException
     * @throws IOException 
     */
    public void disconnect() throws SmsException, IOException
    {
        if (transport_ != null)
        {
            transport_.disconnect();
            transport_ = null;
        }
    }

    /**
     * Probably never called, but good to have if the caller forget to disconnect().
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable
    {
        // Disconnect transport if the caller forget to close us
        try
        {
            disconnect();
        }
        catch (Exception ex)
        {
            // Nothing to do here. The object is gone...
        }
        
        super.finalize();
    }
}
