/*
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.kafka.adminapi.config;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.trustedanalytics.kafka.adminapi.kafka.KafkaReader;
import org.trustedanalytics.kafka.adminapi.kafka.KafkaWriter;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

@Configuration
public class KafkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

    @Autowired
    private Environment env;

    @Value("${kafka.zookeeperUri}")
    private String zookeeperUri;

    @Value("${kafka.brokersUri}")
    private String brokersUri;

    @Bean
    public ZkClient zkClient() {
        // below are default values
        final int sessionTimeoutMs = 10 * 1000;
        final int connectionTimeoutMs = 10 * 1000;
        final int operationRetryTimeout = 2 * sessionTimeoutMs;

        // Note: We must initialize the ZkClient with ZKStringSerializer.  If we don't then
        // createTopic() will only seem to work (it will return without error). The topic will exist in
        // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the topic.
        // source: http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api

        return new ZkClient(
                zookeeperUri,
                sessionTimeoutMs,
                connectionTimeoutMs,
                ZKStringSerializer$.MODULE$,
                operationRetryTimeout);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public KafkaWriter writer() {
        return new KafkaWriter(brokersUri);
    }

    private ConsumerConfig consumerConfig() {
        // to be able to read topics many times we need to randomize the consumer group name
        Random rnd = new Random();
        String consumerGroupId = "" + new Date().getTime() + "_"+ rnd.nextInt(10000);

        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeperUri);
        props.put("group.id", consumerGroupId);
        props.put("consumer.timeout.ms", "1000"); // it will throw ConsumerTimeoutException
        props.put("auto.offset.reset", "smallest"); // when there is no (valid) offset
        props.put("zookeeper.session.timeout.ms", "1000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

    public ConsumerConnector connector() {
        return Consumer.createJavaConsumerConnector(consumerConfig());
    }
}
