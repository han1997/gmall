package com.atguigu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
public class GmallCartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartWebApplication.class, args);
    }

}
