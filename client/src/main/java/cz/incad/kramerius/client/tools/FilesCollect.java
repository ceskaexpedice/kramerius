package cz.incad.kramerius.client.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

import cz.incad.kramerius.client.resources.ResourcesLoaderFilter;
import cz.incad.kramerius.client.resources.merge.JSONArrayMerge;
import cz.incad.kramerius.client.resources.merge.Merge;
import cz.incad.kramerius.client.resources.merge.validators.MenuDefValidateInput;
import cz.incad.kramerius.client.resources.merge.validators.ValidateInput;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.utils.StringUtils;

public class FilesCollect {

    public static final Logger LOGGER = Logger.getLogger(FilesCollect.class.getName());

    private HttpServletRequest request;

    public void configure(Map props) {
        this.request = (HttpServletRequest) props.get("request");
    }

    public String getViewersDefinitions() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"viewers.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"viewers.def");
            MenuDefValidateInput vinput = new MenuDefValidateInput();
            JSONArray merged = merge(confPath,warPath, vinput);
            return merged.toString();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        }
    }

    public String getDownloadsDefinitions() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"downloads.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"downloads.def");
            MenuDefValidateInput vinput = new MenuDefValidateInput();
            JSONArray merged = merge(confPath,warPath, vinput);
            return merged.toString();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        }
    }

    public String getShareDefinitions() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"shares.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"shares.def");
            MenuDefValidateInput vinput = new MenuDefValidateInput();
            JSONArray merged = merge(confPath,warPath, vinput);
            return merged.toString();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        }
    }

    public String getMenuDefinitions() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"menu.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"menu.def");
            MenuDefValidateInput vinput = new MenuDefValidateInput();
            JSONArray merged = merge(confPath,warPath, vinput);
            return merged.toString();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "[]";
        }
    }

    public List<String> getStylesheets() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"cssfiles.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"cssfiles.def");
            JSONArray mrg = merge(confPath, warPath, null);
            List<String> alist = new ArrayList<String>();
            for (int i = 0, ll = mrg.length(); i < ll; i++) {
                alist.add(mrg.getString(i));
            }
            return alist;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<String>();
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<String>();
        }
    }

    public List<String> getJavascripts() {
        try {
            File confPath = new File(K5Configuration.getExtensionsHome()+File.separator+"jsfiles.def");
            File warPath = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/classes/res")+File.separator+"jsfiles.def");
            JSONArray mrg = merge(confPath, warPath, null);
            List<String> alist = new ArrayList<String>();
            for (int i = 0, ll = mrg.length(); i < ll; i++) {
                alist.add(mrg.getString(i));
            }
            return alist;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<String>();
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<String>();
        }
    }

    private JSONArray merge(File confPath, File warPath, ValidateInput<JSONArray> validateInput) throws IOException,
            FileNotFoundException, JSONException {
        JSONArrayMerge merge = new JSONArrayMerge();
        if (validateInput != null) merge.setValidateInput(validateInput);

        if (warPath.exists() && confPath.exists()) {
            String realString = IOUtils.readAsString(new FileInputStream(warPath), Charset.forName("UTF-8"), true);
            String confString = IOUtils.readAsString(new FileInputStream(confPath), Charset.forName("UTF-8"), true);
            String mergered = merge.merge(realString, confString);
            return new JSONArray(mergered);
        } else if (warPath.exists()) {
            String string = IOUtils.readAsString(new FileInputStream(warPath), Charset.forName("UTF-8"), true);
            JSONArray arr = new JSONArray(string);
            return arr;
        } else if (confPath.exists()){
            String string = IOUtils.readAsString(new FileInputStream(warPath), Charset.forName("UTF-8"), true);
            JSONArray arr = new JSONArray(string);
            return arr;
        } 
        return new JSONArray();
    }
}
