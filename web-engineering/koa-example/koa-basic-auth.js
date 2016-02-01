/************* 权限控制 ****************/

var auth = require('koa-basic-auth');
var koa = require('koa');
var app = koa();

// custom 401 handling

app.use(function *(next){
    try {
        yield next;
    } catch (err) {
        if (401 == err.status) {
            this.status = 401;
            this.set('WWW-Authenticate', 'Basic');
            this.body = 'password is error';
        } else {
            throw err;
        }
    }
});

// require auth

app.use(auth({ name: 'tj', pass: 'tobi1' }));

// secret response

app.use(function *(){
    this.body = 'secret';
});

app.listen(3000);
console.log('listening on port 3000');