
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="ga" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>




<div class="newrole">

<div>

<form id="vc_image_association"  method="post" enctype="multipart/form-data">
    <table>
      <tr><td><label for="IMG_THUMB">IMG_THUMB</label></td></tr>
      <tr><td><input name="IMG_THUMB" type="file" accept="image/*"/></td></tr>
      <tr><td><label for="IMG_FULL">IMG_FULL</label></td></tr>
      <tr><td><input name="IMG_FULL" type="file" accept="image/*"/></td></tr>
    </table>
</div>

<script>
$("form#vc_image_association").submit(function(){

    var formData = new FormData($(this)[0]);
    $.ajax({
        url: 'vc?pid=${ga.parameterCollection.pid}&action=IMAGES_UPLOAD',
        type: 'POST',
        data: formData,
        async: false,
        success: function (data) {
          colAdm.refresh();
        },
        cache: false,
        contentType: false,
        processData: false
    });

    return false;
});
</script>
