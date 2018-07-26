package com.microdev;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.entity.GlobalConfiguration;
import com.microdev.common.utils.*;
import com.microdev.model.MyMetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


import javax.sql.DataSource;
import java.net.URISyntaxException;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication
@MapperScan("com.microdev.mapper")
public class WgbApplication {
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		return dataSource;
	}
	@AutoConfigureOrder()
	public static void main(String[] args) {
		SpringApplication.run(WgbApplication.class, args);
		/*try {
			WebClientEnum.CLIENT.initClient(new MsgWebSocketClient ("ws://192.168.0.19:8091"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace ( );
		}*/
		try{
			MsgWebSocketServer ms = WebServerEnum.server.init(new MsgWebSocketServer (8099));
			System.out.println (WebSocketBuilder.wsMap);
		}catch (Exception e){
			e.printStackTrace ();
		}
		}
}
