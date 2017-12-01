<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>

<view:object name="indexerData" clz="cz.incad.Kramerius.views.inc.admin.IndexerAdminModelViewObject"></view:object>

<c:forEach var="object" items="${indexerData.objects}" varStatus="status">
<tr class="indexer_result" pid="${object.source}">
<td class="indexer_result_status">&nbsp;</td>
<td width="100%"><a title="index document" href="javascript:indexDoc('${object.source}', '${object['dc.title']}');">${object['dc.title']}</a></td><td style="min-width:240px;" width="240px"></td>
<td style="min-width:138px;">${object.source}</td>
<td style="min-width:138px;"><a href="${object.ref}" target="_blank">${object.ref}</a></td>
<td style="min-width:138px;">${object.date}</td>
</tr>

 </c:forEach>
