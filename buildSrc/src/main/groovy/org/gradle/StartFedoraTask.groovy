package org.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction

/**
 * Created by pstastny on 10/20/2017.
 */
class StartFedoraTask extends  DefaultTask {

    @TaskAction
    protected void exec() {
        FedoraExtension ext = getProject().getExtensions().getByType(FedoraExtension.class);
        ["java", "-jar",ext.getTargetFile(),"--port","18080", "--headless"].execute()
    }


// a wrapper closure around executing a string
// can take either a string or a list of strings (for arguments with spaces)
// prints all output, complains and halts on error
//    def runCommand = { strList ->
//        assert ( strList instanceof String ||
//                ( strList instanceof List && strList.each{ it instanceof String } ) \
//)
//        def proc = strList.execute()
//        proc.in.eachLine { line -> println line }
//        proc.out.close()
//        proc.waitFor()
//
//        print "[INFO] ( "
//        if(strList instanceof List) {
//            strList.each { print "${it} " }
//        } else {
//            print strList
//        }
//        println " )"
//
//        if (proc.exitValue()) {
//            println "gave the following error: "
//            println "[ERROR] ${proc.getErrorStream()}"
//        }
//        assert !proc.exitValue()
//    }

}
