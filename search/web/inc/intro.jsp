<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>




<div id="intro">
     <ul>
         <li><a href="#intro1"><fmt:message bundle="${lctx}">Nejnovější</fmt:message></a></li>
         <li><a href="#intro2"><fmt:message bundle="${lctx}">Nejžádanější</fmt:message></a></li>
         <li><a href="#intro3"><fmt:message bundle="${lctx}">Informace</fmt:message></a></li>
     </ul>
     <div id="intro1" style="height:220px;"></div>
         <script>
            //$(document).ready(function(){
                $.get('inc/newest.jsp?' , function(data){
                   $('#intro1').html(data) ;
                });
            //});
         </script>
     <div id="intro2" style="height:220px;">
     </div>
         <script>
            //$(document).ready(function(){
                $.get('inc/mostDesirables.jsp', function(data){
                   $('#intro2').html(data) ;
                });
            //});
         </script>
     <div id="intro3">
        <%  if(request.getRemoteUser()!=null) {  %>
        <%@ include file="text/edit_intro.jsp" %>
	<% }else{
            Injector inj = (Injector)application.getAttribute(Injector.class.getName());
            TextsService ts = (TextsService)inj.getInstance(TextsService.class);	
			Provider<Locale> provider = inj.getProvider(Locale.class);
			
			if (ts.isAvailable("intro", provider.get())) {
                out.println(ts.getText("intro", provider.get()));
			} else {
				out.println(ts.getText("default_intro",provider.get()));
			}
			
        } %>
         </div>
</div>
<script language="javascript">
        $(document).ready(function(){
            $('#intro').tabs();
        });
</script>