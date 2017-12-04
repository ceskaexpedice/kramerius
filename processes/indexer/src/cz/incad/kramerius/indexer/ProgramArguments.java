/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class ProgramArguments {

    public String from;
    public String to;
    public Boolean fullIndex = false;
    public Boolean updateKey = false;
    public int maxDocuments = 0;
    public int docId;
    public String title="";
    String action;
    String value;
    String params;
    
    //action:
    //fromKrameriusModel uuid    rekursivne indexuje dokument
    //krameriusModel modelName   rekursivne indexuje vsechny dokumenty modelu 
    //fromPID uuid params        indexuje dokument a prida parametry do transformace
    //deleteModel model          zmaze z indexu vsechny dokumenty modelu, a podrizene
    //deleteDocument pid_path    zmaze z indexu dokument a podrizene
    //deletePid uuid             zmaze jen dokument

    public ProgramArguments() {
    }

    public Boolean parse(String[] args) {
        try {
            if(args.length<2) return false;
            action = args[0];
            value = args[1];
            for(int i=2;i<args.length;i++)
            title += args[i];
                return true;
        } catch (Exception ex) {
            Logger.getLogger(ProgramArguments.class.getName())
                    .log(Level.SEVERE, Arrays.toString(args), ex);
            return false;
        }
    }
}
