For build project, we use gradle build tool (version 5.+). For more information, see  http://www.gradle.org/.
You should always use embedded version of gradle, i.e. ./gradlew task NOT gradle task. Because gradle's behaviour can differ between it's versions,
which can result in failure to successfully execute some of the tasks.

Display all tasks: 

./gradlew tasks

To build search.war:

./gradlew :search:clean :search:build


Creating full distribution (contains rightseditor,editor, K5, Client and javadocs ):

./gradlew clean build distZip -> creates zip file

Note: It is expected you have rightseditor and editor wars in your m2 manager.

Creating patch distribution (K5 core, K5 Client, security-core only)

./gradlew clean build distPatchZip


To publish all artifacts to github repository:

1. Put the following authentication properties to ~/.gradle/gradle.properties:
githubRepositoryToken=your github personal authentication token (generate in Github Profile - Settings -> Developer Settings -> Personal Access Tokens)
githubRepositoryUser=your user name

2. Set the project version in project_root/gradle.properties

3. ./gradlew publish





