package com.vpm.taskserver.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * RabbitMQ configuration properties
 * Maps to spring.rabbitmq prefix in application.properties
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMQProperties {

    private Queue queue = new Queue();
    private Exchange exchange = new Exchange();
    private RoutingKey routingKey = new RoutingKey();

    @Data
    public static class Queue {

        private String volunteerAssigned;
        private String volunteerSuggestionReported;

        private Dql dql = new Dql();

        @Data
        public static class Dql {

            private String volunteerAssigned;
            private String volunteerSuggestionReported;

        }

    }

    @Data
    public static class Exchange {

        private String volunteerAssigned;
        private String volunteerSuggestionReported;

        private Dlx dlx = new Dlx();

        @Data
        public static class Dlx {

            private String volunteerAssigned;
            private String volunteerSuggestionReported;

        }

    }

    @Data
    public static class RoutingKey {

        private String volunteerAssigned;
        private String volunteerSuggestionReported;

        private Dql dql = new Dql();

        @Data
        public static class Dql {

            private String volunteerAssigned;
            private String volunteerSuggestionReported;

        }

    }

}
