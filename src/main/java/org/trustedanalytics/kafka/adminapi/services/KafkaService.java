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

package org.trustedanalytics.kafka.adminapi.services;

import kafka.admin.AdminUtils;
import kafka.consumer.ConsumerConfig;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.trustedanalytics.kafka.adminapi.config.KafkaConfig;
import org.trustedanalytics.kafka.adminapi.kafka.KafkaReader;
import org.trustedanalytics.kafka.adminapi.kafka.KafkaWriter;
import org.trustedanalytics.kafka.adminapi.model.TopicDescription;
import scala.collection.JavaConverters;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private KafkaWriter writer;

    @Autowired
    private KafkaConfig config;

    public List<String> listTopics() {
        LOG.info("Listing topics");

        List<String> resultTopicList = new ArrayList<>();

        java.util.Map<String, Properties> topicsWithConfigs = JavaConverters.mapAsJavaMapConverter(AdminUtils.fetchAllTopicConfigs(zkClient)).asJava();
        LOG.debug("topicsWithConfigs: {}", topicsWithConfigs);

        resultTopicList.addAll(topicsWithConfigs.keySet());

        LOG.debug("Topics found: {}", resultTopicList);
        return resultTopicList;
    }

    public void createTopic(TopicDescription topicDescription) {
        LOG.info("Topic creation: {}", topicDescription);

        Properties topicConfig = new Properties();
        AdminUtils.createTopic(zkClient, topicDescription.getTopic(), topicDescription.getPartitions(), TopicDescription.DEFAULT_REPLICATION_FACTOR, topicConfig);

        LOG.debug("Topic created");
    }

    public boolean topicExists(String topic) {
        return AdminUtils.topicExists(zkClient, topic);
    }

    public List<String> readTopic(String topic) {
        LOG.info("readTopic: {}", topic);
        List<String> messages;
        try (KafkaReader reader = new KafkaReader(config.connector())) {
            messages = reader.readMessages(topic);
        }
        return messages;
    }

    public void writeMessage(String topic, String message) {
        LOG.debug("writeMessage to Kafka: topic={}, msg={}", topic, message);
        writer.writeMessage(topic, message);
    }

    @PreDestroy
    protected void destroy() {
        try {
            LOG.debug("Closing zkClient");
            zkClient.close();
        } catch (ZkInterruptedException ex) {
            LOG.warn("Closing zkClient interrupted", ex);
        }
    }
}
