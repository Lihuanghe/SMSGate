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
package org.marre.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Contains various utiity functions related to io operations.
 * 
 * @author Markus
 * @version $Id$
 */
public final class IOUtil
{
    /**
     * The default buffer size to use for the copy method.
     */
    private static final int DEFAULT_COPY_SIZE = 1024 * 8;
    
    /**
     * This class isn't intended to be instantiated.
     */
    private IOUtil()
    {
        /* Empty */
    }
    
    /**
     * Copy data from in to out using the workbuff as temporary storage.
     * 
     * @param in stream to read from
     * @param out stream to write to
     * @param workbuff buffer to use as temporary storage while copying
     * @return number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static int copy(InputStream in, OutputStream out, byte[] workbuff)
        throws IOException 
    {
        int bytescopied = 0;
        int bytesread = 0;
        
        while ((bytesread = in.read(workbuff)) != -1) 
        {
            out.write(workbuff, 0, bytesread);
            bytescopied += bytesread;
        }
        
        return bytescopied;
    }

    /**
     * Copy data from in to out using a temporary buffer of workbuffsize bytes.
     * 
     * @param in stream to read from
     * @param out stream to write to
     * @param workbuffsize how large the work buffer should be
     * @return number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static int copy(InputStream in, OutputStream out, int workbuffsize)
        throws IOException
    {
        return IOUtil.copy(in, out, new byte[workbuffsize]);
    }
    
    /**
     * Copy data from in to out using a default temporary buffer.
     * 
     * @param in stream to read from
     * @param out stream to write to
     * @return number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static int copy(InputStream in, OutputStream out)
        throws IOException
    {
        return IOUtil.copy(in, out, new byte[DEFAULT_COPY_SIZE]);
    }
}
