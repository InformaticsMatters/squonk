function init () {

    jsPlumb.setContainer($('#plumbContainer'));

    $('.palette-item').draggable({
        cursor: 'move',
        helper: 'clone',
        scroll: false,
        appendTo: '#plumbContainer',
        start: function () {},
        stop: function (event, ui) {}
    });

    $('#plumbContainer').droppable({
        accept: '.palette-item',
        drop: function(event, ui) {
            var clone = $(ui.draggable).clone();
            clone.css({position: 'absolute', top: ui.position.top, left: ui.position.left});
            $(this).append(clone);
            jsPlumb.draggable($('#plumbContainer .palette-item'), {containment:'parent'});
        }
    });

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

    $('.properties-button').click(function () {
        var effect = 'slide';
        var options = { direction: 'right' };
        var duration = 300;

        $(this).toggleClass('pressed');
        $('.properties-panel').toggle(effect, options, duration);
   });
}

