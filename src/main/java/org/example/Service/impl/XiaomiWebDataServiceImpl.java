package org.example.Service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.example.Entity.XiaomiDataEntity;
import org.example.Service.XiaomiWebDataService;
import org.example.Utils.DateUtils;
import org.example.Utils.GetHeaderUtils;
import org.example.constants.*;
import org.example.grmsapi.CommonResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class XiaomiWebDataServiceImpl implements XiaomiWebDataService {

    @Override
    public CommonResult<?> getMIUIData() throws IOException {
        //依靠获取的新的afterParameter来循环请求
        String afterParameter = "";
        //起始值数据量为0，总数为第一次请求获取到的10
        int dataNum = 0, total = 10;//测试total值为50
        while (dataNum < total) {
            Connection connect = Jsoup.connect(XiaomiRequestParameter.MIUI_FRONT_PARA +
                    afterParameter +
                    XiaomiRequestParameter.MIUI_BACK_PARA);

            // 发起请求并接受响应
            Connection connection = connect
                    // 防止 UnsupportedMimeTypeException 异常
                    .ignoreContentType(true)
                    // 伪装
                    .userAgent(UserAgentConstants.USER_AGENT);
            Connection.Response response = connection.execute();

            // 解析响应体
            JSONObject responseJson = JSONObject.parseObject(response.body());
            if (Integer.valueOf(responseJson.get("code").toString()) != 200) {
                return CommonResult.failed("API调用失败，错误代码为" + responseJson.get("code"));
            }
            JSONObject entity = JSONObject.parseObject(responseJson.get("entity").toString());
            JSONObject jsonObject = JSONObject.parseObject(entity.toString());
//            测试时关闭total赋值
            while (total == 10) {
                total = Integer.valueOf(jsonObject.get("total").toString());
            }
            afterParameter = jsonObject.get("after").toString();
            JSONArray dataArray = JSONArray.parseArray(jsonObject.get("records").toString());
            dataNum += dataArray.size();
            String collectTime = DateUtils.timeStamp2Date(new Date().getTime(),"");
            List<XiaomiDataEntity> resultData = dataETL(dataArray,collectTime);

            createExcelIfNotExists(DataBasePathConstants.MIUI_PATH);
            try {
                writeMIUIExcel(resultData, DataBasePathConstants.MIUI_PATH);
            } catch (InvalidFormatException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return CommonResult.success(afterParameter, "API调用成功！");

    }

    private List<XiaomiDataEntity> dataETL(JSONArray dataArray, String collectTime) {
        List<XiaomiDataEntity> resultData = new ArrayList<>();
        for (int i = 0; i < dataArray.size(); i++) {
            XiaomiDataEntity dataEntity = new XiaomiDataEntity();//临时实体
            JSONObject dataObject = dataArray.getJSONObject(i);
            JSONObject authorObject = JSONObject.parseObject(dataObject.get("author").toString());
            dataEntity.setUserId(authorObject.getString("userId"));
            dataEntity.setUserName(authorObject.getString("name"));
            JSONObject levelInfoObject = JSONObject.parseObject(authorObject.get("userGrowLevelInfo").toString());
            dataEntity.setLevel(levelInfoObject.getInteger("level"));
            dataEntity.setUserTitle(levelInfoObject.getString("title"));
            dataEntity.setTextTitle(dataObject.getString("title"));
            dataEntity.setSummary(dataObject.getString("summary"));
            JSONObject boardObject = JSONArray.parseArray(dataObject.get("boards").toString()).getJSONObject(0);
            dataEntity.setBoardId(boardObject.getString("boardId"));
            dataEntity.setBoardName(boardObject.getString("boardName"));
            dataEntity.setUrl(PostUrlParameter.XIAOMI_POST_FONT_PARA + "&postId=" + dataObject.getString("id") + "&fromBoardId=" + dataEntity.getBoardId());
            dataEntity.setIpRegion(dataObject.getString("ipRegion"));
            dataEntity.setLikeCnt(dataObject.getInteger("likeCnt"));
            dataEntity.setCommentCnt(dataObject.getInteger("commentCnt"));
            dataEntity.setPublishDate(DateUtils.timeStamp2Date(dataObject.getLong("createTime"),""));
            dataEntity.setCollectTime(collectTime);
            resultData.add(dataEntity);
        }
        return resultData;
    }

    private void createExcelIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
            List<String> headList = GetHeaderUtils.getHeader();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(XiaomiSheetNameConstants.MIUI_SHEET_NAME);
            for (int i = 0; i < headList.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            XSSFCellStyle style = setHeadCellStyle(workbook);
            XSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headList.size(); i++) {
                XSSFCell cell = row.createCell(i);
                cell.setCellStyle(style);
                cell.setCellValue(headList.get(i));
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
        }
    }

    private XSSFCellStyle setHeadCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }


    private void writeMIUIExcel(List<XiaomiDataEntity> dataList, String filePath) throws IOException, InvalidFormatException, InterruptedException {
        File file = new File(filePath);
        if(file.exists()) {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
            XSSFSheet sheet = workbook.getSheet(XiaomiSheetNameConstants.MIUI_SHEET_NAME);
            int rowNum = sheet.getLastRowNum() + 1;

            for (int j = 0; j < dataList.size(); j++) {
                XSSFRow sheetRow = sheet.createRow(rowNum++);
                XiaomiDataEntity dataEntity = dataList.get(j);
                for (int k = 0; k < GetHeaderUtils.getHeader().size(); k++) {
                    switch (k) {
                        case 0 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getUserId());
                            break;
                        }
                        case 1 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getUserName());
                            break;
                        }
                        case 2 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getLevel());
                            break;
                        }
                        case 3 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getUserTitle());
                            break;
                        }
                        case 4 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getTextTitle());
                            break;
                        }
                        case 5 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getSummary());
                            break;
                        }
                        case 6 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getBoardId());
                            break;
                        }
                        case 7 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getBoardName());
                            break;
                        }
                        case 8 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getUrl());
                            break;
                        }
                        case 9 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getIpRegion());
                            break;
                        }
                        case 10 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getLikeCnt());
                            break;
                        }
                        case 11 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getCommentCnt());
                            break;
                        }
                        case 12 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getPublishDate());
                            break;
                        }
                        case 13 :{
                            sheetRow.createCell(k).setCellValue(dataEntity.getCollectTime());
                            break;
                        }
                    }
                }
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
//            Thread.sleep(50);
        }
    }

}
