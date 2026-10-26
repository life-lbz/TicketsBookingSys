package com.ticket.system.config;

import com.ticket.system.service.TotalSalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(TotalSalesService totalSalesService) {
        return args -> {
            logger.info("=== 开始初始化销售数据 ===");
            try {
                totalSalesService.updateAllSalesData();
                logger.info("=== 销售数据初始化完成 ===");
            } catch (Exception e) {
                logger.error("销售数据初始化失败", e);
            }
        };
    }
}
