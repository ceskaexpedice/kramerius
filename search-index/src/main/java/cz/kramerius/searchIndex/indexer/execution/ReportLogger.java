/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.kramerius.searchIndex.indexer.execution;

import java.io.OutputStream;
import java.io.PrintStream;

public class ReportLogger {

    private PrintStream stream;

    public ReportLogger(OutputStream outputStream) {
        this.stream = outputStream == null ? null : new PrintStream(outputStream);
    }

    public void report(String message) {
        if (stream != null) {
            stream.println(message);
        }
    }

    public void report(String message, Throwable e) {
        if (stream != null) {
            stream.print(message + ": ");
            e.printStackTrace(stream);
        }
    }

    public void close() {
        if (stream != null) {
            stream.close();
        }
    }

}
