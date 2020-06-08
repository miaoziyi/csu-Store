package org.csu.csumall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.csu.csumall.mapper")
public class CsumallApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsumallApplication.class, args);
    }

}
