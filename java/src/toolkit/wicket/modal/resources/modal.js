function modalPosition(id) {
    var windowWidth = $(window).width();
    var windowHeight = $(window).height();
    var block = $('#' + id);
    var left = (windowWidth - block.width()) * 0.5;
    if(left < 0) left = 0;
    var top = (windowHeight - block.height()) * 0.5;
    if(top < 0) top = 0;
    block.css('left', left + 'px');
    block.css('top', top + 'px');
    block.fadeIn('slow');
};