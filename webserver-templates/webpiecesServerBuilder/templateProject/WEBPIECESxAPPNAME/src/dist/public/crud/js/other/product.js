$(document).ready(function(){
	
	// initially hide buttons
	$('button').hide();
	
	// Display buttons on hover and fade the image
	$('.static-item').hover(
	function(){
		$("img",this).animate({"opacity": "0.5"}, 350);
		$(".left",this).show(350);
		$(".right",this).show(350);
		},
	function() {
		$("img",this).stop().animate({"opacity": "1"},350);
		$(".left",this).hide(350);
		$(".right",this).hide(350);
	});
	
	// When hover over buttons, give them a border
	$('button').mouseenter(function(){
		$(this).addClass('hover');
	});
	$('button').mouseleave(function(){
		$(this).removeClass('hover');
	});
	
	staticProperties();
});


function staticProperties(){

	$container = $('#static-page-wrap');

	// wait until image loaded
	$container.imagesLoaded(function(){

		// setting gray background box height
		$img = $("#prodImg");
		var $imgHeight = $img.outerHeight();
		var $staticHeight = ($imgHeight + 30) + "px";
		$(".static-item").css("height",$staticHeight);

		// set static-page-wrap height
		var $pageHeight = ($imgHeight + 50) + "px";
		$("#static-page-wrap").css("height",$pageHeight);
	});

}
