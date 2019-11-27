package cz.incad.kramerius.pdf;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocumentException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;

public interface SimplePDFService {


    /**
     * Only render previous prepared document
     * @param rdoc
     * @param os
     * @param path
     * @param i18nServlet
     * @param fontMap
     * @throws IOException
     * @throws DocumentException
     */
    public void pdf(PreparedDocument rdoc, OutputStream os, FontMap fontMap) throws IOException,DocumentException;
}

