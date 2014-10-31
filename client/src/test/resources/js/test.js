/** test DA  */
test( "Assert thumbs", function() {
	var th = new Thumbs('#canvasthumbs');  
	console.log(th);
	ok( th !== null, "Thumbs created" );
	th.init();
	
	var intialized = (th.docs !== null && 	th.thumbIndex == -1);
	ok(intialized, "Thumbs initialized" );

	// test other
	console.log("panel height == "+th.panelHeight);
	console.log("panel width == "+th.panelWidth);
	th.getYear({count:100, offset:0,year:2008, accumulated:1});
});




