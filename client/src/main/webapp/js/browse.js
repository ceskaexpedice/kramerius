K5.eventsHandler.addHandler(function(type, data) {
    if (type == "i18n/dictionary") {
        if (!K5.gui.browse) {
            K5.gui.browse = new Browse(K5, $("#browse"));
        }
    }
    if (type === "widow/url/hash") {
        K5.gui.browse.processHash(true);
    }
});


function Browse(application, elem) {
    this.application = application;
    this.elem = elem;
    this._init();
}

Browse.prototype = {
    ctx: {},
    _init: function() {
        this.browseField = "browse_title";
        this.showField = "dc.title";
        this.rowsPerRequest = 50;
        this.maxNumSections = 50;
        this.maxSectionLength = 300;
        this.groupedParams = "&group=true&group.truncate=true&group.ngroups=true&group.field=root_pid&group.format=simple&group.sort=level%20asc";
        this.facetParams = "&facet=true&facet.field=" + this.browseField + "&facet.sort=false&facet.mincount=1";
      
        this.addContextButtons();
        this.typesDiv = this.elem.find(".types");
        this.sectionsDiv = this.elem.find(".sections>.scroll>ul");
        this.sectionsScroll = this.elem.find(".sections>.scroll");
        
        this.rightArrow = $('<div>', {class: 'medium arrow'});
        this.rightArrow.css({right: "4px"});
        this.rightArrow.load("svg.vm?svg=arrowright");

        //this.leftArrow = $('<div>', {class: 'arrow arrowLeft'});
        this.leftArrow = $('<div>', {class: 'medium arrow'});
        this.leftArrow.css({left: "4px"});
        this.leftArrow.load("svg.vm?svg=arrowleft");
        this.sectionsScroll.parent().append(this.leftArrow);
        this.sectionsScroll.parent().append(this.rightArrow);
        
        
        this.rightArrow.click(_.bind(function() {
            this.doDeltaScroll(1);
        }, this));
        this.leftArrow.click(_.bind(function() {
            this.doDeltaScroll(-1);
        }, this));
        
        this.sectionDiv = this.elem.find(".section");
        this.sectionDiv.scroll(_.bind(function() {
            this.onScroll();
        }, this));
        this.getTypes();
        
        this.columns = 1;
        if (($.cookie('browse_columns') !== undefined) && !isNaN(parseInt($.cookie('browse_columns')))) {
            this.columns = parseInt($.cookie('browse_columns'));
        }
        this.resizeResults();
        
        $('.loading').hide();
    },
    plus: function(){
        this.columns++;
        this.resizeResults();
        $.cookie('browse_columns', this.columns);
    },
    minus: function(){
        if(this.columns > 1){
            this.columns--;
            this.resizeResults();
            $.cookie('browse_columns', this.columns);
        }
    },
    resizeResults: function(){
        var w = Math.floor(100.0 / this.columns);
        $("#browse .res").css("width", "calc("+w+"% - 6px)");
    },
    doDeltaScroll: function(dx){
        var finalPos = this.sectionsScroll.scrollLeft() + this.sectionsScroll.width() * 0.8 * dx;
        this.doAbsScroll(finalPos);
    },
    doAbsScroll: function(finalPos){
        var speed = 500;
        var th = this;
        th.scrolling = true;
        this.sectionsScroll.animate({scrollLeft: finalPos}, speed, function() {
            th.checkArrows();
            th.scrolling = false;
        });
    },
    checkArrows: function() {
        if (this.sectionsScroll.scrollLeft() <= 0) {
            this.leftArrow.hide();
        } else {
            this.leftArrow.show();
        }

        if (this.sectionsScroll.scrollLeft() >= ($(this.sectionsScroll)[0].scrollWidth - this.sectionsScroll.width())) {
            this.rightArrow.hide();
        } else {
            this.rightArrow.show();
        }
    },
    onScroll: function() {
        var el = this.sectionDiv.find('.more_docs');
        if (el.length > 0) {
            if (isScrolledIntoView($(el), this.sectionDiv)) {
                var start = el.data('start');
                var from = el.data('from');
                var to = el.data('to');
                this.getSectionDocs(start, from, to);
            }
        }
    },
    processHash: function(full) {

        var hash = window.location.hash;
        if (hash.length > 1) {
            this.sectionsScroll.parent().show();
            var parts = hash.substring(1).split(";");
            var typ = parts[0];
            if (this.selectTyp !== typ) {
                if("authors"===typ){
                    this.loadAuthors();
                }else if("titles"===typ){
                    this.loadTitles();
                }else{
                    var count = this.typesDiv.find("." + typ).data("count");
                    this.loadTyp(typ, count);
                }
            }
            
            if (parts.length > 1) {
                var section = parts[1];
                section = section.substring(1,section.length-1);
                if (this.selectedSection !== section) {
                    this.loadSection(section);
                }
            }
            
        }else{
            this.sectionsScroll.parent().hide();
        }
    },
    renderTypes: function() {
        //Pridame tituly
        var div = $('<div/>', {class: 'button titles'});
        
        div.click(_.bind(function() {
            window.location.hash = 'titles';
        }, this));
        div.append('<label>' + K5.i18n.translatable('browse.titles') + ' </label>');
        
        var input = $('<input />', {type: "text"});
        input.keyup(_.bind(function(ev){
            this.doSuggest('titles');
        }, this));
        div.append(input);
        this.typesDiv.append(div);
        
        //Pridame autori
        div = $('<div/>', {class: 'button authors'});
        
        div.click(_.bind(function() {
            window.location.hash = 'authors';
        }, this));
        div.append('<label>' + K5.i18n.translatable('browse.authors') + ' </label>');
        
        input = $('<input />', {type: "text"});
        input.keyup(_.bind(function(ev){
            this.doSuggest('authors');
        }, this));
        div.append(input);
        this.typesDiv.append(div);
        
    },
    doSuggest: function(typ){
        var val = this.typesDiv.find("." + typ + " input").val();
        if(val.length >= 3){
            //this.loadSection(val);
            var hash = window.location.hash;
            if (hash.length > 1) {
                hash = hash.substring(1);
                var parts = hash.split(";");
                window.location.hash = parts[0] + ";{" + val.toUpperCase() + "}";
            }
        }
    },
    getTypes: function() {
        this.root_models = {};
        K5.api.askForCache("browse_types",
                _.bind(function(data) {
                    console.log("types from cache: " + data);
                    this.root_models = jQuery.parseJSON(data);
                    this.renderTypes();
                    this.processHash(false);
                }, this),
                _.bind(function(data) {
                    //File not found in cache. We should generate
                    var q = "q=browse_title:[* TO *]&rows=0&facet=true&facet.field=model_path&facet.mincount=1" + this.groupedParams;
                    if(K5.indexConfig){
                        q += "&fq=";
                        var models = K5.indexConfig.browse.models;
                        var modelField = K5.indexConfig.mappings.fedora_model;
                        for(var i=0; i<models.length; i++){
                            q += modelField + ':"' + models[i];
                            if(i<models.length-1){
                                q += " OR "
                            }
                        }
                    }
                    K5.api.askForSolr(q, _.bind(function(data) {
                        var arr = data.facet_counts.facet_fields.model_path;
                        for (var i = 0; i < arr.length; i++) {
                            var val = arr[i];
                            var count = parseInt(arr[++i]);
                            val = val.split("/")[0];
                            if (this.root_models[val]) {
                                this.root_models[val] = count + this.root_models[val];
                            } else {
                                this.root_models[val] = count;
                            }
                        }
                        this.saveTypes();
                        this.renderTypes();
                        this.processHash(false);

                    }, this), "application/json");
                }, this));

    },
    loadTitles: function() {
        this.selectTyp = "titles";
        this.selectedSection = "";
        this.selectTypCount = 0;
        this.typesDiv.find(".button").removeClass("sel");
        this.typesDiv.find(".titles").addClass("sel");
        this.sectionsDiv.html("");
        this.sectionDiv.html("");
        
        $('.loading').show();
        K5.api.askForCache('browse_sections_titles',
                _.bind(function(data) {
                    console.log("titles from cache: " + data);
                    this.sections = jQuery.parseJSON(data);
                    this.renderSections();
                    this.processHash(true);
                    $('.loading').hide();
                }, this),
                _.bind(function(data) {
                    console.log("titles not in cache. Loading...");
                    this.sectionLength = 50;
                    this.sections = [];
                    this.nextSection("");
                    $('.loading').hide();
                }, this));

    },
    loadAuthors: function() {
        this.selectTyp = "authors";
        this.selectedSection = "";
        this.selectTypCount = 0;
        this.typesDiv.find(".button").removeClass("sel");
        this.typesDiv.find(".authors").addClass("sel");
        this.sectionsDiv.html("");
        this.sectionDiv.html("");
        
        $('.loading').show();
        K5.api.askForCache('browse_sections_authors',
                _.bind(function(data) {
                    console.log("authors from cache: " + data);
                    this.sections = jQuery.parseJSON(data);
                    this.renderSections();
                    this.processHash(true);
                    $('.loading').hide();
                }, this),
                _.bind(function(data) {
                    console.log("authors not in cache. Loading...");
                    this.sectionLength = 50;
                    this.sections = [];
                    this.nextAuthor("");
                    $('.loading').hide();
                }, this));

    },
    
    nextAuthor: function(startterm) {
        var q = "terms.fl=browse_autor&terms.limit=200&terms.lower.incl=false&terms.sort=index&terms.lower=" + startterm;

        //sort=" + this.browseField + " asc& + this.groupedParams
        K5.api.askForTerms(q, _.bind(function(data) {
            //var arr = data.grouped.root_pid.doclist.docs;
            var arr = data.terms.browse_autor;
            //var numFound = data.grouped.root_pid.doclist.numFound;
            var numFound = arr.length;
            if (numFound === 0) {
                this.saveSections();
                $('.loading').hide();
                this.processHash(true);
                return;
            } else {
                //var value = arr[arr.length - 1][this.browseField];
                var value = arr[arr.length - 2];
                //var key = value.substring(0, 3);
                var i = 0;
                var j = 0;
                var c;
                var key = "";
                while (i < 3 && j < value.length) {
                    c = value.substring(j, j + 1);
                    key = key + c;
                    j++;
                    if (c !== "|") {
                        i++;
                    }
                }
                if (i === 3) {
                    c = value.substring(j, j + 1);
                    while ((c === " " || c === "|") && j < value.length) {
                        key = key + c;
                        j++;
                        c = value.substring(j, j + 1);
                    }
                }
                if (key === startterm) {
                    key = key + "|||";
                } else {
                    //var show = arr[arr.length - 1][this.showField].substring(0, 3);
                    var text = value.split("##")[1].substring(0, 3);
                    
                    var sec = {};
                    sec[key] = text;
                    this.sections.push(sec);
                    var div = $('<li/>', {class: 'button', "data-key": key});
                    div.data("key", key);
                    //div.append(text);
                    div.click(_.bind(function() {
                        this.loadSection(key);
                    }, this));
                    var spine = $('<div/>', {class: 'spine'});
                    spine.append(text);
                    div.append(spine);
                    $(this.sectionsDiv).append(div);
                }
                if (numFound > 0) {
                    //if (arr.length === this.sectionLength) {
                    this.nextAuthor(key);
                } else {
                    $('.loading').hide();
                    this.processHash(true);
                    this.saveSections();
                }
            }
        }, this), "application/json");
    },
    loadTyp: function(typ, count) {
        this.selectTyp = typ;
        this.selectedSection = "";
        this.selectTypCount = count;
        this.typesDiv.find(".button").removeClass("sel");
        this.typesDiv.find("." + typ).addClass("sel");
        this.sectionsDiv.html("");
        this.sectionDiv.html("");
        this.getSections();

    },
    saveTypes: function() {
        K5.api.saveToCache("browse_types", JSON.stringify(this.root_models), function(data) {
            console.log("types to cache: " + data);
        });
    },
    saveSections: function() {
        K5.api.saveToCache('browse_sections_' + this.selectTyp, JSON.stringify(this.sections), function(data) {
            console.log("sections to cache: " + data);
        });
    },
    sectionClick: function(key){
        var hash = window.location.hash;
        if (hash.length > 1) {
            hash = hash.substring(1);
            var parts = hash.split(";");
            window.location.hash = parts[0] + ";{" + key + "}";
        }
    },
    putFirstSections: function() {
        var div = $('<li/>', {class: 'button', "data-key": "*"});
        //div.append("!");
        div.click(_.bind(function() {
            this.sectionClick("*");
        }, this));
                
        var spine = $('<div/>', {class: 'spine'});
        spine.append("!");
        div.append(spine);
        $(this.sectionsDiv).append(div);

        div = $('<li/>', {class: 'button', "data-key": "A"});
        //div.append("A");
        div.click(_.bind(function() {
            this.sectionClick("A");
        }, this));
                
        spine = $('<div/>', {class: 'spine'});
        spine.append("A");
        div.append(spine);
        $(this.sectionsDiv).append(div);
    },
    getSections: function() {
        $('.loading').show();
        K5.api.askForCache('browse_sections_' + this.selectTyp,
                _.bind(function(data) {
                    console.log("section from cache: " + data);
                    this.sections = jQuery.parseJSON(data);
                    this.renderSections();
                    this.processHash(true);
                }, this),
                _.bind(function(data) {
                    console.log("section " + this.selectTyp + " not in cache. Loading...");

                    this.sectionLength = 20;
                    this.numSections = this.selectTypCount / this.sectionLength;
                    if (this.numSections > this.maxNumSections) {
                        this.numSections = this.maxNumSections;
                        this.sectionLength = Math.min(this.maxSectionLength, Math.floor(this.selectTypCount / this.numSections));
                    }

                    this.putFirstSections();
                    this.sections = [];
                    this.nextSection("A", 0);
                }, this));

    },
    renderSections: function() {

        if (!jQuery.isEmptyObject(this.sections)) {
            this.putFirstSections();
            $.each(this.sections, _.bind(function(idx, value) {
                var key = Object.keys(value)[0];
                var text = value[key];
                var div = $('<li/>', {class: 'button', "data-key": key});
                //div.append(text);
                div.click(_.bind(function() {
                    this.sectionClick(key);
                }, this));
                
                var spine = $('<div/>', {class: 'spine'});
                spine.append(text);
                div.append(spine);
                $(this.sectionsDiv).append(div);
            }, this));
            this.checkArrows();
        }
        
        $('.loading').hide();
    },
    nextSection: function(startterm, start) {
        
//        var q = "fq=model_path:" + this.selectTyp + "&rows=0&facet.limit=" + this.sectionLength +
//                "&q=" + this.browseField + ":[\"" + startterm + " *\" TO *]" +
//                "&fl=" + this.browseField + "," + this.showField + this.facetParams;
        
        var q = "rows=0&facet.limit=" + this.sectionLength +
                "&q=" + this.browseField + ":[\"" + encodeURIComponent(startterm) + " *\" TO *]" +
                "&fl=" + this.browseField + "," + this.showField + this.facetParams;
        if(K5.indexConfig){
            q += "&fq=";
            var models = K5.indexConfig.browse.models;
            var modelField = K5.indexConfig.mappings.fedora_model;
            for(var i=0; i<models.length; i++){
                q += modelField + ':"' + models[i] + '"';
                if(i<models.length-1){
                    q += " OR "
                }
            }
        }

        K5.api.askForSolr(q, _.bind(function(data) {
            //var arr = data.grouped.root_pid.doclist.docs;
            var arr = data.facet_counts.facet_fields[this.browseField];
            //var numFound = data.grouped.root_pid.doclist.numFound;
            var numFound = data.response.numFound;
            if (arr.length === 0) {
                this.saveSections();
                $('.loading').hide();
                this.processHash(true);
                return;
            } else {
                //var value = arr[arr.length - 1][this.browseField];
                var parts = arr[arr.length - 2].split("##");
                var value = parts[0];
                var text = parts[1].substring(0, 3);
                //var key = value.substring(0, 3);
                var i = 0;
                var j = 0;
                var c;
                var key = "";
                while (i < 3 && j < value.length) {
                    c = value.substring(j, j + 1);
                    key = key + c;
                    j++;
                    if (c !== "|") {
                        i++;
                    }
                }
                if (i === 3) {
                    c = value.substring(j, j + 1);
                    while ((c === " " || c === "|") && j < value.length) {
                        key = key + c;
                        j++;
                        c = value.substring(j, j + 1);
                    }
                }
                if (key.trim() === startterm.trim()) {
                    key = key.trim() + "|||";
                } else {
                    //var show = arr[arr.length - 1][this.showField].substring(0, 3);
                    var sec = {};
                    sec[key] = text;
                    this.sections.push(sec);
                    var div = $('<li/>', {class: 'button', "data-key": key});
                    div.data("key", key);
                    //div.append(text);
                    div.click(_.bind(function() {
                        this.loadSection(key);
                    }, this));
                    var spine = $('<div/>', {class: 'spine'});
                    spine.append(text);
                    div.append(spine);
                    $(this.sectionsDiv).append(div);
                }
                if (numFound > 0) {
                    //if (arr.length === this.sectionLength) {
                    this.nextSection(key);
                } else {
                    $('.loading').hide();
                    this.processHash(true);
                    this.saveSections();
                }
            }
        }, this), "application/json");
    },
    getSectionDocs: function(start, from, to) {
        var q = "sort=" + this.browseField + " asc&rows=" + this.rowsPerRequest + "&start=" + start +
                "&q=" + this.browseField + ":[\"" + encodeURIComponent(escapeSolrChars(from)) + " *\" TO " + to.trim() + "]" +
                "&fl=PID,dc.title,dc.creator,datum_str";
        if(K5.indexConfig){
            q += "&fq=";
            var models = K5.indexConfig.browse.models;
            var modelField = K5.indexConfig.mappings.fedora_model;
            for(var i=0; i<models.length; i++){
                q += modelField + ':"' + models[i] + '"';
                if(i<models.length-1){
                    q += " OR "
                }
            }
        }
        $('.loading').show();
        K5.api.askForSolr(q, _.bind(function(data) {
            this.sectionDiv.find('.more_docs').remove();
            var arr = data.response.docs;
            for (var i = 0; i < arr.length; i++) {
                var doc = arr[i];
                var div = $('<div/>', {class: 'res'});
                div.data("pid", doc.PID);
                var title = $('<span class="title">' + doc['dc.title'] + '</span>');
                title.click(function() {
                    K5.api.gotoDisplayingItemPage($(this).parent().data("pid"));
                });
                div.append(title);
                if (doc['dc.creator']) {
                    div.append('<div>' + doc['dc.creator'] + '</div>');
                }
                if (doc['datum_str']) {
                    div.append('<div>' + doc['datum_str'] + '</div>');
                }
                this.sectionDiv.append(div);
            }
            this.resizeResults();
            $('.loading').hide();
            var nextStart = start + this.rowsPerRequest;
            if (data.response.numFound > nextStart) {
                var more = $('<div class="more_docs" data-start="' + nextStart + '">more...</div>');
                more.data("from", from);
                more.data("to", to);
                this.sectionDiv.append(more);
                //this.getSectionDocs(start + this.rowsPerRequest, from, to);
            } else {
            }

        }, this), "application/json");
    },
    loadAuthor:function(){
        
        var q = "terms.fl=browse_autor&terms.limit=200&terms.lower.incl=true&terms.sort=index&terms.lower=" + this.selectedSection;
        K5.api.askForTerms(q, _.bind(function(data) {
            var arr = data.terms.browse_autor;
            for (var i = 0; i < arr.length; i++) {
                var br = arr[i++];
                var text = br.split("##")[1];
                var div = $('<div/>', {class: 'res'});
                div.data("author", text);
                var title = $('<span class="title">' + text + '</span>');
                title.click(function() {
                    var q = "&author=\"" + $(this).parent().data("author") + "\""; 
                    K5.api.gotoResultsPage(q);
                });
                div.append(title);
                this.sectionDiv.append(div);
            }
            this.resizeResults();
            $('.loading').hide();
        }, this), "application/json");
    },
    loadSection: function(section) {
        if(this.sectionsDiv.children().length===0) return;
        this.selectedSection = section;
        this.sectionsDiv.find(".button").removeClass("sel sel2");
        
        var nearest_section = null;
        $.each(this.sections, function(idx, value) {
            var key = Object.keys(value)[0];
            var text = value[key];
            if(key > section){
                return;
            }
            nearest_section = key;
        });
            
        
        var el = this.sectionsDiv.find('.button[data-key="' + nearest_section + '"]');
        var nextkey = el.next().data("key");
        if (nextkey === null) {
            nextkey = "*";
        } else {
            nextkey = "\"" + nextkey + "\"";
        }
        el.addClass("sel");
        el.next().addClass("sel2");
        
        var finalPos = this.sectionsScroll.scrollLeft() + el.offset().left - this.sectionsScroll.width() * 0.5 ;
        this.doAbsScroll(finalPos);
        
        this.sectionDiv.html("");
        if(this.selectTyp==="authors"){
            this.loadAuthor();
        }else{
            this.getSectionDocs(0, section, nextkey);
        }
        
    },
    addContextButtons: function() {
        var text = $("#item_menu").html();
        $("#contextbuttons").html(text);
    }
};
