require('../../css/vod/bmap');

var longitude = getQueryString('longitude');
var latitude = getQueryString('latitude');
var mapPointForS1 = require("../../imgs/square/play/mapPointForS1.png");
if( typeof longitude == "string" && typeof latitude == "string" ){
    renderOption2(longitude, latitude);
}
function renderOption2(longitude, latitude){
    var point = new BMap.Point(longitude, latitude);  // 创建点坐标
    // 百度地图
    var BMapDom = document.createElement('DIV');
    document.body.appendChild( BMapDom );
    //BMapDom.style.height = (globalData.commentFixedHeight + 25 + 35) + 'px';
    BMapDom.style.height = '100%';
    var map = new BMap.Map( BMapDom );          // 创建地图实例
    map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
    var myIcon = new BMap.Icon(mapPointForS1, new BMap.Size(32, 45), {
        //offset: new BMap.Size(0, -5),    //相当于CSS精灵
        imageOffset: new BMap.Size(7, 0)    //图片的偏移量。为了是图片底部中心对准坐标点。
    });
    var marker = new BMap.Marker(point, {icon:myIcon});  // 创建标注
    map.addOverlay(marker);               // 将标注添加到地图中

}
//获取location参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}