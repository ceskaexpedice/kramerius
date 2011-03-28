/*
 * rendering html -> for dialog contents
 */

/** creates link */
function link(target, text) {
	  return tag("a", [text], {href: target});
}

/** div elm */
function div(text, ident) {
	return tag("div",[text], {id:ident});
}

/** span elm */
function span(text, ident) {
	return tag("span",[text], {id:ident});
}
/** span with style */
function span(text, ident,style) {
	return tag("span",[text], {id:ident, style:style});
}

/** input element */
function input(type, inpsize, inpname, inpvalue, ident) {
	return tag("input",[""],{id:ident,type:type,size:inpsize,name:inpname,value:inpvalue});
}

/** text input elm */
var text = partial(input, "text");

/** hr elm */
function hr() {
	return tag("hr",[""], {});
}
/** hr elm */
function br() {
	return tag("br",[""], {});
}
/** strong elm */
function strong(text) {
	return tag("strong",[text], {});
}

/** headers */
function h(number, text) {
	return tag("h"+number,[text], {});
}
/** h1 */
var h1=partial(h,1);
/** h2 */
var h2=partial(h,2);
/** h3 */
var h3=partial(h,3);
/** h4 */
var h4=partial(h,4);
/** h5 */
var h5=partial(h,5);

/**
 * creates common tag
 */
function tag(name, content, attributes) {
	if (!name) alert("name is undefined ");
	return {name: name, attributes: attributes, content: content};
}


/**
 * escaping
 */
function escapeHTML(text) {
	var replacements = [[/&/g, "&amp;"], [/"/g, "&quot;"],
	                    [/</g, "&lt;"], [/>/g, "&gt;"]];
	$.each(replacements,function(index, replace) {
		text = text.replace(replace[0], replace[1]);
	});
	return text;
}

/**
 * produce html output
 */
function renderHTML(element) {
	  var pieces = [];

	  function renderAttributes(attributes) {
	    var result = [];
	    if (attributes) {
	      for (var name in attributes) 
	        result.push(" " + name + "=\"" +
	                    escapeHTML(attributes[name]) + "\"");
	    }
	    return result.join("");
	  }

	  function render(element) {
		  // Text node
	    if (typeof element == "string") {
	      pieces.push(escapeHTML(element));
	    }
	    // Empty tag
	    else if (!element.content || element.content.length == 0) {
	    	pieces.push("<" + element.name +
	                  renderAttributes(element.attributes) + "/>");
	    }
	    // Tag with content
	    else {
	    	pieces.push("<" + element.name +
	                  renderAttributes(element.attributes) + ">");
	      $.each(element.content,function(i,v) {render(v);});
	      pieces.push("</" + element.name + ">");
	    }
	  }

	  render(element);
	  return pieces.join("");
}