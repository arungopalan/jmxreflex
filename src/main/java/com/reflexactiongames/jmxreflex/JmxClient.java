package com.reflexactiongames.jmxreflex;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JmxClient {

	private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private JmxClient() {
		// All methods are static!
	}

	public static void invokeByPid(String pid, String beanName, String methodName, Object[] params) throws Exception {
		invokeByMainClassOrPid(null, pid, beanName, methodName, params);
	}

	public static void invokeByUrl(String url1, String beanName, String methodName, Object[] params) throws Exception {
		JMXServiceURL jmxServiceUrl = new JMXServiceURL(url1);
		JMXConnector connector = null;

		try {
			connector = JMXConnectorFactory.connect(jmxServiceUrl);
			MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
			ObjectName mbeanName = new ObjectName(beanName);
			mbeanConn.invoke(mbeanName, methodName, params, null);

		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (Exception e) {

				}
			}
		}
	}

	public static void invokeByMainClass(String mainClassName, String beanName, String methodName, Object[] params)
			throws Exception {
		invokeByMainClassOrPid(mainClassName, null, beanName, methodName, params);
	}

	private static void invokeByMainClassOrPid(String mainClassName, String pid, String beanName, String methodName,
			Object[] params) throws Exception {

		if (mainClassName == null && pid == null) {
			throw new RuntimeException("Either main class or pid must be passed.");
		}

		if (mainClassName != null && pid != null) {
			throw new RuntimeException("Only one of mainClassName or pid should be passed.");
		}

		List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();

		boolean serverFound = false;

		for (VirtualMachineDescriptor desc : virtualMachineDescriptors) {

			if (desc.displayName().equals(mainClassName) || desc.id().equals(pid)) {

				serverFound = true;

				VirtualMachine virtualMachine = VirtualMachine.attach(desc);
				Properties props = virtualMachine.getAgentProperties();
				String connectorAddress = props.getProperty(CONNECTOR_ADDRESS);

				// no connector address, so we start the JMX agent
				if (connectorAddress == null) {
					String agent = virtualMachine.getSystemProperties().getProperty("java.home") + File.separator
							+ "lib" + File.separator + "management-agent.jar";
					virtualMachine.loadAgent(agent);

					// agent is started, get the connector address
					connectorAddress = virtualMachine.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
					assert connectorAddress != null;
				}

				JMXServiceURL url = new JMXServiceURL(connectorAddress);
				JMXConnector connector = null;

				try {
					connector = JMXConnectorFactory.connect(url);
					MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();

					ObjectName mbeanName = new ObjectName(beanName);
					mbeanConn.invoke(mbeanName, methodName, params, null);

				} finally {
					if (connector != null) {
						try {
							connector.close();
						} catch (Exception e) {

						}
					}
				}
			}

		}

		if (!serverFound) {
			throw new Exception("No server found");
		}

	}

}
