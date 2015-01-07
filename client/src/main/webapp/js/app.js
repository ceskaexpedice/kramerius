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

        //this.cool = new Cool(this);	


        /** 
         * Authentication support 
         * @member 
         * {@link AuthenticationSupport} 
         */
        this.authentication = new AuthenticationSupport(this); 
        

        this.outputs = {
                        'pdf':new PDFSupport(this),
                        'print':new PrintSupport(this)
        };

        /** 
         * Gui objects	
         * @member 
         */
        this.gui = {
                clipboard: new Clipboard(this),
                vc: new VirtualCollections(this),
                footer:new Footer(this),
                historyitems:[],
                page:''
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
        };
        // to utils
        this.proccessDetails = function(json, info) {
            var model = json["model"];
            var details = json["details"];
            var root_title = json["root_title"];
            if (details) {

                if (model === "periodical") {

                    info.short = " ";
                    info.full = " ";
                    info.min = root_title;
                    

                } else if (model === "periodicalvolume") {

                    info.short = root_title.substring(0, this.maxInfoLength) +
                            K5.i18n.translatable('field.datum') + ": " + details.year + " ";
                    info.full = "<div>" + root_title + "</div>" +
                            K5.i18n.translatable('field.datum') + ": " + details.year + " ";
                    info.min = K5.i18n.translatable('field.datum') + ": " + details.year + " ";
                    if (details.volumeNumber) {
                        var v = K5.i18n.translatable('mods.periodicalvolumenumber') + " " + details.volumeNumber;
                        info.short += v;
                        info.full += v;
                        info.min += v;
                    }

                } else if (model === "internalpart") {
                    var dArr = details.split("##");
                    info.full = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
                    info.short = dArr[0] + " " + dArr[1] + " " + dArr[2] + " " + dArr[3];
                } else if (model === "periodicalitem") {
                    if (details.issueNumber !== root_title) {
                        var s = details.issueNumber + " " + details.date + " " + details.partNumber;
                        info.full = s;
                        info.short = s;
                        info.min = s;
                    } else {
                        info.full = details.date + " " + details.partNumber;
                        info.short = details.date + " " + details.partNumber;
                        info.min = details.date + " " + details.partNumber;
                    }
                } else if (model === "monographunit") {
                    info.full = details.title + " " + details.partNumber;
                    info.short = details.title + " " + details.partNumber;
                    info.min = details.title + " " + details.partNumber;
                } else if (model === "page") {
                    var s= K5.i18n.translatable('mods.page.partType.' + details.type);
                    info.full = s;
                    info.short = s;
                    info.min = json.title + " " + s + ""  ;
                } else {
                    info.full = details;
                    info.short = details;
                    info.min = details;
                }
            } else {
                
                    info.short = " ";
                    info.full = " ";
                    info.min = root_title;
            }
        };

        

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
                        this.i18n.askForDictionary(configuration["language"],configuration["country"]);
                }

                if (configuration["page"]) {
                    this.gui.page=configuration["page"];
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

                if (configuration["conf"]["pdf"]) {
                    K5.outputs.pdf.initConfiguration(configuration["conf"]["pdf"]);
                }

                // pdf configuration
                if (configuration["conf"]["pdf"]) {
                    K5.gui.clipboard.initConfiguration(configuration["conf"]["pdf"]);
                }

                // pdf configuration
                if (configuration["conf"]["authentication"]) {
                    K5.authentication.initConfiguration(configuration["conf"]["authentication"]);
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

                        if (configuration["defs"]["downloadoptions"]) {
                            this.gui["downloadoptions"] =  new MenuActionsControll();
                            this.gui["downloadoptions"].initalizeActions(configuration["defs"]["downloadoptions"]);
                        }

                        if (configuration["defs"]["shareoptions"]) {
                            this.gui["shareoptions"] =  new MenuActionsControll();
                            this.gui["shareoptions"].initalizeActions(configuration["defs"]["shareoptions"]);
                        }
                        
                        if (configuration["defs"]["viewers"]) {
                                this.gui["viewers"] =  new ViewersControll();
                                this.gui["viewers"].initalizeViewers(configuration["defs"]["viewers"]);
                                this.gui["viewers"].loadSessionInitialization(configuration["session"]);
                        }
                } 
                // index configuration
                if (configuration["conf"]["index"]) {
                    this.indexConfig = configuration["conf"]["index"];
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

                // left and right arrows
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


