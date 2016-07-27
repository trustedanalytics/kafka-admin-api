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
import kafka.common.TopicExistsException;
import kafka.common.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleBadRequest(InvalidTopicException ex) {
        LOG.error("Invalid topic", ex);
        return ex.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleBadRequest(HttpMessageConversionException ex) {
        LOG.error("Handling request malformed exception", ex);
        return "Request malformed";
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public String handleConflict(TopicExistsException ex) {
        LOG.error("Handling TopicExistsException", ex);
        return ex.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleUnknownTopic(UnknownTopicOrPartitionException ex) {
        LOG.error("Unknown topic", ex);
        return ex.getMessage();
    }

    /**
     * This is a generic exception handler.
     * It tries to resolve a response status based on the ResponseStatus annotation
     * applied on a class of an object passed as an argument.
     * It looks recursively at the the exception class and exception root causes.
     * If no such ResponseStatus annotation is found anywhere then HttpStatus.INTERNAL_SERVER_ERROR is returned.
     * @param ex The exception object
     * @return The response entity
     */
    @ExceptionHandler
    public ResponseEntity<String> handleGenericException(Exception ex) {
        LOG.error("Handling generic exception", ex);
        HttpStatus responseStatus = resolveAnnotatedResponseStatus(ex);
        if (responseStatus == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(ex.getMessage(), responseStatus);
    }

    private HttpStatus resolveAnnotatedResponseStatus(Throwable ex) {
        ResponseStatus responseStatus = findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.code();
        }
        else if (ex.getCause() instanceof Exception) {
            return resolveAnnotatedResponseStatus(ex.getCause());
        }
        return null;
    }
}

