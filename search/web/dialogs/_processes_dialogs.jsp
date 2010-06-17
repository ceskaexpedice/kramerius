<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<!-- staticky export dialogs -->
<div id="process_started" style="display:none;">
	<div id="process_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
		<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingexport</fmt:message></td></tr>
    	</table>
	</div>
	<div id="process_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.exportrunning</fmt:message><br/></p>
	</div>
	<div id="process_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.exportfailed</fmt:message></p>
	</div>
</div>

<!-- reindexace -->
<div id="reindex_started" style="display:none;">
	<div id="reindex_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
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

<!-- vypis procesu -->
<div id="processes" style="display:none;"></div>



<!-- replikator monografie -->
<div id="replikator_monograph_started" style="display:none;">
	<div id="replikator_monograph_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingmonographimport</fmt:message></td></tr>
    	</table>
	</div>
	<div id="replikator_monograph_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.monographimportrunning</fmt:message><br/></p>
	</div>
	<div id="replikator_monograph_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.monographimportfailed</fmt:message></p>
	</div>
</div>


<!-- replikator periodika -->
<div id="replikator_periodical_started" style="display:none;">
	<div id="replikator_periodical_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingperiodicsimport</fmt:message></td></tr>
    	</table>
	</div>
	<div id="replikator_periodical_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.periodicsimportrunning</fmt:message><br/></p>
	</div>
	<div id="replikator_periodical_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.periodicsimportfailed</fmt:message></p>
	</div>
</div>

<!-- enumerator -->
<div id="enumerator_started" style="display:none;">
	<div id="enumerator_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingenumerator</fmt:message></td></tr>
    	</table>
	</div>
	<div id="enumerator_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.enumeratorrunning</fmt:message><br/></p>
	</div>
	<div id="enumerator_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.enumeratorfailed</fmt:message></p>
	</div>
</div>

<!-- replication rights -->
<div id="replicationrights_started" style="display:none;">
	<div id="replicationrights_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center"><fmt:message bundle="${lctx}">administrator.dialogs.waitingreplicationrights</fmt:message></td></tr>
    	</table>
	</div>
	<div id="replicationrights_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.replicationrightsrunning</fmt:message><br/></p>
	</div>
	<div id="replicationrights_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;"><fmt:message bundle="${lctx}">administrator.dialogs.replicationrightsfailed</fmt:message></p>
	</div>
</div>

<!-- confirmation dialog -->

<div id="confirm_dialog" title="<fmt:message bundle="${lctx}">administrator.dialogs.confirm</fmt:message>" style="display:none;">
	<span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
        <span id="proccess_confirm_text"></span>
</div>