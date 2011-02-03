<%@page contentType="text/html" pageEncoding="UTF-8"%>
<div align="center" id="languages" >::
    <%
    String[] langs = kconfig.getPropertyList("interface.languages");

    for(int i=0; i<langs.length; i=i+2){
    %>
    <a href="javascript:setLanguage('<%=langs[i+1]%>')"><%=langs[i]%></a> :: 
    <%
    }
    %>
</div>
<div align="center" >
    ©2008-2011. 
    Developed under GNU GPL by 
    <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> <br/><br/>
</div>
