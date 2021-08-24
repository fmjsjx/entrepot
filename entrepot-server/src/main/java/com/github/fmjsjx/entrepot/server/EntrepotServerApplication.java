package com.github.fmjsjx.entrepot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.fmjsjx.entrepot.server.util.BannerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class EntrepotServerApplication {

    public static void main(String[] args) throws Exception {
        var ctx = SpringApplication.run(EntrepotServerApplication.class, args);
        var app = ctx.getBean(AppProperties.class);
        BannerUtil.printBanner(s -> log.info("-- Banner --\n{}", s), app.getName(), app.getVersion());
    }

}
