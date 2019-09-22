package com.bdc.brouter_core;

import com.bdc.annotation.RouterBean;

import java.util.Map;

public interface IRouteLoadPath {
    Map<String, RouterBean> loadPath();
}
