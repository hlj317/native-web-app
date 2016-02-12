require("../../css/view-learn/sidebar");
var $panelNav = $('#panelNav');
$('#panelSwitch').click(function(){
    $panelNav.css({"opacity":1});
    if($panelNav.hasClass('active')){
        $panelNav.removeClass('active');
    }else{
        $panelNav.addClass('active');
    }
});