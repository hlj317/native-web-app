require("../../css/view-learn/sidebar2");

var $body = $('body');
function disableScroll(e) {
    e.preventDefault();
}
$('#panelSwitch').click(function(){
    if($body.hasClass('panel-active')){
        $body.removeClass('panel-active');
        $body.off('touchmove', disableScroll);
    }else{
        $body.addClass('panel-active');
        $body.on('touchmove', disableScroll);
    }
});