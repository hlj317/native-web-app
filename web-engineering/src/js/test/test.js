var Modal = require('modal');
var modal = new Modal({
    title : "天气提示",
    content : "请注意今天的天气会变得很冷，要多穿衣服！"
});

var modal2 = new Modal({
    title : "天气提示2",
    content : "请注意今天的天气会变得很冷，要多穿衣服！2"
});

alert(modal === modal2);

modal.on("show",function(config){
    alert(config.title);
});

modal.on("hide",function(config){
    alert("已关闭"+config.content);
});

modal.show();

window.modal = modal;
window.modal2 = modal2;