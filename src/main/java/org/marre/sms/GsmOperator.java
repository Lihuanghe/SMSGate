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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains information about a GSM operator.
 * 
 * Currently holds, country, name, mcc and mnc.
 * 
 * @author Markus
 * @version $Id$
 */
public class GsmOperator
{
    private static final Logger log_ = LoggerFactory.getLogger(GsmOperator.class);
    private static List<GsmOperator> operators_;
    
    private final String name_;
    private final String country_;
    private final int mcc_;
    private final int mnc_;

    private GsmOperator(String name, String country, int mcc, int mnc)
    {
        name_ = name;
        country_ = country;
        mcc_ = mcc;
        mnc_ = mnc;
    }

    /**
     * @return Returns the country.
     */
    public String getCountry()
    {
        return country_;
    }

    /**
     * @return Returns the mcc.
     */
    public int getMcc()
    {
        return mcc_;
    }

    /**
     * @return Returns the mnc.
     */
    public int getMnc()
    {
        return mnc_;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name_;
    }

    /**
     * @return a list of all GsmOperator that are known to smsj.
     */
    public static List<GsmOperator> getOperators() {
        return Collections.unmodifiableList(operators_);
    }
    
    private static void loadOperatorsFromResource(String resourceName)
    {
        InputStream in = GsmOperator.class.getResourceAsStream(resourceName);
        
        if (in != null)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                List<GsmOperator> operators = new LinkedList<GsmOperator>();
                
                for (String row=reader.readLine(); row != null; row=reader.readLine()) {
                    StringTokenizer st = new StringTokenizer(row, "|");

                    String country = st.nextToken();
                    String name = st.nextToken();
                    int mcc = Integer.parseInt(st.nextToken());
                    int mnc = Integer.parseInt(st.nextToken());
                    
                    operators.add(new GsmOperator(name, country, mcc, mnc));
                }
                
                // Store result as an unmodifiable list
                operators_ = Collections.unmodifiableList(operators);
            }
            catch (IOException ex)
            {
                log_.error("Failed to load mcc and mnc properties", ex);
            }
        }
    }
    
    static 
    {
        loadOperatorsFromResource("resources/gsmoperators.txt");
    }
}
