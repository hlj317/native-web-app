require('../../css/vod/a');

//克隆对象
var Plane = function(){
    this.blood = 100;
    this.attackLevel = 1;
    this.defenseLevel = 1;
}

var plane = new Plane();
plane.blood = 30;
plane.attackLevel = 2;
plane.defenseLevel = 5;

var obj = {};
obj.clone = obj.clone || function(obj){
    function f(){};
    f.prototype = obj;
    return new f();
}

var plane2 = obj.clone(plane);


//模拟new()
function Person(name){
    this.name = name;
}
Person.prototype.getName = function(){
    return this.name;
}
ObjectFactory = function(){
    var obj = {};
    var _constructor = [].shift.call(arguments);
    obj.__proto__ = _constructor.prototype;
    var ret = _constructor.apply(obj,arguments);
    return typeof ret == "object" ? ret : obj;
}
var a = ObjectFactory(Person,"hlj");


//改变原型方法
var proObj = {
    name : "hlj"
};

Array.prototype.sort = (function(fn){

    return function(){
        return fn.apply(proObj,arguments);
    }

})(Array.prototype.sort);

var arr = [2,1,3];

arr.sort();   //此时sort方法失效


//封装判断类型方法
var typeObj = {};
var typeArr = ['String','Array','Number'];
for(var i = 0,type;type = typeArr[i++];){
    (function(type){

        typeObj["is"+type] = function(obj){
            return Object.prototype.toString.call(obj) === "[object "+type+"]";
        }

    })(type);
}


//增加数组去除重复元素原型方法
Array.prototype.filterSame = function(){
    var self = this;
    return this.filter(function(item,index){
       if(index !== self.lastIndexOf(item)){
           return false;
       }else{
           return true;
       }
    })
}














