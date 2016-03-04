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

/**
 * Represents an phonenumber in SMSj.
 * <p>
 * The address can be a phonenumber (+463482422) or alphanumeric
 * ('SmsService'). Not all networks and transports supports alphanumeric
 * sending id.
 * <p>
 * Max address length is <br>
 * - 20 digits (excluding any initial '+') or<br>
 * - 11 alphanumeric chars (if TON == TON_ALPHANUMERIC).
 * <p>
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public final class SmsAddress
{
    private static final String ALLOWED_DIGITS = "+0123456789*#ab";

    private SmsTon ton_ = SmsTon.INTERNATIONAL;
    private SmsNpi npi_ = SmsNpi.ISDN_TELEPHONE;

    private String address_;

    /**
     * Creates an SmsAddress object.
     * <p>
     * This constructor tries to be intelligent by choosing the correct
     * NPI and TON from the given address.
     *
     * @param address The address
     * @throws SmsException Thrown if the address is invalid
     */
    public SmsAddress(String address)
        throws SmsException
    {
        SmsNpi npi = SmsNpi.ISDN_TELEPHONE;
        SmsTon ton = SmsTon.INTERNATIONAL;

        for (int i = 0; i < address.length(); i++)
        {
            char ch = address.charAt(i);
            if (ALLOWED_DIGITS.indexOf(ch) == -1)
            {
                ton = SmsTon.ALPHANUMERIC;
                npi = SmsNpi.UNKNOWN;
                break;
            }
        }

        init(address, ton, npi);
    }

    /**
     * Creates an SmsAddress object.
     * <p>
     * If you choose TON_ALPHANUMERIC then the NPI will be set to NPI_UNKNOWN.
     *
     * @param address The address
     * @param ton The type of number
     * @param npi The number plan indication
     * @throws SmsException Thrown if the address is invalid
     */
    public SmsAddress(String address, SmsTon ton, SmsNpi npi)
        throws SmsException
    {
        init(address, ton, npi);
    }

    private void init(String address, SmsTon ton, SmsNpi npi)
        throws SmsException
    {
        int msisdnLength;

        if (address == null)
        {
            throw new SmsException("Empty msisdn.");
        }

        ton_ = ton;
        address_ = address.trim();
        msisdnLength = address_.length();

        if (msisdnLength == 0)
        {
            throw new SmsException("Empty address.");
        }

        if (ton == SmsTon.ALPHANUMERIC)
        {
            npi_ = SmsNpi.UNKNOWN;

            if (address.length() > 11)
            {
                throw new SmsException("Alphanumeric address can be at most 11 chars.");
            }
        }
        else
        {
            npi_ = npi;

            // Trim '+' from address
            if (address_.charAt(0) == '+')
            {
                address_ = address_.substring(1);
                msisdnLength -= 1;
            }

            if (msisdnLength > 20)
            {
                throw new SmsException("Too long address, Max allowed is 20 digits (excluding any inital '+').");
            }

            for (int i = 0; i < address.length(); i++)
            {
                char ch = address.charAt(i);
                if (ALLOWED_DIGITS.indexOf(ch) == -1)
                {
                    throw new SmsException("Invalid digit in address. '" + ch + "'.");
                }
            }
        }
    }

    /**
     * Returns the msisdn.
     *
     * @return The address
     */
    public String getAddress()
    {
        return address_;
    }

    public boolean isAlphanumeric() {
        return ton_ == SmsTon.ALPHANUMERIC;
    }

    /**
     * Returns the TON field
     *
     * @return The TON
     */
    public SmsTon getTypeOfNumber()
    {
        return ton_;
    }

    /**
     * Returns the NPI field
     *
     * @return The NPI
     */
    public SmsNpi getNumberingPlanIdentification()
    {
        return npi_;
    }
}

