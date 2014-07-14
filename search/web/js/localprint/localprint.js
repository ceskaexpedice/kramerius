function LocalPrint(output) {
        //this.height
        this.output = 'html';
        this.printPartDialog = null;
        this.printSetupDialog = null;
}

LocalPrint.prototype = {


    printFull:function(out) {        
        this.output = out;
        function printURL(output) {
            var layout = 'portrait'; 
            var page = $( "#page option:selected" ).val();
            var pids = getAffectedPids();
            var structs = map(function(pid) {
                var divided = pid.split("_");
                var structure = {
                    models:divided[0],
                    pid:divided[1]
                };
                return structure;
            }, pids);

            var pStr = reduce(function(memo, value, status) {
                    memo = memo + encodeURIComponent(value.pid);
                    memo = memo + (status.last ? "": ",");
                    return memo;
                }, "",structs);

            if (output === 'html') {
                var transcode = viewerOptions.isContentDJVU() || viewerOptions.isContentPDF();
                window.open("inc/_iprint.jsp?pids="+pStr+"&transcode="+transcode+"&page="+page+"&layout="+layout, "_blank");        
            } else {
                window.open("localPrintPDF?pids="+pStr+"&pagesize="+page+"&imgop=FULL", "_blank");        
            }
        }
        
        if (this.output === 'html') {
            printURL(this.output);
        } else {
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
                                        click: function() {
                                            printURL(this.output);
                                            $(this).dialog("close");
                                        }
                                },
                                {
                                        text: dictionary['common.close'],
                                        click: function() {
                                            $(this).dialog("close");
                                        }
                                }]
                    });
                    $.get("inc/_iprint_setup.jsp",function(data) {
                        $('#printSetup').html(data);
                    });
                }
        }       

    },

    printPart:function(out) {
        this.output = out;
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
                                    var pids = getAffectedPids();
                                    var structs = map(function(pid) {
                                        var divided = pid.split("_");
                                        var structure = {
                                            models:divided[0],
                                            pid:divided[1]
                                        };
                                        return structure;
                                    }, pids);

                                    var pStr = reduce(function(memo, value, status) {
                                            memo = memo + encodeURIComponent(value.pid);
                                            memo = memo + (status.last ? "": ",");
                                            return memo;
                                    }, "",structs);

                                    var page = $( "#pagepart option:selected" ).val();
                                    var transcode = viewerOptions.isContentDJVU() || viewerOptions.isContentPDF();
                                    var positions = window.selObjects.relativePositions();

                                    var positionsString = "xpos="+positions[0]+"&ypos="+positions[1]+"&width="+(positions[2]-positions[0])+"&height="+(positions[3]-positions[1]);
                                    if (this.output === 'html') {
                                            window.open("inc/_iprint_select_part_done.jsp?pid="+pStr+"&transcode="+transcode+"&"+positionsString, "_blank");
                                    } else {
                                            window.open("localPrintPDF?pids="+pStr+"&pagesize="+page+"&imgop=CUT"+"&"+positionsString, "_blank");        
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
        
        $.get("inc/_iprint_select_part.jsp?output="+this.output,function(data) {
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
                var url = "img?pid="+encodeURIComponent(structs[0].pid)+"&stream=IMG_FULL&action=";
                var action = (transcode ? "TRANSCODE":"GETRAW");
                url = url+action;

                simg.src = url;
            });
    }    
}

var localprint = new LocalPrint();
