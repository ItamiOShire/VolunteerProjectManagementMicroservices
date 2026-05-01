package com.vpm.volunteerserver.config.properties;


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

        private String volunteerAssignedToTask;
        private String volunteerSuggestionReported;
        private String volunteerAssignedToProject;

        private Dlq dlq = new Dlq();

        @Data
        public static class Dlq {

            private String volunteerAssignedToTask;
            private String volunteerSuggestionReported;
            private String volunteerAssignedToProject;

        }

    }

    @Data
    public static class Exchange {

        private String volunteerAssignedToTask;
        private String volunteerSuggestionReported;
        private String volunteerAssignedToProject;

        private Dlx dlx = new Dlx();

        @Data
        public static class Dlx {

            private String volunteerAssignedToTask;
            private String volunteerSuggestionReported;
            private String volunteerAssignedToProject;

        }

    }

    @Data
    public static class RoutingKey {

        private String volunteerAssignedToTask;
        private String volunteerSuggestionReported;
        private String volunteerAssignedToProject;

        private Dlq dlq = new Dlq();

        @Data
        public static class Dlq {

            private String volunteerAssignedToTask;
            private String volunteerSuggestionReported;
            private String volunteerAssignedToProject;

        }

    }

}
