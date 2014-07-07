For build project, we use gradle build tool (version 2.+). For more information, see  http://www.gradle.org/.

Display all tasks: 

gradle tasks

To build search.war:

gradle :search:clean :search:build


Creating full distribution (contains rightseditor,editor,  K4 app and javadocs ):

gradle distTar -> creates tar.gz file
gradle distZip -> creates zip file

Note: It is expected that you will have rightseditor and editor wars in your m2 repo.

Creating patch distribution (k4 app only)

gradle  patchDistTar
gradle  patchDistZip

