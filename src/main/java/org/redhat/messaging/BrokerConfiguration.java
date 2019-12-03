package org.redhat.messaging;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
public class BrokerConfiguration {

   @Bean
   public JmsConnectionFactory server1ConnectionFactory() {
      return new JmsConnectionFactory();
   }

   @Bean
   public JmsConnectionFactory server2ConnectionFactory() {
      return new JmsConnectionFactory("amqp://localhost:5772");
   }
   @Bean
   @Primary
   public JmsTemplate jmsSenderTemplate() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server1ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Bean
   public JmsTemplate jmsReceiver1Template() {
      CachingConnectionFactory ccf = new CachingConnectionFactory();
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server1ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }


   @Bean
   public JmsTemplate jmsReceiver2Template() {
      CachingConnectionFactory ccf = new CachingConnectionFactory();
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server2ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

}
