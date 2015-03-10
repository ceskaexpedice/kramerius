/**
 * Display audio player
 * @constructor
 */
function AudioView(appl, selector) {
        this.application = (appl || K5);
        var jqSel = (selector || '#viewer>div.container');        
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

        
        this.container.append(_leftNavigationArrow());    
        this.container.append(_rightNavigationArrow());    

        var audioContainer = $("<div/>",{'id':'audioContainer'});
        audioContainer.css("width","100%");

        function jplayer() {
            var div = $("<div/>");
            div.attr("id","jquery_jplayer_1")
            div.addClass("jp-jplayer");
            return div;
        }

        function jpaudio() {
            var div = $("<div/>");
            div.addClass("jp-audio");
            div.attr("id","jp_container_1");
            div.css("margin","0 auto");
            div.css("margin-top","60px");
            div.append(jpsingle());
            return div;
        }

        function jpsingle() {
            var div = $("<div/>");
            div.addClass("jp-type-single");
            div.append(jpgui());
            div.append(jptitle());
            div.append(jpnsolution());
            
            return div;
        }

        function jpgui() {
            var div = $("<div/>");
            div.addClass("jp-gui");
            div.addClass("jp-interface");
            div.append(jpcontrols());
            div.append(jpprogress());
            div.append(jpvolumebar());
            div.append(jptimeholder());
            /*
            div.append(jptitle());
            div.append(jpnsolution());
            */
            return div;
        }

        function jpcontrols() {
            var ul = $("<ul/>");
            ul.addClass("jp-controls");
            
            var li = $("<li/>");
            var ahr = $("<a/>");
            ahr.addClass("jp-play");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            /* disabled pause ? */
            li = $("<li/>");
            ahr = $("<a/>");
            ahr.addClass("jp-pause");
            ahr.css("display","none");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            li = $("<li/>");
            ahr = $("<a/>");
            ahr.addClass("jp-stop");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            li = $("<li/>");
            ahr = $("<a/>");
            ahr.addClass("jp-mute");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            li = $("<li/>");
            ahr = $("<a/>");
            ahr.addClass("jp-unmute");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            li = $("<li/>");
            ahr = $("<a/>");
            ahr.addClass("jp-volume-max");
            ahr.attr("href","javascript:;");
            ahr.attr("tabindex","1");
            li.append(ahr);
            ul.append(li);

            return ul;
        }

        function jpprogress() {
            var div = $("<div/>");
            div.addClass("jp-progress");

            var seek = $("<div/>");
            seek.addClass("jp-seek-bar");

            var jpplay = $("<div/>");
            jpplay.addClass("jp-play-bar");
            
            seek.append(jpplay);
            div.append(seek);
            
            return div;
        }

        function jpvolumebar() {
            var div = $("<div/>");
            div.addClass("jp-volume-bar");

            var volume = $("<div/>");
            volume.addClass("jp-volume-bar-value");
            div.append(volume);
            return div;
        }


        function jptimeholder() {
            var div = $("<div/>");
            div.addClass("jp-time-holder");

            var ctimediv = $("<div/>");
            ctimediv.addClass("jp-current-time");
            div.append(ctimediv);

            var durtimediv = $("<div/>");
            durtimediv.addClass("jp-duration");
            div.append(durtimediv);

 
            var ul = $("<ul/>");
            ul.addClass("jp-toggles");
            var li = $("<li/>");
            var ahref = $("<a/>");
            ahref.addClass("jp-repeat");
            ahref.attr("tabindex","1");
            li.append(ahref);
            ul.append(li);

            li = $("<li/>");
            ahref = $("<a/>");
            ahref.addClass("jp-repeat-off");
            ahref.attr("tabindex","1");
            li.append(ahref);
            ul.append(li);
            div.append(ul);

            return div;
        }

        function jptitle() {
            var div = $("<div/>");
            div.addClass("jp-title");
            return div;
        }
        
        function jpnsolution() {
            var div = $("<div/>");
            div.addClass("jp-no-solution");
            var span = $("<div/>");
            span.text("Update Required");
            span.html("To play the media you will need to either update your browser to a recent version or update your <a href=\"http://get.adobe.com/flashplayer/\" target=\"_blank\">Flash plugin</a>.");
 
            div.append(span);
            return div;
            
        }
    


        audioContainer.append(jplayer());
        audioContainer.append(jpaudio());

        
        
        this.container.append(audioContainer);

        this.jplayer();
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
