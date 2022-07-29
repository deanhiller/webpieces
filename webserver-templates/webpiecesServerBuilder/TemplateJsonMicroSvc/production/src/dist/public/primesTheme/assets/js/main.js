
jQuery(document).ready(function() {

	//*****  STYLE SWITCHER  *****//

	$(".style1" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style1.css" );
		return false;
	});

	$(".style2" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style2.css" );
		return false;
	});

	$(".style3" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style3.css" );
		return false;
	});

	$(".style4" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style4.css" );
		return false;
	});

	$(".style5" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style5.css" );
		return false;
	});

	$(".style6" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style6.css" );
		return false;
	});

	$(".style7" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style7.css" );
		return false;
	});

	$(".style8" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style8.css" );
		return false;
	});

	$(".style9" ).click(function(){
		$("#colors" ).attr("href", "assets/css/style9.css" );
		return false;
	});


	// Style Switcher	
	$('#style-switcher').animate({
		left: '-150px'
	});

	$('#style-switcher h2 a').click(function(e){
		e.preventDefault();
		var div = $('#style-switcher');
		console.log(div.css('left'));
		if (div.css('left') === '-150px') {
			$('#style-switcher').animate({
				left: '0px'
			}); 
		} else {
			$('#style-switcher').animate({
				left: '-150px'
			});
		}
	})

	$('.colors li a').click(function(e){
		e.preventDefault();
		$(this).parent().parent().find('a').removeClass('active');
		$(this).addClass('active');
	})




/***** Scroll Up *****/

		$('a[href*=#]').bind("click", function(e){
			var anchor = $(this);
			$('html, body').stop().animate({
				scrollTop: $(anchor.attr('href')).offset().top - 50
			}, 1500);
			e.preventDefault();
		});

    $(window).scroll(function() {
      if ($(this).scrollTop() > 100) {
        $('.menu-top').addClass('menu-shrink');
      } else {
        $('.menu-top').removeClass('menu-shrink');
      }
    });

		$(document).on('click','.navbar-collapse.in',function(e) {
			if( $(e.target).is('a') && $(e.target).attr('class') != 'dropdown-toggle' ) {
				$(this).collapse('hide');
			}
		});


	
   

    
/***** Contact form *****/
	    
    $('.contact-form form input[type="text"], .contact-form form textarea').on('focus', function() {
    	$('.contact-form form input[type="text"], .contact-form form textarea').removeClass('contact-error');
    });
	$('.contact-form form').submit(function(e) {
		e.preventDefault();
	    $('.contact-form form input[type="text"], .contact-form form textarea').removeClass('contact-error');
	    var postdata = $('.contact-form form').serialize();
	    $.ajax({
	        type: 'POST',
	        url: 'assets/contact.php',
	        data: postdata,
	        dataType: 'json',
	        success: function(json) {
	            if(json.emailMessage != '') {
	                $('.contact-form form .contact-email').addClass('contact-error');
	            }
	            if(json.subjectMessage != '') {
	                $('.contact-form form .contact-subject').addClass('contact-error');
	            }
	            if(json.messageMessage != '') {
	                $('.contact-form form textarea').addClass('contact-error');
	            }
	            if(json.emailMessage == '' && json.subjectMessage == '' && json.messageMessage == '') {
	                $('.contact-form form').fadeOut('fast', function() {
	                    $('.contact-form').append('<p>Thanks for contacting us!</p>');
	                });
	            }
	        }
	    });
	});

    
});

