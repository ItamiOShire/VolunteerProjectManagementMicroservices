package com.vpm.organizationserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrganizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationServerApplication.class, args);
    }

}
