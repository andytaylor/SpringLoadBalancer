package org.redhat.messaging;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;

@SpringBootApplication
public class BrokerConfiguration implements ErrorHandler {

   @Bean
   public JmsConnectionFactory server1ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5672,amqp://localhost:5972)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   public JmsConnectionFactory server2ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5772,amqp://localhost:6072)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   public JmsConnectionFactory server3ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5872,amqp://localhost:6172)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   @Primary
   public JmsTemplate jmsSender1Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server1ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Bean
   @Primary
   public JmsTemplate jmsSender2Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server2ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Bean
   @Primary
   public JmsTemplate jmsSender3Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(server3ConnectionFactory());
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

   @Bean
   public JmsListenerContainerFactory<?> myFactory(@Qualifier("server1ConnectionFactory") ConnectionFactory connectionFactory,
                                                   DefaultJmsListenerContainerFactoryConfigurer configurer) {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setErrorHandler(this);
      // This provides all boot's default to this factory, including the message converter
      configurer.configure(factory, connectionFactory);
      // You could still override some of Boot's default if necessary.
      return factory;
   }

   @Bean
   public JmsListenerContainerFactory<?> myFactory2(@Qualifier("server2ConnectionFactory") ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setErrorHandler(this);
      // This provides all boot's default to this factory, including the message converter
      configurer.configure(factory, connectionFactory);
      // You could still override some of Boot's default if necessary.
      return factory;
   }

   @Bean
   public JmsListenerContainerFactory<?> myFactory3(@Qualifier("server3ConnectionFactory") ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setErrorHandler(this);
      // This provides all boot's default to this factory, including the message converter
      configurer.configure(factory, connectionFactory);
      // You could still override some of Boot's default if necessary.
      return factory;
   }

   @Override
   public void handleError(Throwable throwable) {
      System.out.println("BrokerConfiguration.handleError");
   }
}
