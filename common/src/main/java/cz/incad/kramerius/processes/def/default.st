/** default sablona */
group defaultTemplate;

formfield(bundle, field) ::=<<
    <label>$bundle.(field.name)$</label><br>
    <input type="text" size="30" id="$field.name$" value="$field.value$"></input><br>
>>

/** formular */
form(bundle, fields, process) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {
            
            
            var vals = ''; 
            vals = vals + '$first(fields).name$='+\$('#$first(fields).name$').val();
            $rest(fields):{f|
            vals = vals +  ';$f.name$='+\$('#$f.name$').val();
            }$
            
            var url = "lr?action=form_post&def=$process$&paramsMapping={"+vals+"}&out=text";
            processStarter("$process$").start(url);
                                    
        }
    -->        
    </script>

<form>

    $fields:{f|$formfield(bundle=bundle, field=f)$}$ 

</form>

  
</div>
>>