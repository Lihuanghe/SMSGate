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

import java.util.Properties;

import org.marre.sms.SmsException;

/**
 * SMS Transport Manager.
 * 
 * Contains methods to manage the different transports.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public final class SmsTransportManager
{
    private SmsTransportManager()
    {
        // This is a static class
    }

    /**
     * Dynamically instantiates and initializes an SmsTransport.
     * 
     * @param classname
     *            Full classname of the transport. E.g.
     *            "org.marre.sms.transport.gsm.GsmTransport"
     * @param properties
     *            Properties to initialize the transport with.
     * @return An initialized SmsTransport object
     * @throws SmsException
     *             If we failed to load the requested transport
     */
    public static SmsTransport getTransport(String classname, Properties properties) throws SmsException
    {
        SmsTransport transport = null;

        try
        {
            // Try to find and instanciate the transport
            Class clazz = Class.forName(classname);
            Object obj = clazz.newInstance();

            // No promblems so far. Initialize the transport
            transport = (SmsTransport) obj;
            transport.init(properties);

            return (SmsTransport) obj;
        }
        catch (ClassCastException ex)
        {
            throw new SmsException(classname + "is not an SmsTransport.", ex);
        }
        catch (ClassNotFoundException ex)
        {
            throw new SmsException("Couldn't find " + classname + ". Please check your classpath.", ex);
        }
        catch (InstantiationException ex)
        {
            throw new SmsException("Couldn't create " + classname + ". Please check your classpath.", ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new SmsException("Couldn't create " + classname + ". Please check your classpath.", ex);
        }
    }
}
