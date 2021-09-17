package com.zx.sms.codec.smpp.android.gsm;

public class PduParser {
	   byte pdu[];
       int cur;
       SmsHeader userDataHeader;
       byte[] userData;
       int mUserDataSeptetPadding;
       int mUserDataSize;



       public PduParser(byte[] pdu) {
           this.pdu = pdu;
           cur = 0;
           mUserDataSeptetPadding = 0;
       }

 
       /**
        * returns non-sign-extended byte value
        */
       int getByte() {
           return pdu[cur++] & 0xff;
       }

 
       /**
        * Parses an SC timestamp and returns a currentTimeMillis()-style
        * timestamp
        */

  

       /**
        * Pulls the user data out of the PDU, and separates the payload from
        * the header if there is one.
        *
        * @param hasUserDataHeader true if there is a user data header
        * @param dataInSeptets true if the data payload is in septets instead
        *  of octets
        * @return the number of septets or octets in the user data payload
        */
       int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
           int offset = cur;
           int userDataLength = pdu[offset++] & 0xff;
           int headerSeptets = 0;
           int userDataHeaderLength = 0;

           if (hasUserDataHeader) {
               userDataHeaderLength = pdu[offset++] & 0xff;

               byte[] udh = new byte[userDataHeaderLength];
               System.arraycopy(pdu, offset, udh, 0, userDataHeaderLength);
               userDataHeader = SmsHeader.fromByteArray(udh);
               offset += userDataHeaderLength;

               int headerBits = (userDataHeaderLength + 1) * 8;
               headerSeptets = headerBits / 7;
               headerSeptets += (headerBits % 7) > 0 ? 1 : 0;
               mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
           }

           int bufferLen;
           if (dataInSeptets) {
               /*
                * Here we just create the user data length to be the remainder of
                * the pdu minus the user data header, since userDataLength means
                * the number of uncompressed sepets.
                */
               bufferLen = pdu.length - offset;
           } else {
               /*
                * userDataLength is the count of octets, so just subtract the
                * user data header.
                */
               bufferLen = userDataLength - (hasUserDataHeader ? (userDataHeaderLength + 1) : 0);
               if (bufferLen < 0) {
                   bufferLen = 0;
               }
           }

           userData = new byte[bufferLen];
           System.arraycopy(pdu, offset, userData, 0, userData.length);
           cur = offset;

           if (dataInSeptets) {
               // Return the number of septets
               int count = userDataLength - headerSeptets;
               // If count < 0, return 0 (means UDL was probably incorrect)
               return count < 0 ? 0 : count;
           } else {
               // Return the number of octets
               return userData.length;
           }
       }

       /**
        * Returns the user data payload, not including the headers
        *
        * @return the user data payload, not including the headers
        */
       byte[] getUserData() {
           return userData;
       }

       /**
        * Returns the number of padding bits at the begining of the user data
        * array before the start of the septets.
        *
        * @return the number of padding bits at the begining of the user data
        * array before the start of the septets
        */
       int getUserDataSeptetPadding() {
           return mUserDataSeptetPadding;
       }

       /**
        * Returns an object representing the user data headers
        *
        * {@hide}
        */
       SmsHeader getUserDataHeader() {
           return userDataHeader;
       }

/*
       XXX Not sure what this one is supposed to be doing, and no one is using
       it.
       String getUserDataGSM8bit() {
           // System.out.println("remainder of pud:" +
           // HexDump.dumpHexString(pdu, cur, pdu.length - cur));
           int count = pdu[cur++] & 0xff;
           int size = pdu[cur++];

           // skip over header for now
           cur += size;

           if (pdu[cur - 1] == 0x01) {
               int tid = pdu[cur++] & 0xff;
               int type = pdu[cur++] & 0xff;

               size = pdu[cur++] & 0xff;

               int i = cur;

               while (pdu[i++] != '\0') {
               }

               int length = i - cur;
               String mimeType = new String(pdu, cur, length);

               cur += length;

               if (false) {
                   System.out.println("tid = 0x" + HexDump.toHexString(tid));
                   System.out.println("type = 0x" + HexDump.toHexString(type));
                   System.out.println("header size = " + size);
                   System.out.println("mimeType = " + mimeType);
                   System.out.println("remainder of header:" +
                    HexDump.dumpHexString(pdu, cur, (size - mimeType.length())));
               }

               cur += size - mimeType.length();

               // System.out.println("data count = " + count + " cur = " + cur
               // + " :" + HexDump.dumpHexString(pdu, cur, pdu.length - cur));

               MMSMessage msg = MMSMessage.parseEncoding(mContext, pdu, cur,
                       pdu.length - cur);
           } else {
               System.out.println(new String(pdu, cur, pdu.length - cur - 1));
           }

           return IccUtils.bytesToHexString(pdu);
       }
*/

       /**
        * Interprets the user data payload as pack GSM 7bit characters, and
        * decodes them into a String.
        *
        * @param septetCount the number of septets in the user data payload
        * @return a String with the decoded characters
        */
       String getUserDataGSM7Bit(int septetCount) {
           String ret;

           ret = GsmAlphabet.gsm7BitPackedToString(pdu, cur, septetCount,
                   mUserDataSeptetPadding);

           cur += (septetCount * 7) / 8;

           return ret;
       }

}
