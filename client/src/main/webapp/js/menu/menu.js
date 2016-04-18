/**
 * Controll menu actions
 * @constructor
 */
function MenuActionsControll() {
    this.ctx = {};
}

MenuActionsControll.prototype = {
        /*
        'ctx': {
                                
        },*/   
        
        '_instantiate':function(objname) {
                return eval('(function() { return new '+ objname+'();})()');      
        },

        'createAction':function(action) {
            var retval = {
                        "name":action.name,
                        "i18nkey":action.i18nkey,
                        "object":this._instantiate(action.object)
                };
            if (action["icon"]) {
                retval["icon"] = action["icon"];
            }
            
            return retval;
        },

        'initalizeActions':function(actions) {
                var nacts = _.map(actions, _.bind(function(a) { 
                        return this.createAction(a);
                },this));
                this.ctx["actions"]=nacts;
        },
        
        'displayAction':function(actname) {
                $("#ctxmenu-"+actname).removeClass('ctxmenuitemdisabled');
                $("#ctxmenu-"+actname).addClass('ctxmenuitemenabled');
        },

        'hideAction':function(actname) {
                $("#ctxmenu-"+actname).addClass('ctxmenuitemdisabled');
                $("#ctxmenu-"+actname).removeClass('ctxmenuitemenabled');
        },
 
       'refreshActions':function() {
                var act = null;
                _.each(this.ctx.actions, _.bind(function(itm) {
                        if (itm.object.enabled()) {
                                this.displayAction(itm.name);
                        } else {
                                this.hideAction(itm.name);
                        }
                }, this));
        },

        'action':function(actname) {
                var act = null;
                _.each(this.ctx.actions, function(itm) {
                        if (itm.name === actname) {
                                act = itm.object;
                        }
                });
                if (act) {
                        act.doAction();                        
                } 
        }
}


/**
 * Persistent url dialog
 * @constructor
 */
function PersistentURL() {}

PersistentURL.prototype = {
        'doAction':function() { 
                cleanWindow();
                divopen("#viewer div.persistents");
                var sel = K5.api.ctx.item.selected;
		var handleurl = window.location.protocol+"//"+window.location.host+"/client/handle/"+sel;
                $("#persisturl").val(handleurl);
                $("#persisturl").select();
        },
        'enabled': function() {
                return true;
        }
}

// audio data
function _isAudio() {
    var audiomodels = ["soundrecording","track"];
    var selected = K5.api.ctx.item.selected; 
    var itm = K5.api.ctx.item[selected];
    var audio = _.reduce(audiomodels, function(memo, value, index) {
        if (!memo) {
            memo = itm["model"] === value;
        }
        return memo;
    }, false);
    return audio;
}

function _isPDF() {
    var selected = K5.api.ctx.item.selected; 
    var itm = K5.api.ctx.item[selected];
    var streams = itm.streams;
    return (itm.datanode &&  streams["IMG_FULL"].mimeType == "application/pdf");
}


/**
 * Display clipboard  
 * @constructor
 */
function DisplayClipboard() {}

DisplayClipboard.prototype =  {
        'doAction':function() { 
               K5.gui.clipboard.display();
        },
        'enabled': function() {
                var arr = K5.gui.clipboard.ctx.selected;         
                return arr.length > 0;
        }
}


/**
 * Print one page pdf 
 * @constructor 
 */
function PDFOnePage() {}
PDFOnePage.prototype = {

        'doAction':function() { 
                cleanWindow();
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                var page = removeHistoryPostfix(K5.api.ctx.item.selected);
                K5.outputs.pdf.page(page);
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                    if (itm['rights']) {
                        var flag = itm['rights']['read'] && 
                                   itm['rights']['pdf_resource'] && 
                                   itm['rights']['show_client_pdf_menu'] && 
                                   itm['rights']['show_client_print_menu']; 
                        if (!flag) {
                           return false;
                        }
                    }
                    if ((!_isAudio()) && (!_isPDF())) {
                        return K5.api.ctx.item[selected].datanode; 
                    } else return false;
                } else {
                        return false;
                }
        },
        "group":  "PDF"
}


/**
 * Download page
 * @constructor
 */
function DownloadPage() {}
DownloadPage.prototype = {
       'doAction':function() {
                var selected = K5.api.ctx.item.selected; 
                window.location.assign("api/item/"+selected+"/full?asFile=true")
                cleanWindow();
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                        return itm.datanode &&  (!K5.gui.selected.disabledDisplay);
                } else {
                        return false;
                }
        },
        "group": "RAW"
}

/**
 * Print only one part of page
 * @constructor
 */
function PrintPartPage() {}

PrintPartPage.prototype = {

       'doAction':function() {
                function ntab(url) {
                        var win = window.open(url, '_blank');
                        win.focus();
                }
                var sel = K5.api.ctx.item.selected;
                var itm = K5.api.ctx.item[sel];
                ntab("?page=printpart&item="+removeHistoryPostfix(itm.pid));
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                    if (!_isAudio() && (!_isPDF())) {
                        return K5.api.ctx.item[selected].datanode; 
                    } else return false;
                } else {
                        return false;
                }
       },
       "group": "PDF"

}


/**
 * Print only one part of page
 * @constructor
 */
function PrintPartItem() {
        this.diff = 25;
}

PrintPartItem.prototype = {
        'doAction':function() {
                cleanWindow();
                $('#viewer>div.container')
                       .append('<div id="overlay">'+
                                '<div id="okButton" class="small"></div>'+
                                '<div id="cancelButton" class="small"></div>'+

                                '<div id="selectbox"></div>'+
                                '<div id="left-top" class="point between"></div>'+
                                '<div id="right-top" class="point  between"></div>'+
                                '<div id="left-bottom" class="point  between"></div>'+
                                '<div id="right-bottom" class="point  between"></div>'+
                        '</div>'); 

                $("#okButton").load("svg.vm?svg=ok");
                $("#cancelButton").load("svg.vm?svg=close");

                $("#left-top").load("svg.vm?svg=topleft");
                $("#right-top").load("svg.vm?svg=topright");

                $("#left-bottom").load("svg.vm?svg=bottomleft");
                $("#right-bottom").load("svg.vm?svg=bottomright");

                $("#cancelButton").click(function() {
                        $("#header").show();
                        $("#metadata").show();
                        $(".thumbs").show();

                        var rect = [];
                        rect.push(K5.gui.selected.edit.selection.x1);
                        rect.push(K5.gui.selected.edit.selection.y1);

                        rect.push(K5.gui.selected.edit.selection.x2);
                        rect.push(K5.gui.selected.edit.selection.y2);

                        K5.eventsHandler.trigger("gui/item/crop/stop",rect);

                        $("#overlay").remove();
                        $("#header").show();
                        
                        
                        K5.gui.selected.showLeftrightbuttons();


                        if (K5.gui.selected.selectionEndNotif) {
                            K5.gui.selected.selectionEndNotif();
                        }
                });

                $("#okButton").click(function() {
                        // select selection
                    
                        $("#header").show();
                        $("#metadata").show();
                        $(".thumbs").show();

                        var rect = [];

                        rect.push(K5.gui.selected.edit.selection.x1);
                        rect.push(K5.gui.selected.edit.selection.y1);

                        rect.push(K5.gui.selected.edit.selection.x2);
                        rect.push(K5.gui.selected.edit.selection.y2);
                        
                        K5.eventsHandler.trigger("gui/item/crop/stop",rect);

                        var offset = [];
                        offset.push($("#overlay").offset().left);
                        offset.push($("#overlay").offset().top);

                        K5.gui.selected.crop(rect, offset);

                        $("#overlay").remove();
                        $("#header").show();

                        K5.gui.selected.showLeftrightbuttons();

                        if (K5.gui.selected.selectionEndNotif) {
                            K5.gui.selected.selectionEndNotif();
                        }
                });

                $( "#left-top" ).draggable({
                        start:function( event, ui ) {
                                K5.gui.selected.edit.selection.startLeftTop(ui.position);
                        },
                        drag: function( event, ui ) {
                                var v = K5.gui.selected.edit.selection.getSelectionBoxPosition();  
                                var t = ui.position;                                
                                if ( t.top > v.y2-25) {
                                        ui.position.top=v.y2-25;
                                }
                                if (t.left > v.x2-25) {
                                        ui.position.left=v.x2-25;
                                }
                                K5.gui.selected.edit.selection.dragLeftTop(ui.position);
                        }
                });

                $( "#right-top" ).draggable({
                        start:function( event, ui ) {
                                K5.gui.selected.edit.selection.startRightTop(ui.position);
                        },
                        drag: function( event, ui ) {
                                var v = K5.gui.selected.edit.selection.getSelectionBoxPosition();  
                                var t = ui.position;                                
                                if ( t.top > v.y2 - 25) {
                                        ui.position.top=v.y2-25;
                                } 
                                if (t.left < v.x1+25) {
                                        ui.position.left=v.x1+25;
                                }

                                K5.gui.selected.edit.selection.dragRightTop(ui.position);
                        }
                });


                $( "#left-bottom" ).draggable({
                        start:function( event, ui ) {
                                K5.gui.selected.edit.selection.startLeftBottom(ui.position);
                        },
                        drag: function( event, ui ) {
                                var v = K5.gui.selected.edit.selection.getSelectionBoxPosition();  
                                var t = ui.position;                                
                                if ( t.top < v.y1+25) {
                                        ui.position.top=v.y1+25;
                                }
                                if (t.left > v.x2-25) {
                                        ui.position.left=v.x2-25;
                                }
                                K5.gui.selected.edit.selection.dragLeftBottom(ui.position);
                        }
                });

                $( "#right-bottom" ).draggable({
                        start:function( event, ui ) {
                                K5.gui.selected.edit.selection.startRightBottom(ui.position);
                        },
                        drag: function( event, ui ) {
                                var v = K5.gui.selected.edit.selection.getSelectionBoxPosition();  
                                var t = ui.position;                                
                                if ( t.top < v.y1+25) {
                                        ui.position.top=v.y1+25;
                                }
                                if (t.left < v.x1+25) {
                                        ui.position.left=v.x1+25;
                                }
                                K5.gui.selected.edit.selection.dragRightBottom(ui.position);
                        }
                });


                setTimeout(function() {

                        $("#header").hide();
                        $("#metadata").hide();
                        $(".thumbs").hide();
                        
                        
                        if (K5.gui.selected.fit) {
                            K5.gui.selected.fit();
                        }
                        if (K5.gui.selected.hideLeftrightbuttons) {
                            K5.gui.selected.hideLeftrightbuttons();
                        }

                        if (K5.gui.selected.selectionStartNotif) {
                            K5.gui.selected.selectionStartNotif();
                        }

                        K5.gui.selected["edit"]= {};
                        K5.gui.selected.edit.selection = new SelectObject(K5);
                        
                        //K5.gui.selected.edit.selection.changeAndStoreWidth();
                        
                        K5.gui.selected.edit.selection.page();
                                
                },200);
                
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                    if (itm['rights']) {
                        var flag = itm['rights']['read'] && 
                                   itm['rights']['pdf_resource'] && 
                                   itm['rights']['show_client_pdf_menu'] && 
                                   itm['rights']['show_client_print_menu']; 
                        if (!flag) {
                           return false;
                        }
                    }
                    if ((!_isAudio()) && (!_isPDF())) {
                        return K5.api.ctx.item[selected].datanode; 
                    } else return false;
                } else {
                        return false;
                }
        },
        "group": "PDF"

}


function PrintPage() {}

PrintPage.prototype = {
        'doAction':function() {
            cleanWindow();
            var page = removeHistoryPostfix(K5.api.ctx.item.selected);
            K5.outputs.print.page(page);
    },
    'enabled': function() {
            var selected = K5.api.ctx.item.selected; 
            var itm = K5.api.ctx.item[selected];
            if (!itm['forbidden']) {
                if (itm['rights']) {
                    var flag = itm['rights']['read'] && 
                               itm['rights']['pdf_resource'] && 
                               itm['rights']['show_client_pdf_menu'] && 
                               itm['rights']['show_client_print_menu']; 
                    if (!flag) {
                       return false;
                    }
                }
                if ((!_isAudio()) && (!_isPDF())) {
                    return K5.api.ctx.item[selected].datanode; 
                } else return false;
            } else {
                    return false;
            }
    },
    "group": "PRINT"
}


function PrintSiblings() {}
PrintSiblings.prototype = {
        'doAction':function() {
                cleanWindow();
                //var page = removeHistoryPostfix(K5.api.ctx.item.selected);
                var v = K5.api.ctx.item.selected;
                K5.outputs.print.siblings(v);
        },
        
        'enabled': function() {
            var selected = K5.api.ctx.item.selected; 
            var itm = K5.api.ctx.item[selected];
            if (!itm['forbidden']) {
                if (itm['rights']) {
                    var flag = itm['rights']['read'] && 
                               itm['rights']['pdf_resource'] && 
                               itm['rights']['show_client_pdf_menu'] && 
                               itm['rights']['show_client_print_menu']; 
                    if (!flag) {
                       return false;
                    }
                }
                if ((!_isAudio()) && (!_isPDF())) {
                    return K5.api.ctx.item[selected].datanode; 
                } else return false;
            } else {
                    return false;
            }
        },
        "group": "PRINT"
}


function PrintTitle() {}

PrintTitle.prototype = {
        'doAction':function() {
                cleanWindow();
                var page = removeHistoryPostfix(K5.api.ctx.item.selected);
                K5.outputs.print.title(page);
        },
        
        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                 
                    if (itm['rights']) {
                        var flag = itm['rights']['read'] && 
                                   itm['rights']['pdf_resource'] && 
                                   itm['rights']['show_client_pdf_menu'] && 
                                   itm['rights']['show_client_print_menu']; 
                        if (!flag) {
                           return false;
                        }
                    }
 
                    if ((!_isAudio()) && (!_isPDF())) {
                        var children = K5.api.ctx.item[selected]["children"];
                        if (children) {
                            var pages = _.reduce(children, function(memo, value, index) {
                                if (value["model"] === "page") {
                                    memo.push(value);
                                }
                                return memo;
                            }, []);
                            if (pages.length > 0) {
                                return true;
                            } else return false;
                            
                        } else {
                            return false;
                        }
                    } else return false;
                } else {
                        return false;
                }
        },
        "group": "PRINT"
}




function PDFSiblingsTitle() {
    this.ctx = {};
    $.getJSON("api/pdf", _.bind(function(conf) {
        this.ctx["conf"] = conf;
    },this));
    this.dialog = null;
}

PDFSiblingsTitle.prototype = {
        'doAction':function() {
                cleanWindow();
                var v = K5.api.ctx.item.selected;
                K5.outputs.pdf.siblings(v);
        },
        'message' :function() {
            if (this.ctx && this.ctx.conf) { 
                if (this.ctx.conf.pdfMaxRange !== "unlimited") {
		    var f = K5.i18n.ctx.dictionary['ctx.actions.pdftitle.message.1'];
		    var s = K5.i18n.ctx.dictionary['ctx.actions.pdftitle.message.2'];
                    return f+this.ctx.conf.pdfMaxRange+s; 
                }
            } else return null;
        },

        'enabled': function() {
            var selected = K5.api.ctx.item.selected; 

            var itm = K5.api.ctx.item[selected];
            if (!itm['forbidden']) {
                if (itm['rights']) {
                    var flag = itm['rights']['read'] && 
                               itm['rights']['pdf_resource'] && 
                               itm['rights']['show_client_pdf_menu'] && 
                               itm['rights']['show_client_print_menu']; 
                    if (!flag) {
                       return false;
                    }
                }

                if ((!_isAudio()) && (!_isPDF())) {
                    return K5.api.ctx.item[selected].datanode; 
                } else return false;
            } else {
                    return false;
            }
        }
}


function PDFTitle() {
    this.ctx = {};
    $.getJSON("api/pdf", _.bind(function(conf) {
        this.ctx["conf"] = conf;
    },this));
}

PDFTitle.prototype = {
        'doAction':function() {
                cleanWindow();
                //K5.outputs.pdf.asyncTitle(K5.api.ctx.item.selected);
                K5.outputs.pdf.title(K5.api.ctx.item.selected);
        },
        'message' :function() {
            //this.ctx.conf
            if (this.ctx && this.ctx.conf) { 
                if (this.ctx.conf.pdfMaxRange !== "unlimited") {
                var f = K5.i18n.ctx.dictionary['ctx.actions.pdftitle.message.1'];
                var s = K5.i18n.ctx.dictionary['ctx.actions.pdftitle.message.2'];
                    return f+this.ctx.conf.pdfMaxRange; 
                }
            } else return null;
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                    if (itm['rights']) {
                        var flag = itm['rights']['read'] && 
                                   itm['rights']['pdf_resource'] && 
                                   itm['rights']['show_client_pdf_menu'] && 
                                   itm['rights']['show_client_print_menu']; 
                        if (!flag) {
                           return false;
                        }
                    }
                    if ((!_isAudio()) && (!_isPDF())) {
                        var children = K5.api.ctx.item[selected]["children"];
                        if (children) {
                            var pages = _.reduce(children, function(memo, value, index) {
                                if (value["model"] === "page") {
                                    memo.push(value);
                                }
                                return memo;
                            }, []);
                            if (pages.length > 0) {
                                return true;
                            } else return false;
                            
                        } else {
                            return false;
                        }
                    } else return false;
                } else {
                        return false;
                }
        },
        "group": "PDF"

}



/**
 * Download OCR
 * @constructor
 */
function DownloadOCR() {}
DownloadOCR.prototype = {
        'doAction':function() {
                cleanWindow();

                var sel = K5.api.ctx.item.selected;
                var itm = K5.api.ctx.item[sel];

                $.get("api/item/"+itm.pid+"/streams/TEXT_OCR",_.bind(function(data) {
                        $("#ocr_data").val(data);
                        $("#ocr_data").select();
                        
                },this)).fail(function() {
                        $("#ocr_data").val("");
                });
                divopen("#viewer div.ocr");
        },
        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                        var datanode = K5.api.ctx.item[selected].datanode;
                        var ocr = K5.api.ctx.item[selected]["streams"] && K5.api.ctx.item[selected]["streams"]["TEXT_OCR"];
                        return ocr && datanode; 
                } else {
                        return false;
                }
        },
        "group": "RAW"
}


function _socialUrl() {
        var ind = window.location.href.indexOf("index.vm");
        var v = window.location.href.substring(0, ind+"index.vm".length)+"?page=doc#!"+K5.api.ctx.item.selected;
        v = encodeURIComponent(v);
        return v;
}

function GooglePlusShare() {}
GooglePlusShare.prototype = {
        'doAction':function() {
                var share = "https://plus.google.com/share?url="+_socialUrl();
                window.open(share,'', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600');return false;
        },
        'enabled':function() {
                return true;
        }
}

function FacebookShare() {}
FacebookShare.prototype = {
        'doAction':function() {
                var share = "https://www.facebook.com/sharer/sharer.php?u="+_socialUrl();
                window.open(share,'', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600');return false;

        },
        'enabled':function() {
                return true;
        }
}

