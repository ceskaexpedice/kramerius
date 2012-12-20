<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="detail"
    clz="cz.incad.Kramerius.views.statistics.details.ModelStatisticsDetail"></view:object>

    
<div>
    <script type="text/javascript">
        var statisticsDetail = {
                items:[<c:forEach var="d" items="${detail.descriptions}" varStatus="status"> '${d}' ${not status.last ? ',' : ''} </c:forEach> ],
                title: '${detail.title}'
        };
    </script>
</div>    



