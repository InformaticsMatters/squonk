function makeMenuItemActive(itemId) {

    $('.item').removeClass("active");
    $('#' + itemId).addClass("active");
}