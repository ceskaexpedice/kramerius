/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.indexer;

import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
System.out.println(Arrays.asList(args));
            ProgramArguments arguments = new ProgramArguments();
            if (!arguments.parse(args)) {
                System.out.println("Program arguments are invalid");
            }

            Indexer indexer = new Indexer(arguments);
            indexer.run();
            

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex);
        }
    }

}
