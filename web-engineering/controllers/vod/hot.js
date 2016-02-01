var httpTools = require( '../tools/http-tools'),
    session = require( 'koa-session'),
    config = require( '../../config' );

//渲染热门页面
function *renderIndex() {

    //session和cookie处理
    //var jsessionid = "123213213123213";
    //this.session[jsessionid] = {
    //    "username" : "hlj",
    //    "id": "a1"
    //}
    //this.cookies.set( 'sessionid', jsessionid, {path: '/',domain:'127.0.0.1', maxAge: 36000*1000,signed: true } );
    //this.cookies.set( 'username', "hlj", {path: '/',domain:'127.0.0.1', maxAge: 36000*1000,signed: true } );

    yield this.render('hot',{
        "title":"热门",
        "entry":"hot",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染分类页面
function *renderClass() {
    yield this.render('class',{
        "title":"分类",
        "entry":"class",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染搜索页面
function *renderSearch() {
    yield this.render('search',{
        "title":"搜索",
        "entry":"search",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//获取今日精彩
function *getWonderful(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/special","POST",this);
    this.body = ret;
}

//获取视频列表（直播+点播）
function *getMixList(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/query?preFlag=true","POST",this);
    this.body = ret;
}

//获取直播列表
function *getList(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/channel/city","POST",this);
    this.body = ret;
}

//获取banner
function *getBanner(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/square/banner","POST",this);
    this.body = ret;
}

//获取热门频道列表
function *getHotChannelList(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/channel/hot","POST",this);
    this.body = ret;
}

module.exports = {
    getWonderful : getWonderful,
    getMixList : getMixList,
    getBanner : getBanner,
    getHotChannelList : getHotChannelList,
    getList : getList,
    renderIndex : renderIndex,
    renderClass : renderClass,
    renderSearch : renderSearch
}
