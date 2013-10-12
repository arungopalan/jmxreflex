jmxreflex
=========

A command line interface for calling methods exposed via JMX.

Note that in the usage example below, JAVA_HOME should point to JDK and NOT JRE. tools.jar is included in JDK under libs directory.

<pre>
<code>
usage: java -cp "$JAVA_HOME\lib\tools.jar:lib\commons-cli-1.2:jmxreflex.jar com.reflexactiongames.jmxreflex.JmxReflexMain <option>
 -a <arg>     A comma separated value of arguments to pass to the JMX
              method. Only string values are supported
 -b <arg>     The identifier for the mbean
 -c <arg>     The main class identifying the process. If there multiple
              processes running with this mail class then the JMX method
              will be called on each instance.
 -h           Prints this help
 -m <arg>     The method to invoke on the mbean
 -pid <arg>   The process id of the Java process to connect to. This can
              be done only if the java process is running on the same
              machine and was started by the same user. See JMX
              documentation for more detail.
 -u <arg>     The JMX URL to connect to
</code>
</pre>
