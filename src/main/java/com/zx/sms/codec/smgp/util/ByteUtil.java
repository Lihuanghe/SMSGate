package com.zx.sms.codec.smgp.util;

public class ByteUtil {

	public static void main(String[] args) {
		byte[] buffer = new byte[20];
		int2byte(577, buffer, 0);
		int offset = appendString("sdfs".getBytes(), 10, buffer, 4);
		System.out.println(offset);
		System.out.println(byte2int(buffer, 0));
		System.out.println(new String(removeString(buffer, 4)));

	}

	public static byte[] rfillBytes(byte[] src, int len) {

		if (src.length == len) {
			return src;
		} else if (src.length > len) {
			byte[] tmp = new byte[len];
			System.arraycopy(src, 0, tmp, 0, len);
			return tmp;
		} else {
			byte[] tmp = new byte[len];
			System.arraycopy(src, 0, tmp, 0, src.length);
			for (int i = src.length; i < len; i++) {
				tmp[i] = 0;
			}
			return tmp;
		}

	}

	public static void rfillBytes(byte[] src, int len, byte[] bytes, int offset) {

		if (src.length == len) {
			System.arraycopy(src, 0, bytes, offset, len);
		} else if (src.length > len) {

			System.arraycopy(src, 0, bytes, offset, len);

		} else {

			System.arraycopy(src, 0, bytes, offset, src.length);
			for (int i = offset + src.length; i < offset + len; i++) {
				bytes[i] = 0;
			}

		}

	}

	public static int appendString(byte[] src, int maxLen, byte[] bytes, int offset) {

		if (src.length >= maxLen) {
			System.arraycopy(src, 0, bytes, offset, maxLen - 1);
			offset += maxLen - 1;
			bytes[offset++] = 0;
		} else {

			System.arraycopy(src, 0, bytes, offset, src.length);
			offset += src.length;
			bytes[offset++] = 0;
		}
		return offset;
	}

	public static byte[] removeString(byte[] buffer, int offset) {

	    int len=0;    
	    while(offset+len< buffer.length  && buffer[offset+len]!=0){
	    	len++; 	
	    }
       
	    byte[] bytes=null;
	    if(offset+len<buffer.length)
	        bytes=new byte[len+1];
	    else
	    	bytes=new byte[len];
	    System.arraycopy(buffer, offset, bytes, 0, len);
	    return bytes;	
	}

	public static byte[] lfillBytes(byte[] src, int len) {

		if (src.length == len) {
			return src;
		} else if (src.length > len) {
			byte[] tmp = new byte[len];
			System.arraycopy(src, 0, tmp, 0, len);
			return tmp;
		} else {
			byte[] tmp = new byte[len];
			for (int i = 0; i < len - src.length; i++) {
				tmp[i] = 0;
			}
			System.arraycopy(src, 0, tmp, len - src.length, src.length);
			return tmp;
		}

	}

	public static void lfillBytes(byte[] src, int len, byte[] dest, int offset) {

		if (src.length == len) {
			System.arraycopy(src, 0, dest, offset, len);
		} else if (src.length > len) {

			System.arraycopy(src, 0, dest, offset, len);

		} else {

			for (int i = offset; i < offset + len - src.length; i++) {
				dest[i] = 0;
			}
			System.arraycopy(src, 0, dest, offset + len - src.length, src.length);

		}

	}

	public static void printBytes(byte[] bytes) {
		System.out.print("bytes=[");
		for (int i = 0; i < bytes.length; i++) {
			if (i == 0) {
				System.out.print(bytes[i]);
			} else {
				System.out.print("," + bytes[i]);
			}
		}
		System.out.println("]");
	}

	public static byte[] rtrimBytes(byte[] src) {

		int i = src.length - 1;
		for (; i >= 0; i--) {
			if (src[i] != 0) {
				break;
			}
		}
		if (i == src.length-1) {
			return src;
		}
		if( i == -1){
			return new byte[0];
		}
		byte[] tmp = new byte[i + 1];
		System.arraycopy(src, 0, tmp, 0, i + 1);
		return tmp;
	}

	public static byte[] ltrimBytes(byte[] src) {

		int i = 0;
		for (; i < src.length; i++) {
			if (src[i] != 0) {
				break;
			}
		}
		if (i == src.length) {
			return new byte[0];
		};
		
		if (i == 0) {
			return src;
		};
		
		byte[] tmp = new byte[src.length - i];
		System.arraycopy(src, i, tmp, 0, src.length - i);
		return tmp;
	}

	private static String HexCode[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	public static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return HexCode[d1] + HexCode[d2];
	}

	public static short byte2short(byte b[], int offset) {
		return (short) ((b[offset + 1] & 0xff) | (b[offset + 0] & 0xff) << 8);
	}

	public static short byte2short(byte b[]) {
		return (short) ((b[1] & 0xff) | (b[0] & 0xff) << 8);
	}

	public static int byte2int(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16
				| (b[offset] & 0xff) << 24;
	}

	public static int byte2int(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
	}

	public static long byte2long(byte b[]) {
		return (long) b[7] & (long) 255 | ((long) b[6] & (long) 255) << 8 | ((long) b[5] & (long) 255) << 16
				| ((long) b[4] & (long) 255) << 24 | ((long) b[3] & (long) 255) << 32
				| ((long) b[2] & (long) 255) << 40 | ((long) b[1] & (long) 255) << 48 | (long) b[0] << 56;
	}

	public static long byte2long(byte b[], int offset) {
		return (long) b[offset + 7] & (long) 255 | ((long) b[offset + 6] & (long) 255) << 8
				| ((long) b[offset + 5] & (long) 255) << 16 | ((long) b[offset + 4] & (long) 255) << 24
				| ((long) b[offset + 3] & (long) 255) << 32 | ((long) b[offset + 2] & (long) 255) << 40
				| ((long) b[offset + 1] & (long) 255) << 48 | (long) b[offset] << 56;
	}

	public static byte[] int2byte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

	public static void int2byte(int n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 24);
		buf[offset + 1] = (byte) (n >> 16);
		buf[offset + 2] = (byte) (n >> 8);
		buf[offset + 3] = (byte) n;
	}

	public static byte[] short2byte(int n) {
		byte b[] = new byte[2];
		b[0] = (byte) (n >> 8);
		b[1] = (byte) n;
		return b;
	}

	public static void short2byte(int n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 8);
		buf[offset + 1] = (byte) n;
	}

	public static byte[] long2byte(long n) {
		byte b[] = new byte[8];
		b[0] = (byte) (int) (n >> 56);
		b[1] = (byte) (int) (n >> 48);
		b[2] = (byte) (int) (n >> 40);
		b[3] = (byte) (int) (n >> 32);
		b[4] = (byte) (int) (n >> 24);
		b[5] = (byte) (int) (n >> 16);
		b[6] = (byte) (int) (n >> 8);
		b[7] = (byte) (int) n;
		return b;
	}

	public static void long2byte(long n, byte buf[], int offset) {
		buf[offset] = (byte) (int) (n >> 56);
		buf[offset + 1] = (byte) (int) (n >> 48);
		buf[offset + 2] = (byte) (int) (n >> 40);
		buf[offset + 3] = (byte) (int) (n >> 32);
		buf[offset + 4] = (byte) (int) (n >> 24);
		buf[offset + 5] = (byte) (int) (n >> 16);
		buf[offset + 6] = (byte) (int) (n >> 8);
		buf[offset + 7] = (byte) (int) n;
	}

	public static boolean bytesEquals(byte[] bytes1, byte[] bytes2) {
		if (bytes1 == null && bytes2 == null) {
			return true;
		}
		if (bytes1 == bytes2) {
			return true;
		}
		if (bytes1 == null || bytes2 == null) {
			return false;
		}
		if (bytes1.length != bytes2.length) {
			return false;
		}
		for (int i = 0; i < bytes1.length; i++) {
			if (bytes1[i] != bytes2[i]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] convertAndFill(long msgId) {
		byte[] bytes = new byte[10];
		System.arraycopy(ByteUtil.long2byte(msgId), 0, bytes, 0, 8);
		return bytes;
	}

	public static long trimAndConvert(byte[] bytes) {
		byte[] tmp = new byte[8];
		System.arraycopy(bytes, 0, tmp, 0, 8);
		return ByteUtil.byte2long(tmp);
	}

}
