<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<div id="printsetup">

<h4><view:msg>print.settings.label</view:msg></h4>

<select id="page">
  <option value="a4"><view:msg>print.settings.a4</view:msg></option>
  <option value="a3"><view:msg>print.settings.a3</view:msg></option>
</select>

</div>
 
