/**
 * utility.js tests
 */

test( "Assert lookup keys", function() {
	var toplevel = {};
	toplevel["a"]={};
	toplevel.a["b"]={};
	toplevel.a.b["c"]={};
	
	var exists = lookUpKey("a/b/c",toplevel);
	ok(exists, "Key exists" );

	exists = lookUpKey("a/b/c",toplevel);
	ok(exists, "Key exists" );
	exists = lookUpKey("a/b",toplevel);
	ok(exists, "Key exists" );
	exists = lookUpKey("a",toplevel);
	ok(exists, "Key exists" );
	
	var notexists = lookUpKey("a/b/c/d",toplevel);
	ok(!notexists, "Key not exists" );

	notexists = lookUpKey("ba/b/c/d",toplevel);
	ok(!notexists, "Key not exists" );

});



