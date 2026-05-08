package com.vpm.projectserver;

import com.vpm.projectserver.config.IntegrationTestsDBConfig;
import com.vpm.projectserver.config.IntegrationTestsRabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({
        IntegrationTestsDBConfig.class,
        IntegrationTestsRabbitMQConfig.class})
class ProjectServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
