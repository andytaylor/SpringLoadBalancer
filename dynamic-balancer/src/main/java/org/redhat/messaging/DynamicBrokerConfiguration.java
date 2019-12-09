package org.redhat.messaging;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

import static org.redhat.messaging.DynamicLoadBalancerUtil.executeJob;

@Configuration
@PropertySource("classpath:broker.properties")
public class DynamicBrokerConfiguration implements JmsListenerConfigurer {

   @Value("#{'${brokerUrls}'.trim().split(';')}")
   private List<String> brokerUrls;

   @Value("${clientConcurrency}")
   private String clientConcurrency;

   @Bean
   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
   }

   @Bean
   public JmsConnectionFactory dynamicServer1ConnectionFactory() {
      return new JmsConnectionFactory(brokerUrls.get(0));
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
         SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
         final int brokerId = i + 1;
         endpoint.setId("BrokerEndpoint" + brokerId);
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
         jmsListenerEndpointRegistrar.registerEndpoint(endpoint, new DynamicJmsListenerContainerFactory(brokerUrls.get(i)));
      }
   }

   @Bean
   public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setConnectionFactory(dynamicServer1ConnectionFactory());

      return factory;
   }

   static final class DynamicJmsListenerContainerFactory extends DefaultJmsListenerContainerFactory {

      private final String brokerUrl;

      public DynamicJmsListenerContainerFactory(String brokerUrl) {
         this.brokerUrl = brokerUrl;
      }

      @Override
      protected void initializeContainer(DefaultMessageListenerContainer container) {
         setConnectionFactory(new JmsConnectionFactory(brokerUrl));
         super.initializeContainer(container);
      }

      @Override
      protected DefaultMessageListenerContainer createContainerInstance() {
         setConnectionFactory(new JmsConnectionFactory(brokerUrl));
         return super.createContainerInstance();
      }
   }
}
