package com.zx.sms.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;

public class IPRange {

	private InetAddress inetAddress;
	
	private String mask="";

	public IPRange(String ip) throws UnknownHostException {
		int idx = ip.indexOf('/');
		String address = null;
		String mask = null;
		if (idx > 0 && idx < ip.length() - 1) {
			address = ip.substring(0, idx);
			mask = ip.substring(idx + 1);
		} else if (idx < 0) {
			address = ip;
		} else {
			throw new IllegalArgumentException("not an valid ip format!");
		}
		this.inetAddress = InetAddress.getByName(address);
		if (StringUtils.isNotBlank(mask)) {
			this.mask = mask;
			this.inetAddress = gennetworkPart(this.inetAddress,Byte.valueOf(mask));
		}
	}
	
	private InetAddress gennetworkPart(InetAddress src,byte bmask) throws UnknownHostException {
		byte[] addr = src.getAddress();
		int pos = bmask / 8;
		int bitp = bmask % 8;

		if (pos > addr.length)
			throw new IllegalArgumentException("not an valid mask value :" + mask);

		byte[] result = new byte[addr.length];
		for (int i = 0; i < pos; i++) {
			result[i] = addr[i];
		}
		if (bitp > 0) {
			byte tmpmask = (byte) (((byte) 0xff) << (8 - bitp));
			result[pos] =(byte) (addr[pos] & tmpmask);
		}
		return InetAddress.getByAddress(result);
	}
	public boolean isInRange(String src) {
		try {
			return isInRange(InetAddress.getByName(src));
		}catch(Exception e) {
			return false;
		}
	}
	public boolean isInRange(InetAddress src) {
		try {
		if(StringUtils.isBlank(this.mask))
			return inetAddress.equals(src);
		else
			return inetAddress.equals(gennetworkPart(src,Byte.valueOf(mask).byteValue()));
		}catch(Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IPRange [inetAddress=");
		builder.append(inetAddress);
		builder.append(", mask=");
		builder.append(mask);
		builder.append("]");
		return builder.toString();
	}
	
}
