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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.marre.util.StringUtil;

/**
 * Baseclass for UcpMsg.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public abstract class UcpMsg
{
    protected static final byte STX = (byte) 0x02;
    protected static final byte ETX = (byte) 0x03;

    private static Logger log_ = LoggerFactory.getLogger(UcpMsg.class);

    protected String[] ucpFields_;
    protected char or_; // 'O' or 'R'
    protected int trn_;
    protected byte ot_;

    public UcpMsg(int nFields)
    {
        ucpFields_ = new String[nFields];
    }

    public void setField(int field, String value)
    {
        ucpFields_[field] = value;
    }

    public String getField(int field)
    {
        return ucpFields_[field];
    }

    protected void setOR(char or)
    {
        or_ = or;
    }

    protected void setOT(byte ot)
    {
        ot_ = ot;
    }

    public void setTRN(int trn)
    {
        trn_ = trn;
    }

    public byte calcChecksum(String data)
    {
        int checksum = 0;
        for (int i = 0; i < data.length(); i++)
        {
            checksum = (checksum + data.charAt(i)) % 256;
        }
        return (byte) (checksum & 0xff);
    }

    public String buildCommand()
    {
        StringBuffer command = new StringBuffer(200);
        int length = 0;

        // CALC LENGTH

        // length = trn + len + o|r + ot
        length = 3 + 5 + 2 + 3;
        // length += data
        for (int i = 0; i < ucpFields_.length; i++)
        {
            if (ucpFields_[i] != null)
            {
                length += ucpFields_[i].length();
            }
            length += 1;
        }
        // length += checksum
        length += 3;

        // HEADER (TRN/LEN/O|R/OT)

        // TRN (2 num char)
        command.append(StringUtil.intToString(trn_, 2));
        command.append('/');
        // LEN (5 num char)
        command.append(StringUtil.intToString(length, 5));
        command.append('/');
        // O|R (Char 'O' or 'R')
        command.append(or_);
        command.append('/');
        // OT (2 num char)
        command.append(StringUtil.intToString(ot_, 2));
        command.append('/');

        // DATA
        for (int i = 0; i < ucpFields_.length; i++)
        {
            if (ucpFields_[i] != null)
            {
                command.append(ucpFields_[i]);
            }
            command.append('/');
        }

        // CHECKSUM
        command.append(StringUtil.byteToHexString(calcChecksum(command.toString())));

        return command.toString();
    }

    public void writeTo(OutputStream os) throws IOException
    {
        // STX
        os.write(STX);

        // Command
        os.write(buildCommand().getBytes());

        // ETX
        os.write(ETX);
    }

    public byte[] getCommand()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
        try
        {
            writeTo(baos);
        }
        catch (IOException ex)
        {
            log_.debug("getCommand() - Failed to write to an ByteArrayOutputStream!");
        }
        return baos.toByteArray();
    }
}
