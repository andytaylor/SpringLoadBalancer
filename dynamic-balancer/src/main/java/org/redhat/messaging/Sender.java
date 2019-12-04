package org.redhat.messaging;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@EnableJms
@SpringBootApplication
public class Sender  implements CommandLineRunner {

   @Autowired
   @Qualifier("jmsSenderTemplate")
   private JmsTemplate jmsTemplate;

   public static void main(String[] args) {
      SpringApplication.run(Sender.class, args);
   }

   @Override
   public void run(String... strings) throws Exception {
      for (int i = 0; i < 10; i++) {
         sendMessage("message: " + i);
      }
   }

   public void sendMessage(String text) {
      System.out.println(String.format("Sending '%s'", text));
      this.jmsTemplate.convertAndSend("example", text);
   }
}
