<!DOCTYPE HTML>
<html>
<head>
<meta charset="utf-8">
<title>jQuery File Upload Example</title>
</head>
<body>
<link href="http://hayageek.github.io/jQuery-Upload-File/4.0.10/uploadfile.css" rel="stylesheet">

<!--
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
-->
<script src="../../js/jquery-1.5.1.min.js" type="text/javascript" ></script>

<form id="files" method="post" enctype="multipart/form-data">
	<label for="IMG_THUMB">Maly nahled</label>
    <input name="IMG_THUMB" type="file" accept="image/*"/>
	<label for="IMG_FULL">Maly nahled</label>
    <input name="IMG_FULL" type="file" accept="image/*"/>
</form>


<a href="javascript:poslat()"> POSLAT </a>

<script>
alert($("form#files").size());
function poslat() {

	$("form#files").submit();
   
}

$("form#files").submit(function(){

    var formData = new FormData($(this)[0]);
	alert(formData);
	
    $.ajax({
        url: '../../vc?pid=vc:xxxx&action=IMAGES_UPLOAD',
        type: 'POST',
        data: formData,
        async: false,
        success: function (data) {
            alert(data)
        },
        cache: false,
        contentType: false,
        processData: false
    });

    return false;
});
</script>

</body> 
</html>