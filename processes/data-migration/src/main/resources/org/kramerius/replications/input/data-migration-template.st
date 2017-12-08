/** sablona pro replikace */
group migrationdatatemplate;


/** formular */
form(bundle, handle) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {

            var charsThatMustBeEscaped = [':',';','}','{','\\\\'];
            var handle = \$('#handle').val().escapeChars(charsThatMustBeEscaped);
            var user = \$('#user').val().escapeChars(charsThatMustBeEscaped);
            var password = \$('#pswd').val().escapeChars(charsThatMustBeEscaped);
			

            var url = "lr?action=form_post&def=data_migration&paramsMapping="+encodeURI("{")+"url="+encodeURIComponent(handle)+";username="+user+";pswd="+encodeURI(password)+";replicateCollections=true"+encodeURI("}")+"&out=text";
            processStarter("data_migration").start(url);
            
        }
    -->
    </script>

<form>

    <label>$bundle.("process.k4_replications.inputs.handle")$</label><br>
    <input type="text" style="width:100%" id="handle" value="$handle$"></input><br>

    <label>$bundle.("process.k4_replications.inputs.user")$</label><br>
    <input type="text" style="width:100%" id="user" value=""></input><br>

    <label>$bundle.("process.k4_replications.inputs.pswd")$</label><br>
    <input type="password" style="width:100%" id="pswd" value=""></input><br>


</form>

  
</div>
>>