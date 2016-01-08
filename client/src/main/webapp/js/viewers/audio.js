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
        _checkArrows();
        
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
        $("#audioContainer").remove();
        $("#pageleft").remove();
        $("#pageright").remove();

}

AudioView.prototype.addContextButtons=  function() {
    _ctxbuttonsrefresh();
}

AudioView.prototype.arrowbuttons = function() {
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
                K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                        var arr = data[0]['siblings'];
                        var index = _.reduce(arr, function(memo, value, index) {
                                return (value.selected) ? index : memo;
                        }, -1);

                        if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                        if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  
                });
        }

}

AudioView.prototype.isEnabled= function(data) {
    var model = (data["model"] || "" );
    return model === "track";
}


AudioView.prototype.containsLeftStructure = function() {
    return true;
}
