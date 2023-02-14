package com.zx.sms.common.storedMap;

import java.io.Serializable;

import com.zx.sms.common.util.CachedMillisecondClock;

public class VersionObject<T extends Serializable> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6721353360960222853L;
	private T obj;
	private long version;
	public VersionObject(T obj){
		this.obj = obj;
		this.version = CachedMillisecondClock.INS.now();
	}
	
	public T getObj() {
		return obj;
	}
	public long getVersion() {
		return version;
	}
	
}
