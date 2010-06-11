package org.kramerius;

import java.util.logging.Logger;

import com.qbizm.kramerius.imptool.poc.Main;

public class Replicate {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length!=1){
            usage();
        }
        if ("monographs".equalsIgnoreCase(args[0])){
            Download.replicateMonographs();
        }else if ("periodicals".equalsIgnoreCase(args[0])){
            Download.replicatePeriodicals();
        }else{
            usage();
        }
    }
    
    private static void usage(){
        System.out.println("Usage: Replicate monographs | periodicals");
        System.exit(-1);
    }
    
    private static final Logger log = Logger.getLogger(Replicate.class.getName());

}
