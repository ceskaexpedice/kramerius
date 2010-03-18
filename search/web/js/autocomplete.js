/**
 * tomas.dockal@incad.cz
 * slouzi pro autocomplete pri vyhledavani
 * postaveno pro ESP - Query Completion Server completionserver
 *  ->server:15200
 *
 * na zacatku kodu je definice elementu a url pro ziskani autocomplete
 *
 * <form action="./" autocomplete="Off">
 *      <input name="query" id="query" type="text" onkeyup="doAutocomplete(this.value, event);" >
 * </form>
 *
 * <div id="autocomplete" ></div>
 *
 * styl pouzity pro vybranou polozku je
 * #autocomplete .selected {
 * }
 */

//var queryField="#query";
//var completeUrl="http://tomas02.incadsbs.local:8084/cpojHelpKC/proxy.jsp?http://tomasesp.incad.cz:15200/search?q=";

// id kam se vlozi autocomplete
//var autoCompleteDiv="#autocomplete";

//var lookupField = "facet_autor";
//lookupField = "title";

//adresa kde se hledaji autocomplete
//obcas nutne pouzit proxy pro povoleni ajaxu
//var completeUrl="http://tomas02.incadsbs.local:8084/cpojHelpKC/proxy.jsp?http://tomasesp.incad.cz:15200/search?q=";
var completeUrl = "/terms.jsp?";

function doAutocomplete(text, lookupField, key, div, queryField){
    //autoCompleteDiv="#autocomplete";
    if( key.keyCode >=16 && key.keyCode <= 19 ){
        return;
    }
    if( key.keyCode >=37 && key.keyCode <= 40){
        moveSelected(key.keyCode, div, queryField);
        return;
    }
    if( key.keyCode == 13){
        //$("form").submit();
        var value = $(queryField).val().toLowerCase() + "*";
        if(lookupField=='facet_autor') lookupField = 'search_autor';
        if(lookupField=='root_title') lookupField = 'search_title';
        window.location = searchPage + "?q=" + lookupField + ":" + value;
        return;
    }
    json(text, lookupField, div, queryField);
//ajax(text);
}
/** pohybuje v seznamu slov
 * 
 */
function moveSelected(key, autoCompleteDiv, queryField){
    var cur = $(autoCompleteDiv +" .selected:first");
    if(cur.length >0 ){
        cur.toggleClass("selected");
        if(key>38){//dolu a vpravo
            next=cur.next("div");
        }else {
            next=cur.prev("div")
        }
        next.toggleClass("selected");
        //$(queryField).val(next.text());
        $(queryField).val(next.attr("title"));
    }else{
        //$(queryField).val($(autoCompleteDiv +" div:first").text());
        $(queryField).val($(autoCompleteDiv +" div:first").attr("title"));
        $(autoCompleteDiv +" div:first").toggleClass("selected");
    }
}
// pouze pro testovani - pouzivat json(lepsi ale neudela error)

function ajax(text, lookupField, autoCompleteDiv){
    $.ajax({
        type: "POST",
        url: url + "field="+lookupField+"&t=" + text,
        cache: false,
        success: function(data){
            parseData(data, lookupField, autoCompleteDiv);
        }
    });

}
function json(text, lookupField, autoCompleteDiv){
    $.ajaxSetup({
        cache: false,
        type: "POST"
    });
    var url = searchPage + completeUrl + "field="+lookupField+"&t=" + text;
    $.getJSON(url, function(data) {
        parseData(data, lookupField, autoCompleteDiv);
    });
}

function parseData(data, lookupField, autoCompleteDiv){
    if(data == "" ){
        $(autoCompleteDiv).hide();
        return;
    }
    var outText="";
    for(var j=1; j<data.terms.length ; j++){
        
        for(var i=0 ; i<data.terms[j].length ; i=i+2){
            outText+="<div title=\""+data.terms[j][i]+
                "\" class=\"suggest\" onclick=\"resultClick('"+data.terms[j][i]+"','"+lookupField+"')\">" + data.terms[j][i] + " (" + data.terms[j][i+1] + ")</div>";
        }
        
    }
    if(outText.length>0){
        $(autoCompleteDiv).html( outText );
        $(autoCompleteDiv).show();
    }else{
        $(autoCompleteDiv).hide();
    }
}

function resultClick(value, lookupField){
    window.location = searchPage + "?fq=" + lookupField + ":\"" + value + "\"";
}