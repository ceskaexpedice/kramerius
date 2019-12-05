/** sablona pro replikace */
group replicationTemplate;

/** restart button */
restartbutton(ctx) ::=<<
    $if(ctx.restartButtonEnabled)$
         '$ctx.bundle.("common.restart")$': bind(function() {
            this.dialog.dialog("close");
            this.restart();
         },this),
    $endif$
>>

/** js pro dialog */
dialogscripts(ctx)  ::=<<
            <script type="text/javascript">
                var dictionary = null;
                
                function ReplicationTemplateOutput() {}

                ReplicationTemplateOutput.prototype.restart = function() {
                    var charsThatMustBeEscaped = [':',';','}','{'];
                    var handle = '$ctx.handle$'.escapeChars(charsThatMustBeEscaped);;
                    var user = \$('#user').val().escapeChars(charsThatMustBeEscaped);
                    var password = \$('#password').val().escapeChars(charsThatMustBeEscaped);
                    var restartUUID = "$ctx.lrProcess.uUID$".escapeChars(charsThatMustBeEscaped);;
                    
                    var url = "lr?action=form_post&def=k4_replication&paramsMapping="+encodeURI("{")+"url="+encodeURIComponent(handle)+";username="+encodeURIComponent(user)+";pswd="+encodeURI(password)+";previousProcess="+restartUUID+encodeURI("}")+"&out=text";
                    
                    processStarter("k4_replication").start(url, function() {
                          window.close();
                    });
                }
                
                
                ReplicationTemplateOutput.prototype.openDialog=function() {
                    if (this.dialog) {
                        this.dialog.dialog('open');
                    } else {
                        this.dialog = \$('#replicationoutput').dialog({
                            width:800,
                            height:600,
                            modal:true,
                            title: '$ctx.escapedTitle$ ($ctx.pid$)',
                            buttons: {

                                $restartbutton(ctx=ctx)$                                 

                                '$ctx.bundle.("common.close")$': function() {
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

/* prelozi model */
model(type) ::=<<fedora.model.$type$>>

/** obsah komentare */
dialogcontent(ctx)  ::=<<


        <div id="replicationoutput" style="display:none">
            <h3> Replikace $ctx.bundle.(model(type=ctx.type))$: $ctx.title$ </h3>
            <div style="font-size: 85%;font-family:monospace; margin-left: 10px;">
                <ul>
                    <li>$ctx.bundle.("replication.output.publishers")$:$ctx.publishers;separator=","$ </li>
                    <li>$ctx.bundle.("replication.output.creators")$:$ctx.creators;separator=","$</li>
                    <li>$ctx.bundle.("replication.output.identifiers")$:<strong>$ctx.identifiers;separator=","$</strong></li>
                    <li>$ctx.bundle.("replication.output.date")$:$ctx.date;separator=","$</li>
                </ul>
            </div>
                
            <div style="margin: 5px">     
                
$if(ctx.firstPhaseFilePresent)$
                 <div style="clear:both;" $failed(ctx=ctx,property="firstPhaseFailed")$> $icon(ctx=ctx,property="firstPhaseFailed")$  <span style="float:left; font-size:14; margin-left:15px;">$ctx.bundle.("replication.output.firstphase")$</span> </div>
$endif$
$if(ctx.secondPhaseFilePresent)$
                 <div style="clear:both;" $failed(ctx=ctx,property="secondPhaseFailed")$> $icon(ctx=ctx,property="secondPhaseFailed")$ <span style="float:left; font-size:14;  margin-left:15px;" >$ctx.bundle.("replication.output.secondphase")$</span> </div> 
$endif$
$if(ctx.thirdPhaseFilePresent)$
                 <div style="clear:both;" $failed(ctx=ctx,property="thirdPhaseFailed")$> $icon(ctx=ctx,property="thirdPhaseFailed")$ <span style="float:left; font-size:14;  margin-left:15px;">$ctx.bundle.("replication.output.thirdphase")$</span>  </div>
$endif$
            </div>

        
$if(ctx.errorOccured)$
            <div id="k4_replication_error_form" style="clear:both; errordisplay(ctx=ctx)">
                <div id="k4_replication_error_message" style="color:red; height:250px; width:100%; overflow:scroll;">
                    <pre style="font-family:Monaco, 'DejaVu Sans Mono', 'Bitstream Vera Sans Mono', 'Lucida Console', monospace">
$ctx.formatedErrorMessage$    
                    </pre>
                </div>
                
                
    $if(ctx.restartButtonEnabled)$
                <table width="100%">
                    <tr width="60%"><td></td><td align="right" width="40%"><label> $ctx.bundle.("replication.output.restartuser")$ </label></td></tr>
                    <tr width="60%"><td></td><td align="right" width="40%"><input id="user" type="text">  </input></td></tr>
                    <tr width="60%"><td></td><td align="right" width="40%"><label> $ctx.bundle.("replication.output.restartpassword")$</label></td></tr>
                    <tr width="60%"><td></td><td align="right" width="40%"><input id="password" type="password">  </input></td></tr>
                </table>
    $endif$
            </div>  
$endif$

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
            <script  src="js/admin/processes/processes.js" language="javascript" type="text/javascript"></script>
            <script  src="js/admin/processes/starter.js" language="javascript" type="text/javascript"></script>
            
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