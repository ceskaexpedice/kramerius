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
    String action;
    String uuid;
    String indexDocXslt;

    public ProgramArguments() {
    }

    public Boolean parse(String[] args) {
        try {
            int total = args.length;
            int i = 0;
            while (i < total) {
                if (args[i].equalsIgnoreCase("-fullindex")) {
                    fullIndex = true;
                } else if (args[i].equalsIgnoreCase("-cfgFile")) {
                    i++;
                    configFile = args[i];
                } else if (args[i].equalsIgnoreCase("-log4jFile")) {
                    i++;
                    log4jFile = args[i];
                } else if (args[i].equalsIgnoreCase("-action")) {
                    i++;
                    action = args[i];
                } else if (args[i].equalsIgnoreCase("-pid")) {
                    i++;
                    if(!args[i].startsWith("uuid:")) uuid = "uuid:";
                    uuid += args[i];
                } else if (args[i].equalsIgnoreCase("-maxDocuments")) {
                    i++;
                    maxDocuments = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("-from")) {
                    i++;
                    from = args[i];
                } else if (args[i].equalsIgnoreCase("-to")) {
                    i++;
                    to = args[i];
                }

                i++;
            }
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
