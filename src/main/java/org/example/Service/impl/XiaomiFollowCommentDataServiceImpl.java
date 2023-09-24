package org.example.Service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Entity.XiaomiFollowCommentDataEntity;
import org.example.Service.XiaomiFollowCommentDataService;
import org.example.Utils.DateUtils;
import org.example.Utils.ExcelUtils;
import org.example.Utils.GetHeaderUtils;
import org.example.Utils.StringUtils;
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
public class XiaomiFollowCommentDataServiceImpl implements XiaomiFollowCommentDataService {
    @Override
    public CommonResult<?> getXiaomiFollowCommentData(String sourceFilePath) throws IOException, InterruptedException {
        if (StringUtils.isEmpty(sourceFilePath)) {
            sourceFilePath = DataBasePathConstants.XIAOMI_MAIN_COMMENT_PATH;
        }
        File file = new File(sourceFilePath);
        if (!file.exists()) {
            return CommonResult.failed("Api调用失败，路径为空，请确认路径名正确！");
        }
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = workbook.getSheet(XiaomiSheetNameConstants.XIAOMI_MAIN_COMMENT_SHEET_NAME);
        if (sheet == null) {
            return CommonResult.failed("Api调用失败，sheet为空，请确认路径或sheet名称正确！");
        }
        int rowIndex = 1;
        String collectTime = DateUtils.timeStamp2Date(new Date().getTime(),"");
        File resultFile = new File(DataBasePathConstants.XIAOMI_FOLLOW_COMMENT_PATH);
        if (!resultFile.exists()) {
            ExcelUtils.createExcelIfNotExists(DataBasePathConstants.XIAOMI_FOLLOW_COMMENT_PATH);
            ExcelUtils.setEntityHeader(DataBasePathConstants.XIAOMI_FOLLOW_COMMENT_PATH, XiaomiSheetNameConstants.XIAOMI_FOLLOW_COMMENT_SHEET_NAME, GetHeaderUtils.getXiaomiFollowCommentDataHeader());
        }
        int lastRowNum = sheet.getLastRowNum();
        while (rowIndex <= lastRowNum) {
            String afterPara = "";
            XSSFRow row = sheet.getRow(rowIndex++);
            double replyNum = (row.getCell(GetHeaderUtils.getXiaomiMainCommentDataHeader().indexOf("Reply")).getNumericCellValue());
            if (replyNum >= 2) {
                boolean isLastPage = false;
                //小米社区的跟帖品论的请求地址为 前参 + *after + postId + commentId + 后参
                //当after的值为空时请求会获得编号为2-11的跟帖记录
                String commentId = StringUtils.XiaomiCommentIdParaConvert(row.getCell(GetHeaderUtils.
                        getXiaomiMainCommentDataHeader().indexOf("CommentId")).getStringCellValue());
                String postId = row.getCell(GetHeaderUtils.getXiaomiMainCommentDataHeader().indexOf("SubjectId")).getStringCellValue();
                while (!isLastPage) {
                    Thread.sleep(50);
                    Connection connect = Jsoup.connect(XiaomiRequestParameter.FOLLOW_COMMENT_FONT_PARA + afterPara + postId
                            + commentId + XiaomiRequestParameter.FOLLOW_COMMENT_BACK_PARA);
                    Connection.Response response = connect.ignoreContentType(true)
                            .userAgent(UserAgentConstants.USER_AGENT).execute();
                    JSONObject responseJson = JSONObject.parseObject(response.body());
                    String responseCode = responseJson.get("code").toString();
                    if (Integer.valueOf(responseCode) != 200) {
                        String responseMessage = responseJson.getString("message");
                        if (responseMessage.equals(ResultMessageConstants.XIAOMI_COMMENT_NOT_EXISTS)) {
                            continue;
                        }
                        return CommonResult.failed("API调用失败，" + "错误代码为:" + responseCode
                                + "。"+ responseMessage);
                    }
                    JSONObject entityObject = JSONObject.parseObject(responseJson.getJSONObject("entity").toString());
                    JSONArray records = JSONArray.parseArray(entityObject.getJSONArray("records").toString());
                    if (records.size() == 0) {
                        break;//请求到空值，跟帖爬取完毕
                    }
                    List<XiaomiFollowCommentDataEntity> resultData = XiaomiFollowCommentDataEtl(records, collectTime);
                    if (resultData.size() == 10) {
                        afterPara = resultData.get(9).getCommentId();
                    } else {
                        isLastPage = true;
                    }
                    writeXiaomiCommentDataExcel(resultData,resultFile);
                }
            }
        }
        return CommonResult.success("Api调用成功");
    }

    private List<XiaomiFollowCommentDataEntity> XiaomiFollowCommentDataEtl(JSONArray dataArray, String collectTime) {
        if (dataArray.size() > 0) {
            List<XiaomiFollowCommentDataEntity> resultList = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                XiaomiFollowCommentDataEntity followCommentEntity = new XiaomiFollowCommentDataEntity();
                JSONObject dataObject = dataArray.getJSONObject(i);
                JSONObject authorObject = JSONObject.parseObject(dataObject.getJSONObject("author").toString());
                String userId = authorObject.getString("userId");
                //跳过广告
                if (!userId.equals("0")) {
                    followCommentEntity.setAuthorId(userId);
                    followCommentEntity.setAuthorName(authorObject.getString("name"));
                    JSONObject levelInfoObject = JSONObject.parseObject(authorObject.getJSONObject("userGrowLevelInfo").toString());
                    followCommentEntity.setAuthorLevel(levelInfoObject.getInteger("level"));
                    followCommentEntity.setAuthorTitle(levelInfoObject.getString("title"));
                    followCommentEntity.setCommentId(dataObject.getString("commentId"));
                    followCommentEntity.setSubjectId(dataObject.getString("subjectId"));
                    followCommentEntity.setSourceId(dataObject.getString("sourceId"));
                    followCommentEntity.setSourceId(dataObject.getString("sourceUserId"));
                    followCommentEntity.setIpRegion(dataObject.getString("ipRegion"));
                    followCommentEntity.setCommentText(dataObject.getString("text"));
                    followCommentEntity.setSupportNum(dataObject.getInteger("supportNum"));
                    followCommentEntity.setPublishDate(DateUtils.timeStamp2Date(dataObject.getLong("time"), ""));
                    followCommentEntity.setCollectTime(collectTime);
                    resultList.add(followCommentEntity);
                }
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    private void writeXiaomiCommentDataExcel(List<XiaomiFollowCommentDataEntity> dataList, File fileName) throws IOException {
        if (fileName.exists()) {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fileName));
            XSSFSheet sheet = workbook.getSheet(XiaomiSheetNameConstants.XIAOMI_MAIN_COMMENT_SHEET_NAME);
            int rowNum = sheet.getLastRowNum() + 1;

            for (int j = 0; j < dataList.size(); j++) {
                XSSFRow sheetRow = sheet.createRow(rowNum++);
                XiaomiFollowCommentDataEntity dataEntity = dataList.get(j);
                for (int k = 0; k < GetHeaderUtils.getXiaomiFollowCommentDataHeader().size(); k++) {
                    switch (k) {
                        case 0 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getAuthorId());
                            break;
                        }
                        case 1 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getAuthorName());
                            break;
                        }
                        case 2 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getAuthorLevel());
                            break;
                        }
                        case 3 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getAuthorTitle());
                            break;
                        }
                        case 4 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getCommentId());
                            break;
                        }
                        case 5 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getSubjectId());
                            break;
                        }
                        case 6 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getSourceId());
                            break;
                        }
                        case 7 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getSourceUserId());
                            break;
                        }
                        case 8 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getIpRegion());
                            break;
                        }
                        case 9 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getCommentText());
                            break;
                        }
                        case 10 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getSupportNum());
                            break;
                        }
                        case 11 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getPublishDate());
                            break;
                        }
                        case 12 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getCollectTime());
                            break;
                        }
                    }
                }
            }
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
        }
    }
}
