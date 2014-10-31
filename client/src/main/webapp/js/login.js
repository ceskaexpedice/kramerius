/**
 * Authentication supports login and logout actions
 * @constructor
 */
function AuthenticationSupport(application) {
        this.application = application;

        this.application.eventsHandler.addHandler(_.bind(function(type, configuration)  {
                if (type === "application/init/end") {
                        if (configuration["user"]) {
                                this.contextdata(configuration["user"]);

                                if (configuration["profile"]) {
                                        if (!configuration.profile["favorites"]) {
                                            configuration.profile["favorites"] = [];
                                        }
                                        this.profiledata(configuration["profile"]);
                                        this.profileDisplay = new ProfileDisplay(application);
                    
                                } else {
                                        this.askForProfileRequest();
                                }
                        }
                }
        }, this));
}

AuthenticationSupport.prototype = {
        
        ctx:{},
        
        profileDisplay: null,        
        
        registration:new Registration(),

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

                        // receive profile
                        // this.askForProfileRequest();

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

        changePassword: function(npass) {
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


