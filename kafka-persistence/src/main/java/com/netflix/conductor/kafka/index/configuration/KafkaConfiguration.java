package com.netflix.conductor.kafka.index.configuration;
        
import com.netflix.conductor.core.config.Configuration;
import com.netflix.conductor.kafka.index.producer.KafkaProducer;
import com.swiggy.kafka.clients.configs.*;
import com.swiggy.kafka.clients.configs.enums.ProducerAcks;
import com.swiggy.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class KafkaConfiguration {

    @Inject
    Configuration configuration;

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public ProducerConfig tier1ProducerConfig() {
        String primaryProducerBootStrapServer = configuration.getProperty("tier1.producer.primary.bootstrapServers", "localhost:9092");
        String primaryProducerUsername = configuration.getProperty("tier1.producer.primary.password", "");
        String primaryProducerPassword = configuration.getProperty("tier1.producer.primary.username", "");
        String primaryProducerAuthMechanism = configuration.getProperty("tier1.producer.primary.authMechanism", "NONE");

        CommonConfig.Cluster primaryCluster =  new CommonConfig.Cluster();
        primaryCluster.setBootstrapServers(primaryProducerBootStrapServer);
        primaryCluster.setAuthMechanism(AuthMechanism.valueOf(primaryProducerAuthMechanism));
        primaryCluster.setUsername(primaryProducerUsername);
        primaryCluster.setPassword(primaryProducerPassword);

        String secondaryProducerBootStrapServer = configuration.getProperty("tier1.producer.secondary.bootstrapServers", "localhost:9093");
        String secondaryProducerUsername = configuration.getProperty("tier1.producer.secondary.password", "");
        String secondaryProducerPassword = configuration.getProperty("tier1.producer.secondary.username", "");
        String secondaryProducerAuthMechanism = configuration.getProperty("tier1.producer.secondary.authMechanism", "NONE");
        CommonConfig.Cluster secondaryCluster =  new CommonConfig.Cluster();
        secondaryCluster.setBootstrapServers(secondaryProducerBootStrapServer);
        secondaryCluster.setAuthMechanism(AuthMechanism.valueOf(secondaryProducerAuthMechanism));
        secondaryCluster.setUsername(secondaryProducerUsername);
        secondaryCluster.setPassword(secondaryProducerPassword);
        ProducerConfig producerConfig = ProducerConfig.builder().primary(primaryCluster).secondary(secondaryCluster).clientId("ff-flo").enableCompression(true).acks(ProducerAcks.ALL).build();
        logger.info("Created producer config " + producerConfig + " successfully");
        return producerConfig;
    }

    public Topic floEventLogsTopic() {
        String name = configuration.getProperty("flo.indexer.topic.name", "ff-flo-event-logs");
        String keyId = configuration.getProperty("flo.indexer.topic.keyId", "flo_indexer_encryption_key_id");
        boolean enableEncryption = Boolean.valueOf(configuration.getProperty("flo.indexer.topic.enableEncryption", "false"));
        return Topic.builder().name(name).keyId(keyId).enableEncryption(enableEncryption).faultStrategy(Topic.FaultStrategy.NONE).build();
    }

    @Singleton
    public Producer getTier1Producer() {
        Map<String, Topic> topics = new HashMap<>();
        Topic topic = floEventLogsTopic();
        topics.put(topic.getName(), topic);
        return new Producer(tier1ProducerConfig().toBuilder().topics(topics).build());
    }

}