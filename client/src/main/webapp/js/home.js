K5.eventsHandler.addHandler(function(type, data) {
    if (!K5.gui.currasels) {
        K5.gui["currasels"] = {};
    }

    if (type === "api/feed/newest") {
        K5.gui.currasels["newest"] = new Carousel("#yearRows>div.latest", {"json": K5.api.ctx.feed.newest});
        K5.gui.currasels["newest"].setName("newest");
    }

    if (type === "api/feed/mostdesirable") {
        K5.gui.currasels["mostdesirable"] = new Carousel("#yearRows>div.popular", {"json": K5.api.ctx.feed.mostdesirable});
        K5.gui.currasels["mostdesirable"].setName("mostdesirable");
    }

    if (type === "api/feed/cool") {
        K5.gui.currasels["cool"] = new Carousel("#yearRows>div.cool", {"json": K5.api.ctx.feed.cool});
        K5.gui.currasels["cool"].setName("cool");
        if (K5.gui.home) {
                K5.gui.home.displayBackground();
        }
    }

    if (type === "application/init/end") {
        /* if (K5.authentication.ctx["profile"] && K5.authentication.ctx.profile["favorites"]) {
                var mapped = _.map(K5.authentication.ctx.profile.favorites, function(pid) { 
                        var obj = {};
                        obj["pid"] = pid;                       
                        obj["title"] = 'title';                       
                        return  obj; 
                });
                var data = {'data':mapped};
                K5.gui.currasels["profilefavorites"] = new Carousel("#yearRows>div.profilefavorites", {"json": data}, true);
                K5.gui.currasels["profilefavorites"].setName("profilefavorites");
        }  
        */
        K5.gui.home = new HomeEffects(K5);
    }
    if (type === "i18n/dictionary") {
        if(!K5.gui.home){
            K5.gui.home = new HomeEffects(K5);
        }
        K5.gui.home.getDocs();
    }
});



function HomeEffects(application) {
    this.application = application;
    this._init();
    this.backgroundDisplayed = false;
}

HomeEffects.prototype = {
    ctx: {},
    _init: function() {
        
        this.setSizes();
        if (K5.gui.currasels["cool"]) {
                this.displayBackground();
        }
        this.infoHidden = false;
        if (isTouchDevice()) {
            $("#buttons").swipe({
                swipeUp: function(event, direction, distance, duration, fingerCount) {
                    $("#band").animate({'bottom': 20}, 200);
                },
                threshold: 2
            });
        }
            
        $('#buttons>div.button').click(_.partial(function(he) {
            he.selBand(this);
        }, this));

        $('#buttons>div.button').mouseenter(_.partial(function(he) {
            he.selBand(this);
            $("#band").animate({'bottom': 41}, 200);
        }, this));
        $('#band').mouseleave(function() {
            $("#band").animate({'bottom': -147}, 200);
            $('#buttons>div.button').removeClass('sel');
        });
        
        //podle #153 mame otevrene "vybrane" a po 5 sec schovame
        $("#band").animate({'bottom': 41}, 200);
        setTimeout(function() {
            $("#band").animate({'bottom': -147}, 200);
            $('#buttons>div.button').removeClass('sel');
        }.bind(this), 5000);
        
        /* Komentovane podle issue 199
         * 
        $("#home>div.infobox").mouseenter(_.bind(function() {
            clearTimeout(this.hiddingInfo);
            this.showInfo();
            
        }, this));
        $("#home>div.infobox").mouseleave(_.bind(function() {
            this.hiddingInfo = setTimeout(function() {
                this.hideInfo();
            }.bind(this), 3000);
        }, this));
        
        */
        this.addContextButtons();
    },
    
    addContextButtons: function(){
        var text = $("#item_menu").html();
        $("#contextbuttons").html(text);
    },
    
    showInfo: function() {
        $("#home>div.infobox").animate({'opacity': '1.0', 'left': '105px'}, 500);
        this.infoHidden = false;
    },
    
    hideInfo: function() {
        $("#home>div.infobox").animate({'opacity': '0.2'}, 500, _.bind(function() {
            this.infoHidden = true;
        }, this));
        
    },
    displayBackground: function() {
        if (this.backgroundDisplayed) return;
        var image = new Image();

        var srcs = [];
        if (K5.api.ctx["feed"] &&  K5.api.ctx.feed["cool"] && K5.api.ctx.feed.cool["data"]) {
                //srcs = K5.cool.coolData.data;
                srcs = K5.api.ctx.feed.cool["data"];
        }
	if (srcs.length == 0) return;

        var index = Math.floor(Math.random() * (srcs.length - 1));
        var pid = srcs[index].pid;
        var src = 'api/item/' + pid + '/full';

        image.onload = _.bind(function() {
            
            $("#home div.img").css("background-image", "url(" + src + ")");
            this.showInfo();
            $("#home div.img").animate({'backgroundPosition': '50%'}, 600);
            
            
            var a = $("<div/>", {class: "a"});
            if(srcs[index].root_title !== srcs[index].title){
                a.append(srcs[index].root_title + " [" + srcs[index].title + "]");
            }else{
                a.append(srcs[index].root_title);
            }
            
            a.click(function(){
                K5.api.gotoDisplayingItemPage(pid);
            });
            $("#pidinfo").append(a);
            if(srcs[index].author){
                $("#pidinfo").append('<div class="details">' + srcs[index].author + '</div>');
            }
            
            /* Komentovane podle issue 199
             * 
            
            this.hiddingInfo = setTimeout(function() {
                this.hideInfo();
            }.bind(this), 3000);
            
            */

            this.backgroundDisplayed = true;
        }, this);

        image.onerror = _.bind(function() {
            this.displayBackground();
            this.backgroundDisplayed = false;
        }, this);
        image.src = src;
    },
    selBand: function(obj) {
        $('#buttons>div.button').removeClass('sel');

        $("#yearRows>div.row").hide();
        $(obj).addClass('sel');
        var div = $(obj).data("row");
        $("#yearRows>div." + div).show();
    },
    setSizes: function(){
        $("#home div.container").show();
        var h = window.innerHeight - $('#header').height() - $('#footer').height() - $('#buttons').height();
        
        $('#facets').css('top', 0);
        $('#facets').css('overflow', 'auto');
        $('#facets').css('left', 0);
        $('#home div.container').css('height', h); //30 = 2x15 padding 

        $('#facets').css('height', "100%");
        
    },
    getDocs: function() {
        $.get("raw_results.vm?page=home", _.bind(function(data) {
            var json = jQuery.parseJSON(data);
            K5.eventsHandler.trigger("results/loaded", json);
        }, this));
    },
};
