require("../../css/test/test-bar-iscroll5");
require("zepto");
var iScroll = require("bb-iscroll");
var myScroll;

$(function(){
    myScroll = new iScroll('#wrapper',{
        eventPassthrough: true,
        scrollX: true,
        scrollY: false,
        preventDefault: false,
        isSliderBlock : true,   //此配置项是否支持滑动回弹效果
        sliderBlock : 80        //滑动块宽度
    });
    myScroll.on("scrollEnd",function(){
        var _index = filterDis(this.x);
        $("li").removeClass();
        $($("li")[_index+3]).addClass("selected");
    })
})

function filterDis(x){
    var _ak,
        _index;
    if(Math.abs(x)<80){
        _index = 0;
    }else{
        _ak = Math.abs(x % 80);
        _index = Math.abs(Math.floor(x/80))-1;
        _index = _ak > 80/2 ? _index+1 : _index;
    }
    return _index;
}