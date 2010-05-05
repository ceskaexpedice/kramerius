/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

/**
 *
 * @author Administrator
 */
public class ProgramArguments {

    public String configFile = "";
    public String log4jFile = "";
    public String from;
    public String to;
    public Boolean fullIndex = false;
    public Boolean updateKey = false;
    public int maxDocuments = 0;
    public int docId;
    public String title="";
    String action;
    String value;
    String indexDocXslt;

    public ProgramArguments() {
    }

    public Boolean parse(String[] args) {
        try {
            if(args.length<5) return false;
            configFile = args[0];
            log4jFile = args[1];
            action = args[2];
            value = args[3];
            for(int i=4;i<args.length;i++)
            title += args[i];
            
            if (configFile.equals("")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }
}
