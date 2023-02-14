package com.zx.sms.common.storedMap;

import java.io.Serializable;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.util.RuntimeExceptionWrapper;
import com.zx.sms.common.util.FstObjectSerializeUtil;

public class FstSerialBinding<E extends Serializable> implements EntryBinding<E> {
	private final static byte[] ZERO_LENGTH_BYTE_ARRAY = new byte[0];
	private ByteArrayBinding bb = new ByteArrayBinding();
	
		@Override
	public E entryToObject(DatabaseEntry entry) {
		byte[] data = bb.entryToObject(entry);
		if(data ==null || data.length ==0) return null;
		try{
			return (E)FstObjectSerializeUtil.read(data);
		}catch(Exception ex){
			throw RuntimeExceptionWrapper.wrapIfNeeded(ex);
		}
	}

	@Override
	public void objectToEntry(E object, DatabaseEntry entry) {
		if(object==null){
			bb.objectToEntry(ZERO_LENGTH_BYTE_ARRAY, entry);
		}else{
			try{
				bb.objectToEntry(FstObjectSerializeUtil.write(object), entry);
			}catch(Exception ex){
				throw RuntimeExceptionWrapper.wrapIfNeeded(ex);
			}
		}
	}
}
