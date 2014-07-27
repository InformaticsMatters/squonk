var onCanvasDrop;
var onCanvasItemDragStop;

function setupLayout() {
    var mainLayoutSettings = {
        west: {
            size: 305,
            slidable: false,
            spacing_closed: 0,
            spacing_open: 0,
            togglerLength_closed: 00,
            togglerLength_open: 00,
            onresize:  $.layout.callbacks.resizePaneAccordions
        }
    };
    mainLayout = $('#layoutContainer').layout(mainLayoutSettings);

    $("#accordion-west").accordion({heightStyle: 'fill'});

    var internalLayoutSettings = {
        east: {
            size: 20,
            resizable: false,
            spacing_open: 0,
            togglerLength_closed: 00,
            togglerLength_open: 00
        }
    };
    internalLayout = $('#internalLayoutContainer').layout(internalLayoutSettings);

    $('.properties-panel').hide();
    $('#prop-button').click(function() {
        var eastCurrentSize  = internalLayout.state.east.size;
        if (eastCurrentSize == 20) {
            internalLayout.sizePane('east', 300);
            $('.properties-panel').show();
            $(this).addClass('pressed');
        } else {
            internalLayout.sizePane('east', 20);
            $('.properties-panel').hide();
            $(this).removeClass('pressed');
        }
    });
}

function setupPalette() {
    $('.palette-item').draggable({
        cursor: 'move',
        helper: 'clone',
        scroll: false,
        appendTo: '#plumbContainer',
        start: function () {},
        stop: function (event, ui) {}
    });
}

function setupCanvas() {
    jsPlumb.setContainer($('#plumbContainer'));

    $('#plumbContainer').droppable({
        accept: '.palette-item',
        drop: function(event, ui) {
            onCanvasDrop(ui.position.left, ui.position.top);
        }
    });
}

function makeCanvasItemsDraggable(selector) {
    jsPlumb.draggable($(selector), {
        containment: 'parent',
        stop: function(params) {
            var index = $('#' + params.el.id).index('.canvas-item');
            onCanvasItemDragStop(index, params.pos[0], params.pos[1]);
        }
    });
}

function setupAccordions() {
	// add inactive class to all accordion headers
	$('.accordion-header').toggleClass('inactive-header');
	// open the first accordion section when page loads
	$('.accordion-header').first().toggleClass('active-header').toggleClass('inactive-header');
    //	$('.accordion-content').first().slideDown().toggleClass('opened-content');
	// the accordion effect
	$('.accordion-header').click(function () {
		if($(this).is('.inactive-header')) {
			$('.active-header').toggleClass('active-header').toggleClass('inactive-header');//.next().slideToggle().toggleClass('opened-content');
			$(this).toggleClass('active-header').toggleClass('inactive-header');
            // $(this).next().slideToggle().toggleClass('opened-content');
		} else {
			$(this).toggleClass('active-header').toggleClass('inactive-header');
            // $(this).next().slideToggle().toggleClass('opened-content');
		}
	});
}

function addSourceEndpoint(itemId) {
    var sourceEndpointOptions = {
        isSource:true,
        paintStyle : {
            fillStyle:'green'
        },
        connectorStyle : {
            strokeStyle:'green',
            lineWidth:8
        }
    };
    var sourceEndpoint = jsPlumb.addEndpoint(itemId, sourceEndpointOptions);
}

function addTargetEndpoint(itemId) {
    var targetEndpointOptions = {
        endpoint: 'Rectangle',
        anchor: 'TopCenter',
        isTarget:true,
        paintStyle : {
            fillStyle:'red'
        },
    };
    var targetEndpoint = jsPlumb.addEndpoint(itemId, targetEndpointOptions);
}

function init () {
    setupLayout();
    setupPalette();
    setupCanvas();
    setupAccordions();
}


