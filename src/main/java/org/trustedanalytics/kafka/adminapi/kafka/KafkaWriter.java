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
package org.trustedanalytics.kafka.adminapi.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaWriter {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaWriter.class);

    private final String brokersUri;
    private KafkaProducer<String, String> kafkaProducer;

    public KafkaWriter(String brokersUri) {
        this.brokersUri = brokersUri;
    }

    public void init() {
        LOG.debug("opening connection to Kafka");
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersUri);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<>(producerConfig);
    }

    public void destroy() {
        kafkaProducer.close();
        LOG.debug("closing connection to Kafka");
    }

    public void writeMessage(String topic, String message) {
        LOG.debug("sending message to Kafka: {}, {}", topic, message);
        kafkaProducer.send(new ProducerRecord<>(topic, message));
        LOG.debug("message sent");
    }


}
