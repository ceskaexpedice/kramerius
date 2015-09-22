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
    letters: ["0","A","B","C","Č","D","E","F","G","H","CH","I","J","K","L","M","N","O","P","Q","R","Ř","S","Š","T","U","V","W","X","Y","Z","Ž"],
    
    ctx: {},
    _init: function() {
        this.rowsPerRequest = 200;
        this.browseField = "browse_title";
        this.showField = "dc.title";
        this.input = this.elem.find(".input input");
        this.lettersDiv = this.elem.find(".letters>.scroll>ul");
        this.sectionsScroll = this.elem.find(".letters>.scroll");
        this.resultsDiv = this.elem.find(".results");
        this.titlesList = this.resultsDiv.find(".titles ul");
        this.authorsList = this.resultsDiv.find(".authors ul");
        this.renderLetters();
        this.processHash();
        this.input.keyup(_.bind(function(ev){
            this.typing =  true;
            //this.doSuggest();
        }, this));
        
        setTimeout(function() {
            this.checkSuggest()
        }.bind(this), 1000);
        
    },
    checkSuggest: function(){
        if(!this.typing){
            this.doSuggest();
        }
        this.typing = false;
        setTimeout(function() {
            this.checkSuggest()
        }.bind(this), 500);
    },
    doSuggest: function(){
        var val = this.input.val();
        var hash = window.location.hash;
        if(val.length >= 1){
            //if(hash.length > 1 && val !== hash.substring(1)){
                window.location.hash = val.toUpperCase();
            //}
        }
    },
    processHash: function() {
        var hash = window.location.hash;
        var letter = "A";
        this.hash = "A";
        if (hash.length > 1) {
            hash = hash.substring(1);
            this.hash = hash;
            letter = hash.substring(0,1);
            if(hash.startsWith('CH')){
                letter = hash.substring(0,2);
            }
            
        }
        this.hash = this.replaceChars(this.hash);
        
        this.selectedLetter = letter;
        this.sectionsScroll.parent().show();

        this.loadLetter();
        this.lettersDiv.find(".button").removeClass("sel");
        var el = this.lettersDiv.find('.button[data-key="' + letter + '"]');
        el.addClass("sel");
    },
    loadLetter: function(){
        this.titlesList.empty();
        this.getTitles(0);
        this.authorsList.empty();
        this.getAuthors(this.hash, true);
    },
    getAuthors: function(start, include){
        var q = "terms.fl=browse_autor&terms.limit="+this.rowsPerRequest+"&terms.lower.incl="+include+"&terms.sort=index&terms.lower=" + start;
        $('.authors .loading').show();
        K5.api.askForTerms(q, _.bind(function(data) {
            this.authorsList.find('.more_docs').remove();
            var arr = data.terms.browse_autor;
            for (var i = 0; i < arr.length; i++) {
                var br = arr[i++];
                var text = br.split("##")[1];
                var div = $('<li/>', {class: 'res'});
                div.data("author", text);
                var title = $('<span class="title">' + text + '</span>');
                title.click(function() {
                    var q = "&author=\"" + $(this).parent().data("author") + "\""; 
                    K5.api.gotoResultsPage(q);
                });
                
        
                div.append(title);
                this.authorsList.append(div);
            }
            $('.authors .loading').hide();
            if (arr.length === this.rowsPerRequest * 2) {
                var nextStart = arr[arr.length-2];
                var more = $('<div class="more_docs" data-start="' + nextStart + '">more...</div>');
                this.authorsList.append(more);
                more.click(function() {
                    var q = "&author=\"" + $(this).parent().data("author") + "\""; 
                    K5.gui.browse.getAuthors($(this).data("start"), false);
                });
            }
        }, this), "application/json");
    },
    replaceChars: function(s){
        var ret = s;
        $.ajax({
            type: 'GET',
            url: "utfsort.vm?term=" + s,
            async:false
          }).success(function(data){
              ret = data;
          });
        return ret;
    },
    getTitles: function(start){
        
        var q = "sort=" + this.browseField + " asc&rows=" + this.rowsPerRequest + "&start=" + start +
                "&q=" + this.browseField + ":[\"" + this.hash + " *\" TO *]" +
                "&fl=PID,dc.title,dc.creator,datum_str,dostupnost";
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
        $('.titles .loading').show();
        K5.api.askForSolr(q, _.bind(function(data) {
            this.titlesList.find('.more_docs').remove();
            var arr = data.response.docs;
            for (var i = 0; i < arr.length; i++) {
                var doc = arr[i];
                var div = $('<li/>', {class: 'res policy'});
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
                
                if (doc['dostupnost']) {
                    div.addClass(doc['dostupnost']);
                    div.attr("title", doc['dostupnost']);
                }
                
                this.titlesList.append(div);
            }
            //this.resizeResults();
            $('.titles .loading').hide();
            var nextStart = start + this.rowsPerRequest;
            if (data.response.numFound > nextStart) {
                var more = $('<div class="more_docs" data-start="' + nextStart + '">more...</div>');
                this.titlesList.append(more);
                more.click(function() {
                    var q = "&author=\"" + $(this).parent().data("author") + "\""; 
                    K5.gui.browse.getTitles($(this).data("start"));
                });
            }

        }, this), "application/json");
    },
    letterClick: function(letter){
        this.input.val(letter);
        window.location.hash = letter;
    },
    renderLetters: function() {
            $.each(this.letters, _.bind(function(idx, letterDisp) {
                console.log(letterDisp);
                var letter = letterDisp;
                if(letter === "0"){
                    letter = "!";
                }
                var div = $('<li/>', {class: 'button', "data-key": letter});
                div.click(_.bind(function() {
                    this.letterClick(letter);
                }, this));
                
                var spine = $('<div/>', {class: 'spine'});
                spine.append(letterDisp);
                div.append(spine);
                $(this.lettersDiv).append(div);
            }, this));
        
    }
};
