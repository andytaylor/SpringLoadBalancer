package org.redhat.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;
import java.util.Random;

@EnableJms
@SpringBootApplication
public class DynamicLoadBalancer {


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
      executeJob(text, 1);
   }

   @Qualifier("jmsReceiver2Template")
   @JmsListener(destination = "example", containerFactory = "myFactory2")
   public void receiveMessage2(String text) {
      executeJob(text, 2);
   }

   public void executeJob(String text, int broker) {
      System.out.println(String.format("Received from '%s' '%s'", broker, text));
      Random random = new Random();
      try {
         Thread.sleep(random.nextInt(5000) + 5000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      System.out.println("LoadBalancer.executeJob");
   }

}
