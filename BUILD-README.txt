For build project, we use gradle build tool (version 2.+). For more information, see  http://www.gradle.org/.

Display all tasks: 

gradle tasks

To build search.war:

gradle :search:clean :search:build

To build client.war:

gradle :client:clean :client:build


Creating full distribution (contains rightseditor,editor, K5, Client and javadocs ):

gradle clean build distZip -> creates zip file

Note: It is expected you have rightseditor and editor wars in your m2 repo.

Creating patch distribution (K5 core, K5 Client, security-core only)

gradle  clean build distPatchZip



