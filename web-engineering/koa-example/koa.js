var koa = require('koa');
var app = koa();

// x-response-time

app.use(function *(next){
    var start = new Date;
    yield next;
    var ms = new Date - start;
    this.set('X-Response-Time', ms + 'ms');
    console.log('**********');
});

// logger

app.use(function *(next){
    var start = new Date;
    yield next;
    var ms = new Date - start;
    console.log('%s %s - %s', this.method, this.url, ms);
});

// response

app.use(function *(){
    this.body = 'Hello World';
});

app.listen(3000,function(){
    console.log( 'check server listening on port 3000');
});

app.on('error', function(err, ctx){
    console.log('server error');
});