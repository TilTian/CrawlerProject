package org.example.Service;


import org.example.grmsapi.CommonResult;

import java.io.IOException;


public interface XiaomiWebDataService {

    public CommonResult<?> getMIUIData() throws IOException;
}