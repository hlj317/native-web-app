var FastClick = require('fastclick');
$(function() {
    FastClick.attach(document.body);
    var div = $(".div");
    div.on("click",function(){
        $("body").append("<div>aa</div>");
    })
});
