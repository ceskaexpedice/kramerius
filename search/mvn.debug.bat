
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=9999,suspend=n,server=y -Xms256m -Xmx512m -XX:MaxPermSize=128m

mvn tomcat:run
