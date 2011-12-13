package cz.incad.Kramerius.views.item.menu;

/**
 *
 * @author Alberto
 */
public class ContextMenuItem {

    private String key;
    private String dataType;
    private String jsFunction;
    private String jsArgs;
    private boolean supportMultiple;
    
    public ContextMenuItem(String key, String dataType, String jsFunction, String jsArgs, boolean supportMultiple){
        this.key = key;
        this.dataType = dataType;
        this.jsFunction = jsFunction;
        this.jsArgs = jsArgs;
        this.supportMultiple = supportMultiple;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    public String getJsFunction() {
        return jsFunction;
    }
    public void setJsFunction(String jsFunction) {
        this.jsFunction = jsFunction;
    }
    public String getJsArgs() {
        return jsArgs;
    }
    public void setJsArgs(String jsArgs) {
        this.jsArgs = jsArgs;
    }
    public boolean isSupportMultiple() {
        return supportMultiple;
    }
    public void setSupportMultiple(boolean supportMultiple) {
        this.supportMultiple = supportMultiple;
    }
    
}
