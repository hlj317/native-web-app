require("../../css/mobile-learn/localstorage-demo");

var src= require("../../imgs/square/city/video-default.png");//这里一定要在服务器上运行，图片要是当前服务器的图片！

function set(key){
    var img = document.createElement('img');//创建图片元素
    img.addEventListener('load',function(){//绑定加载时间
        var imgcavens = document.createElement('canvas');
        imgcontent = imgcavens.getContext('2d');
        imgcavens.width = this.width;//设置画布大小为图片本身的大小
        imgcavens.height = this.height;
        imgcontent.drawImage(this,0,0,this.width,this.height);
        var imgAsDataUrl = imgcavens.toDataURL('image/png');//这个方法一定要在服务器上运行
        /*
         对图像数据做出修改以后，可以使用toDataURL方法，将Canvas数据重新转化成一般的图像文件形式。
         function convertCanvasToImage(canvas) {
         var image = new Image();
         image.src = canvas.toDataURL("image/png");
         return image;
         }
         上面的代码将Canvas数据，转化成PNG data URI。
         */
        try{
            localStorage.setItem(key,imgAsDataUrl);//保存图片地址
        }catch(e)
        {
            console.log("storageFaild: "+e);//错误信息
        }
    },false)
    img.src = src;//指定需要存储的图片地址
}
function get(key){
    var srcStr = localStorage.getItem(key);//获取本地存储的元素
    var imgobj = document.createElement('img');
    imgobj.src = srcStr;//指定图片路径
    document.body.appendChild(imgobj);//在页面中添加元素
}
window.set = set;
window.get = get;