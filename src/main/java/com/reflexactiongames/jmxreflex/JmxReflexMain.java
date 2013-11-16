package com.reflexactiongames.jmxreflex;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class JmxReflexMain {

	public static void main(String[] args) throws Exception {

		// create Options object
		Options options = new Options();

		options.addOption("h", false, "Prints this help");
		options.addOption("u", true, "The JMX URL to connect to");
		options.addOption(
				"pid",
				true,
				"The process id of the Java process to connect to. "
						+ "This can be done only if the java process is running on the same machine and was started by the same user. "
						+ "See JMX documentation for more detail.");
		options.addOption(
				"c",
				true,
				"The main class identifying the process. If there multiple processes running with this mail class then the JMX method will be called on each instance.");

		options.addOption("b", true, "The identifier for the mbean");

		options.addOption("m", true, "The method to invoke on the mbean");

		options.addOption("a", true,
				"A comma separated value of arguments to pass to the JMX method. Only string values are supported");

		options.addOption("s", true, "A comma separated values of class names to identify the signature.");

		CommandLineParser parser = new BasicParser();

		CommandLine commandLine = parser.parse(options, args);

		String url = null, pid = null, mainClassName = null, beanName = null, methodName = null;

		int connectionParams = 0;

		if (commandLine.hasOption("h")) {
			showHelp(options);
			return;
		}

		if (commandLine.hasOption("u")) {
			url = commandLine.getOptionValue("u");
			connectionParams++;
		}

		if (commandLine.hasOption("pid")) {
			pid = commandLine.getOptionValue("pid");
			connectionParams++;
		}

		if (commandLine.hasOption("c")) {
			mainClassName = commandLine.getOptionValue("c");
			connectionParams++;
		}

		String s = "";
		if (commandLine.hasOption("s")) {
			s = commandLine.getOptionValue("s");
		}
		String[] signature = s.split(",");

		if (connectionParams == 0) {
			System.err.println("You must pass either URL, PID or Main class");
			showHelp(options);
			System.exit(1);
		}

		if (connectionParams > 1) {
			System.err.println("Please pass only one option between URL, PID or Main class name.");
			showHelp(options);
			System.exit(1);
		}

		if (commandLine.hasOption("b")) {
			beanName = commandLine.getOptionValue("b");
		} else {
			System.err.println("Bean object name is required");
			showHelp(options);
			System.exit(1);
		}

		if (commandLine.hasOption("m")) {
			methodName = commandLine.getOptionValue("m");
		} else {
			System.err.println("Method name is required");
			showHelp(options);
			System.exit(1);
		}

		Object[] params = null;
		if (commandLine.hasOption("a")) {
			String argumentsCsv = commandLine.getOptionValue("a");
			String[] arguments = argumentsCsv.split(",");
			params = new Object[arguments.length];
			int index = 0;
			for (String argument : arguments) {
				params[index] = argument;
			}
		}

		if (mainClassName != null) {
			JmxClient.invokeByMainClass(mainClassName, beanName, methodName, params, signature);
		} else if (pid != null) {
			JmxClient.invokeByPid(pid, beanName, methodName, params, signature);
		} else {
			JmxClient.invokeByUrl(url, beanName, methodName, params, signature);
		}
	}

	private static final void showHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java -cp \"$JAVA_HOME\\lib\\tools.jar:lib\\commons-cli-1.2:jmxreflex.jar\" com.reflexactiongames.jmxreflex.JmxReflexMain <options>",
						options);
	}

}
