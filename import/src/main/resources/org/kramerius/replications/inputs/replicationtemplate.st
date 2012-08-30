/** sablona pro replikace */
group replicationTemplate;


/** formular */
form(bundle, handle) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {
            
            var charsThatMustBeEscaped = [':',';','}','{'];
            var handle = \$('#handle').val().escapeChars(charsThatMustBeEscaped);
            var user = \$('#user').val().escapeChars(charsThatMustBeEscaped);
            var password = \$('#password').val().escapeChars(charsThatMustBeEscaped);
            
            var url = "lr?action=form_post&def=k4_replication&paramsMapping={url="+handle+";user="+user+";password="+passsword}&out=text";
            processStarter("k4_replication").start(url);
            
        }
    -->
    </script>

<form>

    <label>$bundle.("processes.defaultfields.handle")$</label><br>
    <input type="text" style="width:100%" id="handle" value="$handle$"></input><br>

    <label>$bundle.("processes.defaultfields.user")$</label><br>
    <input type="text" style="width:100%" id="user" value=""></input><br>

    <label>$bundle.("processes.defaultfields.pswd")$</label><br>
    <input type="password" style="width:100%" id="pswd" value=""></input><br>

</form>

  
</div>
>>