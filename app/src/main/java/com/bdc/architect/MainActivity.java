package com.bdc.architect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bdc.annotation.BRouter;
import com.bdc.annotation.RouterBean;
import com.bdc.architect.test.BRouter$$Group$$business;
import com.bdc.brouter_core.IRouteLoadGroup;
import com.bdc.brouter_core.IRouteLoadPath;

import java.util.Map;

@BRouter(path="/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void jumpBusiness(View view) {
        IRouteLoadGroup group= new BRouter$$Group$$business();
        Map<String, Class<? extends IRouteLoadPath>> groupMap = group.loadGroup();
        Class<? extends IRouteLoadPath> business = groupMap.get("business");
        try {
            IRouteLoadPath iRouteLoadPath = business.newInstance();
            Map<String, RouterBean> pathMap = iRouteLoadPath.loadPath();
            RouterBean routerBean = pathMap.get("/business/BusinessActivity");
            Class clazz = routerBean.getClazz();
            Intent intent=new Intent(this,clazz);
            intent.putExtra("name","bdc");
            startActivity(intent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
