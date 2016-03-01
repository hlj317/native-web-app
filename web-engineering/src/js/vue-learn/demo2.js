require('../../css/vue-learn/demo2');
var Vue = require('vue');

// 这是我们的 Model
var exampleData = {
    message: 'Vue.js'
}

window.exampleData = exampleData;

// 创建一个 Vue 实例或 "ViewModel"
// 它连接 View 与 Model
var exampleVM = new Vue({
    el: '#app',
    data: exampleData
})


var exampleVM2 = new Vue({
    el: '#example-2',
    data: {
        greeting: true
    }
})

