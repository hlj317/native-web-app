/************测试环境，生产环境************/

var koa			= require( 'koa' ),
    render 		= require( 'koa-views' ),
    bodyParser	= require( 'koa-bodyparser' ),
    statics		= require( 'koa-static' ),
    path		= require( 'path' ),
    routes      = require('./routes');

var app = koa();

//设置中间件，为http请求对象传入参数
app.use( statics( path.join( __dirname, './assets')))
    .use( render( path.join( __dirname, './assets' ), { default: 'ejs' }))
    .use( bodyParser() )
    .use( function *( next ){
    // ignore favicon
    if (this.path === '/favicon.ico') return;
    this.body = this.request.body;
    yield next;
});
 
// use routes 调用路由
routes( app );

// 捕捉未知异常
process.on('uncaughtException', function (err) {
    console.error(err);
});

app.listen( '3008', function(){
    console.log( 'check server listening on port 3008');
});
