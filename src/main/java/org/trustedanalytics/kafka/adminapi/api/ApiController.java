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

package org.trustedanalytics.kafka.adminapi.api;

import kafka.common.InvalidTopicException;
import kafka.common.Topic;
import kafka.common.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;
import org.trustedanalytics.kafka.adminapi.model.TopicDescription;
import org.trustedanalytics.kafka.adminapi.services.KafkaService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping(value = "/api")
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private KafkaService kafkaService;

    @RequestMapping(method = RequestMethod.GET, value = "/topics")
    @ResponseBody
    public List<String> listTopics() {
        LOG.info("listTopics invoked.");
        return kafkaService.listTopics();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/topics", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTopic(@RequestBody TopicDescription topicDescription) {
        LOG.info("createTopic invoked: {}", topicDescription);

        if (StringUtils.isEmpty(topicDescription.getTopic())) {
            throw new InvalidTopicException("Missing mandatory topic name");
        }
        Topic.validate(topicDescription.getTopic());

        if (topicDescription.getPartitions() <= 0) {
            throw new InvalidTopicException("Number of partitions must be larger than 0");
        }

        kafkaService.createTopic(topicDescription);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/topics/{topic}")
    @ResponseBody
    public DeferredResult<List<String>> readTopic(@PathVariable final String topic) {
        LOG.info("readTopic invoked: {}", topic);

        if (StringUtils.isEmpty(topic)) {
            throw new InvalidTopicException("Missing mandatory topic name");
        }
        Topic.validate(topic);
        if (!kafkaService.topicExists(topic)) {
            throw new UnknownTopicOrPartitionException("Topic does not exist: " + topic);
        }

        DeferredResult<List<String>> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(() -> kafkaService.readTopic(topic))
                .whenCompleteAsync((result, throwable) -> {
                    deferredResult.setResult(result);
                    deferredResult.setErrorResult(throwable);
                });
        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/topics/{topic}", consumes = "text/plain")
    @ResponseStatus(HttpStatus.CREATED)
    public void writeMessage(@PathVariable String topic, @RequestBody String message) {
        LOG.info("writeMessage invoked: {}, {}", topic, message);

        kafkaService.writeMessage(topic, message);
    }

}
