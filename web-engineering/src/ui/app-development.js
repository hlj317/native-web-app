/************开发环境************/

var koa			= require( 'koa' ),
    render 		= require( 'koa-views' ),
    bodyParser	= require( 'koa-bodyparser' ),
    statics		= require( 'koa-static' ),
    keygrip     = require('keygrip'),
    path		= require( 'path' ),
    session     = require( 'koa-session'),
    routes      = require('./../../routes');

var app = koa();

//设置签名Cookie密钥，该密钥会被传递给 KeyGrip
app.keys = new keygrip(['im a newer secret','i like turtle'],'sha256');

//设置中间件，为http请求对象传入参数
app.use( statics( path.join( __dirname, './src')))
    .use( render( path.join( __dirname, './src/vod' ), { default: 'ejs' }))
    .use( bodyParser() )
    .use(session(app))
    .use( function *( next ){
    // ignore favicon
    if (this.path === '/favicon.ico') {
        this.body = "ignore";
        //return;
    };
    this.body = this.request.body;
    yield next;
});

// use routes 调用路由
routes( app );

console.log("进入开发调试环境");
//HMR，将webpack开发服务器以中间件的形式集成到local webserver
var webpackDevMiddleware = require('koa-webpack-dev-middleware');
var webpack = require('webpack');
var genConf = require('./../../webpack.config.js');
var webpackDevConf = genConf({debug: true});   //开发环境

app.use(webpackDevMiddleware(webpack(webpackDevConf), {
    contentBase: webpackDevConf.output.path,
    publicPath: webpackDevConf.output.publicPath,
    hot: true,   //热启动，页面引用静态资源放在内存中，方便调试
    // stats: webpackDevConf.devServer.stats
    stats: {
        cached: false,
        colors: true
    }
}));

app.listen( '3009', function(){
    console.log( 'check server listening on port 3009');
});
