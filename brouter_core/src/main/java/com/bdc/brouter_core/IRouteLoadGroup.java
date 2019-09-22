package com.bdc.brouter_core;

import java.util.Map;

public interface IRouteLoadGroup {
    Map<String,Class<? extends IRouteLoadPath>> loadGroup();
}
