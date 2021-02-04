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
package org.marre.mime.encoder;

import java.io.IOException;
import java.io.OutputStream;

import org.marre.mime.*;
import org.marre.mime.MimeHeaderParameter;
import org.marre.util.StringUtil;

/**
 * Converts mime documents to text.
 * 
 * TODO: Content-Transfer-Encoding. <br>
 * TODO: Special handling of some headers like Content-Id.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class TextMimeEncoder implements MimeEncoder
{
    /** The length of the random boundary string. */
    private static final int DEFAULT_BOUNDARY_STRING_LENGTH = 35;

    /**
     * Creates a TextMimeEncoder.
     */
    public TextMimeEncoder()
    {
    }

    /**
     * Writes the content-type of the message to the given stream.
     * 
     * @param os
     *            The stream to write to
     * @param msg
     *            The message to get the content-type from
     * @throws IOException
     *             Thrown if we fail to write the content-type to the stream
     */
    public void writeContentType(OutputStream os, MimeBodyPart msg) throws IOException
    {
        MimeContentType ct = msg.getContentType();

        if (msg instanceof MimeMultipart)
        {
            String boundary = StringUtil.randString(DEFAULT_BOUNDARY_STRING_LENGTH);
            ct.setParam("boundary", boundary);
        }

        writeHeader(os, ct);
    }

    /**
     * Writes the headers of the message to the given stream.
     * 
     * @param os
     *            The stream to write to
     * @param msg
     *            The message to get the headers from
     * @throws IOException
     *             Thrown if we fail to write the headers to the stream
     */
    public void writeHeaders(OutputStream os, MimeBodyPart msg) throws IOException
    {
        for (MimeHeader header : msg.getHeaders()) {
            writeHeader(os, header);
        }
        os.write("\r\n".getBytes());
    }

    /**
     * Writes the body of the message to the given stream.
     * 
     * @param os
     *            The stream to write to
     * @param msg
     *            The message to get the data from
     * @throws IOException
     *             Thrown if we fail to write the body to the stream
     */
    public void writeBody(OutputStream os, MimeBodyPart msg) throws IOException
    {
        if (msg instanceof MimeMultipart)
        {
            String ct = msg.getContentType().getValue();
            if (ct.startsWith("application/vnd.wap.multipart."))
            {
                // WSP encoded multipart
            }
            else
            {
                writeMultipart(os, (MimeMultipart) msg);
            }
        }
        else
        {
            os.write(msg.getBody());
            os.write("\r\n".getBytes());
        }
    }

    /**
     * Write one header to the stream.
     * 
     * @param os
     *            The stream to write to
     * @param header
     *            The header to write.
     * @throws IOException
     *             Thrown if we fail to write the header to the stream
     */
    protected void writeHeader(OutputStream os, MimeHeader header) throws IOException
    {
        StringBuilder strBuff = new StringBuilder();

        String name = header.getName();
        String value = header.getValue();

        strBuff.append(name).append(": ").append(value);

        for (MimeHeaderParameter headerParam : header.getParameters()) {
            // + "; charset=adsfasdf; param=value"
            strBuff.append("; ").append(headerParam.getName()).append("=").append(headerParam.getValue());
        }

        // <CR><LF>
        strBuff.append("\r\n");

        os.write(strBuff.toString().getBytes());
    }

    /**
     * Writes a multipart entry to the stream.
     * 
     * @param os
     *            The stream to write to
     * @param multipart
     *            The header to write.
     * @throws IOException
     *             Thrown if we fail to write an entry to the stream
     */
    private void writeMultipart(OutputStream os, MimeMultipart multipart) throws IOException
    {
        MimeContentType ct = multipart.getContentType();
        MimeHeaderParameter boundaryParam = ct.getParameter("boundary");
        String boundary = "--" + boundaryParam.getValue();

        for (MimeBodyPart part : multipart.getBodyParts()) {
            // Write boundary string
            os.write(boundary.getBytes());
            os.write("\r\n".getBytes());

            // Generate headers + content-type
            writeContentType(os, part);
            writeHeaders(os, part);

            // Write data
            writeBody(os, part);
        }
        // Write end of boundary
        os.write(boundary.getBytes());
        os.write("--\r\n".getBytes());
    }
}
