package com.anbao.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 人流量预测定时任务
 * 每天凌晨 2 点自动执行 Prophet 预测脚本
 */
@Component
public class ForecastTask {

    private static final String PYTHON_CMD = System.getenv("PYTHON_CMD") != null
            ? System.getenv("PYTHON_CMD") : "python3";
    private static final String SCRIPT_PATH = System.getenv("FORECAST_SCRIPT_PATH") != null
            ? System.getenv("FORECAST_SCRIPT_PATH") : "/opt/safely/cloud/time_serie_ARIMA/forecast_prophet.py";

    /**
     * 每天凌晨 2:00 执行预测
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyForecast() {
        System.out.println("[ForecastTask] 开始执行人流量预测...");
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_CMD, SCRIPT_PATH);
            pb.redirectErrorStream(true);
            pb.environment().putAll(System.getenv());  // 继承所有环境变量

            Process process = pb.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[ForecastTask] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[ForecastTask] 预测完成");
            } else {
                System.err.println("[ForecastTask] 预测脚本退出码: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("[ForecastTask] 执行失败: " + e.getMessage());
        }
    }
}
