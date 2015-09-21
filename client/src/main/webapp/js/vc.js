
/** Represents objects displaying all virtual collections */
var VirtualCollections = function(application) {
        
    var f = _.bind(function(type, data) {
	console.log("event type :"+type);
        if (type === "api/vc") {
            this.check();
            this.init();

            this.translate(K5.i18n.ctx.language);

            this.resizediv();

            $("#yearRows").bind("wresize", function() {
                K5.gui.vc.resizediv();
            });

            this.checkArrows();    
        }
    },this);

    application.eventsHandler.addHandler(f);

}

VirtualCollections.prototype = {
        ctx:{
                // elements container
                "elements":[]
        },

        /** tests if given key is present in the context */
        isKeyReady: function(keys) {
                return lookUpKey(keys, this.ctx);
        },


        /** check wheather html contains necessary elements */
        check: function() {
                var expecting = ["#yearRows", "#foot"];                
                $.each(expecting,_.bind(function(index,value) {
                        var size = $(value).size();
                        if (size <= 0)  {
                                throw  new AppError("expeting element ! "+value, this, 1);
                        }
                }, this));
        },              
         
        /** gui initialization */
        init: function() {
                {
                        this.ctx.elements["scroll"] =  $('<div/>', {class: 'scroll'});
                        $("#yearRows").append(this.ctx.elements["scroll"]);

                        this.ctx.elements["topArrow"] = $('<div>', {class: 'medium button'});
                        this.ctx.elements["topArrow"].css({left: 3});
                        this.ctx.elements["topArrow"].load("svg.vm?svg=arrowtop");
                        $("#yearRows").append(this.ctx.elements["topArrow"]);

                        this.ctx.elements["bottomArrow"] = $('<div>', {class: 'medium button'});
                        this.ctx.elements["bottomArrow"].css({"top": "initial", bottom: 36, left: 3});
                        this.ctx.elements["bottomArrow"].load("svg.vm?svg=arrowbottom");
                        $("#yearRows").append(this.ctx.elements["bottomArrow"]);
                }

                // top arrow click
                this.ctx.elements.topArrow.click(_.bind(function() {
                    this.doScroll(-1);
                    this.checkArrows();
                },this));

                // bottom arrow click
                this.ctx.elements.bottomArrow.click(_.bind(function() {
                    this.doScroll(1);
                    this.checkArrows();
                },this));
		
                		
	        if (K5.api.isKeyReady("vc")) {
                    $.each(K5.api.ctx.vc, _.bind(function(item) {
                        var div = $('<div>', {class: 'row'});
                        this.ctx.elements.scroll.append(div);
                        this.addVc(item, div);
                    },this));
	        } else {
                  throw new AppError("virtual collections is not present in the api context", this, 1);
	        }

                //this.checkArrows();
        },

        doScroll: function(dx){
                var speed = 500;
                var finalPos = dx * 0.8 * $("#yearRows").height() +  this.ctx.elements.scroll.scrollTop();
                this.ctx.elements.scroll.animate({scrollTop:finalPos}, speed);
                this.checkArrows();
        },
        
        checkArrows: function(){
                var bottom = ($(this.ctx.elements.scroll)[0].scrollHeight - this.ctx.elements.scroll.height());
                if(this.ctx.elements.scroll.scrollTop() >= bottom){
                    this.ctx.elements.bottomArrow.show();
                /*
                } else {
                    this.ctx.elements.bottomArrow.hide();
                */
                }
                if(this.ctx.elements.scroll.scrollTop() !=0){
                    this.ctx.elements.topArrow.show();
                /*
                } else {
                    this.ctx.elements.topArrow.hide();
                */
                }
        },

        /** add new virtual collection */    
        addVc: function(pid, div) {
                if (!this.isKeyReady("vc")) {
                        this.ctx["vc"] = [];
                }                
                var vc = new VirtualCollection(div, {pid: pid});
                vc.panelsize();
                vc.init();
                this.ctx.vc.push(vc);
        },

        /** resize rows element */            
        resizediv: function() {
                if (console) console.log(" resizing div  ");
                var h = $(document).height();
                if (h < 1) {
                    h = $(document).height();
                }
                if(console) console.log("height =="+$('#footer').offset().top);
                $('#yearRows').css('height', $('#footer').offset().top - $('#header').height());
        },

        /** append element wich can be localided */    
        translatable:function (key, collections, language) {
                var collections  = K5.api.ctx["vc"] || {};	
                return '<span class="vc" data-key="' + key + '">' + collections[key][language] + '</span>';
        },

        /** append element wich can be localized */    
        translate: function(language) {
            var collections  = K5.api.ctx["vc"] || {};	
            $('.vc').each(function() {
	        var key = $(this).data("key");
	        if(collections[key]){
	            $(this).text(collections[key][language]);
	        }
            });
        },

        /** append element wich can be localized */    
        translateKey: function(key) {
            var collections  = K5.api.ctx["vc"] || {};	
            var language = K5.i18n.ctx.language;
            if(collections[key]){
                return collections[key][language];
            }else{
                return key;
            }
        }
}


var VirtualCollection = function(elem, options) {
    this.elem = elem;
    this.$elem = $(elem);
    this.pid = options.pid;
    //this.init();
    var vc = this;
}

VirtualCollection.prototype = {
    background: "silver",
    rowsPerRequest: 50,
    thumbHeight: 128,
    panelWidth: 500,
    panelHeight: 138,
    init: function() {

        this.scroll = $('<div/>', {class: 'scroll'});
        this.container = $('<ul/>');
        this.container.addClass('container');
        this.scroll.append(this.container);
        this.$elem.append(this.scroll);

        //this.rightArrow = $('<div>', {class: 'arrow arrowRight'});
        this.rightArrow = $('<div>', {class: 'medium button'});
        this.rightArrow.css({right: "25px", top: 50});
        this.rightArrow.load("svg.vm?svg=arrowright");

        //this.leftArrow = $('<div>', {class: 'arrow arrowLeft'});
        this.leftArrow = $('<div>', {class: 'medium button'});
        this.leftArrow.css({left: "24px", top: "50px"});
        this.leftArrow.load("svg.vm?svg=arrowleft");
        this.$elem.append(this.leftArrow);
        this.$elem.append(this.rightArrow);


        this.titleBand = $('<div>', {class: 'rowtitle'});
        this.$elem.append(this.titleBand);
        //this.titleBand.append(vctranslatable(this.pid, K5.api.ctx.vc, K5.i18n.ctx.language));
        this.titleBand.append(K5.i18n.translatable(this.pid));
        

        var obj = this;
        this.titleBand.click(function() {
            window.location.href = "?page=search&collection=" + obj.pid;
        });
        this.totalBand = $('<div>', {class: 'total'});
        this.$elem.append(this.totalBand);


        this.getThumbs({offset: 0});
        this.rightArrow.click(function() {
            obj.doScroll(1);
        });
        this.leftArrow.click(function() {
            obj.doScroll(-1);
        });
        
        if (isTouchDevice()) {
            this.$elem.swipe({
                swipeLeft: function(event, direction, distance, duration, fingerCount) {
                    if (!obj.scrolling) {
                        obj.doScroll(1);
                    }
                },
                swipeRight: function(event, direction, distance, duration, fingerCount) {
                    if (!obj.scrolling) {
                        obj.doScroll(-1);
                    }
                },
                threshold: 2
            });
        }else{
            this.$elem.mousewheel(function(event) {
                if (!obj.scrolling) {
                    if (event.deltaX !== 0) {
                        obj.doScroll(event.deltaX);
                    } else {
                        obj.doScroll(-event.deltaY);
                    }
                }
            });
        }
        this.leftArrow.hide();
    },
    setSizes: function() {

        this.panelHeight = this.$elem.height();
        this.panelWidth = this.$elem.width();
    },

    panelsize: function() {

        this.panelHeight = this.$elem.height();
        this.panelWidth = this.$elem.width();
    },

    doScroll: function(dx) {
        var speed = 500;
        var finalPos = this.scroll.scrollLeft() + this.panelWidth * 0.8 * dx;
        var th = this;
        this.scroll.animate({scrollLeft: finalPos}, speed, function() {
            th.checkArrows();
        });
        this.checkArrows();

    },
    checkArrows: function() {
        if (this.scroll.scrollLeft() <= 0) {
            this.leftArrow.hide();
        } else {
            this.leftArrow.show();
        }

        if (this.scroll.scrollLeft() >= ($(this.scroll)[0].scrollWidth - this.scroll.width())) {
            this.rightArrow.hide();
        } else {
            this.rightArrow.show();
        }
    },
    getThumbs: function(params) {
        //var url = "collection.vm?pid="+this.pid;
        var url = "api/search?fq=level:0&q=collection:\"" + this.pid + "\"";
        var th = this;
        $.getJSON(url, function(data) {
            th.docs = {"docs": data.response.docs, "count": data.response.numFound};
            th.totalBand.html(data.response.numFound);
            th.render();
        });
    },
    render: function() {
        var docs = this.docs.docs;
        if(docs.length === 0){
            this.$elem.hide();
            return;
        }
        for (var i = 0; i < docs.length; i++) {
            var pid = docs[i]["PID"];
            var imgsrc = "api/item/" + pid + "/thumb";// + this.thumbHeight;
            var thumb = $('<li/>', {class: 'thumb'});
            thumb.data("metadata", docs[i]["dc.title"]);
            var title = docs[i]["dc.title"];
            var shortTitle = title;
            var creator = "";
            var maxLength = 90;
            var showTooltip = false;
            if (shortTitle.length > maxLength) {
                shortTitle = shortTitle.substring(0, maxLength) + "...";
                showTooltip = true;
            }
            if (docs[i]["dc.creator"]) {
                creator = '<div class="autor">' + docs[i]["dc.creator"] + '</div>';
            }
            title = '<div class="title">' + title + '</div>';
            thumb.data("pid", pid);
            this.container.append(thumb);
            var policy = $('<div/>', {class: 'policy'});
            if (docs[i]['dostupnost']) {
                policy.addClass(docs[i]['dostupnost']);
            }
            thumb.append(policy);

            if(showTooltip){
                thumb.attr("title", title + creator);
                thumb.tooltip({
                    content: title + creator,
                    position: {my: "left bottom-10", at: "right-100 bottom"}
                });
            }
            thumb.click(function() {
                K5.api.gotoDisplayingItemPage($(this).data('pid'));
            });
            this.addThumb(thumb, imgsrc);
            var ithumb = $('<div/>', {class: 'info'});
            ithumb.html('<div class="title">' + shortTitle + '</div>' + creator);
            thumb.append(ithumb);
        }

    },
    addThumb: function(div, imgurl) {
        var divimg = $('<div/>', {class: 'img'});
        var img = $('<img/>', {src: imgurl});
        $(div).append(divimg);
        $(divimg).append(img);
    }
}



