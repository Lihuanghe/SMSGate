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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Simple Serial port comm.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class SerialComm implements GsmComm
{
    private static final Logger log_ = LoggerFactory.getLogger(SerialComm.class);

    private static final int DEFAULT_BIT_RATE = 19200;
    private static final int DEFAULT_TIMEOUT = 0;
    
    private SerialPort serialPort_;
    private OutputStream serialOs_;
    private InputStream serialIs_;

    private final String appName_;
    private final String portName_;
    private int bitRate_;
    private int dataBits_; 
    private int stopBits_;
    private int parity_;
    private int flowControl_;
    private int timeout_;
    private boolean echo_;

    /**
     * Constructor.
     * 
     * @param portName
     */
    public SerialComm(String appName, String portName)
    {
        appName_ = appName;
        portName_ = portName;
        bitRate_ = DEFAULT_BIT_RATE;
        dataBits_ = SerialPort.DATABITS_8;
        stopBits_ = SerialPort.STOPBITS_1;
        parity_ = SerialPort.PARITY_NONE;
        flowControl_ = SerialPort.FLOWCONTROL_NONE;
        timeout_ = DEFAULT_TIMEOUT;
        echo_ = true;
    }

    private SerialPort openSerialPort(String portName)
        throws PortInUseException, IOException
    {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        // find the requested port
        while (portList.hasMoreElements()) 
        {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

            log_.debug("Port" +
                    "\n  name : " + portId.getName() +
                    "\n  currentOwner : " + portId.getCurrentOwner() + 
                    "\n  serial : " + (portId.getPortType() == CommPortIdentifier.PORT_SERIAL));
            
            // Check for serial port
            if ( (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) &&
                 (portId.getName().equals(portName)) ) 
            {
                return (SerialPort) portId.open(appName_, 3000);
            }
        }

        throw new IOException("Port [" + portName + "] not found");
    }

    /* (non-Javadoc)
     * @see org.marre.sms.transport.gsm.GsmComm#open()
     */
    public void open() 
        throws IOException
    {
        try {
            serialPort_ = openSerialPort(portName_);
            log_.debug("Opened port : " + serialPort_);
        } catch (PortInUseException piuEx) {
            log_.error("Failed to open serial port", piuEx);
            throw (IOException) new IOException(piuEx.getMessage()).initCause(piuEx);
        }

        if (serialPort_ == null)
        {
            log_.error("Failed to open serial port");
            throw new IOException("Failed to open port : " + portName_);
        }
        
        try
        {
            serialPort_.setSerialPortParams(bitRate_, dataBits_, stopBits_, parity_);
        } 
        catch (UnsupportedCommOperationException e)
        {
            log_.warn("setSerialPortParams failed", e);
        } 

        try
        {
            serialPort_.setFlowControlMode(flowControl_);
        } 
        catch (UnsupportedCommOperationException e)
        {
            log_.warn("setFlowControlMode failed", e);
        } 

        if (timeout_ > 0) 
        {
            try 
            {
                serialPort_.enableReceiveTimeout(timeout_);
            }
            catch (UnsupportedCommOperationException e)
            {
                log_.warn("enableReceiveTimeout failed", e);
            }
        }
        else 
        {
            serialPort_.disableReceiveTimeout();
        }
        
        try 
        {
            serialOs_ = serialPort_.getOutputStream();
            serialIs_ = new BufferedInputStream(serialPort_.getInputStream());
        }
        catch (IOException ex) 
        {
            log_.error("Failed to get in and output streams", ex);
            close();
            throw ex;
        }
    }

    /* (non-Javadoc)
     * @see org.marre.sms.transport.gsm.GsmComm#close()
     */
    public void close()
    {
        if (serialOs_ != null)
        {
            try { serialOs_.close(); } catch (Exception ex) { log_.error("serialOs_.close failed", ex); }
        }
        
        if (serialIs_ != null)
        {
            try { serialIs_.close(); } catch (Exception ex) { log_.error("serialIs_.close failed", ex); }
        }
        
        if (serialPort_ != null)
        {        
            try { serialPort_.close(); } catch (Exception ex) { log_.error("serialPort_.close failed", ex); }
        }
        
        serialOs_ = null;
        serialIs_ = null;            
        serialPort_ = null; 
    }

    /* (non-Javadoc)
     * @see org.marre.sms.transport.gsm.GsmComm#sendLine(java.lang.String)
     */
    public void send(String row) 
        throws IOException
    {
        // TODO: Remove \r\n from log
        log_.debug(">> " + row);

        serialOs_.write(row.getBytes());
        
        if (echo_) {
            String echo = readOneRowOfData(null);
            
            // Some devices adds an extra \r\n as well
            serialIs_.mark(2);
            if ((serialIs_.read() != '\r') || (serialIs_.read() != '\n')) {
                serialIs_.reset();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.marre.sms.transport.gsm.GsmComm#readLine()
     */
    public String readLine() 
        throws IOException
    {
        return readLine(null);
    }
    
    public String readLine(String find) 
        throws IOException
    {
        return readOneRowOfData(find);
    }
    
    private String readOneRowOfData(String find)
        throws IOException
    {
        StringBuilder buffer = new StringBuilder(256);
        int ch;

        while (true)
        {
            ch = serialIs_.read();
            
            if ( ch == '\r' )
            {
                continue;
            }
            
            if ( (ch == -1) ||
                 (ch == '\n') )
            {
                break;
            }
                        
            buffer.append((char) ch);
            
            if ( (find != null) && 
                 (find.equals(buffer.toString())) ) {
                // Found the string we are looking for...
                break;
            }
        }

        String row = buffer.toString();
        
        // LOG
        log_.debug("<< " + row);

        return row;
    }
    
    public void setBitRate(String bitRate)
    {
        if      ("110".equals(bitRate))    bitRate_ = 110;
        else if ("134".equals(bitRate))    bitRate_ = 134;
        else if ("150".equals(bitRate))    bitRate_ = 150;
        else if ("300".equals(bitRate))    bitRate_ = 300;
        else if ("600".equals(bitRate))    bitRate_ = 600;
        else if ("1200".equals(bitRate))   bitRate_ = 1200;
        else if ("2400".equals(bitRate))   bitRate_ = 2400;
        else if ("4800".equals(bitRate))   bitRate_ = 4800;
        else if ("9600".equals(bitRate))   bitRate_ = 9600;
        else if ("14400".equals(bitRate))  bitRate_ = 14400;
        else if ("19200".equals(bitRate))  bitRate_ = 19200;
        else if ("38400".equals(bitRate))  bitRate_ = 38400;
        else if ("57600".equals(bitRate))  bitRate_ = 57600;
        else if ("115200".equals(bitRate)) bitRate_ = 115200;
        else if ("128000".equals(bitRate)) bitRate_ = 128000;
        else                               bitRate_ = DEFAULT_BIT_RATE;        
    }
    
    public void setDataBits(String dataBits)
    {
        if      ("5".equals(dataBits)) dataBits_ = SerialPort.DATABITS_5;
        else if ("6".equals(dataBits)) dataBits_ = SerialPort.DATABITS_6;
        else if ("7".equals(dataBits)) dataBits_ = SerialPort.DATABITS_7;
        else if ("8".equals(dataBits)) dataBits_ = SerialPort.DATABITS_8;
        else                           dataBits_ = SerialPort.DATABITS_8;
    }

    public void setFlowControl(String flowControl)
    {
        if      ("RTSCTS".equals(flowControl))  flowControl_ = SerialPort.FLOWCONTROL_RTSCTS_IN  | SerialPort.FLOWCONTROL_RTSCTS_OUT;
        else if ("XONXOFF".equals(flowControl)) flowControl_ = SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
        else if ("NONE".equals(flowControl))    flowControl_ = SerialPort.FLOWCONTROL_NONE;
        else                                    flowControl_ = SerialPort.FLOWCONTROL_NONE;
    }

    public void setParity(String parity)
    {
        if      ("NONE".equals(parity))  parity_ = SerialPort.PARITY_NONE;
        else if ("EVEN".equals(parity))  parity_ = SerialPort.PARITY_EVEN;
        else if ("ODD".equals(parity))   parity_ = SerialPort.PARITY_ODD;
        else if ("MARK".equals(parity))  parity_ = SerialPort.PARITY_MARK;
        else if ("SPACE".equals(parity)) parity_ = SerialPort.PARITY_SPACE;
        else                             parity_ = SerialPort.PARITY_NONE;
    }

    public void setStopBits(String stopBits)
    {
        if      ("1".equals(stopBits))   stopBits_ = SerialPort.STOPBITS_1;
        else if ("1.5".equals(stopBits)) stopBits_ = SerialPort.STOPBITS_1_5;
        else if ("2".equals(stopBits))   stopBits_ = SerialPort.STOPBITS_2;
        else                             stopBits_ = SerialPort.STOPBITS_1;
    }
    
    public void setTimeout(String timeout)
    {
        try 
        {
            timeout_ = Integer.parseInt(timeout);
        } 
        catch (NumberFormatException e)
        {
            timeout_ = DEFAULT_TIMEOUT;
        }
    }
    
    public void setEcho(boolean echo)
    {
        echo_ = echo;
    }
}
