package org.kramerius.genpdf;

import com.lowagie.text.DocumentException;
import cz.incad.kramerius.pdf.OutOfRangeException;

import java.io.File;
import java.io.IOException;

public interface SpecialNeedsService {

    public File generate(String pid, String user) throws DocumentException, IOException, OutOfRangeException;
}