var Validate = function(){
    this.cache = [];  //保存校验规则
    this.add = function(rule){
        var validateFun = this.FormValidate[rule],
            args = Array.prototype.slice.call(arguments);

        args.shift();

        var validateObj = {
                "validateFun" : validateFun,
                "args" : args
            }

        this.cache.push(validateObj);
    };
    this.start = function(){
        var validateCode = true;
        for(var funObj;funObj = this.cache.shift();){
            if(!funObj["validateFun"].apply(this,funObj["args"])){
                validateCode = false;
            }
        }
        return validateCode;
    };
    this.FormValidate = {

        isEmpty : function(value,errorTip){
            if(value == ""){
                alert(errorTip);
                return false;
            }else{
                return true;
            }
        },

        isLength : function(value,errorTip,num){
            if(value.length < num){
                alert(errorTip);
                return false;
            }else{
                return true;
            }
        },

        isMobile : function(value,errorTip){
            var reg = /^1[3|5|8][0-9]{9}$/;
            if(!reg.test(value)){
                alert(errorTip);
                return false;
            }else{
                return true;
            }
        }

    }
}


//用户名不能为空
//密码长度不能少于6位
//手机号码格式不正确  /^1[3|5|8][0-9]{9}$/

var validateFunc = function(){
    var validate = new Validate();  //创建一个validate对象；
    validate.add("isEmpty",$("#username").val(),"用户名不能为空");
    validate.add("isLength",$("#username").val(),"用户名不能少于10位",10);
    validate.add("isLength",$("#password").val(),"密码长度不能少于6位",6);
    validate.add("isMobile",$("#mobilephone").val(),"手机号码格式不正确");
    var errorMsg = validate.start();   //获取校验结果
    return errorMsg;  //返回校验结果
}

$("#submit").on("click",function(){
    if(!validateFunc()){
        alert("表单填写有误，无法提交！");
        return false;
    }else{
        alert("表单提交成功！");
    }
})




