package com.micsig.tbook.tbookscope.config;


import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Sample.MemDepthFactory;

/**
 * Created by zhuzh on 2018-12-5.
 */

public class ScopeConfig {

    private static IConfig config = null;

    public static IConfig getConfig(){
        if(config == null){
            config = Config();
        }
        return config;
    }

    private static IConfig Config(){
        config = new SmartTO1000Config( MemDepthFactory.MEM_DEPTH_1800M);
        return config;
    }
}
