package com.zx.sms.codec.cmpp7F;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.io.output.ByteArrayOutputStream;

public abstract class AttachObjectSerializeUtil {
	public static byte[] write(Serializable obj) throws Exception{
		ByteArrayOutputStream arroutput = new ByteArrayOutputStream();
		ObjectOutputStream objoutput = new ObjectOutputStream(arroutput);
		try{
			objoutput.writeObject(obj);
			return arroutput.toByteArray();
		}finally{
			objoutput.close();
		}
	}
	
	public static Serializable read(byte[] bytes)  throws Exception{
		ByteArrayInputStream arrinput = new ByteArrayInputStream(bytes);
		ObjectInputStream objinput = new ObjectInputStream(arrinput);
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
