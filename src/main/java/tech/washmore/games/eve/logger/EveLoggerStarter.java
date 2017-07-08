package tech.washmore.games.eve.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

/**
 * @author Washmore
 * @version V1.0
 * @summary SpringBoot程序入口
 * @Copyright (c) 2017, www.washmore.tech
 * @since 2017/6/13
 */
@SpringBootApplication
@EnableScheduling
public class EveLoggerStarter {

    public static void main(String[] args) {
        try {
            SpringApplication.run(EveLoggerStarter.class, args);
            System.out.println("---------------正常启动--^_^------------#####################################");
        } catch (Exception e) {
            System.out.println("---------------启动挂了--T_T------------#####################################");
        }
//        while (true) {
//            try {
//                Thread.sleep(1000 * 3600L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
