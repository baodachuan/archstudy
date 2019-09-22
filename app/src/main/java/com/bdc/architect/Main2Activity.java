package com.bdc.architect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bdc.annotation.BRouter;

@BRouter(path="/app/Main2Activity")
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}
