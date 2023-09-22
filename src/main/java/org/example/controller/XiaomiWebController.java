package org.example.controller;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.Service.XiaomiCommentDataService;
import org.example.Service.XiaomiWebDataService;
import org.example.constants.DataBasePathConstants;
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
    private XiaomiWebDataService xiaomiWebDataService;

    @Resource
    private XiaomiCommentDataService xiaomiCommentDataService;

    @GetMapping("getXiaomiData")
    public CommonResult<?> getData() throws Exception {
        return xiaomiWebDataService.getMIUIData();
    }

    @GetMapping("getXiaomiCommentData")
    public CommonResult<?> getCommentData() throws Exception {
        return xiaomiCommentDataService.getXiaomiMainCommentData(DataBasePathConstants.MIUI_PATH);
    }

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("send")
    public void sendMsg() {

        String msg = "12345";

        //2.发送消息
        // 未指定分区发送
        kafkaTemplate.send("thermal_runaway_full_date_test", msg);
        // 指定分区发送
        //kafkaTemplate.send(TOPIC_NAME, 0, String.valueOf(order.getOrderId()), JSON.toJSONString(order));

    }
}
