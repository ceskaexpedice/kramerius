/**
 * Clipboard functionality
 *
 * @constructor
 * @param {Application} application - The application instance  {@link Application}.
 * @deprecated Removed 
 */
function Clipboard(application) {
        this.application = application;
        this.dirty = false;
}

Clipboard.prototype = {

        ctx: {
                "selected":[],
                "widths":{},
                "current":0,
                "scrollbusy":false,
                "configuration":{}
        },
        

        init:function(data) {
                this.ctx.selected = data;
        },        

        initConfiguration:function(configuration) {
                this.ctx.configuration["pdf"]=configuration;
        },        

        /**
         * Find item in the current selection
         * @param {Object} item 
         */
        findInSelection:function(item) {
                var found = _.reduce(this.ctx.selected, function(memo, value, index) {
                        if (memo === null && value.pid === item.pid) {
                                memo = value;
                        }
                        return memo;
                }, null);
                return found;
        },
        
        
/*
        refreshButtons:function(addenabled) {
                if (addenabled) {
                        $("#vwr_ctx_addclipboard").css( "display","inline-block");
                        $("#vwr_ctx_removeclipboard").hide();              
                } else {
                        $("#vwr_ctx_addclipboard").hide();              
                        $("#vwr_ctx_removeclipboard").css( "display","inline-block");
                }

        },        
*/
        /**
         *  Add current item
         */
        addCurrent:function() {
                var sel = K5.api.ctx.item.selected;
                var item = K5.api.ctx.item[sel];
                this.add(item);                                
        },

        /**
         * Remove current item
         */
        removeCurrent:function() {
                var sel = K5.api.ctx.item.selected;
                var item = K5.api.ctx.item[sel];
                this.remove(item);                                
        },
        
        /**
         * Add new item into selection
         * @param {Object} item Item to selection 
         */
        add:function(item) {
                this.ctx.selected.push(item);                
                this.poststate();

                var item = K5.api.ctx.item[K5.api.ctx.item.selected];
                K5.eventsHandler.trigger("application/menu/ctxchanged", null);
        },

        /**
         * Remove item from the selection
         * @param {Object} item
         */
        remove:function(item) {
                var narr = [];                        
                var pid = item.pid || item;

                $.each(this.ctx.selected, function(index, value) {
                        if (value.pid !== pid) {
                                narr.push(value);    
                        }
                });

                this.ctx.selected = narr;                
                this.poststate();

                var item = K5.api.ctx.item[K5.api.ctx.item.selected];
                K5.eventsHandler.trigger("application/menu/ctxchanged", null);
        },

        /**
         * Save favorites
         */
        poststate:function() {
                var encodedData = Base64.encode(JSON.stringify(this.ctx.selected));
                $.ajax({
                        dataType: "json",
                        type: "POST",
                        //contentType:'application/json',
                        'url': 'session?name=clipboard',
                        //'beforeSend': function(xhr) { xhr.setRequestHeader("Authorization","Basic " + Base64.encode(uname + ":" + pass)); },
                        data: {'encodedfield':encodedData}
                        //success: postok
                });

        },
                

        /**
         * Returns current selection
         * @method
         */
        getSelected:function() {
                return this.ctx.selected;        
        },

        /**
         * Returns true if current pid is displayed
         * @method
         */
        isCurrentSelected:function() {
                var selected = K5.api.ctx.item ? K5.api.ctx.item.selected : null;
                if (selected) {
                        return this.findInSelection(K5.api.ctx.item[selected]) != null;
                } else {
                        return false;
                }
        },
        
        toggle:function() {
            if (visible("#viewer>div.selections")) { cleanWindow(); } 
            else { this.display();  }
        },
        
        /**
         * Display current selection 
         * @method
         */
        display:function() {
                cleanWindow();
                divopen("#viewer div.selections");
                this.render();
                this.ctx.containerw = $("#selections_thumbs ul").width();
                this.ctx.current = 0;
                this.refreshLeftRightButtons();
        },

        update:function() {
                this.render();
                this.ctx.containerw = $("#selections_thumbs ul").width();
                this.ctx.current = 0;
                this.refreshLeftRightButtons();
        },

        meta:function(thumb) {
                var meta =  $("<div>");
                meta.addClass("info");
                
                var title =  $("<div>");
                title.addClass("title");
                title.text(thumb["root_title"]);
                meta.append(title);

                if (thumb.details) {
                        for(var d in thumb.details) {
                                var det =  $("<div>");
                                det.addClass("details");
                                det.text(thumb.details[d]);
                                meta.append(det);
                                
                        }
                }

                var model =  $("<div>");
                model.addClass("title");
                model.html(K5.i18n.translatable('fedora.model.'+thumb.model));
                meta.append(model);
            
                return meta;
        },

        thumb:function(thumb) {

                var selitem = $('<div/>');
                selitem.data("pid", thumb.pid);
                selitem.addClass("thumb");

                var imgdiv = $('<div/>');
                imgdiv.data("pid", thumb.pid);
                

                var imgsrc = 'api/item/' + thumb.pid + '/thumb';
                var img = $('<img/>', {src: imgsrc});

                var svgdiv =  $("<div>");
                svgdiv.addClass("clp_cancel_buttons");
                svgdiv.addClass("small");
 
                svgdiv.attr("onclick","( function() { K5.gui.clipboard.remove('"+thumb.pid+"'); K5.gui.clipboard.update(); })();");

                //ahref.append(img);

                imgdiv.append(img);
                imgdiv.click(function() {
                        K5.api.gotoItemPage($(this).data('pid'));
                });

                selitem.append(imgdiv);
                selitem.append(this.meta(thumb));
                selitem.append(svgdiv);                

                var th = $('<li/>', {class: 'selitem', 'data-pid': thumb.pid});
                th.attr("title", thumb.title);
                th.append(selitem);

                return th;
        },

        render:function() {
                var data = this.ctx.selected;
                $("#selections_thumbs").html("");
                this.scrollElem = _.reduce(data, _.bind(function(memo, value){ 
                        memo.append(this.thumb(value));                        
                        return memo; },this), $("<ul/>"));
                this.scrollElem.css('position','relative');
                this.scrollElem.css('left','0px');

                $("#selections_thumbs").append(this.scrollElem);

                
                this.arrowButtons();        
                this.refreshButtons();
                this.closeButtons();

        },
        
        arrowButtons:function() {
                var rarr = $('<div/>', {class: 'medium arrow'});
                rarr.css({right: "4px","display":"none"});

                rarr.load("svg.vm?svg=arrowright");
                $("#selections_thumbs").append(rarr);
                this.rightArrow = rarr;

                rarr.click(_.bind(function() {
                    this.doScroll(1);
                }, this));

        
                var larr = $('<div/>', {class: 'medium arrow'});
                larr.css({left: "4px","display":"none"});

                larr.load("svg.vm?svg=arrowleft");
                $("#selections_thumbs").append(larr);
                this.leftArrow = larr;

                larr.click(_.bind(function() {
                    this.doScroll(-1);
                }, this));

                K5.serverLog("swipes .... "+isTouchDevice());

                if (isTouchDevice()) {
                        $("#selections_thumbs").swipe({
                                swipeLeft: function(event, direction, distance, duration, fingerCount) {
                                        K5.serverLog("swipe left");
                                        K5.gui.clipboard.doScroll(1);
                                },
                                swipeRight: function(event, direction, distance, duration, fingerCount) {
                                        K5.serverLog("swipe right");
                                        K5.gui.clipboard.doScroll(-1);
                                },
                                threshold: 2
                        });

                        $("#selections_thumbs ul li img").swipe({
                                swipeLeft: function(event, direction, distance, duration, fingerCount) {
                                        K5.serverLog("swipe left image");
                                        K5.gui.clipboard.doScroll(1);
                                },
                                swipeRight: function(event, direction, distance, duration, fingerCount) {
                                        K5.serverLog("swipe right image");
                                        K5.gui.clipboard.doScroll(-1);
                                },
                                threshold: 2
                        });

                }
        },

        doScroll: function(dx){
                if (this.ctx.scrollbusy) return;
                this.ctx.scrollbusy = true;
                function elementEndOffset(array, index) {
                        index = Math.max(index,0);
                        var pid = array[index].pid;
                        var elm = $("#selections_container  li.selitem[data-pid='"+pid+"']");
                        var offset = elm.offset();
                        return {
                                "element":elm,
                                "offset":offset
                        }
                }

                var scrollto = Math.min(Math.max(this.ctx.current+dx,0),this.ctx.selected.length-1); 
                var start = elementEndOffset(this.ctx.selected, this.ctx.current);
                var end = elementEndOffset(this.ctx.selected, scrollto);
                if (scrollto < 0) return;                 
                var difference = start.offset.left -  end.offset.left;

                var finalPos = this.scrollElem.position().left + difference;
                this.scrollElem.animate({'left':finalPos}, 500, _.bind(function() {
                        this.ctx.scrollbusy = false;
                },this));
                
                this.ctx.current = scrollto;
                this.refreshLeftRightButtons();

        },
        
        refreshLeftRightButtons:function() {
                if (this.ctx.current == 0 && this.ctx.selected.length > 1) {
                        this.leftArrow.hide();
                        this.rightArrow.show();                        
                } else  if (this.ctx.current < this.ctx.selected.length-1 && this.ctx.selected.length > 1) {
                        this.leftArrow.show();
                        this.rightArrow.show();                        
                } else  if (this.ctx.current == this.ctx.selected.length-1 && this.ctx.selected.length > 1) {
                        this.leftArrow.show();
                        this.rightArrow.hide();                        
                }
        },       

        refreshButtons:function() {
                // enable buttons                
                $(".selection_footer .button").each(function() {
                    if ($(this).data("ctx")) {
                        var attr = $(this).data("ctx").split(";");
                        if (jQuery.inArray('selection', attr) > -1) {
                           if (K5.gui.clipboard.ctx.selected.length > 0) {
                               $(this).show();
                           } else {
                               $(this).hide();
                           }
                        }

                        if (jQuery.inArray('pdflimit', attr) > -1) {
                                var ll = K5.gui.clipboard.ctx.selected.length;
                                if ((K5.gui.clipboard.ctx.configuration["pdf"]) && 
                                        (K5.gui.clipboard.ctx.configuration["pdf"]["limit"]) && 
                                        (K5.gui.clipboard.ctx.configuration.pdf.limit> ll)) {
                                                $(this).show();
                                } else {
                                                $(this).hide();
                                }
                        }
                    }
                });
        },        
        

        /**
         * Print selection 
         * @method
         */
        print:function() {
                var selected = this.ctx.selected;
                var ll = selected.length;          
                
                var v = _.reduce(selected, function(memo, value, key){ 
                        memo = memo + value.pid;
                        if (key >= 0 && key < ll - 1) memo = memo +",";
                                return memo; 
                        }, "");  
                window.open('print?pids='+v,'_blank');
                cleanWindow();
        },
        
        /**
         * Create pdf from selection
         * @method
         */
        pdf:function() {
                var selected = this.ctx.selected;
                var ll = selected.length;          
                if ((this.ctx.configuration["pdf"]) && (this.ctx.configuration["pdf"]["limit"]) && (this.ctx.configuration.pdf.limit> ll)) {
                        var v = _.reduce(selected, function(memo, value, key){ 
                                        memo = memo + value.pid;
                                        if ((key >= 0) && (key < ll-1)) {
                                                memo = memo +",";
                                        }
                                        return memo; 
                        }, "");  
                        window.open("api/pdf/selection?pids="+ v,"_blank");
                        cleanWindow();
                } 
        },


        closeButtons: function() {
                $(".clp_cancel_buttons").load("svg.vm?svg=close");
        }
}
