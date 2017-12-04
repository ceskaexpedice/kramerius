package cz.incad.kramerius.lp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import cz.incad.kramerius.lp.Medium;
import cz.incad.kramerius.utils.IOUtils;

public class DecriptionHTML {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DecriptionHTML.class.getName());
	
	public static String descriptionHTML(String dcTitle, Medium medium, String[] files, String mediumNumber) throws IOException {
		InputStream stream = DecriptionHTML.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/lp/res/html.stg");
		String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
		
		StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
		StringTemplate htmlPage = group.getInstanceOf("htmlpage");
		htmlPage.setAttribute("dctitle", dcTitle);
		htmlPage.setAttribute("mediumnumber", mediumNumber);
		htmlPage.setAttribute("medium", medium.name());
		htmlPage.setAttribute("files", Arrays.asList(files));
		return htmlPage.toString();
	}
	
	
}
