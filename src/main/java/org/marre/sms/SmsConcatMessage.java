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

import java.util.Random;

/**
 * Baseclass for messages that needs to be concatenated.
 * <p>- Only usable for messages that uses the same UDH fields for all message
 * parts. <br>- This class could be better written. There are several parts
 * that are copy- pasted. <br>- The septet coding could be a bit optimized.
 * <br>
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public abstract class SmsConcatMessage implements SmsMessage
{
    private static final Random rnd_ = new Random();

    /**
     * Creates an empty SmsConcatMessage.
     */
    protected SmsConcatMessage()
    {
        // Empty
    }

    /**
     * Returns the whole UD
     * 
     * @return the UD
     */
    public abstract SmsUserData getUserData();
    
    /**
     * Returns the udh elements
     * <p>
     * The returned UDH is the same as specified when the message was created.
     * No concat headers are added.
     * 
     * @return the UDH as SmsUdhElements
     */
    public abstract SmsUdhElement[] getUdhElements();

    private SmsPdu[] createOctalPdus(SmsUdhElement[] udhElements, SmsUserData ud, int maxBytes)
    {
        int nMaxChars;
        int nMaxConcatChars;
        SmsPdu[] smsPdus = null;

        // 8-bit concat header is 6 bytes...
        nMaxConcatChars = maxBytes - 6;
        nMaxChars = maxBytes;

        if (ud.getLength() <= nMaxChars)
        {
            smsPdus = new SmsPdu[]{new SmsPdu(udhElements, ud)};
        }
        else
        {
            int refno = rnd_.nextInt(256);

            // Calculate number of SMS needed
            int nSms = ud.getLength() / nMaxConcatChars;
            if ((ud.getLength() % nMaxConcatChars) > 0)
            {
                nSms += 1;
            }
            smsPdus = new SmsPdu[nSms];

            // Calculate number of UDHI
            SmsUdhElement[] pduUdhElements = null;
            if (udhElements == null)
            {
                pduUdhElements = new SmsUdhElement[1];
            }
            else
            {
                pduUdhElements = new SmsUdhElement[udhElements.length + 1];

                // Copy the UDH headers
                System.arraycopy(udhElements, 0, pduUdhElements, 1, udhElements.length);
            }

            // Create pdus
            for (int i = 0; i < nSms; i++)
            {
                byte[] pduUd;
                int udBytes;
                int udLength;
                int udOffset;

                // Create concat header
                pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms, i + 1);

                // Create
                // Must concatenate messages
                // Calc pdu length
                udOffset = nMaxConcatChars * i;
                udBytes = ud.getLength() - udOffset;
                if (udBytes > nMaxConcatChars)
                {
                    udBytes = nMaxConcatChars;
                }
                udLength = udBytes;

                pduUd = new byte[udBytes];
                System.arraycopy(ud.getData(), udOffset, pduUd, 0, udBytes);
                smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udLength, ud.getDcs());
            }
        }
        return smsPdus;
    }

    private SmsPdu[] createUnicodePdus(SmsUdhElement[] udhElements, SmsUserData ud, int maxBytes)
    {
        int nMaxConcatChars;
        SmsPdu[] smsPdus = null;

        // 8-bit concat header is 6 bytes...
        nMaxConcatChars = (maxBytes - 6) / 2;

        if (ud.getLength() <= maxBytes)
        {
            smsPdus = new SmsPdu[]{new SmsPdu(udhElements, ud)};
        }
        else
        {
            int refno = rnd_.nextInt(256);

            // Calculate number of SMS needed
            int nSms = (ud.getLength() / 2) / nMaxConcatChars;
            if (((ud.getLength() / 2) % nMaxConcatChars) > 0)
            {
                nSms += 1;
            }
            smsPdus = new SmsPdu[nSms];

            // Calculate number of UDHI
            SmsUdhElement[] pduUdhElements = null;
            if (udhElements == null)
            {
                pduUdhElements = new SmsUdhElement[1];
            }
            else
            {
                pduUdhElements = new SmsUdhElement[udhElements.length + 1];

                // Copy the UDH headers
                System.arraycopy(udhElements, 0, pduUdhElements, 1, udhElements.length);
            }

            // Create pdus
            for (int i = 0; i < nSms; i++)
            {
                byte[] pduUd;
                int udBytes;
                int udLength;
                int udOffset;

                // Create concat header
                pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms, i + 1);

                // Create
                // Must concatenate messages
                // Calc pdu length
                udOffset = nMaxConcatChars * i;
                udLength = (ud.getLength() / 2) - udOffset;
                if (udLength > nMaxConcatChars)
                {
                    udLength = nMaxConcatChars;
                }
                udBytes = udLength * 2;

                pduUd = new byte[udBytes];
                System.arraycopy(ud.getData(), udOffset * 2, pduUd, 0, udBytes);
                smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udBytes, ud.getDcs());
            }
        }
        return smsPdus;
    }

    private SmsPdu[] createSeptetPdus(SmsUdhElement[] udhElements, SmsUserData ud, int maxBytes)
    {
        int nMaxChars;
        int nMaxConcatChars;
        SmsPdu[] smsPdus = null;

        // 8-bit concat header is 6 bytes...
        nMaxConcatChars = ((maxBytes - 6) * 8) / 7;
        nMaxChars = (maxBytes * 8) / 7;

        if (ud.getLength() <= nMaxChars)
        {
            smsPdus = new SmsPdu[]{new SmsPdu(udhElements, ud)};
        }
        else
        {
            int refno = rnd_.nextInt(256);

            // Calculate number of SMS needed
            int nSms = ud.getLength() / nMaxConcatChars;
            if ((ud.getLength() % nMaxConcatChars) > 0)
            {
                nSms += 1;
            }
            smsPdus = new SmsPdu[nSms];

            // Calculate number of UDHI
            SmsUdhElement[] pduUdhElements = null;
            if (udhElements == null)
            {
                pduUdhElements = new SmsUdhElement[1];
            }
            else
            {
                pduUdhElements = new SmsUdhElement[udhElements.length + 1];

                // Copy the UDH headers
                System.arraycopy(udhElements, 0, pduUdhElements, 1, udhElements.length);
            }

            // Convert septets into a string...
            String msg = SmsPduUtil.readSeptets(ud.getData(), ud.getLength());

            // Create pdus
            for (int i = 0; i < nSms; i++)
            {
                byte[] pduUd;
                int udOffset;
                int udLength;

                // Create concat header
                pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms, i + 1);

                // Create
                // Must concatenate messages
                // Calc pdu length
                udOffset = nMaxConcatChars * i;
                udLength = ud.getLength() - udOffset;
                if (udLength > nMaxConcatChars)
                {
                    udLength = nMaxConcatChars;
                }

                pduUd = SmsPduUtil.getSeptets(msg.substring(udOffset, udOffset + udLength));
                smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udLength, ud.getDcs());
            }
        }
        return smsPdus;
    }

    /**
     * Converts this message into SmsPdu:s
     * <p>
     * If the message is too long to fit in one SmsPdu the message is divided
     * into many SmsPdu:s with a 8-bit concat pdu UDH element.
     * 
     * @return Returns the message as SmsPdu:s
     */
    public SmsPdu[] getPdus()
    {
        SmsPdu[] smsPdus;
        SmsUserData ud = getUserData();
        SmsUdhElement[] udhElements = getUdhElements();        
        int udhLength = SmsUdhUtil.getTotalSize(udhElements);
        int nBytesLeft = 140 - udhLength;

        switch (ud.getDcs().getAlphabet())
        {
        case GSM:
            smsPdus = createSeptetPdus(udhElements, ud, nBytesLeft);
            break;
        case UCS2:
            smsPdus = createUnicodePdus(udhElements, ud, nBytesLeft);
            break;
        case LATIN1:
        default:
            smsPdus = createOctalPdus(udhElements, ud, nBytesLeft);
            break;
        }

        return smsPdus;
    }
}
