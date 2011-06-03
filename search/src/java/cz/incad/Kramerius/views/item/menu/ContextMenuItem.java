package cz.incad.Kramerius.views.item.menu;

/**
 *
 * @author Alberto
 */
public class ContextMenuItem {
    public String key;
    public String dataType;
    public String jsFunction;
    public String jsArgs;
    public boolean supportMultiple;
    public ContextMenuItem(String key, String dataType, String jsFunction, String jsArgs, boolean supportMultiple){
        this.key = key;
        this.dataType = dataType;
        this.jsFunction = jsFunction;
        this.jsArgs = jsArgs;
        this.supportMultiple = supportMultiple;
    }
}
