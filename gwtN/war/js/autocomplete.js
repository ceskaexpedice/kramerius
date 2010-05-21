/**
 * tomas.dockal@incad.cz
 * slouzi pro autocomplete pri vyhledavani
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



//adresa kde se hledaji autocomplete
//obcas nutne pouzit proxy pro povoleni ajaxu
var completeUrl = "terms.jsp?";
var autoCompleteDiv = '#autocomplete';

function doAutocomplete(text, lookupField, key, queryField){
    //autoCompleteDiv="#autocomplete";
    if( key.keyCode >=16 && key.keyCode <= 19 ){
        return;
    }
    //arrows
    if( key.keyCode >=37 && key.keyCode <= 40){
        moveSelected(key.keyCode, queryField);
        return;
    }
    if( key.keyCode == 13){
        //$("form").submit();
        var value = $(queryField).val();
        if(cur==null){
            value = value.toLowerCase() + "*";
            if(lookupField=='facet_autor') lookupField = 'search_autor';
            if(lookupField=='root_title'){
                lookupField = 'search_title';
            } 
        }else{
            value = "\"" + value + "\""
        }
        window.location = searchPage + "?q=" + lookupField + ":" + value; 
        return;
    }
    json(text, lookupField, queryField);
    //ajax(text);
}

/** pohybuje v seznamu slov
 * 
 */
function moveSelected(key, queryField){
    cur = $(autoCompleteDiv +">div>div.selected:first");
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
        $(queryField).val($(autoCompleteDiv +">div>div:first").attr("title"));
        $(autoCompleteDiv +">div>div:first").toggleClass("selected");
    }
}
// pouze pro testovani - pouzivat json(lepsi ale neudela error)

function ajax(text, lookupField){
    $.ajax({
        type: "POST",
        url: url + "field="+lookupField+"&t=" + text,
        cache: false,
        success: function(data){
            parseData(data, lookupField, autoCompleteDiv);
        }
    });

}
function json(text, lookupField){
    $.ajaxSetup({
        cache: false,
        type: "POST"
    });
    var url = completeUrl + "field="+lookupField+"&t=" + text.value;
    $.getJSON(url, function(data) {
        parseData(data, lookupField, text);
    });
}
function parseData(data, lookupField, text){
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
        $(autoCompleteDiv).html("<div><img src=\"img/x.png\" align=\"right\"/><br/>" + outText + "</div>");
        var y = $(text).offset().top + $(text).height();
        var x = $(text).offset().left;
        $(autoCompleteDiv).css("left", x);
        $(autoCompleteDiv).css("top", y);
        $(autoCompleteDiv).show();
        
    }else{
        $(autoCompleteDiv).hide();
    }
}

function resultClick(value, lookupField){
    window.location = searchPage + "?fq=" + lookupField + ":\"" + value + "\"";
}