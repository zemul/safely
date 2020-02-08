//<!-- 修改 2019/5/18-->


var canvas = document.getElementById("canvas");
var context = canvas.getContext("2d");
context.lineWidth = 2;
var eraseAllButton = document.getElementById("eraseAllButton");
var strokeStyleSelect = document.getElementById("strokeStyleSelect");
var drawingSurfacsImageData = null;
var mousedown = {};
var dragging = false;
var jsonarray="[]";
var coordinate = eval('('+jsonarray+')');
var loc=null;
// drawHorizontLine(0);
// drawVerticalLine(0);
//获取实际的鼠标在canvas的位置
function windowToCanvas(x, y) {
    var bbox = canvas.getBoundingClientRect();
    return {
        x : x - bbox.left ,
        y : y - bbox.top
    };
}
//保存当前的canvas上的数据
function saveDrawingSurface() {
    drawingSurfacsImageData = context.getImageData(0, 0, canvas.width, canvas.height);
}
//恢复canvas的数据，主要用来显示最新的线段，擦除原来的线段
function restoreDrawingSurface() {
    context.putImageData(drawingSurfacsImageData,
        0, 0, 0, 0, canvas.width, canvas.height
    );
}
//更新
function  updateRubberband(loc) {
    drawRubberbandShape(loc);
}
//画最新的线条
function drawRubberbandShape(loc) {
    context.beginPath();
    context.moveTo(mousedown.x, mousedown.y);
    context.lineTo(loc.x, loc.y);
    context.stroke();
}
var first_point=null;
var is_first_click=true;//判断是否第一次点击
var is_first_checked=true;
canvas.onclick = function(e) {
    // if(dragging) {
    //     e.preventDefault();
    //     loc = windowToCanvas(e.clientX, e.clientY);
    //     restoreDrawingSurface();
    //     updateRubberband(loc);
    // }
    var arr =
        {
            "x" : e.clientX-canvas.getBoundingClientRect().left,
            "y" : e.clientY-canvas.getBoundingClientRect().top
        };
    coordinate.push(arr);
    loc = windowToCanvas(e.clientX, e.clientY);
    e.preventDefault();
    saveDrawingSurface();
    mousedown.x = loc.x;
    mousedown.y = loc.y;
    dragging = true;
    if(is_first_click){
        first_point=arr;
        is_first_click=false
    }
    //使第一次点击的坐标不做范围判断
      if(is_first_checked){
          is_first_checked=false;
          return
      }else {
          if((arr.x<first_point.x+10&&arr.x>first_point.x-10)&&(arr.y<first_point.y+10&&arr.y>first_point.y-10)){
              console.log(1);
              dragging=false
          }
      }

};
canvas.onmousemove = function(e){
    //判断当前是否用户在拖动
    if(dragging) {
        e.preventDefault();
        loc = windowToCanvas(e.clientX, e.clientY);
        restoreDrawingSurface();
        updateRubberband(loc);
    }
};

// canvas.onmouseup = function(e) {
//     loc = windowToCanvas(e.clientX, e.clientY);
//     restoreDrawingSurface();
//     updateRubberband(loc);
//     //鼠标抬起，拖动标记设为否
//     dragging = false;
// };
eraseAllButton.onclick = function(e){
    is_first_click=true;
    is_first_checked=true;
    dragging=false;
    coordinate.splice(0,coordinate.length);
    context.clearRect(0, 0, canvas.width, canvas.height);
    saveDrawingSurface();
    $("#camera-height").val(null)
};
strokeStyleSelect.onchange = function(e){
    context.strokeStyle = strokeStyleSelect.value;
};
context.strokeStyle = strokeStyleSelect.value;
