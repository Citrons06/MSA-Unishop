package my.payservice.config;

import my.payservice.kafka.event.PayEvent;
import my.payservice.kafka.event.ProcessEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, PayEvent> payEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, "true");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, PayEvent> payEventConsumerFactory() {
        JsonDeserializer<PayEvent> deserializer = new JsonDeserializer<>(PayEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setTypeMapper(new DefaultJackson2JavaTypeMapper());

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "pay-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, PayEvent> payEventKafkaTemplate() {
        return new KafkaTemplate<>(payEventProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PayEvent> payEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PayEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(payEventConsumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, ProcessEvent> processEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, "true");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, ProcessEvent> processEventConsumerFactory() {
        JsonDeserializer<ProcessEvent> deserializer = new JsonDeserializer<>(ProcessEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setTypeMapper(new DefaultJackson2JavaTypeMapper());

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "process-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, ProcessEvent> processEventKafkaTemplate() {
        return new KafkaTemplate<>(processEventProducerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessEvent> processEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(processEventConsumerFactory());
        return factory;
    }
}