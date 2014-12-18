
PersistentURLMenuItem



function SomeItem() {
        
}

SomeItem.prototype =  {
        'doAction':function() {
                console.log("something to do !");
        },
        'enabled': function() {
                return true;                
        }
}


function AddToClipboard() {}

AddToClipboard.prototype = {
        'perform':function() { 
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                K5.gui.clipboard.add(itm);
       }
}


function DisplayClipboard() {}

DisplayClipboard.prototype =  {
        'perform':function() { 
                cleanWindow();
                divopen("#viewer div.selections");
                K5.gui.selection = new Selections(K5);
                K5.gui.selection.open(K5.gui.clipboard.ctx.selected, this.performint);
        }
}

function PDFMenuItem() {}
PDFMenuItem.prototype = {
        'perform':function() { 

                cleanWindow();
                
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                window.open("api/pdf/selection?pids="+ selected,"_blank");
        }
}



function PDFAndPrintMenuItem() {}
PDFAndPrintMenuItem.prototype = {
        'loadflag':0,
        'perform':function() { 

                cleanWindow();
                
                var selected = K5.api.ctx.item.selected; 
                var itm = K5.api.ctx.item[selected];
                if (itm.model == 'page') {
                        /* jina akce */
                        var disp = (!K5.gui.selected.disabledDisplay);
                        if (disp) {
                               window.open("api/pdf/selection?pids="+ v,"_blank");
                        } 
                } else {
                        divopen("#viewer div.selections");

                        var sel = K5.api.ctx.item.selected;
                        K5.gui.selection = new Selections(K5);
                        K5.gui.selection.clearSelection();
                        K5.gui.selection.open(K5.api.ctx.item[sel].children, this.performint);
                }
        }
}

function PersistentURLMenuItem() {}

PersistentURLMenuItem.prototype = {
        'perform':function() { 
                this.performint();
        },

        'performint': function() {
                cleanWindow();
                divopen("#viewer div.persistents");
                var wind = window.location.href.split('?')
                if (wind && wind.length > 0) {
                        $("#persisturl").val(wind);
                }
        }

}


function DownloadItem() {}

DownloadItem.prototype = {
        'perform':function() {
                var selected = K5.api.ctx.item.selected; 
                window.location.assign("api/item/"+selected+"/full?asFile=true")
                cleanWindow();
        }
}

function PrintItem() {}
PrintItem.prototype = {
        'perform':function() {
                K5.gui.selected.hideInfo();
                window.open('print.vm?pid='+K5.api.ctx.item.selected+
                        "&full="+true, '_blank');
        }
}


function PrintPartItem() {}

PrintPartItem.prototype = {
        'perform':function() {
                K5.gui.selected.hideInfo();
                $('head').append('<link rel="stylesheet" href="css/selector.css" type="text/css" />');
                $('#viewer>div.container')
                       .append('<div id="overlay">'+

                                '<div id="okButton" class="small"></div>'+
                                '<div id="cancelButton" class="small"></div>'+

                                '<div id="selectbox"></div>'+
                                '<div id="left-top" class="point"></div>'+
                                '<div id="right-top" class="point"></div>'+
                                '<div id="left-bottom" class="point"></div>'+
                                '<div id="right-bottom" class="point"></div>'+
 
                        '</div>'); 

                $("#okButton").load("svg.vm?svg=ok");
                $("#cancelButton").load("svg.vm?svg=close");

                $("#cancelButton").click(function() {

                        var rect = [];
                        rect.push(K5.gui.selected.edit.selection.x1);
                        rect.push(K5.gui.selected.edit.selection.y1);

                        rect.push(K5.gui.selected.edit.selection.x2);
                        rect.push(K5.gui.selected.edit.selection.y2);

                        K5.eventsHandler.trigger("gui/item/crop/stop",rect);

                        $("#overlay").remove();
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
                });

                $.getScript( "js/edit/selects.js", _.bind(function( data, textStatus, jqxhr ) {
                        this.performint();
                },this)).fail(function(jqxhr, settings, exception) {
                        console.log( jqxhr.status ); // Data returned
                        console.log( settings ); // Success
                        console.log( exception ); // 200
                });

        },
        'performint': function() {        
        }

}

function AddFavoritesItem() {}
AddFavoritesItem.prototype = {
        'perform':function() {
                cleanWindow();

                K5.authentication.profileDisplay.appendToFavorites(K5.api.ctx.item.selected);
                K5.authentication.profileDisplay.open();
        }
}


function createAction(key,i18nkey,actionname,actionObj, visible) {
        var a =  {
                'key':key,
                'action':actionname,
                'i18nkey':i18nkey,
                'actionObject':actionObj,
                'visible':visible

        };
        return a;
}

var _actions = [];

_actions.push(createAction('pdf','ctx.actions.generatepdf','K5.gui.menu.doAction("pdf")', new PDFMenuItem(),
(function() {
        /*
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];
        if (itm.model == 'page') {
                return true;
        } else {
                return false;
        }*/
        return false;
})()
));

_actions.push(createAction('printandpdf','ctx.actions.printandpdf','K5.gui.menu.doAction("printandpdf")', new PDFAndPrintMenuItem(),
(function() {
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];
        if (itm.model == 'page') {
                //return(!K5.gui.selected.disabledDisplay);
                return false;
        } else {
            var found = _.reduce(itm.children, function(memo, value, index) {
                if (memo === null && value.model === 'page') {
                        memo = value;
                }
                return memo;
            }, null);
            return found != null;            
        }
})()
));

// TODO: co s tim ?
_actions.push(createAction('url','ctx.actions.persistenturl','K5.gui.menu.doAction("url")', new PersistentURLMenuItem(), true ));

_actions.push(createAction('download','ctx.actions.downloadoriginal','K5.gui.menu.doAction("download")',new DownloadItem(),
(function() {
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];
        return itm.datanode &&  (!K5.gui.selected.disabledDisplay);
})()
));


/** favorites */
_actions.push(createAction('favorites','ctx.actions.addtofavorites','K5.gui.menu.doAction("favorites")',new AddFavoritesItem(),
(function() {
        if (K5.authentication.profileDisplay) {
                return true;         
        } else return false;
})()
));


_actions.push(createAction('printpart','ctx.actions.printpart','K5.gui.menu.doAction("printpart")',new PrintPartItem(),
(function() {
        /*   
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];
        return itm.datanode && (!K5.gui.selected.disabledDisplay);
        */
        return false;
})()
));


_actions.push(createAction('print','ctx.actions.printpage','K5.gui.menu.doAction("print")',new PrintItem(),
(function() {
        /*
        var selected = K5.api.ctx.item.selected; 
        var itm = K5.api.ctx.item[selected];
        return itm.datanode && (!K5.gui.selected.disabledDisplay);
        */
        return false;
})()
));


_actions.push(createAction('addclipboard','ctx.actions.addclipboard','K5.gui.menu.doAction("addclipboard")',new AddToClipboard(),
(function() {
        /*
        var selected = K5.api.ctx.item.selected; 
        return  (!K5.gui.selected.disabledDisplay);
        */
        return K5.gui.clipboard.ctx.selected.length > 0;
})()
));

_actions.push(createAction('clipboard','ctx.actions.clipboard','K5.gui.menu.doAction("clipboard")',new DisplayClipboard(),
(function() {
        
        /*
        var selected = K5.api.ctx.item.selected; 
        return  (!K5.gui.selected.disabledDisplay);
        */
        return true;
})()
));

_actions.push(createAction('print','ctx.actions.print','K5.gui.menu.doAction("print")',new PrintItem(),
(function() {
        /*
        var selected = K5.api.ctx.item.selected; 
        return  (!K5.gui.selected.disabledDisplay);
        */
        return false;
})()
));



K5.gui.menu = {
        'actions': _actions,
        'doAction': function(actname) {
                var act = _.reduce(K5.gui.menu.actions, function(memo, value, index) {
                        if (memo !== null) return memo;
                        else if (value.key == actname) {
                                return value;
                        } else return null;
                }, null);
                if (act !== null)  act.actionObject.perform();
        }
};

//initialized -> trigger event
K5.eventsHandler.trigger("application/menu/ctx/initialized",_actions);



