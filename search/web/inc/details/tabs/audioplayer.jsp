<%-- 
    Document   : audioplayer
    Created on : 27.11.2012, 11:29:14
    Author     : Martin Řehánek <rehan at mzk.cz>
--%>

<%@page import="java.util.Collections"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="cz.incad.kramerius.utils.XMLUtils"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.UnicodeUtil"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.InputStream"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>

<div id="audio.player" class="player"> 
    <div>
        <!--TODO: pokud je senzam skladeb prazdny, tlacitko tady neukazovat-->
        <div>
            Stisknutím následujícího tlačítka otevřete zvukový přehrávač v novém okně.
            <br>
            <input id="openPlayerBtn" type="button" value="Spustit přehrávání">
            <br>
            Budete tak moci poslouchat nahrávky a zároveň dále procházet digitální knihovnu.
            <br>
            Zvukový přehrávač je po otevření naplněn skladbami, které jsou součástí vybrané nahrávky
            nebo kolekce nahrávek.
            Seznam je však omezen jen na ty skladby, které jsou aktuálnímu čtenáři přístupné.
            Může se tedy lišit např. mezi přihlášenými a nepřihlášenými čtenáři, 
            mezi čtenáři přistupujícímí z budovy knihovny nebo z domova.
        </div>

    </div>    
</div>

<script>
    document.getElementById("openPlayerBtn").addEventListener("click", function() {
        //console.log("openPlayer:pid_path: " + pid_path);
        var url = 'audioplayerWindow.jsp?pid_path=' + pid_path;
        target = "audio_player";
        commOpts="width=700,left=100,height=850,top=100"
        barsOpts="location=no,resizable=no,scrollbars=no,status=no,titlebar=no,toolbar=no"
        ieOnlyOpts= "scrollbars=no,directories=no,fullscreen=no";
        options = commOpts + "," + barsOpts + "," + ieOnlyOpts;
        var playerWindow = window.open(url,target, options);
    }, false);
    
</script>

