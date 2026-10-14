package com.insight.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品服务启动入口。
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 */
@SpringBootApplication
public class ProductApplication {

    public static void main(String[] args) {
        // 启动内嵌 Tomcat,把本应用所有 Controller 发布成 HTTP 接口
        SpringApplication.run(ProductApplication.class, args);
    }
}
