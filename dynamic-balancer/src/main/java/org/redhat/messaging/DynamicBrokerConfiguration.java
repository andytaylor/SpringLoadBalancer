package org.redhat.messaging;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;

@SpringBootApplication
public class DynamicBrokerConfiguration implements JmsListenerConfigurer {

   @Bean
   public JmsConnectionFactory server1ConnectionFactory() {
      return new JmsConnectionFactory("amqp://localhost:5672?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   public JmsConnectionFactory server2ConnectionFactory() {
      return new JmsConnectionFactory("amqp://localhost:5772?jms.prefetchPolicy.queuePrefetch=1");
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

   @Override
   public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
      SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
      endpoint.setId("myJmsEndpoint");
      endpoint.setDestination("example");
      endpoint.setMessageListener(message -> {
         System.out.println("BrokerConfiguration.configureJmsListeners");
      });
      jmsListenerEndpointRegistrar.registerEndpoint(endpoint);

      endpoint = new SimpleJmsListenerEndpoint();
      endpoint.setId("myJmsEndpoint2");
      endpoint.setDestination("example");
      endpoint.setMessageListener(message -> {
         System.out.println("BrokerConfiguration.configureJmsListeners2");
      });
      jmsListenerEndpointRegistrar.registerEndpoint(endpoint);
   }

   @Bean
   public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setConnectionFactory(server2ConnectionFactory());

      return factory;
   }
}
