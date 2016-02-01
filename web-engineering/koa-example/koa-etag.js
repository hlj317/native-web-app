/************* etag ****************/

var conditional = require('koa-conditional-get');
var etag = require('koa-etag');
var koa = require('koa');
var app = koa();

// etag works together with conditional-get
app.use(conditional());
app.use(etag());

app.use(function *(next){
    this.body = 'Hello World11';
    yield next;
    return function *(){
        yield next;
        this.body = 'Hello World';
    }
})

app.listen(3000);

console.log('listening on port 3000');