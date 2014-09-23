/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.processes.impl.ProcessStarter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Administrator
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) throws Exception {
            LOG.log(Level.INFO, "process args: {0}", Arrays.toString(args));
            ProgramArguments arguments = new ProgramArguments();
            if (!arguments.parse(args)) {
                throw new Exception("Program arguments are invalid: " + Arrays.toString(args));
            }

            try{
                //ProcessStarter.updateName("Indexace dokumentu: " + arguments.title);
            } catch (Exception ex) {
                System.out.println("Asi jsme v konzoli");
            }

            Indexer indexer = new Indexer(arguments);
            indexer.run();
    }
}
