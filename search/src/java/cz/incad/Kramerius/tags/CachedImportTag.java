package cz.incad.Kramerius.tags;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.ehcache.CacheManager;
import sun.misc.Cache;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

public class CachedImportTag  extends CachedImportSupport {


    @Inject
    CacheManager injectedCacheManager;

    public void setUrl(String url) throws JspTagException {
        this.url = url;
    }

    @Override
    public int doStartTag() throws JspException {
        Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
        inj.injectMembers(this);
        this.setCacheManager(injectedCacheManager);
        return super.doStartTag();
    }

    public void setContext(String context) throws JspTagException {
        this.context = context;
    }

    public void setCharEncoding(String charEncoding) throws JspTagException {
        this.charEncoding = charEncoding;
    }

}