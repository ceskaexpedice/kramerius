package cz.incad.Kramerius.views.adminmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.NoEndPointException;

import com.google.inject.Inject;

import cz.incad.kramerius.service.ResourceBundleService;

public class AdminMenuViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AdminMenuViewObject.class.getName());
	
	@Inject
	ResourceBundleService resourceBundleService;
	@Inject
	HttpServletRequest request;
	@Inject
	Locale locale;
	
	public String processes() throws IOException {
//	    <div align="left"> <a href="javascript:processes(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.lrprocesses.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:processes(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.lrprocesses.title")+" </a> </div>";
	}

	public String importMonographs() throws IOException {
//	    <div align="left"> <a href="javascript:importMonographs(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.importMonograph.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:importMonographs(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importMonograph.title")+" </a> </div>";
	}

	public String importPeriodicals() throws IOException {
//	    <div align="left"> <a href="javascript:importPeriodicals(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.importPeriodical.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:importPeriodicals(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.importPeriodical.title")+" </a> </div>";
	}

	public String showIndexerAdmin() throws IOException {
//	    <div align="left"> <a href="javascript:showIndexerAdmin();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.indexDocuments.title</fmt:message></a> </div>
	    return "<div align=\"left\"> <a href=\"javascript:showIndexerAdmin(); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.indexDocuments.title")+" </a> </div>";
	}

	public String enumerator() throws IOException {
//	    <div align="left"> <a href="javascript:enumerator();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.enumerator.title</fmt:message></a> </div>
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('enumerator'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.enumerator.title")+" </a> </div>";
	}

	public String replicationrights() throws IOException {
//	    <div align="left"> <a href="javascript:replicationrights(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.replicationRights.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('replicationrights'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.replicationrights.title")+" </a> </div>";
	}

	public String convert() throws IOException {
//	    <div align="left"> <a href="javascript:replicationrights(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.replicationRights.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('convert'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.convert.title")+" </a> </div>";
	}

	public String importfoxml() throws IOException {
//	    <div align="left"> <a href="javascript:replicationrights(); javascript:hideAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu.dialogs.replicationRights.title</fmt:message></a> </div>	
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('import'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs.import.title")+" </a> </div>";
	}

	public String noParamsProcess(String processName) throws IOException {
	    return "<div align=\"left\"> <a href=\"javascript:noParamsProcess('"+processName+"'); javascript:hideAdminMenu();\">"+this.resourceBundleService.getResourceBundle("labels", this.locale).getString("administrator.menu.dialogs."+processName+".title")+" </a> </div>";
	}
	
	public String[] getAdminMenuItems() {
		try {
			List<String> menuItems = new ArrayList<String>();
			if (request.getRemoteUser() != null) {
				menuItems.add(processes());
				menuItems.add(importMonographs());
				menuItems.add(importPeriodicals());
				menuItems.add(showIndexerAdmin());
				menuItems.add(noParamsProcess("enumerator"));
				menuItems.add(noParamsProcess("replicationrights"));
				menuItems.add(noParamsProcess("convert"));
				menuItems.add(noParamsProcess("import"));
			}
			return (String[]) menuItems.toArray(new String[menuItems.size()]);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return new String[0];
		}
	}
}
