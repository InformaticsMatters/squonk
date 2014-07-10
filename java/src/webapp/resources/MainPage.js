function init () {
	//Add Inactive Class To All Accordion Headers
	$('.accordion-header').toggleClass('inactive-header');

	//Open The First Accordion Section When Page Loads
	$('.accordion-header').first().toggleClass('active-header').toggleClass('inactive-header');
	$('.accordion-content').first().slideDown().toggleClass('opened-content');

	// The Accordion Effect
	$('.accordion-header').click(function () {
		if($(this).is('.inactive-header')) {
			$('.active-header').toggleClass('active-header').toggleClass('inactive-header').next().slideToggle().toggleClass('opened-content');
			$(this).toggleClass('active-header').toggleClass('inactive-header');
			$(this).next().slideToggle().toggleClass('opened-content');
		}

		else {
			$(this).toggleClass('active-header').toggleClass('inactive-header');
			$(this).next().slideToggle().toggleClass('opened-content');
		}
	});

    $(".properties-button").click(function () {
        var effect = 'slide';
        var options = { direction: 'right' };
        var duration = 300;

        $(this).toggleClass('pressed');
        $('.properties-panel').toggle(effect, options, duration);
   });
}



