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

import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaReader implements Closeable {

    /**
     * Indicates how many messages this reader can consume from a single topic.
     * This is for security reasons against out of memory errors.
     * In case of large topics this constant must be increased.
     */
    private static final int MAX_MESSAGE_LIST_CAPACITY = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(KafkaReader.class);

    private final ConsumerConnector connector;

    public KafkaReader(ConsumerConnector connector) {
        this.connector = connector;
    }

    public List<String> readMessages(String topic) {
        LOG.info("reading messages from topic: {}", topic);
        try {
            List<String> messages = readMessages(createKafkaStream(topic));
            LOG.debug("Received #{} messages.", messages.size());
            return messages;
        } catch (Exception ex) {
            LOG.error("Error occurred during reading topic", ex);
            throw ex;
        }
    }

    private List<String> readMessages(KafkaStream<byte[], byte[]> kafkaStream) {
        List<String> messages = new ArrayList<>();
        try {
            int consumedMessagesCount = 0;
            for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : kafkaStream) {
                String message = new String(messageAndMetadata.message(), StandardCharsets.UTF_8);
                messages.add(message);

                if (++consumedMessagesCount >= MAX_MESSAGE_LIST_CAPACITY) {
                    LOG.debug("Message list is full. No more reading...");
                    break;
                }
            }
        } catch (ConsumerTimeoutException ex) {
            LOG.debug("No more messages", ex);
            // this exception is thrown if there is no more messages available
            // we need to ignore it
        }
        return messages;
    }

    private KafkaStream<byte[], byte[]> createKafkaStream(String topic) {
        LOG.debug("creating Kafka stream for topic {}", topic);
        Map<String, Integer> topicsCountMap = new HashMap<String, Integer>() {{
            put(topic, 1);
        }};
        return connector.createMessageStreams(topicsCountMap).get(topic).get(0);
    }

    @Override
    public void close() {
        LOG.debug("closing connection to Kafka");
        try {
            connector.shutdown();
        } catch (Exception ex) {
            LOG.debug("Error during Kafka connection shutdown. Ignoring.", ex);
        }
    }
}
