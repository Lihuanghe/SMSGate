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
package org.marre.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.marre.mime.MimeBodyPart;
import org.marre.mime.MimeHeader;
import org.marre.mime.MimeMultipart;
import org.marre.mime.encoder.MimeEncoder;

/**
 * Converts mime documents to a wsp encoded stream.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class WapMimeEncoder implements MimeEncoder
{
    private final WspEncodingVersion wspEncodingVersion_;

    public WapMimeEncoder()
    {
        this(WspEncodingVersion.VERSION_1_3);
    }
    
    public WapMimeEncoder(WspEncodingVersion wspEncodingVersion)
    {
        wspEncodingVersion_ = wspEncodingVersion;
    }

    /**
     * Writes an WSP encoded content type header to the given stream.
     * <p>
     * NOTE! It only writes an WSP encoded content-type to the stream. It does
     * not add the content type header id.
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
        if (msg instanceof MimeMultipart)
        {
            String ct = msg.getContentType().getValue();

            // Convert multipart headers...
            // TODO: Clone content type... We shouldn't change the msg...
            String newCt = WspUtil.convertMultipartContentType(ct);
            msg.getContentType().setValue(newCt);
        }

        WspUtil.writeContentType(wspEncodingVersion_, os, msg.getContentType());
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
            WspHeaderEncoder.writeHeader(wspEncodingVersion_, os, header);
        }
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

            // Convert multipart headers...
            // TODO: Clone content type... We shouldn't change the msg...
            String newCt = WspUtil.convertMultipartContentType(ct);
            msg.getContentType().setValue(newCt);

            if (newCt.startsWith("application/vnd.wap.multipart."))
            {
                // WSP encoded multipart
                writeMultipart(os, (MimeMultipart) msg);
            }
            else
            {
                // Not WSP encoded
                // TODO: Write textual "multipart/"
            }
        }
        else
        {
            os.write(msg.getBody());
        }
    }

    // Section 8.5.2 in WAP-230-WSP-20010705
    private void writeMultipart(OutputStream os, MimeMultipart multipart) throws IOException
    {
        Collection<MimeBodyPart> bodyParts = multipart.getBodyParts();

        // nEntries
        WspUtil.writeUintvar(os, bodyParts.size());

        for (MimeBodyPart part : bodyParts) {
            ByteArrayOutputStream headers = new ByteArrayOutputStream();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Generate content-type + headers
            writeContentType(headers, part);
            writeHeaders(headers, part);
            // Done with the headers...
            headers.close();

            // Generate content...
            writeBody(content, part);
            content.close();

            // Write data to the os

            // Length of the content type and headers combined
            WspUtil.writeUintvar(os, headers.size());
            // Length of the data (content)
            WspUtil.writeUintvar(os, content.size());
            // Content type + headers
            os.write(headers.toByteArray());
            // Data
            os.write(content.toByteArray());
        }
    }
}
