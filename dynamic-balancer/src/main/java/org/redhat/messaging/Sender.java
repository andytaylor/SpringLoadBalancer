package org.redhat.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@EnableJms
@SpringBootApplication
public class Sender  implements CommandLineRunner {

   @Autowired
   @Qualifier("jmsSendTemplate")
   private JmsTemplate jmsTemplate;


   public static void main(String[] args) {
      SpringApplication.run(Sender.class, args);
   }

   @Override
   public void run(String... strings) throws Exception {
      for (int i = 0; i < 100; i++) {
         sendMessage("message: " + i);
      }
   }

   public void sendMessage(String text) {
      System.out.println(String.format("Sending to broker 1 '%s'", text));
      this.jmsTemplate.convertAndSend("example", "broker1" + text);
   }
}
