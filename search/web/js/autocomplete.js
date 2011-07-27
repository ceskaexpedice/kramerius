var completeUrl = "terms.jsp?";
var autoCompleteDiv = '#autocomplete';
var cur;

var resultClickFunctionName;

function doAutocomplete(text, lookupField, key, queryField){
    autoCompleteDiv="#autocomplete";
    completeUrl = "terms.jsp?";
    resultClickFunctionName = "resultClick";
    
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
            window.location = searchPage + "?autocomplete=true&autocomplete_q=" + lookupField + ":" + value;
        }else{
            
            resultClick(value, lookupField);
        }
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
            "\" class=\"autocomplete\" onclick=\""+resultClickFunctionName+"('"+data.terms[j][i]+"','"+lookupField+"')\">" + data.terms[j][i] + " (" + data.terms[j][i+1] + ")</div>";
        }
        
    }
    if(outText.length>0){

        $(autoCompleteDiv).html("<div><img src=\"img/x.png\" align=\"right\"/><br/>" + outText + "</div>");
        var y = $(text).offset().top + $(text).height();
        var x = $(text).offset().left;
        $(autoCompleteDiv).css("left", x);
        $(autoCompleteDiv).css("top", y);
        
        //$(autocompleteDiv).css("position","absolute");
        $(autoCompleteDiv).show();
        
    }else{
        $(autoCompleteDiv).hide();
    }
}

function resultClick(value, lookupField){
    if(lookupField=='root_title'){
        window.location = searchPage + "?title=\"" + value + "\"";
    } else{
                
        window.location = searchPage + "?fq=" + lookupField + ":\"" + value + "\"";
    }
}