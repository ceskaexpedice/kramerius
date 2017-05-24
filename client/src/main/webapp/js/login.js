/**
 * Authentication supports login and logout actions
 * @constructor
 */
function AuthenticationSupport(application) {
        this.application = application;

        this.application.eventsHandler.addHandler(_.bind(function(type, configuration)  {
                if (type === "application/init/end") {
                        if (configuration["user"]) {
                                var afterProfile = null;
                                if (configuration["session"] && configuration["session"]["loginActions"] && 
                                        configuration["session"]["loginActions"]["addToFavorites"]) {
                                    afterProfile = _.bind(function() {
                                        var pid = configuration["session"]["loginActions"]["addToFavorites"]["pid"];
                                        this.storeFavoritesToSession();
                                        if (pid) {
                                            this.profileDisplay.appendToFavorites(pid,false);
                                            this.profileDisplay.store(function() {
                                                K5.api.gotoItemPage(pid);
                                            });
                                        }
                                    },this);
                                }
                                this.contextdata(configuration["user"]);
                                if (configuration["profile"]) {
                                        if (!configuration.profile["favorites"]) {
                                            configuration.profile["favorites"] = [];
                                        }
                                        this.profiledata(configuration["profile"]);
                                        this.profileDisplay = new ProfileDisplay(application);
                                        if (afterProfile) afterProfile();
                                } else {
                                    if (afterProfile) {
                                        this.askForProfileRequest(afterProfile);
                                    } else {
                                        this.askForProfileRequest();
                                    } 
                                }
                        }
                        
                        
                }
        }, this));
}

AuthenticationSupport.prototype = {
        
        ctx:{
            "configuration":{},
            "session":{}
        },
        
        profileDisplay: null,
        
        registration:new Registration(),

        initConfiguration: function(conf) {
            this.ctx.configuration["authentication"]=conf;
        },
        
        /** 
         * tests if given key is present in the context 
         * @method
         */
        isKeyReady: function(keys) {
                return lookUpKey(keys, this.ctx);
        },

        contextdata:function(jsonrep) {
                this.ctx["user"]=jsonrep;
                this.ctx["logged"] = (this.ctx.user.lname !== "not_logged");                
        },
        
        profiledata:function(jsonrep) {
                this.ctx["profile"] = jsonrep;
                // for display profile
                this.profileDisplay = new ProfileDisplay(this.application);
        },


        askForRemoveFromProfile:function() {
            var pid = K5.api.ctx.item.selected;
            if (pid) {
                if (this.profileDisplay != null) {
                    K5.authentication.profileDisplay.removeCurrentFromFavorites(false);
                    K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                    K5.authentication.profileDisplay.store();
                    //K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                }
            }
        },
        
        askForAppendToProfile:function() {
            var pid = K5.api.ctx.item.selected;
            if (pid) {
                if (this.profileDisplay != null) {
                    K5.authentication.profileDisplay.appendCurrentToFavorites(false);
                    K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                    K5.authentication.profileDisplay.store();
                    //K5.eventsHandler.trigger("application/menu/ctxchanged", null);
                } else {

                    var fav = {
                        "pid":pid
                    };
                    // save state and redirect
                    this.ctx.session["addToFavorites"] = fav;

                    K5.authentication.storeFavoritesToSession();
                    K5.authentication.options();
                }
            }
        },


        /**
         * Sends logout request and delete context informations
         * @method
         */
        askForLogoutRequest:function(whenready) {
                var successFunction = _.bind(function(data) {
                        delete this.ctx["user"];
                        delete this.ctx["logged"];
                        delete this.ctx["profile"];
                        if (whenready) whenready.apply(null, [data]);
                        this.application.eventsHandler.trigger("authentication/logout",data);
                },this);
                
                $.ajax({
                        dataType: "json",
                        'url': 'authentication?action=logout',
                        success: successFunction
                });
        },
        

        /**
         * Sends logout request and delete context informations
         * @method
         */
        askForChangePswd:function(oldpass, npass, successFunction, failFunction) {
                $.ajax({
                        dataType: "json",
                        "method":"POST",
                        'url': 'authentication?action=savepass',
                        data: {
                             'pswd':npass,
                             'opswd':oldpass
                        },
                        "dataType": "json",
                        success: successFunction,
                        error : failFunction
                });
        },
        


        /** 
         * Sends login request and store user informations
         * @method
         */
        askForLoginRequest:function(uname, pass,whenready) {
                var successFunction = _.bind(function(data) {
                        if (!this.isKeyReady("user")) {
                                this.ctx["user"]={};
                        }
                        this.contextdata(data);
                        if (whenready) whenready.apply(null, [data]);
                        this.application.eventsHandler.trigger("authentication/login",data);
                },this);

                $.ajax({
                        dataType: "json",
                        'url': 'authentication?action=login',
                        'beforeSend': function(xhr) { xhr.setRequestHeader("Authorization","Basic " + Base64.encode(uname + ":" + pass)); },
                        success: successFunction
                });
        }, 
        

        /**
         * Sends request for profile informations and stores them into context
         * @method
         */
        askForProfileRequest:function(whenready) {
                var successFunction = _.bind(function(data) {
                        if (!this.isKeyReady("profile")) {
                                this.ctx["profile"]={};
                        }
                        this.profiledata(data);
                        if (whenready) whenready.apply(null, [data]);
                        this.application.eventsHandler.trigger("authentication/profile",data);
                },this);
                $.ajax({
                        dataType: "json",
                        'url': 'authentication?action=profile',
                        success: successFunction
                });
        },
        
        
        
        /**
         * Display or hide logging options (Google plus, Facebook). If only K5 authentication is supported, redirect to login page  
         * @method
         */
        options:function() {
            cleanWindow();
            var show = _.bind(function() {
                // hack ?? check homepage
                if (K5.gui.page === 'home') {
                    $("div.infobox").hide();
                }
                var conf = this.ctx.configuration["authentication"];
                if (conf) {
                    var gp = conf["gplus"];
                    var fb = conf["fb"];
                    if (gp || fb) {
                        divopen("div.logoptions");
                    } else {
                        linkWithReturn('?page=login');
                    }
                } else {
                    linkWithReturn('?page=login');
                }
            },this);
            
            var hide = _.bind(function() {
                cleanWindow();
                // hack ?? check homepage
                if (K5.gui.page === 'home') {
                    $("div.infobox").show();
                }
            },this);
            
            var v = $("div.logoptions").is(":visible");
            if (!v) show();
            else hide();
            
        },
        
        /** 
         * Perform login action and redirect
         * @method
         */
        loginAndRedirect: function(uname, pass, successUrl, errorUrl) {
            var logged = K5.authentication.isKeyReady("logged");
            if (!logged) {
                // authentication request
                K5.authentication.askForLoginRequest(uname,pass, function(data) {
                        var logged = K5.authentication.ctx["logged"];
                        if (!logged) {
                            window.location.assign(errorUrl)
                        } else {
                            window.location.assign(successUrl)
                        }
                }); 
            }
        },

        /**
         * Change pswd
         */
        changePassAndRedirect: function(oldpass, npass, successUrl, errorUrl) {
            var logged = K5.authentication.isKeyReady("logged");
            if (logged) {
                K5.authentication.askForChangePswd(oldpass,npass, function() {
                    window.location.assign(successUrl);
                }, function() {
                    window.location.assign(errorUrl);
                }); 
            } 
        },
        
        storeFavoritesToSession:function() {
            K5.api.storeToSession("loginActions",this.ctx.session);
        },

        
        /** 
         * Perform logout action and redirect
         * @method
         */
        logoutAndRedirect: function(redirectUrl) {
                // authentication request
                K5.authentication.askForLogoutRequest(function(data) {
                    window.location.assign(redirectUrl)
                }); 
        }
}


