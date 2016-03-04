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
package org.marre.sms.transport.ucp;

import org.marre.util.StringUtil;

/**
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class UcpSeries50 extends UcpMsg
{
    public static final byte OP_SUBMIT_SHORT_MESSAGE = 51;
    public static final byte OP_DELIVER_SHORT_MESSAGE = 52;
    public static final byte OP_DELIVER_NOTIFICATION = 53;
    public static final byte OP_MODIFY_MESSAGE = 54;
    public static final byte OP_INQUIRY_MESSAGE = 55;
    public static final byte OP_DELETE_MESSAGE = 56;
    public static final byte OP_RESPONSE_INQUIRY_MESSAGE = 57;
    public static final byte OP_RESPONSE_DELETE_MESSAGE = 58;

    public static final byte XSER_TYPE_UDH = 1;
    public static final byte XSER_TYPE_DCS = 2;

    protected static final int FIELD_ADC = 0;
    protected static final int FIELD_OADC = 1;
    protected static final int FIELD_AC = 2;
    protected static final int FIELD_NRQ = 3;
    protected static final int FIELD_NADC = 4;
    protected static final int FIELD_NT = 5;
    protected static final int FIELD_NPID = 6;
    protected static final int FIELD_LRQ = 7;
    protected static final int FIELD_LRAD = 8;
    protected static final int FIELD_LPID = 9;
    protected static final int FIELD_DD = 10;
    protected static final int FIELD_DDT = 11;
    protected static final int FIELD_VP = 12;
    protected static final int FIELD_RPID = 13;
    protected static final int FIELD_SCTS = 14;
    protected static final int FIELD_DST = 15;
    protected static final int FIELD_RSN = 16;
    protected static final int FIELD_DSCTS = 17;
    protected static final int FIELD_MT = 18;
    protected static final int FIELD_NB = 19;
    protected static final int FIELD_MSG = 20; // NMsg, AMsg or TMsg (MT)
    protected static final int FIELD_MMS = 21;
    protected static final int FIELD_PR = 22;
    protected static final int FIELD_DCS = 23;
    protected static final int FIELD_MCLS = 24;
    protected static final int FIELD_RPI = 25;
    protected static final int FIELD_CPG = 26;
    protected static final int FIELD_RPLY = 27;
    protected static final int FIELD_OTOA = 28;
    protected static final int FIELD_HPLMN = 29;
    protected static final int FIELD_XSER = 30;
    protected static final int FIELD_RES4 = 31;
    protected static final int FIELD_RES5 = 32;

    public UcpSeries50(byte operation)
    {
        super(33);
        setOR('O');
        setOT(operation);
    }

    public void clearXSer()
    {
        ucpFields_[FIELD_XSER] = null;
    }

    public void addXSer(byte type, byte data)
    {
        StringBuffer xSerBuff = new StringBuffer();

        xSerBuff.append(StringUtil.byteToHexString(type));
        xSerBuff.append("01");
        xSerBuff.append(StringUtil.byteToHexString(data));

        ucpFields_[FIELD_XSER] = (ucpFields_[FIELD_XSER] == null) ? (xSerBuff.toString())
                : (ucpFields_[FIELD_XSER] + xSerBuff.toString());
    }

    public void addXSer(byte type, byte[] data)
    {
        StringBuffer xSerBuff = new StringBuffer();

        xSerBuff.append(StringUtil.byteToHexString(type));
        xSerBuff.append(StringUtil.byteToHexString((byte) (data.length + 1 & 0xff)));
        xSerBuff.append(StringUtil.byteToHexString((byte) (data.length & 0xff)));
        xSerBuff.append(StringUtil.bytesToHexString(data));

        ucpFields_[FIELD_XSER] = (ucpFields_[FIELD_XSER] == null) ? (xSerBuff.toString())
                : (ucpFields_[FIELD_XSER] + xSerBuff.toString());
    }
}
