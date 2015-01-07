
/**
 * Profile dialog
 * @constructor
 */
function ProfileDisplay(app) {
        this.application = app;        
}

ProfileDisplay.prototype = {
        
        dirtyFlag:false,        

        /**
         * Open profile dialog
         * @method
         */
        open:function() {
                var user = K5.authentication.ctx.user;
                $("#prfl_text").text(user["firstname"] +' @ '+ user["surname"]) ;
                this.refreshButtons();
                this.profileContent();
                cleanWindow();
                divopen("#prfl_dialog");
        },    

        close: function() {
                cleanWindow();
        },

        toggle: function() {
                if (!visible("#prfl_dialog")) this.open();
                else this.close();
        },  


        /**
         * Enable or disable buttons (depends on context)
         * @method
         */
        refreshButtons:function() {
                // enable buttons                
                $(".prfl_footer .button").each(function() {
                    if ($(this).data("ctx")) {
                        var attr = $(this).data("ctx").split(";");
                        if (jQuery.inArray('all', attr) > -1 || jQuery.inArray('item', attr) > -1) {
                                //add to favorites button
                                if (K5.authentication.profileDisplay.isItemPage()) {
                                        if (!K5.authentication.profileDisplay.isCurrentPidInFavorites()) {
                                                $(this).show();
                                        } else {
                                                $(this).hide();
                                        }
                                } else {
                                        $(this).hide();
                                }
                        }
                        if (jQuery.inArray('dirty', attr) > -1) {
                                if (K5.authentication.profileDisplay.dirtyFlag) {
                                        $(this).show();
                                } else {
                                        $(this).hide();
                                }
                        }
                    }
                });
        },

        /**     
         * Refresh content
         * @method
         */
        profileContent:function() {
                $("#prfl_content_favorites").empty();
                if (K5.authentication.ctx.profile.favorites) {
                        $.each(K5.authentication.ctx.profile.favorites, _.bind(function( index, value ) {
                               this.createThumb(value); 
                        },this));
                }                 
                $("#prfl_content_favorites").append($("<div>", {"class":"clear"}));   
                this.closeButtons();
        },
        

        /**
         * Create thumb
         * @method
         * @param {String} pid - Pid of the object
         */
        createThumb:function(pid) {
                this._thumb_counter += 1;

                //TODO: to css
                // hlavni kontejner                  
                var icontainer =  $("<div>");

                var obrdiv =  $("<div>");

                var ahref =  $("<a>");
                ahref.attr("href","javascript:K5.api.gotoItemPage('"+pid+"')");
                                
                var img =  $("<img>");
                img.attr("align","middle");                                 
                img.attr("vspace","2");                                 
                img.attr("src","api/item/"+pid+"/thumb");                                 

                var svgdiv =  $("<div>");
                svgdiv.addClass("prfl_ok_buttons");
                svgdiv.addClass("small");
                svgdiv.attr("onclick","K5.authentication.profileDisplay.removeFromFavorites('"+pid+"',true);");


                ahref.append(img);
                obrdiv.append(ahref);

                icontainer.append(obrdiv);
                icontainer.append(svgdiv);
                

        

                $("#prfl_content_favorites").append(icontainer);     
        },

        /**
         * Refresh close buttons
         * @method
         */
        closeButtons: function() {
                $(".prfl_ok_buttons").load("svg.vm?svg=close");
        },
        
        isItemPage:function() {
                var selected = (K5.api.ctx["item"] && K5.api.ctx.item["selected"] ? K5.api.ctx.item["selected"] : null); 
                return selected != null;
        },
        
        /**
         * Returns true if current selected page is part of favorites collection
         * @returns {Boolean}
         */
        isCurrentPidInFavorites:function() {
                var sel = K5.api.ctx.item.selected;
                return this.indexInFavorites(sel) > -1;                                               
        },                
               
        /**
         * Finds and returns index of pidin favorites array otherwise returns -1
         * @method
         * @param {String} pid Pid
         * @returns {Integer} 
         */         
        indexInFavorites:function(pid) {
                return _.reduce(K5.authentication.ctx.profile.favorites, function(memo, item,context){ 
                                if ((memo === -1) && (pid === item)) return context;
                                return memo;
                        }, -1);
        },

        /**
         * Delete concrete pid from favorites
         * @method
         * @param {String} pid Deleting pid
         * @param {Boolean} refreshcontent Also refresh content
         */
        removeFromFavorites:function(pid, refreshcontent) {
                var favorites = K5.authentication.ctx.profile.favorites;                
                var nfavorites = [];
                $.each(favorites, _.bind(function( index, value ) {
                        if (value !== pid)  {
                                nfavorites.push(value);
                        }
                },this));                   
                K5.authentication.ctx.profile.favorites = nfavorites;
                this.dirtyFlag = true;
                if (refreshcontent) {
                        this.profileContent();
                        this.refreshButtons();
                }
        },

        /**
         * Append new pid into favorites collection
         * @method
         * @param {String} pid Deleting pid
         * @param {Boolean} refreshcontent Also refresh content
         * @returns {Boolean} 
         */
        appendToFavorites:function(pid, refreshcontent) {
                if (this.indexInFavorites(pid) >= -1 ) {
                        if (!K5.authentication.ctx.profile.favorites) {
                            K5.authentication.ctx.profile.favorites= [];
                        }
                        K5.authentication.ctx.profile.favorites.push(pid);
                        this.dirtyFlag = true;
                        if (refreshcontent) {
                                this.profileContent();
                                this.refreshButtons();
                        }
                        return true;
                } else return false;
        },               

        /**
         * Remove current displayed pid from favorites
         * @param {Boolean} refreshcontent Refresh content
         * @method
         */
        removeCurrentFromFavorites:function(refreshcontent) {
                this.removeFromFavorites(K5.api.ctx.item.selected, refreshcontent);                
        },        

        /**
         * Add current displayed pid into favorites
         * @method
         * @param {Boolean} refreshcontent Refresh content
         * @returns {Boolean}
         */
        appendCurrentToFavorites:function(refreshcontent) {
                this.appendToFavorites(K5.api.ctx.item.selected, refreshcontent);                
        }, 
        
        /**
         * Store current state of the profileContent
         * @method 
         */        
        store:function(fnc) {
                var encodedData = Base64.encode(JSON.stringify(K5.authentication.ctx.profile));
                var refresh = _.bind(function() { 
                        this.refreshButtons();  this.profileContent(); 
                        if (fnc) {
                            fnc.apply(null, []);
                        }
                        K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                }, this);
                var postok = _.bind(function() { 
                    K5.authentication.askForProfileRequest(refresh); 
                }, this);

                $.ajax({
                        dataType: "json",
                        type: "POST",
                        //contentType:'application/json',
                        'url': 'authentication?action=profile',
                        //'beforeSend': function(xhr) { xhr.setRequestHeader("Authorization","Basic " + Base64.encode(uname + ":" + pass)); },
                        data: {'encodedData':encodedData},
                        success: postok
                });
        }
}




