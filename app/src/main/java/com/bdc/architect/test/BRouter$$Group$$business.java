package com.bdc.architect.test;

import com.bdc.brouter_core.IRouteLoadGroup;
import com.bdc.brouter_core.IRouteLoadPath;

import java.util.HashMap;
import java.util.Map;

public class BRouter$$Group$$business implements IRouteLoadGroup {
    @Override
    public Map<String, Class<? extends IRouteLoadPath>> loadGroup() {
        Map<String, Class<? extends IRouteLoadPath>> groupMap=new HashMap<>();
        groupMap.put("business",BRouter$$Path$$business.class);
        return groupMap;
    }
}
