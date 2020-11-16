package com.wusichao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class orderConsumer {

    @RabbitListener(queues = "order.create")
    public void createListener (String msg) {
        log.info(" createListener consumer msg: {}", msg);
    }

    @RabbitListener(queues = "order.createAndUpdate")
    public void createAndUpdateListener (String msg) {
        log.info("createAndUpdateListener consumer msg: {}", msg);
    }
}
