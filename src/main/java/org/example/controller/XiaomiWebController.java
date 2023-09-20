package org.example.controller;


import org.example.Service.XiaomiWebDataService;
import org.example.Service.impl.XiaomiWebDataServiceImpl;
import org.example.grmsapi.CommonResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/web/data/xiaomi")
public class XiaomiWebController {

    @Resource
    private XiaomiWebDataService xiaomiWebDataService = new XiaomiWebDataServiceImpl();

    @GetMapping("getXiaomiData")
    public CommonResult<?> getData() throws IOException {
        return CommonResult.success(xiaomiWebDataService.getMIUIData());
    }

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping(value = "send")
    public void sendMsg() {

        String msg = "12345";

        //2.发送消息
        // 未指定分区发送
        kafkaTemplate.send("thermal_runaway_full_date_test", msg);
        // 指定分区发送
        //kafkaTemplate.send(TOPIC_NAME, 0, String.valueOf(order.getOrderId()), JSON.toJSONString(order));

    }
}
