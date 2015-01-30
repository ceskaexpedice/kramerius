

/**
 * I18N support objects
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
 
    /** tests if given key is present in the context */
    isKeyReady: function(keys) {
        return lookUpKey(keys, this.ctx);
    },
    
    
    /** Requests for resource bundle */
    askForDictionary:function(lang,country, whenready) {
        $.getJSON("dictionary.vm?language=" + lang, _.bind(function(data) {
                this.ctx['language']=lang;
                this.ctx['country']=country;
                this.ctx['dictionary']=data;
                if (whenready != null) whenready.apply(null, [data]);
                $.getJSON("api/vc", _.bind(function(data) {
                    for(var i=0; i< data.length; i++){
                        this.ctx['dictionary'][data[i].pid] = data[i].descs[lang];
                    }
                    if (whenready != null) whenready.apply(null, [data]);
                    this.application.eventsHandler.trigger("i18n/dictionary",data);
                },this));
	    },this));
	},



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

        /** generovani dom element -> prelozitelny */
        translatable: function(key) {
            var t = this.ctx.dictionary[key]!=null? this.ctx.dictionary[key]: key;
            return '<span class="translate" data-key="' + key + '">' + t + '</span>';
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
            // ?? kam?    
            //K5.gui.vc.translateCollections();
        }
                
        
}


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


