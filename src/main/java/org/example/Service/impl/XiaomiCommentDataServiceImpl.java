package org.example.Service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Entity.XiaomiFollowCommentDataEntity;
import org.example.Entity.XiaomiMainCommentDataEntity;
import org.example.Service.XiaomiCommentDataService;
import org.example.Utils.DateUtils;
import org.example.Utils.ExcelUtils;
import org.example.Utils.GetHeaderUtils;
import org.example.constants.DataBasePathConstants;
import org.example.constants.UserAgentConstants;
import org.example.constants.XiaomiRequestParameter;
import org.example.constants.XiaomiSheetNameConstants;
import org.example.grmsapi.CommonResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class XiaomiCommentDataServiceImpl implements XiaomiCommentDataService {
    @Override
    public CommonResult<?> getXiaomiMainCommentData(String sourceFilePath) throws IOException, InterruptedException {
        if (sourceFilePath == "" || sourceFilePath == null) {
            sourceFilePath = DataBasePathConstants.MIUI_PATH;
        }
        File file = new File(sourceFilePath);
        if (!file.exists()) {
            return CommonResult.failed("Api调用失败，路径为空，请确认路径名正确！");
        }
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = workbook.getSheet(XiaomiSheetNameConstants.MIUI_SHEET_NAME);
        if (sheet == null) {
            return CommonResult.failed("Api调用失败，sheet为空，请确认路径或sheet名称正确！");
        }
        int rowIndex = 1;
        String collectTime = DateUtils.timeStamp2Date(new Date().getTime(),"");
        File resultFile = new File(DataBasePathConstants.XIAOMI_MAIN_COMMENT_PATH);
        if (!resultFile.exists()) {
            ExcelUtils.createExcelIfNotExists(DataBasePathConstants.XIAOMI_MAIN_COMMENT_PATH);
            ExcelUtils.setEntityHeader(DataBasePathConstants.XIAOMI_MAIN_COMMENT_PATH, XiaomiSheetNameConstants.XIAOMI_MAIN_COMMENT_SHEET_NAME, GetHeaderUtils.getXiaomiMainCommentDataHeader());
        }

        while (rowIndex <= sheet.getLastRowNum()) {
            String afterPara;
            int commentCnt = 0;//记录主贴数量
            XSSFRow row = sheet.getRow(rowIndex);
            String postId = row.getCell(GetHeaderUtils.getXiaomiDataHeader().indexOf("PostId")).getStringCellValue();
            boolean isLastPage = false;
            while (!isLastPage) {
                afterPara = commentCnt == 0 ? "" : String.valueOf(commentCnt += 10);
                //建立连接
                Connection connect = Jsoup.connect(XiaomiRequestParameter.COMMENT_FONT_PARA + "&postId=" + postId +
                        "&after=" + afterPara + XiaomiRequestParameter.COMMENT_BACK_PARA);
                Connection.Response response = connect.ignoreContentType(true)
                        .userAgent(UserAgentConstants.USER_AGENT).execute();
                //解析响应体
                JSONObject responseJson = JSONObject.parseObject(response.body());
                if (Integer.valueOf(responseJson.get("code").toString()) != 200) {
                    return CommonResult.failed("API调用失败，" + responseJson.get("message"));
                }
                JSONObject entityObject = JSONObject.parseObject(responseJson.getJSONObject("entity").toString());
                JSONArray records = JSONArray.parseArray(entityObject.getJSONArray("records").toString());
                int recordSize = records.size();
                if (recordSize == 0) {
                    break;
                }
                commentCnt+= recordSize;
                List<XiaomiMainCommentDataEntity> resultData = XiaomiCommentDataEtl(records, collectTime);
                writeXiaomiMainCommentExcel(resultData,DataBasePathConstants.XIAOMI_MAIN_COMMENT_PATH);
                isLastPage = commentCnt % 10 != 0;//请求到最后一页跳出循环
            }
            rowIndex++;
        }

        return null;
    }

    @Override
    public CommonResult<?> getXiaomiMainCommentData(String filePath, String startPostId) {
        return null;
    }

    private List<XiaomiMainCommentDataEntity> XiaomiCommentDataEtl(JSONArray dataArray, String collectTime) {
        if (dataArray.size() > 0) {
            List<XiaomiMainCommentDataEntity> resultList = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                XiaomiMainCommentDataEntity commentEntity = new XiaomiMainCommentDataEntity();
                JSONObject dataObject = dataArray.getJSONObject(i);
                JSONObject authorObject = JSONObject.parseObject(dataObject.getJSONObject("author").toString());
                String userId = authorObject.getString("userId");
                //跳过广告
                if (!userId.equals("0")) {
                    commentEntity.setAuthorId(userId);
                    commentEntity.setAuthorName(authorObject.getString("name"));
                    JSONObject levelInfoObject = JSONObject.parseObject(authorObject.getJSONObject("userGrowLevelInfo").toString());
                    commentEntity.setAuthorLevel(levelInfoObject.getInteger("level"));
                    commentEntity.setAuthorTitle(levelInfoObject.getString("title"));
                    commentEntity.setCommentId(dataObject.getString("commentId"));
                    commentEntity.setSubjectId(dataObject.getString("subjectId"));
                    JSONArray replyObjects = dataObject.getJSONArray("reply");
                    commentEntity.setReply(replyDataEtl(replyObjects, collectTime));
                    commentEntity.setIpRegion(dataObject.getString("ipRegion"));
                    commentEntity.setCommentText(dataObject.getString("text"));
                    commentEntity.setSupportNum(dataObject.getInteger("supportNum"));
                    commentEntity.setPublishDate(DateUtils.timeStamp2Date(dataObject.getLong("time"), ""));
                    commentEntity.setCollectTime(collectTime);
                    resultList.add(commentEntity);
                }
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    private List<XiaomiFollowCommentDataEntity> replyDataEtl(JSONArray replyObjects, String collectTime) {
        if (replyObjects.size() > 0) {
            List<XiaomiFollowCommentDataEntity> resultList = new ArrayList<>();
            for (int i = 0; i < replyObjects.size(); i++) {
                XiaomiFollowCommentDataEntity followCommentEntity = new XiaomiFollowCommentDataEntity();
                JSONObject replyObject = JSONObject.parseObject(replyObjects.getJSONObject(i).toString());
                JSONObject authorObject = JSONObject.parseObject(replyObject.getJSONObject("author").toString());
                followCommentEntity.setAuthorId(authorObject.getString("userId"));
                followCommentEntity.setAuthorName(authorObject.getString("name"));
                JSONObject levelInfoObject = JSONObject.parseObject(authorObject.get("userGrowLevelInfo").toString());
                followCommentEntity.setAuthorLevel(levelInfoObject.getInteger("level"));
                followCommentEntity.setAuthorTitle(levelInfoObject.getString("title"));
                followCommentEntity.setSubjectId(replyObject.getString("subjectId"));
                followCommentEntity.setSourceId(replyObject.getString("sourceId"));
                followCommentEntity.setSourceUserId(replyObject.getString("sourceUserId"));
                followCommentEntity.setIpRegion(replyObject.getString("ipRegion"));
                followCommentEntity.setCommentText(replyObject.getString("text"));
                followCommentEntity.setSupportNum(replyObject.getInteger("supportNum"));
                followCommentEntity.setPublishDate(DateUtils.timeStamp2Date(replyObject.getLong("time"), ""));
                followCommentEntity.setCollectTime(collectTime);
                resultList.add(followCommentEntity);
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    private void writeXiaomiMainCommentExcel(List<XiaomiMainCommentDataEntity> dataList, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
            XSSFSheet sheet = workbook.getSheet(XiaomiSheetNameConstants.XIAOMI_MAIN_COMMENT_SHEET_NAME);
            int rowNum = sheet.getLastRowNum() + 1;

            for (int j = 0; j < dataList.size(); j++) {
                XSSFRow sheetRow = sheet.createRow(rowNum++);
                XiaomiMainCommentDataEntity dataEntity = dataList.get(j);
                for (int k = 0; k < GetHeaderUtils.getXiaomiMainCommentDataHeader().size(); k++) {
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
                            sheetRow.createCell(k).setCellValue(dataEntity.getReply().size());
                            break;
                        }
                        case 7 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getIpRegion());
                            break;
                        }
                        case 8 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getCommentText());
                            break;
                        }
                        case 9 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getSupportNum());
                            break;
                        }
                        case 10 : {
                            sheetRow.createCell(k).setCellValue(dataEntity.getPublishDate());
                            break;
                        }
                        case 11: {
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
        }
    }
}
