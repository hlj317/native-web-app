require("../../css/test/test-bar-iscroll5");
require("zepto");
var iScroll = require("iscroll5");
var myScroll;

$(function(){
    myScroll = new iScroll('#wrapper',{
        eventPassthrough: true,
        scrollX: true,
        scrollY: false,
        preventDefault: false
    });
    myScroll.on("scrollEnd",function(){
        var _x = this.x;
        this._translate(filterDis(_x),0);
    })
})

function filterDis(x){
    var _ak = Math.abs(x % 80);
    var _index = Math.abs(Math.floor(x/80));
    _index = _ak > 40 ? _index+1 : _index;
    $("li").removeClass();
    $($("li")[_index+2]).addClass("selected");
    return -80 * _index;
}
