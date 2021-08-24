package com.github.fmjsjx.entrepot.server;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ AppProperties.class })
public class CoreConfig {

}
