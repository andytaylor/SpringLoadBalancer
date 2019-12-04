package org.redhat.messaging;

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

@Configuration
@PropertySource("classpath:broker.properties")
public class DynamicBrokerConfiguration implements JmsListenerConfigurer {

   @Autowired
   public DynamicLoadBalancer dynamicLoadBalancer;

   @Value("#{'${brokerUrls}'.trim().split(';')}")
   private List<String> brokerUrls;

   @Bean
   public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
   }

   @Bean
   public JmsConnectionFactory dynamicServer1ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5672,amqp://localhost:5972)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   public JmsConnectionFactory dynamicServer2ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5772,amqp://localhost:6072)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   public JmsConnectionFactory dynamicServer3ConnectionFactory() {
      return new JmsConnectionFactory("failover:(amqp://localhost:5872,amqp://localhost:6172)?jms.prefetchPolicy.queuePrefetch=1");
   }

   @Bean
   @Primary
   public JmsTemplate jmsSend1Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(dynamicServer1ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Bean
   public JmsTemplate jmsSend2Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(dynamicServer2ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Bean
   public JmsTemplate jmsSend3Template() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(dynamicServer3ConnectionFactory());
      jmsTemplate.setDefaultDestinationName("example");
      return jmsTemplate;
   }

   @Override
   public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
      int id = 1;
      for (String brokerUrl : brokerUrls) {
         SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
         endpoint.setId("BrokerEndpoint" + id++);
         endpoint.setDestination("example");
         endpoint.setMessageListener(dynamicLoadBalancer);
         jmsListenerEndpointRegistrar.registerEndpoint(endpoint, new DynamicJmsListenerContainerFactory(brokerUrl));
      }
   }

   @Bean
   public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setConnectionFactory(dynamicServer1ConnectionFactory());

      return factory;
   }

   class DynamicJmsListenerContainerFactory extends DefaultJmsListenerContainerFactory {

      private String brokerUrl;

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
