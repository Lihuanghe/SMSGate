package com.zx.sms.common;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.zx.sms.mbean.AbstractJmxCommand;

public class TestAbstractJmxCommand extends AbstractJmxCommand {

	protected void invoke(MBeanServerConnection mconn,String[] args)
	{
		 try {
			ObjectName stat = new ObjectName("com.zx.sms:name=ConnState");
			System.out.println(mconn.invoke(stat, "print",args, new String[]{"java.lang.String"}));
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException{
		TestAbstractJmxCommand c = new TestAbstractJmxCommand();
		 c.main0(args);
	 }
}
