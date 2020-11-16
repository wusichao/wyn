package com.wusichao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class orderController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("send/{exchange}/{rout}/{msg}")
    public String send(@PathVariable String exchange, @PathVariable String rout, @PathVariable String msg) {
        rabbitTemplate.convertAndSend(exchange, rout, msg);
        log.info("send to exchange: {}, rout: {}, msg: {}", exchange, rout, msg);
        return "success";
    }
}
