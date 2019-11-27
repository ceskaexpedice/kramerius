

function _checkZoomIsLocked(map) {
    var selected = K5.api.ctx.item.selected;
    var item = K5.api.ctx.item[selected];

    var ctx = item.context;
    // only first ?? 
    var length = ctx[0].length;
    if (length >= 2) {
        var parent = ctx[0][length-2];
        if (K5.gui.viewers.ctx.session["zoomLock"]) {
            var zoomLock = K5.gui.viewers.ctx.session["zoomLock"];
            if (zoomLock.pid === parent.pid) {
                __deactivateZoom(map);

                // locked
                K5.gui.selected.fit();
                var z = zoomLock.lockedzoom;
                map.getView().setZoom(z);
                return true;
            } 
        }
    }
    return false;
}

function __activateZoom(map) {
    if (K5.gui.selected.ctx["deactivatedInteractions"]) {
        var f = function(idx, item){
            map.addInteraction(item);
        }
        $.each(K5.gui.selected.ctx["deactivatedInteractions"], f);
        delete K5.gui.selected.ctx["deactivatedInteractions"];
    }
}

function __deactivateZoom(map) {
    var toRemoveCollection  = [];
    var itrls = map.getInteractions();
    itrls.forEach(function(item) {
        var toremove = false;
        if (!toremove) toremove = item instanceof ol.interaction.KeyboardZoom;
        if (!toremove) toremove = item instanceof ol.interaction.DoubleClickZoom;
        if (!toremove) toremove = item instanceof ol.interaction.MouseWheelZoom;
        if (!toremove) toremove = item instanceof ol.interaction.DragZoom;
        if (!toremove) toremove = item instanceof ol.interaction.PinchZoom;

        if (toremove) {
            toRemoveCollection.push(item);
        }
    });

    if (toRemoveCollection.length > 0) {
        if (!K5.gui.selected.ctx["deactivatedInteractions"]) {
            K5.gui.selected.ctx["deactivatedInteractions"] = [];
        } 
        K5.gui.selected.ctx["deactivatedInteractions"] = toRemoveCollection;
        $.each(K5.gui.selected.ctx["deactivatedInteractions"], function(idx, item) {
            map.removeInteraction(item);
        });
    }
}


function _unlockZoomAndStore(map) {
    __activateZoom(map);
    K5.gui.viewers.ctx.session["zoomLock"] = {};
    K5.gui.viewers.storeSessionInitialization();
}

function _lockZoomAndStore(map) {
    var selected = K5.api.ctx.item.selected;
    var item = K5.api.ctx.item[selected];

    var ctx = item.context;
    // only first ?? 
    var length = ctx[0].length;
    if (length >= 2) {
        __deactivateZoom(map);
        var parent = ctx[0][length-2];
        var z = map.getView().getZoom() || 1;
        var zoomLock = {
                "pid":parent.pid,
                "lockedzoom":z
        };
        K5.gui.viewers.ctx.session["zoomLock"] = zoomLock;
        K5.gui.viewers.storeSessionInitialization();
    }
}

function _leftNavigationArrow() {
    var leftArrowContainerDiv = $("<div/>",{"id":"pageleft","class":"leftarrow" });
    var icon = $("<div/>",{"id":"pagelefticon", class:"arrow"});
    leftArrowContainerDiv.append(icon);    

    $.get("svg.vm?svg=arrowleft",_.bind(function(data) {
        icon.html(data);            
        _checkArrows();
    },this));

 
    leftArrowContainerDiv.click(_.bind(function() {
        K5.gui.selected.prev();
    }, this));

    icon.click(_.bind(function() {
        //K5.gui.selected.prev();
    }, this));
    return leftArrowContainerDiv;
}

function _rightNavigationArrow() {

    var rightArrowContainerDiv = $("<div/>",{"id":"pageright","class":"rightarrow"});
    var icon = $("<div/>",{"id":"pagerighticon", class:"arrow"});
    rightArrowContainerDiv.append(icon);    


    $.get("svg.vm?svg=arrowright",function(data) {
        icon.html(data);            
        _checkArrows();
    });
 
    rightArrowContainerDiv.click(function() {
        K5.gui.selected.next();
    });


    icon.click(function() {
        //K5.gui.selected.next();
    });

    return rightArrowContainerDiv;
}


function _checkArrows() {
        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected] &&  K5.api.ctx["item"][selected]["siblings"]) {
                var data = K5.api.ctx["item"][selected]["siblings"];
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                        return (value.selected) ? index : memo;
                }, -1);
                if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  

                K5.eventsHandler.trigger("application/menu/ctxchanged", null);
        } else {
                K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                        var arr = data[0]['siblings'];
                        var index = _.reduce(arr, function(memo, value, index) {
                                return (value.selected) ? index : memo;
                        }, -1);

                        if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                        if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  

                        K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                });
        }
}

function _zoomIn(map) {
    var prev = map.getView().getZoom() || 1;
    var z = prev+1;
    //animation
    var anim = ol.animation.zoom({
        resolution:map.getView().getResolution()
    });
    map.beforeRender(anim);    
    map.getView().setZoom(z);
}

function _zoomOut(map) {
    var prev   = (map.getView().getZoom() || 1);
    var z = prev-1;
     //animation
     var anim = ol.animation.zoom({
         resolution:map.getView().getResolution()
     });
     map.beforeRender(anim);    
     map.getView().setZoom(z);
}


function _rotateLeft(map) {
    var rot = map.getView().getRotation();
    var dest = rot  + (-1 * (Math.PI/ 2));
    var rotateLeft = ol.animation.rotate({
        duration: 500,
        rotation: rot
    });
    map.beforeRender(rotateLeft);
    map.getView().setRotation(dest);
}


function _rotateRight (map){
    var rot = map.getView().getRotation();
    var dest = rot  + (1 * (Math.PI/ 2));
    var rotateRight = ol.animation.rotate({
        duration: 500,
        rotation: rot
    });
    map.beforeRender(rotateRight);
    map.getView().setRotation(dest);
}

function _optionspaneLocked(dkey) {
    $("#options_minus").hide();
    $("#options_plus").hide();
    $("#options_fit").hide();
    $.get("svg.vm?svg=lock",function(data) {
        $("#options_lock").html(data);            
    });
    
    if (dkey) {
        $("#options_lock").attr("data-key",dkey);
        $("#options_lock").attr("title",K5.i18n.ctx.dictionary[dkey]);
    }
}

function _optionspaneUnlocked(dkey) {
    $("#options_minus").show();
    $("#options_plus").show();
    $("#options_fit").show();
    $.get("svg.vm?svg=unlock",function(data) {
        $("#options_lock").html(data);            
    });
    if (dkey) {
        $("#options_lock").attr("data-key",dkey);
        $("#options_lock").attr("title",K5.i18n.ctx.dictionary[dkey]);
    }
}

function _optionspane(visibilityOptions) {
    var optionsDiv = $("<div/>",{"id":"options","class":"options"});
    optionsDiv.css("position","absolute");
    var ul = $("<ul/>");
    function li(ul) {
        var li = $("<li/>");
        ul.append(li);
        return li;
    }

    function icondiv(li, id, icon, datakey, title,func) {
        var div = $("<div/>",{"id":id,"class":"small"});
        if (datakey) {
            div.attr("data-key",datakey);
        }
        div.addClass("translate_title");
        
        if (title) {
            div.attr("title",title);
        }

        $.get("svg.vm?svg="+icon,function(data) {
            $("#"+id).html(data);
        });

        li.append(div);
        if (func) {
            jQuery(div).mousedown(function(e){ e.preventDefault(); });
            div.click(func);
        }

        return div;
    }

    if (!visibilityOptions || visibilityOptions.maximize) {
        if(!K5.gui["maximized"]){
            icondiv(li(ul),"options_maximize","maximize","buttons.maximize",K5.i18n.ctx.dictionary["buttons.maximize"], function() {
                K5.gui.selected.maximize();
            });
        } else {
            icondiv(li(ul),"options_maximize","minimize","buttons.minimize",K5.i18n.ctx.dictionary["buttons.minimize"], function() {
                K5.gui.selected.maximize();
            });
        }
    }

    if (!visibilityOptions || visibilityOptions.fit) {
        icondiv(li(ul), "options_fit", "fit", "buttons.fit", K5.i18n.ctx.dictionary["buttons.fit"], function () {
            K5.gui.selected.fit();
        });
    }

    if (!visibilityOptions || visibilityOptions.rotateleft) {
        icondiv(li(ul), "options_rotate_left", "rotateleft", "buttons.rotateleft", K5.i18n.ctx.dictionary["buttons.rotateleft"], function () {
            K5.gui.selected.rotateLeft();
        });
    }

    if (!visibilityOptions || visibilityOptions.rotateright) {
        icondiv(li(ul), "options_rotate_right", "rotateright", "buttons.rotateright", K5.i18n.ctx.dictionary["buttons.rotateright"], function () {
            K5.gui.selected.rotateRight();
        });
    }

    if (!visibilityOptions || visibilityOptions.zoomin) {
        icondiv(li(ul), "options_plus", "plus", "buttons.zoomin", K5.i18n.ctx.dictionary["buttons.zoomin"], function () {
            K5.gui.selected.zoomIn();
        });
    }

    if (!visibilityOptions || visibilityOptions.zoomout) {
        icondiv(li(ul), "options_minus", "minus", "buttons.zoomout", K5.i18n.ctx.dictionary["buttons.zoomout"], function () {
            K5.gui.selected.zoomOut();
        });
    }

    if (!visibilityOptions || visibilityOptions.lock) {
        icondiv(li(ul), "options_lock", "unlock", "buttons.zoomlock", K5.i18n.ctx.dictionary["buttons.zoomlock"], function () {
            var visible = $("#options_minus").is(":visible");
            if (visible) {
                K5.gui.selected.lockZoom();
                _optionspaneLocked("buttons.zoomunlock");
            } else {
                K5.gui.selected.unlockZoom();
                _optionspaneUnlocked("buttons.zoomlock");
            }
        });
    }

    /*
    if (visibilityOptions && visibilityOptions.zoomout) {
        icondiv(li(ul), "options_findnext", "minus", "buttons.zoomout", K5.i18n.ctx.dictionary["buttons.zoomout"], function () {
            //K5.gui.selected.zoomOut();
        });
    }*/


    optionsDiv.append(ul);

    return optionsDiv;
}

function _nextorprev() {
    
}

function _postrederer() {
    var vrs = K5.gui.selected.postrendererEvts().postRenderEventsKeys();
    for (var v = 0,ll=vrs.length;v<ll;v++) {
        var key = vrs[v];
        while (K5.gui.selected.postrendererEvts().isPostrenderEventDefined(key)) {
            var v = K5.gui.selected.postrendererEvts().popPostrenderEvent(key);
            v.apply(null,[]);
        }
    }
}


// because of problem with firefox 
// and dom renderer -> https://github.com/openlayers/ol3/issues/3283
function _olrenderer() {
    var detect = _browserdetect();
    return detect.firefox ? "canvas" : "dom";
}
function _browserdetect() {
    var isOpera = !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
    // Opera 8.0+ (UA detection to detect Blink/v8-powered Opera)
    var isFirefox = typeof InstallTrigger !== 'undefined';   // Firefox 1.0+
    var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
    // At least Safari 3+: "[object HTMLElementConstructor]"
    var isChrome = !!window.chrome && !isOpera;              // Chrome 1+
    var isIE = /*@cc_on!@*/false || !!document.documentMode;   // At least IE6
    
    return {
        "opera":isOpera,
        "firefox":isFirefox,
        "safari":isSafari,
        "chrome":isChrome,
        "ie":isIE
    }
}



