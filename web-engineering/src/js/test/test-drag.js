require('../../css/test/test-drag');
$(function(){
    /*touchstart the title and drag the con moving*/
    //var moveX,moveY,startX,startY;
    //$(document).on("touchstart","#title",function(event){
    //    if($(event.target).attr('id') == 'title'){
    //        var touchPros = event.touches[0];
    //        startX = touchPros.pageX - event.target.parentNode.offsetLeft;
    //        startY = touchPros.pageY - event.target.parentNode.offsetTop;
    //    }
    //    return false;
    //}).on("touchmove","#title",function(event){
    //    if($(event.target).attr('id') == 'title'){
    //        var touchPros = event.touches[0];
    //        moveX = touchPros.pageX - startX;
    //        moveY = touchPros.pageY - startY;
    //
    //        $('#con').css('transform','translate('+moveX+'px,'+moveY+'px)');
    //
    //        //$('#con').css('left',moveX).css('top',moveY);
    //    }
    //});

    /*touchstart the title and drag the con moving*/
    var moveX,moveY,startX,startY;
    $("#title").on("touchmove",function(event){

        var touchPros = event.touches[0];
        moveX = touchPros.pageX-$(this).width()/2;
        moveY = touchPros.pageY-$(this).height()/2;
        console.log("moveX:"+moveX+"&moveY:"+moveY);
        $('#title').css('transform','translate3d('+moveX+'px,'+moveY+'px,0)');
        event.preventDefault();   //禁止浏览器滚动

        //return false;
        //$('#con').css('left',moveX).css('top',moveY);

    })
});