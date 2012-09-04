/** sablona pro replikace */
group replicationTemplate;

/** restart button */
restartbutton(ctx) ::=<<
    $if(ctx.restartButtonEnabled)$
        "Restart": bind(function() {
            this.dialog.dialog("close");
            window.close();
            this.restart();
         },this),
    $endif$
>>

/** js pro dialog */
dialogscripts(ctx)  ::=<<
            <script type="text/javascript">
                var dictionary = null;
                
                function ReplicationTemplateOutput() {}
                ReplicationTemplateOutput.prototype.restart=function() {
                                        
                }
                
                ReplicationTemplateOutput.prototype.processFormSend = function() {
                    var charsThatMustBeEscaped = [':',';','}','{'];
                    var handle = \$('#handle').val().escapeChars(charsThatMustBeEscaped);
                    var user = \$('#user').val().escapeChars(charsThatMustBeEscaped);
                    var password = \$('#pswd').val().escapeChars(charsThatMustBeEscaped);
                    var restartUUID = "";
                    
                    var url = "lr?action=form_post&def=k4_replication&paramsMapping={url="+handle+";username="+user+";pswd="+password+"}&out=text";
                    processStarter("k4_replication").start(url);
                        
                }
                
                
                ReplicationTemplateOutput.prototype.openDialog=function() {
                    if (this.dialog) {
                        this.dialog.dialog('open');
                    } else {
                        this.dialog = \$('#replicationoutput').dialog({
                            width:800,
                            height:600,
                            modal:true,
                            title: '$ctx.title$ ($ctx.pid$)',
                            buttons: {

                                $restartbutton(ctx=ctx)$                                 

                                "Close": function() {
                                    \$(this).dialog("close");
                                    window.close();
                                }
                            }
                        });
                    }
                }
                
                \$(document).ready(function(){
                    var i18nurl = "i18n?action=bundle&name=labels&format=json";
                    \$.getJSON(i18nurl, function(data){
                        dictionary=data.bundle;
                        var output = new ReplicationTemplateOutput();
                        output.openDialog();
                    });
    
                });
            </script>
>>

/** obsah komentare */
dialogcontent(ctx)  ::=<<


        <div id="replicationoutput" style="display:none">
            <h3> Replikace $ctx.type$: $ctx.title$ </h3>
            <div style="font-size: 85%;font-family:monospace; margin-left: 10px;">
                <ul>
                    <li>Vydavatelé:$ctx.publishers;separator=","$ </li>
                    <li>Tvůrci:$ctx.creators;separator=","$</li>
                    <li>Identifikátory:<strong>$ctx.identifiers;separator=","$</strong></li>
                    <li>Datum:$ctx.date;separator=","$</li>
                </ul>
            </div>
                
            <div style="margin: 5px; border-left: 1px solid black; height:50">     
                 <div style="clear:both;" $failed(ctx=ctx,property="firstPhaseFailed")$> $icon(ctx=ctx,property="firstPhaseFailed")$  <span style="float:left; font-size:14; margin-left:15px;">Stahnuti deskriptoru pro replikace</span> </div>
                 <div style="clear:both;" $failed(ctx=ctx,property="secondPhaseFailed")$> $icon(ctx=ctx,property="secondPhaseFailed")$ <span style="float:left; font-size:14;  margin-left:15px;" >Iteracni faze - import jednotlivych objektu</span> </div> 
                 <div style="clear:both;" $failed(ctx=ctx,property="thirdPhaseFailed")$> $icon(ctx=ctx,property="thirdPhaseFailed")$ <span style="float:left; font-size:14;  margin-left:15px;">Indexace replikovaneho</span>  </div>
            </div>

        
            <div id="k4_replication_error_form" style="errordisplay(ctx=ctx)">
                <h3> Chybovy vystup: </h3>
                <div id="k4_replication_error_message" style="color:red; width:500px">
                    <pre style="font-family:Monaco, 'DejaVu Sans Mono', 'Bitstream Vera Sans Mono', 'Lucida Console', monospace">
$if(ctx.errorOccured)$
$ctx.formatedErrorMessage$    
$endif$
                    </pre>
                </div>
                
                
                <table>
                    <tr><td><label> Uzivatelske jmeno uzivane (pro restart) </label></td></tr>
                    <tr><td><input type="text">  </input></td></tr>
                    <tr><td><label> Heslo (pro restart)</label></td></tr>
                    <tr><td><input type="password">  </input></td></tr>
                </table>
            </div>  

        </div>
>>



/** display block | none */
errordisplay(ctx) ::=<<
$if(ctx.errorOccured)$
display:none;
$else$
display:block;
$endif$
>>

/** class failed */
failed(ctx,property) ::=<<
    $if(ctx.(property))$
        class="ui-state-error"
    $endif$
>>

/** ok | cancel icon */
icon(ctx,property) ::=<<
    $if(ctx.(property))$
    <span style="float:left" class="ui-icon ui-icon-close"></span>
    $else$
    <span style="float:left;" class="ui-icon ui-icon-check"></span>
    $endif$
>>



/** Body */
body(ctx) ::=<<
    <body>
        $dialogcontent(ctx=ctx)$
    </body>
>>

/** Hlavicka */
head(ctx) ::=<<

        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" >
            <meta http-equiv="Pragma" content="no-cache">
            <meta http-equiv="Cache-Control" content="no-cache">
            <meta name="description"
             content="$ctx.title$ ($ctx.pid$)" />
            
            <link rel="icon" href="img/favicon.ico" />
            <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
            <link rel="stylesheet" href="css/styles.css" type="text/css" />
            <link rel="StyleSheet" href="css/layout-default-latest.css" type="text/css" />
            <link type="text/css" href="css/smoothness/jquery-ui.custom.css" rel="Stylesheet" />
            
            
            <script src="js/jquery-1.5.1.min.js" type="text/javascript"></script>
            <script src="js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>
            
            <script  src="js/settings.js" language="javascript" type="text/javascript"></script>
            <script src="js/jquery.mousewheel.js" type="text/javascript" ></script>
            <script src="js/jquery.layout-latest.js" type="text/javascript" ></script>

            <script  src="js/cmn/cmn.js" language="javascript" type="text/javascript"></script>

            $dialogscripts(ctx=ctx)$            
        </head>
>>

/** cely vystup */
outputs(context) ::=<<

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"> 
<html>

$head(ctx=context)$
$body(ctx=context)$

</html>
>>