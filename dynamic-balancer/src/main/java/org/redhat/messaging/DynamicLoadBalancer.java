package org.redhat.messaging;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Random;

@SpringBootApplication
public class DynamicLoadBalancer implements MessageListener {

   @Override
   public void onMessage(Message message) {
      TextMessage textMessage = (TextMessage) message;
      try {
         executeJob(textMessage.getText());
      } catch (JMSException e) {
         e.printStackTrace();
      }
   }

   public void executeJob(String text) {
      System.out.println(String.format("Received '%s'", text));
      Random random = new Random();
      try {
         Thread.sleep(random.nextInt(5000) + 5000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      System.out.println("LoadBalancer.executeJob");
   }


}
