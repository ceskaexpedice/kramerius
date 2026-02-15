package cz.inovatika.dochub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PermanentContentSpace {


    public void storeContent(String pid, DocumentType type, InputStream is) throws IOException;

    public OutputStream createOutputStream(String pid, DocumentType type) throws IOException;

    public InputStream getContent(String pid, DocumentType type) throws IOException;


    public boolean exists(String pid, DocumentType type) throws IOException;
}
