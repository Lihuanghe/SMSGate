package com.zx.sms.mbean;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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

	public static String getJVM() {
		return System.getProperty("java.vm.specification.vendor");
	}

	public static boolean isSunJVM() {
		// need to check for Oracle as that is the name for Java7 onwards.
		return getJVM().equals("Sun Microsystems Inc.") || getJVM().startsWith("Oracle");
	}

	abstract protected void invoke(MBeanServerConnection mconn, String[] args);

	public void main0(String[] args) throws IOException {
		int pid = Integer.valueOf(args[0]);
		String connstr = findJMXUrlByProcessId(pid);
		if (connstr != null) {
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

	/**
	 * Finds the JMX Url for a VM by its process id
	 *
	 * @param pid
	 *            The process id value of the VM to search for.
	 *
	 * @return the JMX Url of the VM with the given pid or null if not found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String findJMXUrlByProcessId(int pid) {

		if (isSunJVM()) {
			try {
				// Classes are all dynamically loaded, since they are specific
				// to Sun VM
				// if it fails for any reason default jmx url will be used

				// tools.jar are not always included used by default class
				// loader, so we
				// will try to use custom loader that will try to load tools.jar

				String javaHome = System.getProperty("java.home");
				String tools = javaHome + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
				URLClassLoader loader = new URLClassLoader(new URL[] { new File(tools).toURI().toURL() });

				Class virtualMachine = Class.forName("com.sun.tools.attach.VirtualMachine", true, loader);
				Class virtualMachineDescriptor = Class.forName("com.sun.tools.attach.VirtualMachineDescriptor", true, loader);

				Method getVMList = virtualMachine.getMethod("list", (Class[]) null);
				Method attachToVM = virtualMachine.getMethod("attach", String.class);
				Method getAgentProperties = virtualMachine.getMethod("getAgentProperties", (Class[]) null);
				Method getVMId = virtualMachineDescriptor.getMethod("id", (Class[]) null);

				List allVMs = (List) getVMList.invoke(null, (Object[]) null);

				for (Object vmInstance : allVMs) {
					String id = (String) getVMId.invoke(vmInstance, (Object[]) null);
					if (id.equals(Integer.toString(pid))) {

						Object vm = attachToVM.invoke(null, id);

						Properties agentProperties = (Properties) getAgentProperties.invoke(vm, (Object[]) null);
						String connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);

						if (connectorAddress != null) {
							return connectorAddress;
						} else {
							break;
						}
					}
				}
			} catch (Exception ignore) {
			}
		}

		return null;
	}
}
