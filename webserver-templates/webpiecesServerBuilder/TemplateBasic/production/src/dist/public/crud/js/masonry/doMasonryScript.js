$(document).ready(function(){
	
	// load images
	setTimeout(function () {
	      $('.load-delay').each(function () {
	          var imagex = $(this);
	          var imgOriginal = imagex.data('original');
	          $(imagex).attr('src', imgOriginal);
	      });
	  }, 0);
	
	
	// initially hide buttons
	$('button').hide();
	
	// layout masonry
	var $container = $('#page-wrap').masonry();
	
	// layout again after images loaded
	$container.imagesLoaded( function () {
		$container.masonry({
		columnWidth: 230,
		itemSelector: '.item',
		isFitWidth: true,
		isAnimated: true //!Modernizr.csstransitions 
		});
		// positionFooter();
	});
	
	
	// Display buttons on hover and fade the image
	$('.item').hover(
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
});

/*
//Window load event used just in case window height is dependent upon images
$(window).resize(function(){
	$container=$('#page-wrap').masonry();
	$container.imagesLoaded( function () {
		positionFooter();
	});
});


function positionFooter(){
    $footer = $("#footer");
    var footerHeight = $footer.outerHeight();
	var windowHeight = $(window).height() 
	|| document.documentElement.clientHeight
	|| document.body.clientHeight;
	var wrapHeight = $('#page-wrap').outerHeight();
	var outerHeight = $('#outer').outerHeight();
       
    if ( (wrapHeight + outerHeight+footerHeight) < windowHeight) {
		var newHeight = (windowHeight - (wrapHeight + outerHeight))-20+"px";
		$footer.css("height",newHeight);			
    } else {
		$footer.css("height","200px");
    }
}

*/