/** default sablona */
group defaultTemplate;

bundlekey(name) ::=<<processes.defaultfields.$name$>>

formfield(bundle, field) ::=<<
    <label>$bundle.(bundlekey(name=field.name))$</label><br></br>
    <input type="text" style="width:100%" id="$field.name$" value="$field.value$"></input><br></br>
>>

/** formular */
form(bundle, fields, process) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {
            
            var charsThatMustBeEscaped = [':',';','}','{'];
            
            var vals = ''; 
            vals = vals + '$first(fields).name$='+\$('#$first(fields).name$').val().escapeChars(charsThatMustBeEscaped);
            $rest(fields):{f|
            vals = vals +  ';$f.name$='+\$('#$f.name$').val().escapeChars(charsThatMustBeEscaped);
            }$
            
            var url = "lr?action=form_post&def=$process$&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";
            processStarter("$process$").start(url);
                                    
        }
    -->        
    </script>

<form>

    $fields:{f|$formfield(bundle=bundle, field=f)$}$ 

</form>

  
</div>
>>