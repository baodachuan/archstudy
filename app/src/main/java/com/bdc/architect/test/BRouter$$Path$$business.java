package com.bdc.architect.test;

import com.bdc.annotation.RouterBean;
import com.bdc.brouter_core.IRouteLoadPath;
import com.bdc.business.BusinessActivity;

import java.util.HashMap;
import java.util.Map;

public class BRouter$$Path$$business implements IRouteLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String, RouterBean> pathMap=new HashMap<>();
        pathMap.put("/business/BusinessActivity",
                RouterBean.create(RouterBean.Type.ACITIVTY,
                        BusinessActivity.class,
                        "/business/BusinessActivity",
                        "business"
                        ));
        return pathMap;
    }
}
