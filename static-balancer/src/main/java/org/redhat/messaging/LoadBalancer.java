package org.redhat.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

import java.util.Random;

@EnableJms
@SpringBootApplication
public class LoadBalancer {

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

   @Qualifier("jmsReceiver3Template")
   @JmsListener(destination = "example", containerFactory = "myFactory3")
   public void receiveMessage3(String text) {
      executeJob(text, 3);
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
