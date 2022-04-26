/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foco.boot.rabbit.configuration;

import com.foco.boot.rabbit.constant.RabbitConstant;
import com.foco.boot.rabbit.producer.Producer;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *  * @Author lucoo
 *  * @Date 2021/6/27 10:13
 */
@Configuration
public class RabbitMQConfiguration {
    private static final String topicExchangeName = "topic-exchange";
    private static final String fanoutExchange    = "fanout-exchange";
    private static final String headersExchange   = "headers-exchange";

    @Bean
    public Queue queue() {
        return new Queue(RabbitConstant.DEFAULT_CHANNEL);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    public Binding topicBinding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("org.default.#");
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanoutExchange);
    }

    @Bean
    public Binding fanoutBinding(Queue queue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(headersExchange);
    }

    @Bean
    public Binding headersBinding(Queue queue, HeadersExchange headersExchange) {
        Map<String, Object> map = new HashMap<>();
        map.put("First", "A");
        map.put("Fourth", "D");
        return BindingBuilder.bind(queue).to(headersExchange).whereAny(map).match();
    }
    @Bean
    public Producer producer(){
        return new Producer();
    }

}
