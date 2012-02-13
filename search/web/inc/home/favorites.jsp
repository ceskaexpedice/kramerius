<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%><view:object name="fav" clz="cz.incad.Kramerius.views.favorites.FavoritesViewObject"></view:object>

<c:forEach items="${fav.favorites}" var="pid">
     <div align="center" style="overflow:hidden; border:1px solid #eeeeee; width:100px; height:100px; float:left; margin:5px;">
          <a href="i.jsp?pid=${pid}" >
              <img align="middle" vspace="2" id="img_favorite_${pid}" src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"/>
          </a>
      </div>
</c:forEach>
