package com.zx.sms.codec.cmpp7F;

import java.io.Serializable;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public abstract class AttachObjectSerializeUtil {
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration(); 
	
	public static byte[] write(Serializable obj) throws Exception{
		ByteArrayOutputStream arroutput = new ByteArrayOutputStream();
		FSTObjectOutput objoutput = conf.getObjectOutput(arroutput);
		try{
			objoutput.writeObject(obj);
			objoutput.flush();
			return arroutput.toByteArray();
		}finally{
			objoutput.close();
		}
	}
	
	public static Serializable read(byte[] bytes)  throws Exception{
		FSTObjectInput objinput = conf.getObjectInput(bytes);
		try{
			Object t = objinput.readObject();
			if(t instanceof Serializable){
				return (Serializable)t;
			}else{
				return null;
			}
		}finally{
			objinput.close();
		}
	}
}
