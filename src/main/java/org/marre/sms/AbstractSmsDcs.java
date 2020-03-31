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

import java.io.Serializable;

/**
 * Represents a SMS DCS (Data Coding Scheme).
 *
 * @version $Id$
 * @author Markus Eriksson
 */
public abstract class AbstractSmsDcs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5797340786616956L;
	/** The encoded dcs. */
	protected final byte dcs_;

	/**
	 * Creates a specific DCS.
	 * 
	 * @param dcs
	 *            The dcs.
	 */
	public AbstractSmsDcs(byte dcs) {
		dcs_ = dcs;
	}

	/**
	 * Returns the encoded dcs.
	 * 
	 * @return The dcs.
	 */
	public byte getValue() {
		return dcs_;
	}




	/**
	 * What group (type of message) is the given dcs.
	 * 
	 * @return The matching group. Or null if unknown.
	 */
	public DcsGroup getGroup() {
		switch ((dcs_ & 0xC0)) {
		case 0x00:
			return DcsGroup.GENERAL_DATA_CODING;
		case 0x40:
			return DcsGroup.MSG_MARK_AUTO_DELETE;
		case 0x80:
			return DcsGroup.RESERVED;
		}

		switch ((dcs_ & 0xF0)) {
		case 0xC0:
			return DcsGroup.MESSAGE_WAITING_DISCARD;
		case 0xD0:
			return DcsGroup.MESSAGE_WAITING_STORE_GSM;
		case 0xE0:
			return DcsGroup.MESSAGE_WAITING_STORE_UCS2;
		case 0xF0:
			return DcsGroup.DATA_CODING_MESSAGE;
		default:
			return DcsGroup.GENERAL_DATA_CODING;
		}
	}

	/**
	 * Get the message class.
	 *
	 * @return Returns the message class.
	 */
	public SmsMsgClass getMessageClass() {
		switch (getGroup()) {
		case GENERAL_DATA_CODING:
			// General Data Coding Indication
			if (dcs_ == 0x00) {
				return SmsMsgClass.CLASS_UNKNOWN;
			}

			switch (dcs_ & 0x13) {
			case 0x10:
				return SmsMsgClass.CLASS_0;
			case 0x11:
				return SmsMsgClass.CLASS_1;
			case 0x12:
				return SmsMsgClass.CLASS_2;
			case 0x13:
				return SmsMsgClass.CLASS_3;
			default:
				return SmsMsgClass.CLASS_UNKNOWN;
			}

		case DATA_CODING_MESSAGE:
			// Data coding/message class
			switch (dcs_ & 0x03) {
			case 0x00:
				return SmsMsgClass.CLASS_0;
			case 0x01:
				return SmsMsgClass.CLASS_1;
			case 0x02:
				return SmsMsgClass.CLASS_2;
			case 0x03:
				return SmsMsgClass.CLASS_3;
			default:
				return SmsMsgClass.CLASS_UNKNOWN;
			}

		default:
			return SmsMsgClass.CLASS_UNKNOWN;
		}
	}
	
	abstract public SmsAlphabet getAlphabet();
	abstract public int getMaxMsglength();
}
