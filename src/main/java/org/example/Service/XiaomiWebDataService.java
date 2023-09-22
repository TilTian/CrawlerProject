package org.example.Service;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.grmsapi.CommonResult;

import java.io.IOException;


public interface XiaomiWebDataService {

    public CommonResult<?> getMIUIData() throws IOException, InvalidFormatException, InterruptedException;
}
