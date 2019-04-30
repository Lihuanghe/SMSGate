package com.zx.sms.common.util;

import java.io.Serializable;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public abstract class FstObjectSerializeUtil {
	
	private final static ThreadLocal<FSTConfiguration> conf = new ThreadLocal<FSTConfiguration>() { 
	    public FSTConfiguration initialValue() {
	        return FSTConfiguration.createDefaultConfiguration();
	    }
	};
	
	public static byte[] write(Serializable obj) throws Exception{
		ByteArrayOutputStream arroutput = new ByteArrayOutputStream();
		FSTObjectOutput objoutput = conf.get().getObjectOutput(arroutput);
		try{
			objoutput.writeObject(obj);
			objoutput.flush();
			return arroutput.toByteArray();
		}finally{
			arroutput.close();
		}
	}
	
	public static Serializable read(byte[] bytes)  throws Exception{
		FSTObjectInput objinput = conf.get().getObjectInput(bytes);
		try{
			Object t = objinput.readObject();
			if(t instanceof Serializable){
				return (Serializable)t;
			}else{
				return null;
			}
		}catch(Exception ex) {
			return null;
		}finally{
			
		}
	}
}
