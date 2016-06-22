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

package org.trustedanalytics.kafka.adminapi.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<ExceptionRepresentation> handle(Exception exception) {
        LOG.debug("Handling exception", exception);
        ExceptionRepresentation body = new ExceptionRepresentation(exception.getLocalizedMessage());
        HttpStatus responseStatus = resolveAnnotatedResponseStatus(exception);
        LOG.debug("Response: {}", new ResponseEntity<>(body, responseStatus));
        return new ResponseEntity<>(body, responseStatus);
    }

    private HttpStatus resolveAnnotatedResponseStatus(Throwable exception) {
        ResponseStatus responseStatus = findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.code();
        }
        else if (exception.getCause() instanceof Exception) {
            return resolveAnnotatedResponseStatus(exception.getCause());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private class ExceptionRepresentation {
        private String message;

        public ExceptionRepresentation(String localizedMessage) {
            this.message = localizedMessage;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

}

