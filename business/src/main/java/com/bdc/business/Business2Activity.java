package com.bdc.business;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bdc.annotation.BRouter;

@BRouter(path = "/business/Business2Activity")
public class Business2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business2);
    }
}
