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
package org.marre.sms.transport;

import java.io.IOException;
import java.util.Properties;

import org.marre.sms.SmsAddress;
import org.marre.sms.SmsException;
import org.marre.sms.SmsMessage;

/**
 * Interface for an SMS transport.
 * 
 * This interface is used to transfer an smsj message to the sms server.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public interface SmsTransport
{
    /**
     * Initializes the transport.
     * 
     * Please see each transport for information on what properties that are available.
     *
     * @param props Properties used to configure this transport.
     * @throws SmsException If there was a problem with the configuration. 
     */
    void init(Properties props) throws SmsException;

    /**
     * Connects to the SMS server.
     * 
     * @throws SmsException Indicates a sms related problem.
     * @throws IOException Inidicates a failure to communicate with the SMS server.
     */
    void connect() throws SmsException, IOException;

    /**
     * Pings the SMS sender.
     *
     * Should be used to keep the connection alive.
     * 
     * @throws SmsException Indicates a sms related problem.
     * @throws IOException Inidicates a failure to communicate with the SMS server.
     */
    void ping() throws SmsException, IOException;
    
    /**
     * Sends an SmsMessage to the given destination.
     * 
     * This method returns a local identifier for the message. This messageid can then be used to query the 
     * status, cancel or replace the sent SMS. The format of the identifier is specific to the SmsTransport 
     * implementation. The SmsTransport implementation may decide how long the message id is valid. 
     * 
     * Depending on the implementation this method is blocking or non-blocking.
     * 
     * It is possible that the returned identifier is null. This indicates that the actual transport
     * doesn't handle message ids.
     * 
     * @param msg The Message to send
     * @param dest Destination address
     * @param sender Sender address
     * @return a local identifier for the message.
     * @throws SmsException Indicates a sms related problem.
     * @throws IOException Inidicates a failure to communicate with the SMS server.
     */
    String send(SmsMessage msg, SmsAddress dest, SmsAddress sender) throws SmsException, IOException;

    /**
     * Disconnects from the SMS server.
     * 
     * @throws SmsException Indicates a sms related problem.
     * @throws IOException Inidicates a failure to communicate with the SMS server.
     */
    void disconnect() throws SmsException, IOException;
}
