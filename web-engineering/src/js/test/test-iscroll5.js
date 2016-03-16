require("../../css/vod/iscroll5");
var iScroll = require("iscroll5");
var myScroll;
window.loaded = function() {
    myScroll = new iScroll('#wrapper', {
        eventPassthrough: true,
        scrollX: true,
        scrollY: false,
        preventDefault: false
    });
}
