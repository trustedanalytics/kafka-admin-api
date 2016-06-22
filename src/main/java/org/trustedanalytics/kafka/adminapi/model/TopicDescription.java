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

package org.trustedanalytics.kafka.adminapi.model;

public class TopicDescription {

    public static final int DEFAULT_PARTITION_NUMBER = 2;
    public static final int DEFAULT_REPLICATION_FACTOR = 1;

    private String topic;

    private int partitions;

    public TopicDescription() {
        this.partitions = DEFAULT_PARTITION_NUMBER;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    @Override
    public String toString() {
        return "TopicDescription{" +
                "topic='" + topic + '\'' +
                ", partitions=" + partitions +
                '}';
    }
}
