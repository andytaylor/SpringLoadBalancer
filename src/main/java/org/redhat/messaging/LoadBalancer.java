package org.redhat.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@EnableJms
@SpringBootApplication
public class LoadBalancer {

   @Bean
   public JmsListenerContainerFactory<?> myFactory(@Qualifier("server1ConnectionFactory") ConnectionFactory connectionFactory,
                                                   DefaultJmsListenerContainerFactoryConfigurer configurer) {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      // This provides all boot's default to this factory, including the message converter
      configurer.configure(factory, connectionFactory);
      // You could still override some of Boot's default if necessary.
      return factory;
   }

   @Bean
   public JmsListenerContainerFactory<?> myFactory2(@Qualifier("server2ConnectionFactory") ConnectionFactory connectionFactory,
                                                   DefaultJmsListenerContainerFactoryConfigurer configurer) {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      // This provides all boot's default to this factory, including the message converter
      configurer.configure(factory, connectionFactory);
      // You could still override some of Boot's default if necessary.
      return factory;
   }


   @Qualifier("jmsReceiver1Template")
   @JmsListener(destination = "example", containerFactory = "myFactory")
   public void receiveMessage1(String text) {
      System.out.println(String.format("Received from 1 '%s'", text));
   }

   @Qualifier("jmsReceiver2Template")
   @JmsListener(destination = "example", containerFactory = "myFactory2")
   public void receiveMessage2(String text) {
      System.out.println(String.format("Received from 2 '%s'", text));
   }

  /* @Bean
   public JmsTemplate orderJmsTemplate() {
      JmsTemplate jmsTemplate =
            new JmsTemplate(ccf());
     // jmsTemplate.setDefaultDestination("example");
      jmsTemplate.setReceiveTimeout(5000);

      return jmsTemplate;
   }

   @Bean()
   public CachingConnectionFactory ccf() {
      CachingConnectionFactory ccf = new CachingConnectionFactory();
      //ccf.setAddresses("localhost:5672,localhost:5772");
      return ccf;
   }*/
}
