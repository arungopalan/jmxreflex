package com.reflexactiongames.jmxreflex;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class Main {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {

		// create Options object
		Options options = new Options();
		Option urlOption = OptionBuilder.withArgName("u").hasArg().withDescription("The JMX URL to connect to")
				.create("u");
		options.addOption(urlOption);

		Option processIdOption = OptionBuilder
				.withArgName("p")
				.hasArg()
				.withDescription(
						"The process id of the Java process to connect to. "
								+ "This can be done only if the java process is running on the same machine and was started by the same user. "
								+ "See JMX documentation for more detail.").create("p");
		options.addOption(processIdOption);

		Option mainClassOption = OptionBuilder.withArgName("c").hasArg().withDescription("The name of the main class")
				.create("c");
		options.addOption(mainClassOption);

		Option mbeanNameOption = OptionBuilder.withArgName("b").hasArg().withDescription("The name of the mbean")
				.create("b");
		options.addOption(mbeanNameOption);

		Option methodNameOption = OptionBuilder.withArgName("m").hasArg()
				.withDescription("The method to invoke on the mbean").create("m");
		options.addOption(methodNameOption);

		Option argumentsOption = OptionBuilder
				.withArgName("a")
				.hasArg()
				.withDescription(
						"A comma separated value of arguments to pass to the method. Only string values are supported")
				.create("a");
		options.addOption(argumentsOption);

		CommandLineParser parser = new BasicParser();

		CommandLine commandLine = parser.parse(options, args);

		String url = null, pid = null, mainClassName = null, beanName = null, methodName = null;

		int connectionParams = 0;

		if (commandLine.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar jmxreflex.jar", options);
		}

		if (commandLine.hasOption("u")) {
			url = commandLine.getOptionValue("u");
			connectionParams++;
		}

		if (commandLine.hasOption("p")) {
			pid = commandLine.getOptionValue("p");
			connectionParams++;
		}

		if (commandLine.hasOption("c")) {
			mainClassName = commandLine.getOptionValue("c");
			connectionParams++;
		}

		if (connectionParams == 0) {
			System.err.print("You must pass either URL, PID or Main class");
			System.exit(1);
		}

		if (connectionParams > 1) {
			System.err.print("Please pass only one option between URL, PID or Main class name.");
			System.exit(1);
		}

		if (commandLine.hasOption("b")) {
			beanName = commandLine.getOptionValue("b");
		} else {
			System.err.print("Bean name is required");
			System.exit(1);
		}

		if (commandLine.hasOption("m")) {
			beanName = commandLine.getOptionValue("m");
		} else {
			System.err.print("Method name is required");
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
			JmxClient.invokeByMainClass(mainClassName, beanName, methodName, params);
		} else if (pid != null) {
			JmxClient.invokeByPid(pid, beanName, methodName, params);
		} else {
			JmxClient.invokeByUrl(url, beanName, methodName, params);
		}
	}
}
