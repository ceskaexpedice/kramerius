package cz.incad.kramerius.pdf.commands;

import java.util.logging.Level;

import org.w3c.dom.Element;

import cz.incad.kramerius.utils.StringUtils;

public class LogoImage  extends AbstractITextCommand implements ITextCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LogoImage.class.getName());
    
    private String file;
    private String width;
    private String height;
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("logo")) {
            String width = elm.getAttribute("width");
            String height = elm.getAttribute("height");
            
            if (StringUtils.isAnyString(width) && StringUtils.isAnyString(height)) {
                this.width = width;
                this.height = height;
            }
            
            String file = elm.getAttribute("file");
            if ((file != null) && (!file.equals(""))) {
                this.file = file;
            } else {
                LOGGER.log(Level.WARNING, "cannot load image component. No file "); 
            }
        } else {
           LOGGER.log(Level.WARNING, "cannot load image component. No image elm."); 
        }
    }

    

    
    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        procsListener.after(this);
    }


    public String getFile() {
        return this.file;
    }
    
    public String getWidth() {
        return width;
    }
    
    public String getHeight() {
        return height;
    }

}
