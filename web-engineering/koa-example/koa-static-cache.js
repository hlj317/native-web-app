/************* 静态资源缓存 ****************/

var koa			= require( 'koa' ),
    render 		= require( 'koa-views' ),
    bodyParser	= require( 'koa-bodyparser' ),
    statics		= require( 'koa-static' ),
    path		= require( 'path' ),
    hot         = require('../controllers/vod/hot'),
    Router	= require( 'koa-router' );

var staticCache = require('koa-static-cache');
var app = koa();

//设置中间件，为http请求对象传入参数
app.use( statics( path.join( __dirname, '../assets')))
    .use( render( path.join( __dirname, '../assets' ), { default: 'ejs' }))
    .use( bodyParser() )
    .use(staticCache(path.join(__dirname,'../assets'), {
        maxAge: 365 * 24 * 60 * 60
    }))
    .use( function *( next ){
    this.body = this.request.body;
    yield next;
});

/*创建路由实例对象*/
var router = new Router();

/****************页面请求处理**********************/
router.get('/', hot.renderIndex);                  //热门页面
app.use( router.middleware() );

// 捕捉未知异常
process.on('uncaughtException', function (err) {
    console.error(err);
});

app.listen( '3008', function(){
    console.log( 'check server listening on port 3008');
});