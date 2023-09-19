package org.example.Service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.Service.XiaomiWebDataService;
import org.example.constants.DataFormat;
import org.example.constants.UserAgent;
import org.example.constants.XiaomiConnectParameter;
import org.example.grmsapi.CommonResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class XiaomiWebDataServiceImpl implements XiaomiWebDataService {

    @Override
    public CommonResult<?> getXiaomiData() throws IOException {
        JSONObject object = JSONObject.parseObject(DataFormat.JSON_FORMAT);
        String afterParameter = "1695103580826";
        int i = 0;
        int dataNum = 0, total = 10;
        while (dataNum < total) {
            Connection connect = Jsoup.connect(XiaomiConnectParameter.MIUI_FRONT_PARA +
                    afterParameter +
                    XiaomiConnectParameter.MIUI_BACK_PARA);

            for (String key : object.keySet()) {
                connect.data(key, object.get(key).toString());
            }

            // 发起请求并接受响应
            Connection connection = connect
                    // 防止 UnsupportedMimeTypeException 异常
                    .ignoreContentType(true)
                    // 伪装
                    .userAgent(UserAgent.USER_AGENT);
            Connection.Response response = connection.execute();

            // 解析响应体
            JSONObject responseJson = JSONObject.parseObject(response.body());
            if (Integer.valueOf(responseJson.get("code").toString()) != 200) {
                return CommonResult.failed("API调用失败，错误代码为" + responseJson.get("code"));
            }
            JSONObject entity = JSONObject.parseObject(responseJson.get("entity").toString());
            JSONObject jsonObject = JSONObject.parseObject(entity.toString());
            while (total == 10) {
                total = Integer.valueOf(jsonObject.get("total").toString());
            }
            afterParameter = jsonObject.get("after").toString();
            JSONArray dataArray = JSONArray.parseArray(jsonObject.get("records").toString());
            dataNum += dataArray.size();
        }

        return CommonResult.success("API调用成功！");
    }
}
