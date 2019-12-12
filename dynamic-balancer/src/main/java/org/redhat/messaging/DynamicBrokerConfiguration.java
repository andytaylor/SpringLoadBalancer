package org.redhat.messaging;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.util.List;

import static org.redhat.messaging.DynamicLoadBalancerUtil.executeJob;

@Configuration
@PropertySource("classpath:${client.config:qpid.properties}")
public class DynamicBrokerConfiguration implements JmsListenerConfigurer {

   @Value("#{'${brokerUrls}'.trim().split(';')}")
   private List<String> brokerUrls;

   @Value("${clientConcurrency}")
   private String clientConcurrency;

   @Value("${client}")
   private String client;

   @Value("${connections}")
   private int connections;

   @Bean
   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
   }

   @Bean
   public ConnectionFactory dynamicServer1ConnectionFactory() {
      return createConnectionFactory(brokerUrls.get(0), "sender");
   }

   @Bean
   @Primary
   public JmsTemplate jmsSendTemplate() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(dynamicServer1ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Override
   public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
      for (int i = 0, size = brokerUrls.size(); i < size; i++) {
         final String brokerId = "" + (i + 1);
         ConnectionFactory connectionFactory = createConnectionFactory(brokerUrls.get(i), brokerId);
         for (int j = 0; j < connections; j++) {
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId("BrokerEndpoint" + brokerId + "(" + j + ")");
            endpoint.setDestination("example");
            endpoint.setConcurrency(clientConcurrency);
            //the lambda can be shared by clientConcurrency different threads!
            endpoint.setMessageListener(message -> {
               TextMessage textMessage = (TextMessage) message;
               try {
                  executeJob(textMessage.getText(), brokerId);
               } catch (JMSException e) {
                  e.printStackTrace();
               }
            });

            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            jmsListenerEndpointRegistrar.registerEndpoint(endpoint, factory);
         }
      }
   }

   @Bean
   public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setConnectionFactory(dynamicServer1ConnectionFactory());

      return factory;
   }

   ConnectionFactory createConnectionFactory(String brokerUrl, String id) {
      if ("qpid".equals(client)) {
         return new JmsConnectionFactory(brokerUrl);
      } else {
         try {
            return ActiveMQJMSClient.createConnectionFactory(brokerUrl, id);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

  /* static final class DynamicJmsListenerContainerFactory extends DefaultJmsListenerContainerFactory {

      private ConnectionFactory connectionFactory;

      public DynamicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
         this.connectionFactory = connectionFactory;
      }

      @Override
      protected DefaultMessageListenerContainer createContainerInstance() {
         setConnectionFactory(new JmsConnectionFactory(brokerUrl));
         return super.createContainerInstance();
      }
   }*/

}
