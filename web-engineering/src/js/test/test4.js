var Flower = function(name){
    this.name = name;
}

var XiaoMing = {

    sendFlower : function(target){
        var flower = new Flower("郁金香");
        target.receive(flower);
    }

}

var B = {

    receive : function(flower){
        A.listenGoodMood(function(){
            A.receive(flower);
        })
    }

}

var A = {

    receive : function(flower){
        console.log("A收到了"+flower.name+"，好高兴！");
    },

    listenGoodMood : function(fn){

        setTimeout(function(){
            fn();
        },3000);

    }

}

XiaoMing.sendFlower(B);

var loading = require('../../imgs/square/play/playVideoLoading.gif');
var myImg = require('../../imgs/square/play/emotion5.png');

var ImgObj = function(){

    var $img = $("<img src='' />");
    $("body").append($img);
    return {
        setSrc : function(src){
            $img.attr("src",src);
        }
    }

}();

var proxyImg = function(){

    var $load_img = $("<img src='' />");
    $load_img.attr('src',loading);
    $load_img.on("load",function(){
        setTimeout(function(){
            ImgObj.setSrc($load_img.attr('src'));
        },2000);
    });
    return {
        setSrc : function(src){
            ImgObj.setSrc(loading);
            $load_img.attr("src",src);
        }
    }

}();

proxyImg.setSrc(myImg);

