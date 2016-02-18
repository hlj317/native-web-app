require("../../css/vod/iscroll5");
var iScroll = require("iscroll5");
var myScroll;

window.loaded = function() {
	myScroll = new iScroll('#wrapper', {
		scrollbars: true,
		mouseWheel: true,
		interactiveScrollbars: true,
		shrinkScrollbars: 'scale',
		fadeScrollbars: true
	});
}

document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);