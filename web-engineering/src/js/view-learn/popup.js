require("../../css/view-learn/popup");

// pop over
var $overlay = $('#overlay');

function modalHidden($ele) {
    $ele.removeClass('active');
    $ele.one('transitionend',function(){
        $ele.css({"display": "none"});
        $overlay.removeClass('active');
    });
}

$('.popup-over-link').click(function(){
    var $that = $(this),
        offset = $that.offset();
    $overlay.addClass('active');
    var $whichPopup = $('.'+$(this).data('popup-over'));
    $whichPopup.css({"display": "block", "left": offset.left+30, "top": offset.top+50},100,
        function(){
            $(this).addClass('active');
        });

    $overlay.click(function(){
        modalHidden($whichPopup);
    });
    $whichPopup.click(function(e){
        e.stopPropagation();
    });
});

// pop-page
$('.popup-page-link').click(function(){
    var $that = $(this);
    console.log($(this).data('popup-page'));
    var $whichPopup = $('.'+$(this).data('popup-page'));
    $whichPopup.addClass('active');

    $('.btn-close').click(function(){
        $whichPopup.removeClass('active');
    });
});
