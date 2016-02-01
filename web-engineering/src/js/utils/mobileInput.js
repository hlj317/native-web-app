/***
 * 移动web用输入组件
 * jizaiyi
 * 20150119
 * 初始化参数obj = {
 *     initDom, //必须为文档流中的元素且居于底部
 *     cancel, // 取消评论回调
 *     ok // 提交评论回调，回调方法参数为评论内容
 * }
 */

var ImgsInput = {};    //图片对象集合
//获取图片对象
var getImgsObj = function(){

    ImgsInput = {
        "startInput" : require("../../imgs/square/play/startInput.png"),
        "inputIcon" : require("../../imgs/square/play/inputIcon.png"),
        "emotion" : require("../../imgs/square/play/emotion.png")
    }

}();

// 全局对象 
var G = {
		css: '\
			.hide{display: none!important;}\
			.MobileInput{background: #fff;color: #888;}\
			.MobileInput .startInput{background: url('+ImgsInput["startInput"]+') no-repeat center;background-size: cover;height: 44px;}\
			.MobileInput .inputDiv{position: relative;text-align: center;padding-bottom: 5px;}\
			.MobileInput .div1{position:absolute;top: -37px;z-index: 1;background: #fff;display: table;width: 100%;height: 36px;line-height:36px;font-size: 16px;border-top: 1px solid #ddd;}\
			.MobileInput .div1 div{display: table-cell;}\
			.MobileInput .okBtn{background: url('+ImgsInput["inputIcon"]+') no-repeat -65px 7px;background-size: 200px;}\
			.MobileInput .cancelBtn{background: url('+ImgsInput["inputIcon"]+') no-repeat 25px 9px;background-size: 180px;}\
			.MobileInput .div2{text-align: left;position: relative;}\
			.MobileInput .realInput{width: 70%;margin-left: 6%;height: 34px;font-size: 16px;line-height: 16px;border: 1px solid #ccc;border-radius: 5px;outline: none;text-indent: 5px;}\
			.MobileInput .faceIcon{position: absolute;top: 0;right: 0;background: url('+ImgsInput["inputIcon"]+') no-repeat 15px -29px;background-size: 180px;  width: 20%;height: 34px;float: right;margin: 1px;}\
			.MobileInput .emotion{width: 320px;margin: 0 auto;}\
			.MobileInput .emotionItem{display: inline-block;}\
			.MobileInput .emotionItemBG{display: inline-block;width: 36px;height: 36px;margin: 6px 14px;background: url('+ImgsInput["emotion"]+') no-repeat;background-size: cover;}\
		',
		addCSS: function(cssText){
			var style = document.createElement('style'),  //创建一个style元素
		        head = document.head || document.getElementsByTagName('head')[0]; //获取head元素
		    style.type = 'text/css';
		    var textNode = document.createTextNode(cssText);
		    style.appendChild(textNode);
		    head.appendChild(style); //把创建的style元素插入到head中    
		}
};
// 加入样式 
G.addCSS(G.css);

var MobileInput = function(obj){
	this.init( obj );
};
MobileInput.prototype = {
		config:{
		},
		init: function( obj ){
			var me = this;
			var Dom = obj.initDom;
			if(!Dom){
				alert('initDom参数未定义');
				return;
			}
			me.cancelCB = obj.cancel;
			me.okCB = obj.ok;
			me.container = document.createElement( 'DIV' );
			me.container.classList.add( 'MobileInput' );
			me.startInput = document.createElement( 'DIV' );
			me.startInput.classList.add( 'startInput' );
			
			me.inputDiv = document.createElement( 'DIV' );
			me.inputDiv.classList.add('inputDiv');
			me.inputDiv.classList.add('hide');
			me.div1 = document.createElement( 'DIV' );
			me.div1.classList.add('div1');
			me.cancelBtn = document.createElement( 'DIV' );
			me.cancelBtn.classList.add('cancelBtn');
			me.cancelBtn.innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
			me.okBtn = document.createElement( 'DIV' );
			me.okBtn.classList.add('okBtn');
			me.okBtn.innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
			me.prompt = document.createElement( 'DIV' );
			me.prompt.innerText = '发表评论';
			me.div2 = document.createElement( 'DIV' );
			me.div2.classList.add('div2');
			me.realInput = document.createElement( 'INPUT' );
			me.realInput.type = 'text';
			me.realInput.classList.add('realInput');
			me.realInput.placeholder = '少于200字';
			me.faceIcon = document.createElement( 'DIV' );
			me.faceIcon.classList.add('faceIcon');
			me.div1.appendChild( me.cancelBtn );
			me.div1.appendChild( me.prompt );
			me.div1.appendChild( me.okBtn );
			me.inputDiv.appendChild( me.div1 );
			me.div2.appendChild( me.realInput );
			me.div2.appendChild( me.faceIcon );
			me.inputDiv.appendChild( me.div2 );
			
			me.emotion = document.createElement( 'DIV' );
			me.emotion.classList.add('emotion');
			me.emotion.classList.add('hide');
			var temphtml = '';
			for(var i = 1; i < 21; i ++){
				var imgOffset = 36 * (i - 1);
				temphtml += '<div class="emotionItem" data-value="[em_'+i+']"><div class="emotionItemBG" style="background-position: -'+imgOffset+'px 0;"></div></div>';
			}
			me.emotion.innerHTML = temphtml;
			
			me.container.appendChild( me.startInput );
			me.container.appendChild( me.inputDiv );
			me.container.appendChild( me.emotion );
			Dom.appendChild( me.container );
			
			me.handler();
		},
		// 触摸事件
		touchEvent: function(e){
			var me = this;
			var el = e.target;
			if(el.className == 'emotionItemBG'){
				el = el.parentNode;
			}
			switch(el.className){
			case 'startInput':
				me.startInputEvent();
				break;
			case 'cancelBtn':
				me.cancelBtnEvent();
				break;
			case 'okBtn':
				me.okBtnEvent();
				break;
			case 'realInput':
				me.realInputClickEvent();
				break;
			case 'faceIcon':
				me.faceIconEvent();
				break;
			case 'emotionItem':
				me.emotionItemEvent(el.getAttribute('data-value'));
				break;
			}
			e.preventDefault();
		},
		// 开始输入
		startInputEvent: function(){
			var me = this;
			if(me.startInput.classList.contains( 'hide' )){
				return;
			}
			me.startInput.classList.add( 'hide' );
			me.inputDiv.classList.remove('hide');
			me.realInputClickEvent();
			var ev = document.createEvent('HTMLEvents');
			ev.initEvent('click', false, true);
			me.realInput.dispatchEvent(ev);
			me.realInput.focus();
		},
		// 调用scrollIntoView
		intoView: function(){
			var me = this;
			if(me.inputDiv.classList.contains('hide')){
				return;
			}
			me.div2.scrollIntoView();
		},
		// click事件
		realInputClickEvent: function(){
			var me = this;
			me.hideFace();
			// android非微信浏览器会有输入法盖住input现象
//			me.container.style.height = '1000px'; 
			me.div2.scrollIntoView();
			me.realInput.focus();
			
		},
//		realInputBlurEvent: function(){
//			var me = this;
//			me.container.style.height = 'auto'; 
//		},
		// cancel事件
		cancelBtnEvent: function(){
			var me = this;
			me.hideFace();
			me.startInput.classList.remove( 'hide' );
			me.inputDiv.classList.add('hide');
			me.realInput.blur();
			if(me.cancelCB){
				me.cancelCB();
			}
		},
		// ok事件
		okBtnEvent: function(){
			var me = this;
			me.hideFace();
			me.startInput.classList.remove( 'hide' );
			me.inputDiv.classList.add('hide');
			var value = me.realInput.value;
			me.realInput.value = "";
			me.realInput.blur();
			if(me.okCB){
				me.okCB(value);
			}
			
		},
		// 点击表情图标
		faceIconEvent: function(){
			var me = this;
			if(me.emotion.classList.contains('hide')){
				me.emotion.classList.remove('hide');
				me.faceIcon.style.backgroundPosition = '-54px -28px';
				window.scrollTo(0,9999);
				document.body.scrollTop = 9999;
				me.emotion.scrollIntoView();
				me.realInput.blur();
			}else{
				me.emotion.classList.add('hide');
				me.faceIcon.style.backgroundPosition = '15px -29px';
				me.realInputClickEvent();
				var ev = document.createEvent('HTMLEvents');
				ev.initEvent('click', false, true);
				me.realInput.dispatchEvent(ev);
				me.realInput.focus();
				
			}
			
		},
		// 输入表情
		emotionItemEvent: function(value){
			var me = this;
			if( me.realInput.setSelectionRange ){
				var rangeStart=me.realInput.selectionStart; 
				var rangeEnd=me.realInput.selectionEnd; 
				var tempStr1=me.realInput.value.substring(0,rangeStart); 
				var tempStr2=me.realInput.value.substring(rangeEnd); 
				me.realInput.value=tempStr1+value+tempStr2; 
				var len=value.length; 
				me.realInput.setSelectionRange(rangeStart+len,rangeStart+len); 
				me.realInput.blur();
			}else{
				me.realInput.value += value;
			}
		},
		// 隐藏表情 
		hideFace: function(){
			var me = this;
			if(!me.emotion.classList.contains('hide')){
				me.emotion.classList.add('hide');
				me.faceIcon.style.backgroundPosition = '15px -29px';
			}
		},
		// 设置prompt
		setPrompt: function(str){
			var me = this;
			if(str && str.length > 7){
				str = str.substring(0, 7) + '...';
			}
			me.prompt.innerText = str || '发表评论';
		},
		// 设置placeholder
		setPlaceholder: function(str){
			var me = this;
			me.realInput.placeholder = str || '少于200字';
		},
		handler: function( startDom, resultDom ){
			var me = this;
//			me.realInput.addEventListener('click', me.realInputClickEvent.bind(me), false);
//			me.realInput.addEventListener('blur', me.realInputBlurEvent.bind(me), false);
			me.container.addEventListener('touchend', me.touchEvent.bind(me), false);
		}
};

window.MobileInput = MobileInput;