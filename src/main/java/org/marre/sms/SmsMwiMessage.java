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
package org.marre.sms;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a "Message Waiting" sms.
 * 
 * As described in TS 23.040-650 section 9.2.3.24.2 "Special SMS Message Indication".
 * 
 * On a Sony-Ericsson T610 these messages can be used to display different types of icons in the
 * notification bar.
 *  
 * @author Markus Eriksson
 * @version $Id$
 */
public class SmsMwiMessage extends SmsTextMessage
{
    /**
     * Represents one message waiting udh.
     */
    private static class MsgWaiting
    {
        private final MwiType type_;
        private final int count_;
        private final MwiProfile profile_;
        private final boolean storeMessage_;
        
        private MsgWaiting(MwiType type, int count, MwiProfile profile, boolean storeMessage)
        {
            this.type_ = type;
            this.count_ = count;
            this.profile_ = profile;
            this.storeMessage_ = storeMessage;
        }

        int getCount()
        {
            return count_;
        }

        MwiType getType()
        {
            return type_;
        }

        public MwiProfile getProfile() {
            return profile_;
        }

        public boolean storeMessage() {
            return storeMessage_;
        }
    }
    
    /**
     * List of MsgWaiting "objects".
     */
    protected final LinkedList<MsgWaiting> messages_ = new LinkedList<MsgWaiting>();
    
    /**
     * Creates an empty message.
     */
    public SmsMwiMessage()
    {
        this("", SmsAlphabet.LATIN1);
    }
    
    /**
     * Creates an message with the supplied text (GSM charset).
     * 
     * @param text Description of this message.
     */
    public SmsMwiMessage(String text)
    {
        this(text, SmsAlphabet.ASCII);
    }
    
    /**
     * Creates an message with the supplied text and alphabet.
     * 
     * @param text Description of this message
     * @param alphabet Alphabet to use. Valid values are SmsDcs.ALPHABET_*.
     */
    public SmsMwiMessage(String text, SmsAlphabet alphabet)
    {
        super(text, SmsDcs.getGeneralDataCodingDcs(alphabet, SmsMsgClass.CLASS_UNKNOWN));
    }
    
    /**
     * Adds a message waiting.
     * 
     * @param type Type of message that is waiting. Can be any of TYPE_*.
     * @param count Number of messages waiting for retrieval.
     */
    public void addMsgWaiting(MwiType type, int count)
    {
        addMsgWaiting(type, count, MwiProfile.ID_1, false);
    }
    
    /**
     * Adds a message waiting.
     *
     * @param type Type of message that is waiting. Can be any of TYPE_*.
     * @param count Number of messages waiting for retrieval.
     * @param profile
     * @param storeMessage
     */
    public void addMsgWaiting(MwiType type, int count, MwiProfile profile, boolean storeMessage)
    {
        // count can be at most 255.
        if (count > 255)
        {
            count = 255;
        }
        
        messages_.add(new MsgWaiting(type, count, profile, storeMessage));
    }

    /**
     * Creates a "Message waiting" UDH element using UDH_IEI_SPECIAL_MESSAGE.
     * <p>
     * If more than one type of message is required to be indicated within
     * one SMS message, then multiple "Message waiting" UDH elements must
     * be used.
     * <p>
     * <b>Special handling in concatenated messages:</b><br>
     * <i>
     * "In the case where this IEI is to be used in a concatenated SM then the
     * IEI, its associated IEI length and IEI data shall be contained in the
     * first segment of the concatenated SM. The IEI, its associated IEI length
     * and IEI data should also be contained in every subsequent segment of the
     * concatenated SM although this is not mandatory. However, in the case
     * where these elements are not contained in every subsequent segment of
     * the concatenated SM and where an out of sequence segment delivery
     * occurs or where the first segment is not delivered then processing
     * difficulties may arise at the receiving entity which may result in
     * the concatenated SM being totally or partially discarded."
     * </i>
     *
     * @param msgWaiting The MsgWaiting to convert
     * @return A SmsUdhElement
     */
    protected SmsUdhElement getMessageWaitingUdh(MsgWaiting msgWaiting)
    {
        byte[] udh = new byte[2];

        // Bit 0 and 1 indicate the basic indication type.
        // Bit 4, 3 and 2 indicate the extended message indication type.
        switch (msgWaiting.getType())
        {
        case VOICE: udh[0] = 0x00; break;
        case FAX:   udh[0] = 0x01; break;
        case EMAIL: udh[0] = 0x02; break;
        case VIDEO: udh[0] = 0x07; break;
        }
        
        // Bit 6 and 5 indicates the profile ID of the Multiple Subscriber Profile.
        switch (msgWaiting.getProfile())
        {
        case ID_1: udh[0] |= 0x00; break;
        case ID_2: udh[0] |= 0x20; break;
        case ID_3: udh[0] |= 0x40; break;
        case ID_4: udh[0] |= 0x60; break;
        }
        
        // Bit 7 indicates if the message shall be stored.
        if (msgWaiting.storeMessage())
        {
            udh[0] |= (byte) (0x80);
        }

        // Octet 2 contains the number of messages waiting
        udh[1] = (byte) (msgWaiting.getCount() & 0xff);

        return new SmsUdhElement(SmsUdhIei.SPECIAL_MESSAGE, udh);
    }
    
    /**
     * Builds a udh element for this message.
     * 
     * @see org.marre.sms.SmsTextMessage#getUdhElements()
     */
    public SmsUdhElement[] getUdhElements()
    {
        SmsUdhElement udhElements[] = null;
        int msgCount = messages_.size();
        
        if (msgCount > 0)
        {
            udhElements = new SmsUdhElement[messages_.size()];
            int i = 0;
            
            for(Iterator j = messages_.iterator(); j.hasNext(); i++)
            {
                MsgWaiting msgWaiting = (MsgWaiting) j.next();
                udhElements[i] = getMessageWaitingUdh(msgWaiting);
            }
            
        }
        
        return udhElements;
    }
}
