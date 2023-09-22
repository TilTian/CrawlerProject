package org.example.Service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.grmsapi.CommonResult;

import java.io.IOException;

public interface XiaomiCommentDataService {

    public CommonResult<?> getXiaomiMainCommentData(String fileName) throws IOException, InvalidFormatException, InterruptedException;

    public CommonResult<?> getXiaomiMainCommentData(String fileName, String startPostId);
}
