var toast = require('toast');
toast.show("今天不错哦！",3000);

window.toast = toast;

//小球动画
var Animate = function(dom){

    this.dom = dom;      //进行运动的dom节点
    this.startTime = 0;  //动画开始时间
    this.startPos = 0;   //动画开始时，dom节点的位置，即dom的初始位置
    this.endPos = 0;     //动画结束时，dom节点的位置，即dom的目标位置
    this.propertyName = null;   //dom节点需要被改变的css属性名
    this.easing = null;    //缓动算法
    this.duration = null;  //动画持续时间

}

Animate.prototype = {

    constructor : Animate,

    //各种动画公式
    tween : {
        //t:还剩多少时间,b:当前位置,c:总距离,d:持续时间
        linear : function(t,b,c,d){
            return c*t/d+b;
        },

        easeIn : function(t,b,c,d){
            return c*(t/=d)*t+b;
        },

        strongEaseIn : function(t,b,c,d){
            return c*(t/=d)*t*t*t*t+b;
        },

        strongEaseOut : function(t,b,c,d){
            return c*((t=t/d-1)*t*t*t*t+1)+b;
        },

        sineaseOut : function(t,b,c,d){
            return c*((t=t/d-1)*t*t+1)+b;
        }
    },

    start : function(propertyName,endPos,duration,easing){

        this.startTime = +new Date;  //动画启动时间
        this.startPos = this.dom.getBoundingClientRect()[propertyName];  //dom节点初始位置
        this.propertyName = propertyName;   //dom节点需要被改变的CSS属性名
        this.endPos = endPos;    //dom节点目标位置
        this.duration = duration;   //动画持续时间
        this.easing = this.tween[easing];  //缓动算法

        var self = this;
        var timeId = setInterval(function(){

            if(self.step() === false){
                clearInterval(timeId);
            }

        },4)

    },

    step : function(){
        var t = +new Date;
        if( t>= this.startTime + this.duration){
            this.update(this.endPos);
            return false;
        }
        var pos = this.easing( t - this.startTime,this.startPos,this.endPos - this.startPos,this.duration);   //pos为小球当前位置
        this.update(pos);
    },

    //更新小球的当前位置
    update : function(pos){
        this.dom.style[this.propertyName] = pos + 'px';
    }

}

var div = $("#qiu")[0];
var animate = new Animate(div);

animate.start("left",200,8000,"easeIn");
window.Animate = Animate;
