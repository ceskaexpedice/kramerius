/**
 * Controll menu actions
 * @constructor
 */
function MenuActionsControll() {}

MenuActionsControll.prototype = {

        'ctx': {
                                
        },   
        
        '_instantiate':function(objname) {
                return eval('(function() { return new '+ objname+'();})()');      
        },

        'createAction':function(action) {
                return {
                        "name":action.name,
                        "i18nkey":action.i18nkey,
                        "object":this._instantiate(action.object)
                };
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
                var itm = K5.api.ctx.item[sel];
                $("#persisturl").val(itm.handle.href);
                $("#persisturl").select();
        },
        'enabled': function() {
                return true;
        }
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
                window.open("api/pdf/selection?pids="+ selected,"_blank");
        
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                        return K5.api.ctx.item[selected].datanode; 
                } else {
                        return false;
                }
        }
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
        }
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
                ntab("?page=printpart&item="+itm.pid);
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                        return K5.api.ctx.item[selected].datanode; 
                } else {
                        return false;
                }
       }
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

                        var rect = [];
                        rect.push(K5.gui.selected.edit.selection.x1);
                        rect.push(K5.gui.selected.edit.selection.y1);

                        rect.push(K5.gui.selected.edit.selection.x2);
                        rect.push(K5.gui.selected.edit.selection.y2);

                        K5.eventsHandler.trigger("gui/item/crop/stop",rect);

                        $("#overlay").remove();

                        $("#header").show();
                        K5.gui.selected.showLeftrightbuttons();
        
                });

                $("#okButton").click(function() {
                        // select selection
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
                        K5.gui.selected.fit();
                        K5.gui.selected.hideLeftrightbuttons();

                        K5.gui.selected["edit"]= {};
                        K5.gui.selected.edit.selection = new SelectObject(K5);
                        K5.gui.selected.edit.selection.page();
                                
                },200);
                
        },

        'enabled': function() {
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (!itm['forbidden']) {
                        return K5.api.ctx.item[selected].datanode; 
                } else {
                        return false;
                }
        }
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
        }
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

