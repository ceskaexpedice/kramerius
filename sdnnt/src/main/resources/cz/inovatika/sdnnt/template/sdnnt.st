/** default sablona */
group defaultTemplate;


/** formular */
form(bundle, sdnntendoint, kraminstance,  acronym) ::=<<

<div>


 	<style>
	    #sdnnt-dialog-content {
	      text-align: left;
	      border: 1px solid #ccc;
	    }
	    
	    #dialog-content h2 {
	      text-align: center;
	    }
	  
	  	#dialog-content ul {
	      list-style-type: square; /* Zde nastavte požadovaný znak */
	    }
  	</style>
  
    <script language="JavaScript" type="text/javascript">
    <!--
        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{', '\\\\'];

            var url = "lr?action=form_post&def=sdnnt-sync&out=text";

            processStarter("sdnnt-sync").start(url);
        }

    -->
    </script>
	
	<h3>Synchronizace licenci se seznamem SDNNT </h3>
	
 	<div id="sdnnt-dialog-content">
 		<ul>
			<li>Instance krameria: <strong>$kraminstance$</strong></li>
			<li>Zkratka krameria: <strong>$acronym$</strong></li>
			<li>Sdnnt instance: <strong>$sdnntendoint$</strong></li>
		</ul>
	</div>

</div>
>>