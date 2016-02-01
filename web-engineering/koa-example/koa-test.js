/************ 测试 ****************/

var koa = require('koa');
var app = koa();
var request = require('koa-request');
var co = require('co');

var compute = function* (a,b) {
    var sum = a + b;
    yield sum;
    var c = a - b;
    yield sum;
    var d = a * b;
    yield d;
    var e = a / b;
    return e;
};

var compute2 = function* (a, b) {
    var foo = yield a + b;
    console.log(foo);
};

// co简易实现
//function co(generator){
//    var gen = generator();
//
//    var next = function(data){
//        var result = gen.next(data);
//
//        if(result.done) return;
//
//        if (result.value instanceof Promise) {
//            result.value.then(function (d) {
//                next(d);
//            }, function (err) {
//                next(err);
//            })
//        }else {
//            next();
//        }
//    };
//
//    next();
//}

function* Gen(a){
    var b = yield a;
    console.log(a);  //3
    var c = yield b;
    yield c;
}

var g = Gen(1);
console.log(g.next(2));
console.log(g.next(10));
console.log(g.next(30));
//console.log(g.next(2));//{ value: 1, done: false }
//console.log(g.next(3));//{ value: 3, done: false }
//console.log(g.next(4));//{ value: 4, done: false }
//console.log(g.next());//{ value: undefined, done: true }

//var generator = compute(4,2);
//console.log(generator.next());   //{value:6,done:false}
//console.log(generator.next();    //{value:4,done:false}
//console.log(generator.next());   //{value:8,done:false}
//console.log(generator.next());   //{value:undefined,done:true}


app.use(function *(){

    //var generator = compute2(5,5);
    //generator.next();
    //generator.next("Hello world!");

    // test
    //co(function*(){
    //    var text1 = yield new Promise(function(resolve){
    //        setTimeout(function(){
    //            resolve("I am text1");
    //        }, 5000);
    //    });
    //
    //    console.log(text1);
    //
    //    var text2 = yield new Promise(function(resolve){
    //        setTimeout(function(){
    //            resolve("I am text2");
    //        }, 1000);
    //    });
    //
    //    console.log(text2);
    //});
});

app.listen(3000,function(){
    console.log( 'check server listening on port 3000');
});
