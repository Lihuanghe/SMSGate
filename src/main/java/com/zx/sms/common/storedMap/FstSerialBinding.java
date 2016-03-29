package com.zx.sms.common.storedMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.util.RuntimeExceptionWrapper;

public class FstSerialBinding<E> implements EntryBinding<E> {

	private ByteArrayBinding bb = new ByteArrayBinding();
	
	private static ThreadLocal<FSTConfiguration> conf = new ThreadLocal<FSTConfiguration>() { 
	    public FSTConfiguration initialValue() {
	        return FSTConfiguration.createDefaultConfiguration();
	    }
	};
	
	@Override
	public E entryToObject(DatabaseEntry entry) {
		byte[] data = bb.entryToObject(entry);
		if(data.length ==0) return null;
		
		FSTObjectInput input = conf.get().getObjectInput(data);
		try{
			return (E)input.readObject();
		}catch(Exception ex){
			throw RuntimeExceptionWrapper.wrapIfNeeded(ex);
		}
	}

	@Override
	public void objectToEntry(E object, DatabaseEntry entry) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		FSTObjectOutput output = conf.get().getObjectOutput(bytes);
		try{
			output.writeObject(object);
			output.flush();
			bb.objectToEntry(bytes.toByteArray(), entry);
		}catch(Exception ex){
			throw RuntimeExceptionWrapper.wrapIfNeeded(ex);
		}finally{
			try {
				bytes.close();
			} catch (IOException e) {}
		}
	}
}
