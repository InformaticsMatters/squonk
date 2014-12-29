function makeMenuItemActive(itemId) {
    $('.button').removeClass("active");
    $('#' + itemId).addClass("active");
}