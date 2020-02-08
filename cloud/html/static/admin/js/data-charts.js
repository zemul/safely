//实时人流量图表

var threshold=25;//阈值大小

var flow = echarts.init(document.getElementById('flow_people'));
var option_flow = {
    title: {
        text: '实时人流量',
        left:'center',
        textStyle:{
            //字体风格,'normal','italic','oblique'
            fontStyle:'normal',
            //字体粗细 'normal','bold','bolder','lighter',100 | 200 | 300 | 400...
            fontWeight:'400',
            //字体系列
            fontFamily:'sans-serif',
            //字体大小
            fontSize:18
        }
    },
    legend: {
        data:['实时人流量']
    },
    dataZoom: {
        type: 'slider',
        xAxisIndex: [0],
        filterMode: 'empty'
    },

    xAxis: [
        {
            type: 'category',
            boundaryGap: true,
            data: (function (){
                var now = new Date();
                var res = [];
                var len = 180;
                while (len--) {
                    res.unshift(now.toLocaleTimeString().replace(/^\D*/,''));
                    now = new Date(now -1000);
                }
                return res;
            })()
        },
        {
            type: 'value',

            data: (function (){
                var res = [];

                return res;
            })()
        }
    ],
    yAxis: [
        {
            type: 'value',
            scale: true
        },
        {

        }
    ],
    series: [
        {
            type:'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            data:(function (){
                var res = [];
                var len = 10;
                while (len--) {

                }
                return res;
            }),
        },
        {
            // name:'实时人流量',
            type:'line',
            data:(function (){
                var res = [];
                var len = 0;
                while (len < 180) {
                    res.push(0);
                    len++;
                }
                return res;
            })(),
            itemStyle: {
                normal: {
                    color: '#409EFF',
                    borderColor: '#409EFF',
                    areaStyle: {
                        type: 'default',
                        opacity: 0.1
                    }}},

            //设置阈值大小
            markLine: {
                symbol:'none',
                silent: true,
                data: [{
                    yAxis: threshold,
                    lineStyle:{
                        color:'#FF5722'
                    }
                }
                ]
            }
        },

    ],
    grid: {
        left: '9%',   //距离左边的距离
        right: '6%', //距离右边的距离

    }
};
var count = 61;
var alert_info=0;
flow.setOption(option_flow);
setInterval(function (){
    var axisData = (new Date()).toLocaleTimeString().replace(/^\D*/,'');
    var Data = option_flow.series[1].data;
    Data.shift();
    Data.push(alert_info);//传入数据
    option_flow.xAxis[0].data.shift();
    option_flow.xAxis[0].data.push(axisData);
    option_flow.xAxis[1].data.shift();
    option_flow.xAxis[1].data.push(count++);
    flow.setOption(option_flow);
    option_flow.series[1].markLine.data[0].yAxis=threshold;

    // opt_flowrate.series[0].data[0].value = alert_info*4;
    // area_flowrate.setOption(opt_flowrate, true);
    //大于阈值触发报警
    if(alert_info>threshold){
        console.log("Data.push()")
    }
}, 1000);






var xAxisData
var SeriesData0
var SeriesData1

var people_normal=echarts.init(document.getElementById("people_normal"));
var normal_option = {
    title: {
        text: '最近24小时人流量及未来24小时人流量趋势预测',
        left:'center',
        textStyle:{
            //字体风格,'normal','italic','oblique'
            fontStyle:'normal',
            //字体粗细 'normal','bold','bolder','lighter',100 | 200 | 300 | 400...
            fontWeight:'400',
            //字体系列
            fontFamily:'sans-serif',
            //字体大小
            fontSize:18
        }
    },

    //用formatter回调函数显示多项数据内容
    tooltip: {
        trigger: 'axis',
        formatter: function (params, ticket, callback) {
            var htmlStr = '';
            var valMap = {};
            for(var i=0;i<params.length;i++){
                var param = params[i];
                var xName = param.name;//x轴的名称
                var seriesName = param.seriesName;//图例名称
                var value = param.value;//y轴值
                var color = param.color;//图例颜色
                //过滤无效值
                if(value == '-'){
                    continue;
                }
                //过滤重叠值
                if(valMap[seriesName] == value){
                    continue;
                }

                if(i===0){
                    htmlStr += xName + '<br/>';//x轴的名称
                }
                htmlStr +='<div>';
                //为了保证和原来的效果一样，这里自己实现了一个点的效果
                htmlStr += '<span style="margin-right:5px;display:inline-block;width:10px;height:10px;border-radius:5px;background-color:'+color+';"></span>';

                //圆点后面显示的文本
                htmlStr += seriesName + '：' + value;

                htmlStr += '</div>';
                valMap[seriesName] = value;
            }
            return htmlStr;
        }
    },
    legend: {
        data:['人流量','预测流量'],
        y:'35px'
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    toolbox: {
        feature: {
            saveAsImage: {}
        }
    },
    xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xAxisData
    },
    yAxis: {
        type: 'value'
    },
    series: [
        {
            name:'人流量',
            type:'line',
            data:SeriesData0
        },
        {
            name:'预测流量',
            type:'line',
            smooth:false,   //关键点，为true是不支持虚线，实线就用true
            itemStyle:{
                normal:{
                    lineStyle:{
                        width:2,
                        type:'dotted'  //'dotted'虚线 'solid'实线
                    }
                }
            },

            data:SeriesData1
        },


    ]
};
people_normal.setOption(normal_option);