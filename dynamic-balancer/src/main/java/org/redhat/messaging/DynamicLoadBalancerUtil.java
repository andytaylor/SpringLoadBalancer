package org.redhat.messaging;

import java.util.Random;

public class DynamicLoadBalancerUtil {

   public static void executeJob(String text, String broker) {
      System.out.println(String.format("Received '%s' from `%s`", text, broker));
      Random random = new Random();
      try {
         Thread.sleep(random.nextInt(5000) + 5000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      System.out.println("LoadBalancer.executeJob");
   }

}
