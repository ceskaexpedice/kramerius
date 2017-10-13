/**
 * Display audio player
 * @constructor
 */
function AudioView(appl, selector) {
        this.application = (appl || K5);
        //var jqSel = (selector || '#viewer>div.container');        

        var jqSel = (selector || '#viewer>div.container>div.ol');        
        this.elem = $(jqSel);

        
        this.container = $(jqSel);
}


/** 
 * Open audio stream player  
 * @method
 */
AudioView.prototype.open = function() {
//        var leftArrowContainerDiv = $("<div/>",{"id":"pageleft","class":"leftarrow" });
//        leftArrowContainerDiv.append($("<div/>",{"id":"pagelefticon", class:"arrow"}));    
//        this.container.append(leftArrowContainerDiv);    
//
//        
//        var rightArrowContainerDiv = $("<div/>",{"id":"pageright","class":"rightarrow"});
//        rightArrowContainerDiv.append($("<div/>",{"id":"pagerighticon", class:"arrow"}));    
//        this.container.append(rightArrowContainerDiv);    
//
//        $.get("svg.vm?svg=arrowleft",_.bind(function(data) {
//                $("#pagelefticon").html(data);            
//                this.arrowbuttons();
//        },this));
//
//        $.get("svg.vm?svg=arrowright",_.bind(function(data) {
//                $("#pagerighticon").html(data);            
//                this.arrowbuttons();
//        },this));
// 
//        $("#pageleft").click(_.bind(function() {
//                K5.gui.selected.prev();
//        }, this));
//
//        $("#pageright").click(_.bind(function() {
//                K5.gui.selected.next();
//        }, this));
//
//        $("#pagelefticon").click(_.bind(function() {
//                K5.gui.selected.next();
//        }, this));
//
//        $("#pagerighticon").click(_.bind(function() {
//                K5.gui.selected.next();
//        }, this));
//
//        this.arrowbuttons();


    $.get( "_audioplayer.vm", _.bind(function( data ) {
        this.elem.append(_leftNavigationArrow());    
        this.elem.append(_rightNavigationArrow());    

        this.elem.append(data);

        this.jplayer();
        this._checkArrows();
        
    },this));
    

}

AudioView.prototype.jplayer = function() {
    var pid = K5.api.ctx.item.selected;

    $.get('audioconf?pid=' + pid, function (result) {

        $("#jquery_jplayer_1").jPlayer({
            ready: function () {
                $(this).jPlayer("setMedia", {
                    oga: result.data.oga,
                    wav: result.data.wav,
                    mp3: result.data.mp3
                });
            },
            swfPath: "../../jplayer/swf",
            supplied: "oga, wav, mp3"
        });
        
        $(".jp-title").html('<ul><li>' + result.info.tracks[0].title + '</li></ul>');
    });
}

AudioView.prototype.clearContainer = function() {
   this.container.empty();
}

AudioView.prototype.addContextButtons=  function() {
    _ctxbuttonsrefresh();
}

AudioView.prototype._checkArrows = function() {
        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected] &&  K5.api.ctx["item"][selected]["siblings"]) {
                var data = K5.api.ctx["item"][selected]["siblings"];
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                        return (value.selected) ? index : memo;
                }, -1);
                if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  
        } else {
				
				// nahoru az "soundrecording"
				// pres "soundunit"
								
				var model = K5.api.ctx["item"][K5.api.ctx["item"].selected].model;
				if (K5.api.ctx["item"][K5.api.ctx["item"].selected].context.length > 0) {
					var soundRecordingPid = _.reduce( K5.api.ctx["item"][K5.api.ctx["item"].selected].context[0], function(memo, value, index) {
							if (memo === null) {
								if (value.model === "soundrecording") {
									return value.pid;
								} else return null;
							} else return memo;
        	                return (value.selected) ? index : memo;
            	    }, null);

					if (soundRecordingPid != null) {
						K5.api.ctx["item"][selected]["audiotracks"] = [];
						K5.api.askForItemChildren(soundRecordingPid, function(data) {
							$.each(data, function(t, d) {
								if (d.model === "track") {
									K5.api.ctx["item"][selected]["audiotracks"].push(d);
								} else if (d.model === "soundunit") {
									K5.api.askForItemChildren(d.pid, function(ddata) {
										$.each(ddata, function(tt, dd) {
											if (dd.model === "track") {
												K5.api.ctx["item"][selected]["audiotracks"].push(dd);
											}
										});
									});
								}
							});
						});
					}
				}
        }

}

AudioView.prototype.isEnabled= function(data) {
    var model = (data["model"] || "" );
    return model === "track";
}


AudioView.prototype.containsLeftStructure = function() {
    return true;
}


/**
 * Next item
 * @method      
 */
AudioView.prototype.next =  function() {

    cleanWindow();

    if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/" + K5.api.ctx.item.selected + "/audiotracks"))) {
        var data = K5.api.ctx["item"][ K5.api.ctx["item"]["selected"] ]["audiotracks"];
        //var arr = data[0]['siblings'];
        var index = _.reduce(data, function(memo, value, index) {
            return (K5.api.ctx["item"]["selected"] === value.pid) ? index : memo;
        }, -1);
        if (index <= data.length - 1) {
            var nextPid = data[index + 1].pid;
            var hash = hashParser();
            hash.pid = nextPid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
        }
    } 
    /*
    else {
        K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
            var arr = data[0]['siblings'];
            var index = _.reduce(arr, function(memo, value, index) {
                return (value.selected) ? index : memo;
            }, -1);
            if (index < arr.length - 2) {
                var nextPid = arr[index + 1].pid;
                var hash = hashParser();
                hash.pid = nextPid;
                var histDeep = getHistoryDeep() + 1;
                hash.hist = histDeep;
                K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            }
        });
    }*/
}

/**
 * Previous item
 * @method      
 */       
AudioView.prototype.prev =  function() {

    cleanWindow();

    //this.clearContainer();
    
    if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/" + K5.api.ctx.item.selected + "/audiotracks"))) {
        var data = K5.api.ctx["item"][ K5.api.ctx["item"]["selected"] ]["audiotracks"];
        var index = _.reduce(data, function(memo, value, index) {
            return (K5.api.ctx["item"]["selected"] === value.pid) ? index : memo;
        }, -1);
        if (index > 0) {
            var prevPid = data[index - 1].pid;
            var hash = hashParser();
            hash.pid = prevPid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
        }
    }
	
	/*    
    if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/" + K5.api.ctx.item.selected + "/siblings"))) {
        var data = K5.api.ctx["item"][ K5.api.ctx["item"]["selected"] ]["siblings"];
        var arr = data[0]['siblings'];
        var index = _.reduce(arr, function(memo, value, index) {
            return (value.selected) ? index : memo;
        }, -1);
        if (index > 0) {
            var prevPid = arr[index - 1].pid;
            var hash = hashParser();
            hash.pid = prevPid;
            var histDeep = getHistoryDeep() + 1;
            hash.hist = histDeep;
            K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
        }

    } else {
        K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
            var arr = data[0]['siblings'];
            var index = _.reduce(arr, function(memo, value, index) {
                return (value.selected) ? index : memo;
            }, -1);
            if (index > 0) {
                var prevPid = arr[index - 1].pid;
                var hash = hashParser();
                hash.pid = prevPid;
                var histDeep = getHistoryDeep() + 1;
                hash.hist = histDeep;
                K5.api.gotoDisplayingItemPage(jsonToHash(hash), $("#q").val());
            }
        });
    }*/
    
}

