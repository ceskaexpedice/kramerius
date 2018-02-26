/** sablona pro replikace */
group replicationTemplate;


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
			
			var replicateCollections =  \$( "#collections:checked" ).length ==1 ? "true" : "false";
			var replicateImages = \$( "#images:checked" ).length ==1 ? "true" : "false";
            
            var url = "lr?action=form_post&def=k4_replication&paramsMapping="+encodeURI("{")+"url="+encodeURIComponent(handle)+";username="+user+";pswd="+encodeURI(password)+";replicateCollections="+replicateCollections+";replicateImages="+replicateImages+encodeURI("}")+"&out=text";
                +";replicateCollections="+replicateCollections
                +";replicateImages="+replicateImages
                +"}&out=text";

            processStarter("k4_replication").start(url);
            
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

    <input type="checkbox" id="collections">$bundle.("process.k4_replications.inputs.replicationCollections")$</input>
    <input type="checkbox" id="images">$bundle.("process.k4_replications.inputs.replicationImages")$</input>

</form>

  
</div>
>>