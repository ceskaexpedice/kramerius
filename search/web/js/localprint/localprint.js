function LocalPrint() {
        //this.height
        this.settings = {output:'html',page:'A4'};
        this.printPartDialog = null;
        this.printSetupDialog = null;

        this.printErrorDialog = null;        
}

LocalPrint.prototype = {

    setup:function(set) {
        this.settings = set;
    },

    initSelected: function() {
        this.structs = map(function(pid) {
                var divided = pid.split("_");
                var structure = {
                        models:divided[0],
                        pid:divided[1]
                };
            return structure;
        }, getAffectedPids());
    },    

    accessDeniedDialog: function() {
        if (this.printErrorDialog) {
            this.printErrorDialog.dialog('open');
        } else {
                var strings = '<div id="printAccessDenied"><table style="width:100%; height:100%;"><tr><td style="text-align: center;vertical-align: center;">'+dictionary['rightMsg.localprint']+'</td></tr></table></div>';
                $(document.body).append(strings);
                    this.printErrorDialog = $('#printAccessDenied').dialog({
                        width:350,
                        height:250,
                        modal:true,
                        title:dictionary["administrator.menu.printlocalerror"],
                        buttons:[{
                                        text: dictionary['common.close'],
                                        click: function() {
                                            $(this).dialog("close");
                                        }
                                }]
                        });
               }

    },        
    
    printFull:function() {        
        this.initSelected();

        function printURL(settings) {
            var layout = 'portrait'; 

            var pStr = reduce(function(memo, value, status) {
                    memo = memo + encodeURIComponent(value.pid);
                    memo = memo + (status.last ? "": ",");
                    return memo;
                }, "",this.structs);

            if (settings.output === 'html') {
                var transcode = viewerOptions.isContentDJVU() || viewerOptions.isContentPDF();
                transcode = (!transcode) ?  viewerOptions.mimeType.indexOf('jp2')> 0 : transcode; 
                window.open("inc/_iprint.jsp?pids="+pStr+"&transcode="+transcode+"&page="+settings.page+"&layout="+layout, "_blank");        
            } else {
                window.open("localPrintPDF?pids="+pStr+"&pagesize="+settings.page+"&imgop=FULL", "_blank");        
            }
        }

        $.get("isActionAllowed?action=read&pid="+this.structs[0].pid,bind(function(data) {
                var flag = data[this.structs[0].pid];                        
                if (flag) {
                        if (this.printSetupDialog) {
                            this.printSetupDialog.dialog('open');
                        } else {
                            $(document.body).append('<div id="printSetup"></div>');
                            this.printSetupDialog = $('#printSetup').dialog({
                                        width:350,
                                        height:250,
                                        modal:true,
                                        title:dictionary["administrator.menu.printlocalsetup"],
                                        buttons:  [ 
                                        {
                                                text: dictionary['common.ok'],
                                                click: bind(function() {
                                                            bind(printURL,this)(this.settings);
                                                            this.printSetupDialog.dialog("close");
                                                }, this)
                                        },
                                        {
                                                text: dictionary['common.close'],
                                                click: function() {
                                                    $(this).dialog("close");
                                                }
                                        }]
                                });
                        }
                        $.get("inc/_iprint_setup.jsp?pid="+this.structs[0].pid,function(data) {
                                $('#printSetup').html(data);
                        });
                } else {
                        this.accessDeniedDialog();
                }
        },this));
    },

        

    printPart:function() {
        this.initSelected();
        $.get("isActionAllowed?action=read&pid="+this.structs[0].pid,bind(function(data) {
                var flag = data[this.structs[0].pid];                        
                if (flag) {
                        if (this.printPartDialog) {
                            this.printPartDialog.dialog('open');
                        } else {
                            $(document.body).append('<div id="selectPart">'+
                                '</div>');

                            this.printPartDialog = $('#selectPart').dialog({
                                width:800,
                                height:640,
                                modal:true,
                                title:dictionary["administrator.menu.selectandprintlocal"],
                                buttons:  [
                                            {
                                                text: dictionary['common.ok'],
                                                click: bind(function() {

                                                    var pStr = reduce(function(memo, value, status) {
                                                            memo = memo + encodeURIComponent(value.pid);
                                                            memo = memo + (status.last ? "": ",");
                                                            return memo;
                                                    }, "",this.structs);
        
                                                    var transcode = viewerOptions.isContentDJVU() || viewerOptions.isContentPDF();
                                                    transcode = (!transcode) ?  viewerOptions.mimeType.indexOf('jp2')> 0 : transcode; 
                                                    var positions = window.selObjects.relativePositions();

                                                    var positionsString = "xpos="+positions[0]+"&ypos="+positions[1]+"&width="+(positions[2]-positions[0])+"&height="+(positions[3]-positions[1]);
                                                    if (this.settings.output === 'html') {
                                                            window.open("inc/_iprint_select_part_done.jsp?pid="+pStr+"&transcode="+transcode+"&"+positionsString, "_blank");
                                                    } else {
                                                            window.open("localPrintPDF?pids="+pStr+"&pagesize="+this.settings.page+"&imgop=CUT"+"&"+positionsString, "_blank");        
                                                    }
                                                    this.printPartDialog.dialog("close");
                                                },this)},
                                                {
                                                        text: dictionary['common.close'],
                                                        click: function() {
                                                            $(this).dialog("close");
                                                        }
                                                }
                                        ]
                            });
                        }
                
                        $.get("inc/_iprint_select_part.jsp?output="+this.settings.output,function(data) {
                                $('#selectPart').html(data);
                                
                                var simg = new Image();
        
                                simg.onload = function() {
                                    var w = simg.width;
                                    var h = simg.height;
                                    var pomer = h/w;
                                        
                                    var cw = $('#imagepart').width();
                                    var ch = $('#imagepart').height();
        
                                    var jqimg = $("<img/>",{'src':simg.src});
                                    jqimg.css('height', ch);
                                    jqimg.css('width', (ch/pomer));
                                        
                                    $('#imagepart').append(jqimg);
        
                                    var l = $('#imagepart img').position().left;
                                    var t = $('#imagepart img').position().top;
        
                                    $('#overlay').show();
                                        
                                    $('#overlay').css('left',l);
                                    $('#overlay').css('top',t);
                                    $('#overlay').css('width',cw);
                                    $('#overlay').css('height',ch);
        
                                    $("#overlay").css({top: t, left: l, position:'absolute'});
                                        
                                    $.getScript( "js/localprint/selects/selects.js" )
                                    .done(function( script, textStatus ) {
                                        window.selObjects = new SelectObject();
                                        window.selObjects.center();
                                    }).fail(function( jqxhr, settings, exception ) {
                                        alert("funkce neni k dispozici !");
                                    });
                                };

                                var pids = getAffectedPids();
                                var structs = map(function(pid) {
                                    var divided = pid.split("_");
                                    var structure = {
                                        models:divided[0],
                                        pid:divided[1]
                                    };
                                    return structure;
                                }, pids);
                                    
                                var transcode = viewerOptions.isContentDJVU() || viewerOptions.isContentPDF();
                                transcode = (!transcode) ?  viewerOptions.mimeType.indexOf('jp2')> 0 : transcode; 
                                var url = "img?pid="+encodeURIComponent(structs[0].pid)+"&stream=IMG_FULL&action=";
                                var action = (transcode ? "TRANSCODE":"GETRAW");
                                url = url+action;
        
                                simg.src = url;
                            });                        
                        } else {
                                this.accessDeniedDialog();
                        }
                },this));
            }    
}

var localprint = new LocalPrint();
