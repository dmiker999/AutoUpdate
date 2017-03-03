package com.example.xiao.autoupdate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.xiao.autoupdate.update.UpdateService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkVersion();//应用已启动就开始更新
    }

    public void checkVersion(){

    }

    public void btnAutoUpdate(View view){
        checkVersion();//点击按钮时才更新

        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra("apkUrl","");
        startService(intent);

    }
}
