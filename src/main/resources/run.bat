start javaw -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n -Dhttp.agent=Mozilla -server -Xms100m -Xmx100m -cp . -jar gpw-monitor-1.8.5.jar
