package com.anbao.pojo;

public class flow {
    String avg;
    String time;
    String max;

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "[\"" +time + "\"," + avg  + ']';
    }

    public String getData() {
        return time;
    }

    public void setData(String data) {
        this.time = data;
    }

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }
}
