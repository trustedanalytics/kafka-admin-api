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

import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${kafka.zookeeperUri}")
    private String zookeeperUri;

    @Bean
    public ZkClient createZkClient() {
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

}
