"use strict";
require("babel-polyfill");
const babel = require('babel-core');
const koa = require('koa');
const app = koa();

function* helloWorldGenerator() {
    yield 'hello';
    yield 'world';
    return 'ending';
}

var hw = helloWorldGenerator();

console.log(hw.next());
console.log(hw.next());
console.log(hw.next());


function* f() {
  console.log('执行了！')
}

var generator = f();

setTimeout(function () {
  generator.next()
}, 2000);


function* f() {
  for(var i=0; true; i++) {
    var reset = yield i;
    if(reset) { i = -1; }
  }
}

var g = f();

g.next() // { value: 0, done: false }
g.next() // { value: 1, done: false }
g.next(true) // { value: 0, done: false }


function* foo(x) {
  var y = 2 * (yield (x + 1));
  var z = yield (y / 3);
  return (x + y + z);
}


var a = foo(5);
a.next()  //{value:6,done:false}
a.next()  //{value:NaN,done:false}
a.next()  //{value:NaN,done:true}


var b = foo(5);  
b.next()   //{value:6,done:false}
b.next(12)  //{value:8,done:false}
b.next(13)  //{value:42,done:true}
 





