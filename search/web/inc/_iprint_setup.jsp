<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>


<%@ page isELIgnored="false"%>

<scrd:securedContent action="read" sendForbidden="true" pid="${param.pid}">

<script type="text/javascript">

    function __pageselected() {
        var output = "pdf";//$("#localprintoutput input:checked" ).val();
        var pagesize = $("#localprintpapersize select option:selected" ).val();
        localprint.setup({"output":output, "page":pagesize});
    }

    $(document).ready(function(){
        __pageselected();
        $("#localprintoutput input").change(function() {
            __pageselected();
        });
        $("#localprintpapersize select").change(function() {
            __pageselected();
        });
    });
</script>


<div id="printsetup">


<div id="localprintoutput" style="display:none">
    <h4><view:msg>print.settings.label</view:msg></h4>
    <input type="radio" name="output" value="pdf">PDF</input>
    <input type="radio" name="output" value="html" checked="true">HTML</input> 
</div>


<div id="localprintpapersize">
<h4><view:msg>print.settings.papersize</view:msg></h4>
<select id="page">
  <option value="A4"><view:msg>print.settings.a4</view:msg></option>
  <option value="A3"><view:msg>print.settings.a3</view:msg></option>
</select>
</div>

</div>
 
</scrd:securedContent>
 