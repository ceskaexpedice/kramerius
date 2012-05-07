<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>

<%@ page isELIgnored="false"%>


<scrd:securedContent action="manage_lr_process" sendForbidden="true">

<!-- reindexace -->
<div id="reindex_started" style="display:none;">
	<div id="reindex_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>
		<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingreindex</fmt:message></td></tr>
    	</table>
	</div>
	<div id="reindex_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.reindexrunning</fmt:message><br/></p>
	</div>
	<div id="reindex_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.reindexfailed</fmt:message></p>
	</div>
</div>

</scrd:securedContent>

<!-- vypis procesu -->
<div id="processes" style="display:none;"></div>


<!-- confirmation dialog -->
<div id="confirm_dialog" title="<fmt:message bundle="${lctx}">administrator.dialogs.confirm</fmt:message>" style="display:none;">
	<img src="img/alert.png" alt="alert" />
        <span id="proccess_confirm_text"></span>
</div>



<!-- common -->
<div id="common_started" style="display:none;">
	<div id="common_started_waiting" style="margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center" id="common_started_text"></td></tr>
    	</table>
	</div>
	<div id="common_started_ok" style="margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_ok"><br/></p>
	</div>
	<div id="common_started_failed" style="margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_failed"></p>
	</div>
</div>
