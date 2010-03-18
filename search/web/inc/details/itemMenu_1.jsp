<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="urlSolr" >
    <c:out value="${kconfig.solrHost}" />?q=PID:"<c:out value="${param.pid}" />"
</c:set>
<%--
Get Biblio mods
--%>
<c:set var="models" value="${fn:split(param.path, '/')}"/>
<c:set var="pids" value="${fn:split(param.pid_path, '/')}"/>
<c:set var="lastModel" value="${models[fn:length(models)-1]}" />
<c:set var="model_path" value="${models[0]}" scope="request" />
<c:forEach var="uuid" varStatus="status" items="${fn:split(param.pid_path, '/')}">
    <div id="m_<c:out value="${models[status.count -1]}" />" style="padding:2px;">
    <jsp:useBean id="uuid" type="java.lang.String" />
    <c:set var="urlStr" >
        <c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
    </c:set>
    <c:set var="display" value="none"/>
    
    <c:catch var="exceptions"> 
        <c:import url="${urlStr}" var="xml2" charEncoding="UTF-8"  />
        <c:import url="inc/details/xsl/${models[status.count -1]}.jsp?display=${display}&language=${param.language}${others}" var="xslt" charEncoding="UTF-8"  />
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            <c:out value="${exceptions}" />
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${fn:length(models)==1}">
                    <x:transform doc="${xml2}"  xslt="${xslt}"  >
                        <x:param name="pid" value="${uuid}"/>
                    </x:transform>
                    <div id="<c:out value="${model_path}" />"></div>
                    <script language="javascript">
                        $(document).ready(function(){
                           // recTest("<c:out value="${uuid}" />",
                           //         "",
                           //         "",
                           //         "<c:out value="${model_path}" />" );
                                    
                                    getRelsExt("<c:out value="${uuid}" />",
                                        "*",
                                        "<c:out value="${model_path}" />",
                                        false
                                    );
                        });
                    </script>
                </c:when>
                <c:when test="${status.count==1}">
                    <x:transform doc="${xml2}"  xslt="${xslt}"  >
                        <x:param name="pid" value="${uuid}"/>
                    </x:transform>
                </c:when>
                <c:when test="${status.count==fn:length(models)}">
                    <div id="<c:out value="${model_path}" />"><x:transform doc="${xml2}"  xslt="${xslt}"  /></div>
                    <script language="javascript">
                        $(document).ready(function(){
                            //recTest("<c:out value="${pids[status.count-2]}" />",
                            //        "<c:out value="${pids[status.count-1]}" />",
                            //        "<c:out value="${models[status.count-1]}" />",
                            //        "<c:out value="${model_path}" />",
                             //       "<c:out value="${param.format}" />"
                             //   );
                                getRelsExt("<c:out value="${pids[status.count-2]}" />",
                                        "*",
                                        "<c:out value="${model_path}" />",
                                        false
                                    );
                        });
                    </script>
                    <c:choose>
                        <c:when test="${models[status.count -1]=='internalpart'}">
                        <c:set var="model_path" scope="request" ><c:out value="${model_path}" />_<c:out value="${models[status.count -1]}" /></c:set>
                        <div id="<c:out value="${model_path}" />"></div>
                        <script language="javascript">
                            $(document).ready(function(){
                                getRelsExt("<c:out value="${pids[status.count-1]}" />",
                                        "*",
                                        "<c:out value="${model_path}" />",
                                        false
                                    );
                            });
                        </script>
                        </c:when>
                        <c:when test="${models[status.count -1]!='page'}">
                        <c:set var="model_path" scope="request" ><c:out value="${model_path}" />_<c:out value="${models[status.count -1]}" /></c:set>
                        <div id="<c:out value="${model_path}" />"></div>
                        <script language="javascript">
                            $(document).ready(function(){
                                recTest("<c:out value="${pids[status.count-1]}" />",
                                        "",
                                        "",
                                        "<c:out value="${model_path}" />" );
                            });
                        </script>
                        </c:when>
                    </c:choose>
                    
                </c:when>
                <c:otherwise>
                    <div id="<c:out value="${model_path}" />"><x:transform doc="${xml2}"  xslt="${xslt}"  /></div>
                    <script language="javascript">
                        $(document).ready(function(){
                            recTest("<c:out value="${pids[status.count-2]}" />", 
                                    "<c:out value="${pids[status.count-1]}" />",
                                    "<c:out value="${models[status.count-1]}" />",
                                    "<c:out value="${model_path}" />" );
                        });
                    </script>
                    <c:set var="model_path" scope="request" ><c:out value="${model_path}" />_<c:out value="${models[status.count -1]}" /></c:set>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
    
</c:forEach>

<c:forEach var="model" varStatus="status" items="${models}">
    </div>
</c:forEach>