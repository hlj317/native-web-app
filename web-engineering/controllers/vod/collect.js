var httpTools = require( '../tools/http-tools'),
    config = require( '../../config' );

//渲染喜欢页面
function *renderCollect() {
    yield this.render('collect',{
        "title":"喜欢",
        "entry":"collect",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//渲染点赞的人页面
function *renderPraize() {
    yield this.render('praize',{
        "title":"点赞的人",
        "entry":"praize",
        "yundomain":config.yundomain,
        "staticPath":config.staticPath,
        "basePath":config.basePath
    });
}

//获取用户个人信息
function *getUserInfo(){
    var ret = yield httpTools.redirect(this.req,this.res,"/H5/user/avatar","POST",this);
    this.body = ret;
}

//获取用户收藏列表
function *getCollectList(){
    var ret = yield httpTools.redirect(this.req, this.res,"/H5/favorites/list","POST",this);
    this.body = ret;
}

//获取点赞头像列表
function *getFavourList(){
    var ret = yield httpTools.redirect(this.req, this.res,"/H5/favour/top","POST",this);
    this.body = ret;
}

//直播视频点赞
function *livePrize(){
    var ret = yield httpTools.redirect(this.req, this.res,"/H5/favour/add","POST",this);
    this.body = ret;
}

//点播视频点赞
function *videoPrize(){
    var url = "/vod/square/statfavour/" + this.params.id;
    var ret = yield httpTools.redirect(this.req, this.res,url,"POST",this);
    this.body = ret;
}

//取消收藏
function *cancelCollect(){
    var ret = yield httpTools.redirect(this.req, this.res,"/H5/favorites/delete","POST",this);
    this.body = ret;
}

module.exports = {
    getUserInfo : getUserInfo,
    getCollectList : getCollectList,
    getFavourList : getFavourList,
    livePrize : livePrize,
    videoPrize : videoPrize,
    cancelCollect : cancelCollect,
    renderCollect : renderCollect,
    renderPraize : renderPraize
}
