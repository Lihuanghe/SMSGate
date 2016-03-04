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
package org.marre.sms.transport.gsm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.marre.sms.*;

/**
 * Builds GSM pdu encoded messages.
 * 
 * @todo Add support for validity period.
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public final class GsmEncoder
{
    private GsmEncoder()
    {
        // Utility class
    }
    
    /**
     * Encodes the given sms pdu into a gsm sms pdu.
     * 
     * @param pdu
     * @param destination
     * @param sender
     * @return
     * @throws SmsException
     */
    public static byte[] encodePdu(SmsPdu pdu, SmsAddress destination, SmsAddress sender)
        throws SmsException
    {
        switch (pdu.getDcs().getAlphabet()) {
        case GSM:
            return encodeSeptetPdu(pdu, destination, sender);
         
        default:
            return encodeOctetPdu(pdu, destination, sender);
        }
    }
    
    /**
     * Encodes an septet encoded pdu.
     * 
     * @param pdu
     * @param destination
     * @param sender
     * @return
     * @throws SmsException
     */
    private static byte[] encodeSeptetPdu(SmsPdu pdu, SmsAddress destination, SmsAddress sender)
        throws SmsException
    {
        SmsUserData userData = pdu.getUserData();
        byte[] ud = userData.getData();
        byte[] udh = pdu.getUserDataHeaders();

        int nUdSeptets = userData.getLength();
        int nUdBits = 0;

        int nUdhBytes = (udh == null) ? 0 : udh.length;

        // UDH + UDHL
        int nUdhBits = 0;

        // UD + UDH + UDHL
        int nTotalBytes = 0;
        int nTotalBits = 0;
        int nTotalSeptets = 0;

        int nFillBits = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(161);

        try
        {
            // UDH?
            if (nUdhBytes == 0)
            {
                // TP-Message-Type-Indicator = SUBMIT
                // TP-Reject-Duplicates = ON
                // TP-Validity-Period-Format = No field
                // TP-Status-Report-Request = No
                // TP-User-Data-Header = No
                // TP-Reply-Path = No
                baos.write(0x01);
            }
            else
            {
                // +1 is for the UDHL
                nUdhBits = nUdhBytes * 8;

                if ( (nUdhBits % 7) > 0 )
                {
                    nFillBits = 7 - (nUdhBits % 7);
                }

                // TP-Message-Type-Indicator = SUBMIT
                // TP-Reject-Duplicates = ON
                // TP-Validity-Period-Format = No field
                // TP-Status-Report-Request = No
                // TP-User-Data-Header = Yes
                // TP-Reply-Path = No
                baos.write(0x41);
            }

            nUdBits = nUdSeptets * 7;

            nTotalBits = nUdBits + nFillBits + nUdhBits;
            nTotalSeptets = nTotalBits / 7;

            nTotalBytes = nTotalBits / 8;
            if (nTotalBits % 8 > 0)
            {
                nTotalBytes += 1;
            }

            // TP-Message-Reference
            // Leave to 0x00, MS will set it
            baos.write(0x00);

            // 2-12 octets
            // TP-DA
            // - 1:st octet - length of address (4 bits)
            // - 2:nd octet
            //   - myBit 7 - always 1
            //   - myBit 4-6 - TON
            //   - myBit 0-3 - NPI
            // - n octets - BCD
            writeDestinationAddress(baos, destination);

            // TP-PID
            baos.write(0x00);

            // TP-DCS
            // UCS, septets, language, SMS class...
            baos.write(pdu.getDcs().getValue());

            // TP-VP - Optional
            // Probably not needed

            // UDH?
            if ((udh == null) || (udh.length == 0))
            {
                // TP-UDL
                baos.write(nUdSeptets);

                // TP-UD
                baos.write(ud);
            }
            else
            {
                // The whole UD PDU
                byte[] fullUd = new byte[nTotalBytes];

                // TP-UDL
                // UDL includes the length of the UDHL
                baos.write(nTotalSeptets);

                // TP-UDH (including user data header length)
                System.arraycopy(udh, 0, fullUd, 0, nUdhBytes);

                // TP-UD
                SmsPduUtil.arrayCopy(ud, 0, fullUd, nUdhBytes, nFillBits, nUdBits);

                baos.write(fullUd);
            }
            baos.close();
        }
        catch (IOException ex)
        {
            throw new SmsException(ex);
        }

        return baos.toByteArray();
    }

    /**
     * Encodes an octet encoded sms pdu.
     * 
     * @param pdu
     * @param destination
     * @param sender
     * @return
     * @throws SmsException
     */
    private static byte[] encodeOctetPdu(SmsPdu pdu, SmsAddress destination, SmsAddress sender)
        throws SmsException
    {
        SmsUserData userData = pdu.getUserData();
        byte[] ud = userData.getData();
        byte[] udh = pdu.getUserDataHeaders();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
        
        try
        {
            int nUdBytes = userData.getLength();
            int nUdhBytes = (udh == null) ? 0 : udh.length;
            // +1 For the UDH Length
            int tpUdl = nUdBytes + nUdhBytes + 1;

            // UDH?
            if (nUdhBytes == 0)
            {
                // TP-Message-Type-Indicator = SUBMIT
                // TP-Reject-Duplicates = ON
                // TP-Validity-Period-Format = No field
                // TP-Status-Report-Request = No
                // TP-User-Data-Header = No
                // TP-Reply-Path = No
                baos.write(0x01);
            }
            else
            {
                // TP-Message-Type-Indicator = SUBMIT
                // TP-Reject-Duplicates = ON
                // TP-Validity-Period-Format = No field
                // TP-Status-Report-Request = No
                // TP-User-Data-Header = Yes
                // TP-Reply-Path = No
                baos.write(0x41);
            }

            // TP-Message-Reference
            // Leave to 0x00, MS will set it
            baos.write(0x00);

            // 2-12 octets
            // TP-DA
            // - 1:st octet - length of address (4 bits)
            // - 2:nd octet
            //   - myBit 7 - always 1
            //   - myBit 4-6 - TON
            //   - myBit 0-3 - NPI
            // - n octets - BCD
            writeDestinationAddress(baos, destination);

            // TP-PID
            baos.write(0x00);

            // TP-DCS
            baos.write(pdu.getDcs().getValue());

            // 1 octet/ 7 octets
            // TP-VP - Optional

            // UDH?
            if (nUdhBytes == 0)
            {
                // 1 Integer
                // TP-UDL
                // UDL includes the length of UDH
                baos.write(nUdBytes);

                // n octets
                // TP-UD
                baos.write(ud);
            }
            else
            {
                // The whole UD PDU without the header length byte
                byte[] fullUd = new byte[nUdBytes + nUdhBytes];

                // TP-UDL includes the length of UDH
                // +1 is for the size header...
                baos.write(nUdBytes + nUdhBytes);

                // TP-UDH (including user data header length)
                System.arraycopy(udh, 0, fullUd, 0, nUdhBytes);
                // TP-UD
                System.arraycopy(ud, 0, fullUd, nUdhBytes, nUdBytes);

                baos.write(fullUd);
            }
            baos.close();
        }
        catch (IOException ex)
        {
            throw new SmsException(ex);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Writes a destination address to the given stream in the correct format
     *
     * @param os Stream to write to
     * @param destination Destination address to encode
     * @throws IOException Thrown if failing to write to the stream
     */
    private static void writeDestinationAddress(OutputStream os, SmsAddress destination)
        throws IOException
    {
        String address = destination.getAddress();
        SmsTon ton = destination.getTypeOfNumber();
        SmsNpi npi = destination.getNumberingPlanIdentification();

        // trim leading + from address
        if (address.charAt(0) == '+')
        {
            address = address.substring(1);
        }

        // Length in semi octets
        os.write(address.length());

        // Type Of Address
        os.write(0x80 | ton.getValue() << 4 | npi.getValue());

        // BCD encode
        SmsPduUtil.writeBcdNumber(os, address);
    }    
}
