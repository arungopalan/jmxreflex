package com.reflexactiongames.jmxreflex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
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

    public static String invokeByPid(String pid, String beanName, String methodName, Object[] params, String[] signature) throws Exception {
        return invokeByMainClassOrPid(null, pid, beanName, methodName, params, signature);
    }

    public static String invokeByUrl(String url1, String beanName, String methodName, Object[] params, String[] signature) throws Exception {
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url1);
        JMXConnector connector = null;

        try {
            connector = JMXConnectorFactory.connect(jmxServiceUrl);
            MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
            ObjectName mbeanName = new ObjectName(beanName);

            Object returnValue = mbeanConn.invoke(mbeanName, methodName, params, signature);
            if (returnValue == null) {
                return null;
            } else {
                return String.valueOf(returnValue);
            }

        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (Exception e) {

                }
            }
        }
    }

    public static String invokeByMainClass(String mainClassName, String beanName, String methodName, Object[] params, String[] signature)
            throws Exception {
        return invokeByMainClassOrPid(mainClassName, null, beanName, methodName, params, signature);
    }

    private static String invokeByMainClassOrPid(String mainClassName, String pid, String beanName, String methodName, Object[] params,
            String[] signature) throws Exception {

        if (mainClassName == null && pid == null) {
            throw new RuntimeException("Either main class or pid must be passed.");
        }

        if (mainClassName != null && pid != null) {
            throw new RuntimeException("Only one of mainClassName or pid should be passed.");
        }

        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();

        for (VirtualMachineDescriptor desc : virtualMachineDescriptors) {

            if (desc.displayName().equals(mainClassName) || desc.id().equals(pid)) {

                VirtualMachine virtualMachine = VirtualMachine.attach(desc);
                Properties props = virtualMachine.getAgentProperties();
                String connectorAddress = props.getProperty(CONNECTOR_ADDRESS);

                // no connector address, so we start the JMX agent
                if (connectorAddress == null) {
                    String agent = virtualMachine.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator
                            + "management-agent.jar";
                    virtualMachine.loadAgent(agent);

                    // agent is started, get the connector address
                    connectorAddress = virtualMachine.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                    assert connectorAddress != null;
                }

                return invokeByUrl(connectorAddress, beanName, methodName, params, signature);

            }

        }

        throw new Exception("No server found.");

    }

    public static List<String> listBeansByPid(String pid) throws Exception {
        return listBeansByPidOrMainClass(null, pid);
    }

    private static List<String> listBeansByPidOrMainClass(String mainClassName, String pid) throws Exception {

        if (mainClassName == null && pid == null) {
            throw new RuntimeException("Either main class or pid must be passed.");
        }

        if (mainClassName != null && pid != null) {
            throw new RuntimeException("Only one of mainClassName or pid should be passed.");
        }

        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();

        for (VirtualMachineDescriptor desc : virtualMachineDescriptors) {

            if (desc.displayName().equals(mainClassName) || desc.id().equals(pid)) {

                VirtualMachine virtualMachine = VirtualMachine.attach(desc);
                Properties props = virtualMachine.getAgentProperties();
                String connectorAddress = props.getProperty(CONNECTOR_ADDRESS);

                // no connector address, so we start the JMX agent
                if (connectorAddress == null) {
                    String agent = virtualMachine.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator
                            + "management-agent.jar";
                    virtualMachine.loadAgent(agent);

                    // agent is started, get the connector address
                    connectorAddress = virtualMachine.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                    assert connectorAddress != null;
                }

                return listBeansByUrl(connectorAddress);

            }

        }

        throw new Exception("No server found.");

    }

    public static List<String> listBeansByUrl(String connectorAddress) throws Exception {
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(connectorAddress);
        JMXConnector connector = null;

        try {
            connector = JMXConnectorFactory.connect(jmxServiceUrl);
            MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
            Set<ObjectInstance> set = mbeanConn.queryMBeans(null, null);
            List<String> beanNames = new ArrayList<>();
            for (ObjectInstance objectInstance : set) {
                beanNames.add(objectInstance.getObjectName().getCanonicalName());
            }
            return beanNames;

        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (Exception e) {

                }
            }
        }

    }

    public static List<String> listBeansByMainClass(String mainClassName) throws Exception {
        return listBeansByPidOrMainClass(mainClassName, null);
    }

}
