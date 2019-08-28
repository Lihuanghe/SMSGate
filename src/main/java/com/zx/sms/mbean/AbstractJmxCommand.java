package com.zx.sms.mbean;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public abstract class AbstractJmxCommand {
	private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private static String JAVA_HOME = System.getProperty("java.home");
	
	private static String JVM_SUPPLIER = System.getProperty("java.vm.specification.vendor");

	private static final String CLASS_VIRTUAL_MACHINE = "com.sun.tools.attach.VirtualMachine";

	private static final String CLASS_VIRTUAL_MACHINE_DESCRIPTOR = "com.sun.tools.attach.VirtualMachineDescriptor";

	private static final String CLASS_JMX_REMOTE = "com.sun.management.jmxremote";
		
	private static  URLClassLoader classLoader;
	
	static {
		
		try {
			classLoader = new URLClassLoader(new URL[] { getToolsJar().toURI().toURL() });
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String findJMXUrlByProcessId(int pid) {

		if (!isSunJVM() || null == classLoader) {
			return "";
		}

		String connectorAddress = "";
		
		Object targetVm = null;
		Method attachToVM = null;
		Method detach = null;

		try {

			Class virtualMachine = Class.forName(CLASS_VIRTUAL_MACHINE, true, classLoader);
			Class virtualMachineDescriptor = Class.forName(CLASS_VIRTUAL_MACHINE_DESCRIPTOR, true, classLoader);

			Method getVMList = virtualMachine.getMethod("list", (Class[]) null);
			attachToVM = virtualMachine.getMethod("attach", String.class);
			detach = virtualMachine.getMethod("detach", (Class[]) null);
			Method getAgentProperties = virtualMachine.getMethod("getAgentProperties", (Class[]) null);
			Method getVMId = virtualMachineDescriptor.getMethod("id", (Class[]) null);
			

			List allVMs = (List) getVMList.invoke(null, (Object[]) null);

			for (Object vmInstance : allVMs) {
				String id = (String) getVMId.invoke(vmInstance, (Object[]) null);
				if (id.equals(Integer.toString(pid))) {

					try {
                        targetVm = attachToVM.invoke(null, id);
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }

					Properties agentProperties = (Properties) getAgentProperties.invoke(targetVm, (Object[]) null);
					connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);
					break;
				}
			}

			if (connectorAddress == null ||"".equals(connectorAddress)) {
				// 尝试让agent加载management-agent.jar
				Method loadAgent = virtualMachine.getMethod("loadAgent", String.class, String.class);
				
				for (Object vmInstance : allVMs) {
					String id = (String) getVMId.invoke(vmInstance, (Object[]) null);
					if (id.equals(Integer.toString(pid))) {

						targetVm = attachToVM.invoke(null, id);

						File agentJar = getAgentJar();
						if (null == agentJar) {
							throw new IOException("Management agent Jar not found");
						}

						String agent = agentJar.getCanonicalPath();
						loadAgent.invoke(targetVm, agent, CLASS_JMX_REMOTE);

						Properties agentProperties = (Properties) getAgentProperties.invoke(targetVm, (Object[]) null);
						connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);

						break;
					}
				}
			}

		} catch (Exception ignore) {
			System.err.println(ignore);
		}finally {
			if (null != targetVm && null != detach) {
				try {
					detach.invoke(targetVm, (Object[]) null);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}

		return connectorAddress;
	}

	private static File getToolsJar() {
		String tools = JAVA_HOME + File.separator + "lib" + File.separator + "tools.jar";
		File f = new File(tools);
		if (!f.exists()) {
			tools = JAVA_HOME + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
			f = new File(tools);
		}
		return f;
	}

	private static File getAgentJar() {
		String agent = JAVA_HOME + File.separator + "jre" + File.separator + "lib" + File.separator + "management-agent.jar";
		File f = new File(agent);
		if (!f.exists()) {
			agent = JAVA_HOME + File.separator + "lib" + File.separator + "management-agent.jar";
			f = new File(agent);
			if (!f.exists()) {
				return null;
			}
		}
		return f;
	}

	private static boolean isSunJVM() {
		return JVM_SUPPLIER.equals("Sun Microsystems Inc.") || JVM_SUPPLIER.startsWith("Oracle");
	}

	abstract protected void invoke(MBeanServerConnection mconn, String[] args);

	public void main0(String[] args) throws IOException {
		int pid = Integer.parseInt(args[0]);
		String connstr = findJMXUrlByProcessId(pid);
		System.out.println("Connect to JMXUrl :"+ connstr+"\n");
		if (connstr != null && (!"".equals(connstr))) {
			JMXServiceURL url = new JMXServiceURL(connstr);
			JMXConnector connector = JMXConnectorFactory.connect(url);
			try {
				MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
				if(args.length > 1){
					String[] param = new String[args.length - 1];
					System.arraycopy(args, 1, param, 0, args.length - 1);
					invoke(mbeanConn, param);
				}else{
					invoke(mbeanConn, new String[]{""});
				}
			} finally {
				connector.close();
			}
		} else {
			System.out.println("process " + pid + " does not exists.");
		}

	}

}
