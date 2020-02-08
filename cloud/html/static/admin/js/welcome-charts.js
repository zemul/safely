var freqchart=echarts.init(document.getElementById('freq-charts'),'macarons');
var xdata=['校园大门', '学校食堂', '大型商超', '政府大门', '交通枢纽', '其他'];
var seriesdata=[2, 10, 4, 10, 6, 5];
var rotate=0;
var marginleft='4%'
if(document.body.clientWidth <768){
    rotate=40,
        marginleft='8%'
}else {
    rotate=0
}
freqoption =  {
    color: ['#8fd3e8'],
    tooltip : {
        trigger: 'axis',
        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
        }
    },
    grid: {
        left: marginleft,
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis : [

        {
            axisLabel: {
                interval:0,
                rotate:rotate
            },
            type : 'category',
            data : xdata,
            axisTick: {
                alignWithLabel: true
            }
        }
    ],
    yAxis : [
        {
            type : 'value',
            name:'超出阈值次数(次)',

        }
    ],
    series : [
        {
            name:'超出阈值次数',
            type:'bar',
            barWidth: '40%',
            data:seriesdata
        }
    ]
};
freqchart.setOption(freqoption);


