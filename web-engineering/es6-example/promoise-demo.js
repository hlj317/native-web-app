"use strict";
require("babel-polyfill");
var babel = require('babel-core');
var koa = require('koa');
var app = koa();

function* helloWorldGenerator() {
  yield 'hello';
  yield 'world';
  return 'ending';
}

/*promise例子*/
var promise = new Promise((resolve, reject)=>{
  let x = 10;
  if (x>11/* 异步操作成功 */){
    resolve(3);
  } else {
    reject("throw an error");
  }
});

promise.then((value)=>{
  // success
   console.log(value);
}, function(value) {
   console.log(value);
  // failure
});

let total = [2,3,1];

var val = total.filter((a, b) => {
	if(a<2) return true;
});  

console.log(val);

function timeout(ms){
	return new Promise((resolve,reject) => {
		resolve("done1");
	})
}

timeout(200).then((value) => {
	console.log(value);
});


// function asyncLoadImage(src){

// 	return new Promise((resolve,reject) => {

// 		let image = new Image();
// 		image.onload = function(){
// 			resolve("load correct!");
// 		}
// 		image.onerror = function(){
// 			reject("load error!");
// 		}
// 		image.src = src;

// 	});

// }

// asyncLoadImage("http://www.baidu.com/").then((value) => {
// 	console.log(value);
// },(value) => {
// 	console.log(value);
// });


var p1 = new Promise((resolve,reject) => {

	setTimeout(()=>reject(new Error("error")),5000);

});

var p2 = new Promise((resolve,reject) => {

	setTimeout(()=>reject(p1),2500);

});

p2.then((value)=>{
	console.log(value);
});

p1.catch((value)=>{
	console.log(value);
});


var p3 = new Promise((resolve,reject) => {

	setTimeout(()=>resolve('hello'),50);

});

p3.then((value)=>{
	return new Promise((resolve,reject) => {
		setTimeout(()=>resolve(value),300);
	});
}).then((value)=>{
	console.log(value);
},(value)=>{
	console.log(value);
});


var someAsyncThing = ()=>{
  return new Promise((resolve, reject) =>{
    // 下面一行会报错，因为x没有声明
    resolve(x + 2);
  });
};

someAsyncThing()
.catch(function(error) {
  console.log('oh no', error);
})
.then(function() {
  console.log('carry on');
})














