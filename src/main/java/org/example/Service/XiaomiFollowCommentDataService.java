package org.example.Service;

import org.example.grmsapi.CommonResult;

import java.io.IOException;

public interface XiaomiFollowCommentDataService {

    public CommonResult<?> getXiaomiFollowCommentData(String sourceFilePath) throws IOException, InterruptedException;
}
