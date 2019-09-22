package com.bdc.architect.test;

import com.bdc.annotation.RouterBean;
import com.bdc.architect.MainActivity;
import com.bdc.brouter_core.IRouteLoadPath;

import java.util.HashMap;
import java.util.Map;

public class BRouter$$Path$$app implements IRouteLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String, RouterBean> pathMap=new HashMap<>();
        pathMap.put("/app/MainActivity",
                RouterBean.create(RouterBean.Type.ACITIVTY,
                        MainActivity.class,
                        "/app/MainActivity",
                        "app"
                        ));
        return pathMap;
    }
}
