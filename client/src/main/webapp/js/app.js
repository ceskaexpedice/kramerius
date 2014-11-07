/**
 * Application error
 * @constructor 
 */
function AppError (msg, caller, level) {
        this.messs = msg;        
        this.caller = caller;
        this.level = level;
}

AppError.prototype = {
        getMessage: function() { return this.messs; },
        getCaller: function() { return this.caller; },        
        getLevel: function() { return this.level; }
}


/** 
 * Events application handler 
 * @constructor 
 */
function ApplicationEvents() {}

ApplicationEvents.prototype = {

        handlerEnabled:true,        
        
        /**
         * Enable events firing 
         */
        enableHandler:function() {
                this.handlerEnabled = true;
        },
        /**
         * Disable events firing    
         */
        disableHandler:function() {
                this.handlerEnabled = false;
        },        

        /** contains event handlers*/
        handlers: [],
        
        

        /** 
         * Trigger event 
         * @method
         */
        trigger:function(type, data) {
                if (!this.handlerEnabled) {
                        return;
                }
                $.each(this.handlers,function(idx,obj) { 
                        obj.apply(null, [type,data]);
                });
        },

        /** 
         * Add new handler 
         *@method
         */
        addHandler: function(handler) {
                this.handlers.push(handler);
        },

        /** 
         * Remove already registered handler 
         * @method
         */
        removeHandler:function(handler) {
                /*
                var index = this.handlers.indexOf(handler);
                var nhandlers = [];
                if (index >=0)  {
                        for (var i=0;i<index;i++) {
                                 nhandlers.push(this.handlers[i]);
                        }
                        for (var i=index+1;i<this.handlers.length;i++) {
                                 nhandlers.push(this.handlers[i]);
                        }
                }
                this.handlers = nhandlers;
                */
        }
}


/**
 * Application object holds all properties for application. Referenced as singleton K5. 
 * @constructor
 */	
function Application() {

        this.initialized = false;

        /** 
         * Event handlers 
         * <pre><code>
         *  K5.eventsHandler
         * </code></pre>
         * @member
         * {@link ApplicationEvents} 
         */
        this.eventsHandler =  new ApplicationEvents();

        /** 
         * Api point 
         * <pre><code>
         *  K5.api
         * </code></pre>
         * @member 
         * {@link ClientAPIDev} 
         */		
        this.api = new ClientAPIDev(this);

        /** 
         * I18N object 
         * <pre><code>
         *  K5.i18n
         * </code></pre>
         * @member 
         * {@link I18N} 
         */		
        this.i18n = new I18N(this);
        
        this.cool = new Cool(this);	
        
        /** 
         * Authentication support 
         * @member 
         * {@link AuthenticationSupport} 
         */
        this.authentication = new AuthenticationSupport(this); 
        


        /** 
         * Gui objects	
         * @member 
         */
        this.gui = {
                clipboard: new Clipboard(this),
                vc: new VirtualCollections(this),
                historyitems:[]
        };

        /**
         * Simple server logging
         * @member
         */
        this.serverLog = function(simplemess) {
                $.get("log?message="+simplemess,function(data) { });
        };        


        

        this.preventScrolling = function() {
                document.body.addEventListener('touchmove', function(event) {
                        event.preventDefault();
                }, false);         
        }

        

        this.previoushash = null;

        /** 
         * Initialization  method. Before adnd initialization process fires 'application/init/start' or 'application/init/start' events.
         * @member
         * @param {object} configuration instance.
         * @fires AppError 
         * {@tutorial tutorialID}
         */
        this.init = function (configuration) {
                if(console) console.log("singleton initalization starting ...");
                this.eventsHandler.trigger("application/init/start",configuration);

                // receive dictionary
                if (configuration["language"]) {
                        this.i18n.askForDictionary(configuration["language"]);
                }

                // receive collections 
                if (configuration["page"] && configuration["page"]==="collections") {
                        K5.api.askForCollections();
                }

                if ((configuration["page"] && configuration["page"]==="home") || (!configuration["page"])) {
                        K5.api.askForLatest();
                        K5.api.askForPopular();
                        K5.api.askForCool();
                }

                // session values
                if (configuration["conf"]["pdf"]) {
                    K5.gui.clipboard.initConfiguration(configuration["conf"]["pdf"]);
                }

                // session values
                if (configuration["session"]) {
                        if (configuration.session["clipboard"]) {
                                this.gui.clipboard.init(configuration.session["clipboard"]);                                           
                        }
                }
                
                //context menu and viewers
                if (configuration["defs"]) {
                        if (configuration["defs"]["menu"]) {
                                this.gui["nmenu"] =  new MenuActionsControll();
                                this.gui["nmenu"].initalizeActions(configuration["defs"]["menu"]);
                        }                        
                        if (configuration["defs"]["viewers"]) {
                                this.gui["viewers"] =  new ViewersControll();
                                this.gui["viewers"].initalizeViewers(configuration["defs"]["viewers"]);
                        }
                } 

                $(window).bind("hashchange", _.bind(function () {
                        if (window.location.hash !== this.previoushash) {
                                this.eventsHandler.trigger("widow/url/hash",window.location.hash);
                                this.previoushash = window.location.hash;
                        }
                },this));

                $(window).resize(_.bind(function() {
                    this.eventsHandler.trigger("window/resized",configuration);
                },this));

                
                $(document).keydown(function(e) {
                    switch(e.which) {
                        case 37: // left
                        K5.eventsHandler.trigger("application/keys/left",[]);
                        break;


                        case 39: // right
                        K5.eventsHandler.trigger("application/keys/right",[]);
                        break;


                        default: return; // exit this handler for other keys
                    }
                    e.preventDefault(); // prevent the default action (scroll / move caret)
                });

                //prevent scrolling
                this.preventScrolling();

                // receive 
                this.eventsHandler.trigger("application/init/end",configuration);

                this.initialized=true;
       }
        
        
};

/** 
 * Singleton instance.  
 *
 * @global
 */
var K5 = new Application();


