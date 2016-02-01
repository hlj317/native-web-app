var httpTools = require( '../tools/http-tools'),
    config = require( '../../config' );

//渲染同城页面
function *renderCity() {
    yield this.render('city',{
        "title":"同城",
        "entry":"city",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染地图页面
function *renderMap() {
    yield this.render('map',{
        "title":"地图",
        "entry":"map",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//获取单个视频详情信息
function *getVideoInfo(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/get","POST",this);
    this.body = ret;
}

//分批获取视频点地图信息
function *getMapList(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/map","POST",this);
    this.body = ret;
}

//获取热门城市
function *getHotCity(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/city/hot","POST",this);
    this.body = ret;
}

module.exports = {
    getVideoInfo : getVideoInfo,
    getMapList : getMapList,
    renderCity : renderCity,
    renderMap : renderMap,
    getHotCity : getHotCity
}
