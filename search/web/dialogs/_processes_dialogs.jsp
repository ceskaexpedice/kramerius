<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

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

<div id="processes" style="display:none;">
</div>
