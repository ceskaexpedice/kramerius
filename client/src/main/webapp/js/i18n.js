/* global K5 */

/**
 * @description
 * Envelope I18n operations. Accessed via singleton <code>K5</code>
 * <pre><code>
 *  K5.i18n.askForDictionary(function(data) {
 *      alert("new dictionary :  "+data);
 *  });
 * </code></pre>
 * <pre><code>
 *  var i18nkey ="application.title";
 *  K5.eventsHandler.addHandler(function(type, data) {
 *      if (type == "i18n/dictionary") {
 *          var translated = K5.i18n.translate(i18nkey);
 *          alert(translated);
 *      }
 *  });
 * </code></pre>
 * @constructor
 * @param {Application} application - The application instance.
 */
function I18N(application){
    this.application = application;
    this.application.eventsHandler.addHandler(_.bind(function(type, configuration) {
        if (type == "i18n/dictionary") {
            this.translateAll();
        } 
    },this));
}

I18N.prototype= {

    ctx:{},
 
    /** 
     * Tests if given key is present in the context 
     * @param {string} keys - tested keys
     * @method
     */
    isKeyReady: function(keys) {
        return lookUpKey(keys, this.ctx);
    },
    
    initConfiguration: function(data) {
        this.ctx["configuration"]=data;
    },
    
    changeLanguage:function(lang,country, whenready) {
        $('.opacityloading').show();
        K5.eventsHandler.addHandler(function(type, data) {
            if (type === "i18n/dictionary") {
                $('.opacityloading').hide();
            }
        });
        this.askForDictionary(lang,country, whenready);
    },

    /** 
     * Sends request for new dictionary. It fires "i18n/dictionary" event.
     * @param {string} lang - Requesting language
     * @param {string} country - Requesting coutry - may be null
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
     */
    askForDictionary:function(lang,country, whenready) {
        $.getJSON("dictionary.vm?language=" + lang, _.bind(function(data) {
            this.ctx['language']=lang;
            this.ctx['country']=country;
            this.ctx['dictionary']=data;
            if (whenready != null) whenready.apply(null, [data]);
            $.getJSON("api/vc?sort=ASC&langCode=" + lang, _.bind(function(data) {
                if (this.ctx["configuration"]["cdkSources"]) {
                    for(var i=0; i< data.length; i++){
                        this.ctx['dictionary'][data[i].pid] = data[i].descs[lang];
                    }
                    $.getJSON("api/sources", _.bind(function(data) {
                        this.ctx['linkToSources'] = {};
                        for(var i=0; i< data.length; i++){
                            this.ctx['dictionary'][data[i].pid] = data[i].descs[lang];
                            this.ctx['linkToSources'][data[i].pid] = data[i].url;
                        }
                        if (whenready != null) whenready.apply(null, [data]);
                        this.application.eventsHandler.trigger("i18n/dictionary",data);
                    },this));
                } else {
                    for(var i=0; i< data.length; i++){
                        this.ctx['dictionary'][data[i].pid] = data[i].descs[lang];
                    }
                    
                    if (whenready != null) whenready.apply(null, [data]);
                    this.application.eventsHandler.trigger("i18n/dictionary",data);
                }
                
            },this));
        },this));

	},


    /**
     * Sends request for new text
     * @param {string} nm - Name of the text
     * @param {string} country - Requesting coutry - may be null
     * @param {requestCallback} whenready  - Callback handling responses.
     * @method
	 */
    askForText:function(nm, lang, whenready) {
                $.getJSON("texts.vm?text=" + nm+"&lang="+lang, _.bind(function(data) {
                        if (!K5.i18n.isKeyReady("texts")) {
                                this.ctx['texts']={};
                        }
                        this.ctx['texts'][nm]=data[nm];
                        if (whenready != null) whenready.apply(null, [data]);
                },this)).error(function(jqXHR, textStatus, errorThrown) {
                        console.log("error " + textStatus);
                        console.log("incoming Text " + jqXHR.responseText);
                });
        },         

        
        /**
         * Generate element with class 'translate'. 
         * Every elements contains class 'transalte' are translated when 'i18n/dictionary' has been fired
         * @param {string} key - i18n key
         * @method
         */
        translatable: function(key) {
            var t = this.ctx.dictionary[key]!=null? this.ctx.dictionary[key]: key;
            return '<span class="translate" data-key="' + key + '">' + t + '</span>';
        },
        
        /**
         * Returns translated key 
         * @param {string} key - Key to be translated
         * @method
         */
        translate:function(key) {
            var t = this.ctx.dictionary[key]!=null? this.ctx.dictionary[key]: key;
            return t;
        },
        
        hasKey: function(key){
            return this.ctx.dictionary.hasOwnProperty(key);
        },
        
        translatableElm:function(key, elmId) {
                var t = this.ctx.dictionary[key]? this.ctx.dictionary[key]: key;
                $(elmId).attr('data-key',key);
                if (!$(elmId).hasClass('translate')) {
                        $(elmId).addClass('translate');
                }
                $(elmId).text(t);
        },


        /** prelozi vsechny podlementy oznacene tridou .translate */
        k5translate:function(obj) {
                $(obj).find('.translate').each(function() {
                        var key = $(this).data("key");
                        $(this).text(K5.i18n.ctx.dictionary[key]);
                });
                $(obj).find('.translate_title').each(function() {
                        var key = $(this).data("key");
                        $(this).attr('title', K5.i18n.ctx.dictionary[key]);
                });
                $(obj).find('.translate_placeholder').each(function() {
                        var key = $(this).data("key");
                        $(this).attr('placeholder', K5.i18n.ctx.dictionary[key]);
                });
        },



        translateAll: function() {
            $('.translate').each(function() {
                var key = $(this).data("key");
                $(this).text(K5.i18n.ctx.dictionary[key]);
            });
            $('.translate_title').each(function() {
                var key = $(this).data("key");
                $(this).attr('title', K5.i18n.ctx.dictionary[key]);
            });
            $('.translate_placeholder').each(function() {
                    var key = $(this).data("key");
                    $(this).attr('placeholder', K5.i18n.ctx.dictionary[key]);
            });
        }
}

/*
function k5translateAll() {
    $('.translate').each(function() {
        var key = $(this).data("key");
        $(this).text(dictionary[key]);
    });
    translateCollections();
}

function k5translate(obj) {
    $(obj).find('.translate').each(function() {
        var key = $(this).data("key");
        $(this).text(dictionary[key]);
    });
}


function translatable(key) {
    return '<span class="translate" data-key="' + key + '">' + dictionary[key] + '</span>';
}


function vctranslatable(key, collections, language) {
    return '<span class="vc" data-key="' + key + '">' + collections[key][language] + '</span>';
}
*/
