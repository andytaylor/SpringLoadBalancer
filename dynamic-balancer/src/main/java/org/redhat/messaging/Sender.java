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
   @Qualifier("jmsSend1Template")
   private JmsTemplate jmsTemplate1;

   @Autowired
   @Qualifier("jmsSend2Template")
   private JmsTemplate jmsTemplate2;

   @Autowired
   @Qualifier("jmsSend3Template")
   private JmsTemplate jmsTemplate3;

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
      System.out.println(String.format("Sending to broker 1 '%s'", text));
      this.jmsTemplate1.convertAndSend("example", "broker1" + text);
      System.out.println(String.format("Sending to broker 2 '%s'", text));
      this.jmsTemplate2.convertAndSend("example", "broker2" + text);
      System.out.println(String.format("Sending to broker 3'%s'", text));
      this.jmsTemplate3.convertAndSend("example", "broker3" + text);
   }
}
