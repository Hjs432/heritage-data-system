package com.heritage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 青藏高原非物质文化遗产资源数据管理系统 —— 启动类
 * 服务地址：http://127.0.0.1:8080
 */
@SpringBootApplication
public class HeritageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeritageApplication.class, args);
        System.out.println("====== 青藏高原非物质文化遗产资源数据管理系统 启动成功：http://127.0.0.1:8080 ======");
    }
}