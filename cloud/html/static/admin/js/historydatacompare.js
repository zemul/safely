//数据对比图表一
var FirstChartxAxisData=['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
var FirstChartSeriesData=[10, 52, 200, 334, 390, 330, 220];
var SecondChartxAxisData=['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
var SecondChartSeriesData=[10, 52, 200, 334, 390, 330, 220];
var Firstcomchart=echarts.init(document.getElementById('first-com-chart'));
firstcompareoption = {
    title: {
        text:'时间段一',
    },
    color: ['#3398DB'],
    tooltip : {
        trigger: 'axis',
        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
        }
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis : [
        {
            type : 'category',
            data : FirstChartxAxisData,
            axisTick: {
                alignWithLabel: true
            }
        }
    ],
    yAxis : [
        {
            type : 'value'
        }
    ],
    series : [
        {
            name:'直接访问',
            type:'bar',
            barWidth: '40%',
            data:FirstChartSeriesData
        }
    ]
};
Firstcomchart.setOption(firstcompareoption);

//数据对比图表一
var Secondcomchart=echarts.init(document.getElementById('second-com-chart'));
secondcompareoption = { title: {
        text:'时间段二',
    },
    color: ['#3398DB'],
    tooltip : {
        trigger: 'axis',
        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
        }
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis : [
        {
            type : 'category',
            data : SecondChartxAxisData,
            axisTick: {
                alignWithLabel: true
            }
        }
    ],
    yAxis : [
        {
            type : 'value'
        }
    ],
    series : [
        {
            name:'直接访问',
            type:'bar',
            barWidth: '40%',
            data:SecondChartSeriesData
        }
    ]
};
Secondcomchart.setOption(secondcompareoption);
