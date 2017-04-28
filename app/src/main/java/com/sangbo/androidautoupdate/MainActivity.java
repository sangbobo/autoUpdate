package com.sangbo.androidautoupdate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sangbo.autoupdate.CheckVersion;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckVersion.update(this);
        if(CheckVersion.isMinimumRunLimit(this)){
            //如果当前版本小于服务器最低运行版本，可以禁用当前页面的某些功能。
            //比如不初始化view等等
        }


    }
}
