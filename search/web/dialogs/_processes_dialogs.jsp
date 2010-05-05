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
			<tr><td align="center">Prosím vyčkejte, spouští se proces generovaní PDF.</td></tr>
    	</table>
	</div>
	<div id="process_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Export spuštěn<br/></p>
	</div>
	<div id="process_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Export do PDF selhal, prosim, zkontrolujte tabulku procesů.</p>
		<p><a href="#"> Tabulka procesu</a></p>
	</div>
</div>

<!-- reindexace -->
<div id="reindex_started" style="display:none;">
	<div id="reindex_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center">Prosím vyčkejte, spouští se reindexace.</td></tr>
    	</table>
	</div>
	<div id="reindex_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Reindexace spuštěna<br/></p>
	</div>
	<div id="reindex_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Proces reindexace selhal, prosim, zkontrolujte tabulku procesů.</p>
	</div>
</div>

<!-- vypis procesu -->
<div id="processes" style="display:none;"></div>



<!-- replikator monografie -->
<div id="replikator_monograph_started" style="display:none;">
	<div id="replikator_monograph_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center">Prosím vyčkejte, spouští se import monografií.</td></tr>
    	</table>
	</div>
	<div id="replikator_monograph_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Import spuštěn<br/></p>
	</div>
	<div id="replikator_monograph_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Proces importu selhal, prosim, zkontrolujte tabulku procesů.</p>
	</div>
</div>


<!-- replikator periodika -->
<div id="replikator_periodical_started" style="display:none;">
	<div id="replikator_periodical_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center">Prosím vyčkejte, spouští se import periodik.</td></tr>
    	</table>
	</div>
	<div id="replikator_periodical_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Import spuštěn<br/></p>
	</div>
	<div id="replikator_periodical_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Proces importu selhal, prosim, zkontrolujte tabulku procesů.</p>
	</div>
</div>

<!-- enumerator -->
<div id="enumerator_started" style="display:none;">
	<div id="enumerator_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center">Prosím vyčkejte, spouští se import enumerator.</td></tr>
    	</table>
	</div>
	<div id="enumerator_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Enumerator spuštěn<br/></p>
	</div>
	<div id="enumerator_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Proces enumerator selhal, prosim, zkontrolujte tabulku procesů.</p>
	</div>
</div>

<!-- replication rights -->
<div id="replicationrights_started" style="display:none;">
	<div id="replicationrights_started_waiting" style="display:none;margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading2.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center">Prosím vyčkejte, spouští se proces replicationrights.</td></tr>
    	</table>
	</div>
	<div id="replicationrights_started_ok" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Replicationrights spuštěn<br/></p>
	</div>
	<div id="replicationrights_started_failed" style="display:none;margin: 12px;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;">Proces replicationrights selhal, prosim, zkontrolujte tabulku procesů.</p>
	</div>
</div>
