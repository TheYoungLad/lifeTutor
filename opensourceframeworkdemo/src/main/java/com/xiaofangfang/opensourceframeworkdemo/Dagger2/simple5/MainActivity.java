package com.xiaofangfang.opensourceframeworkdemo.Dagger2.simple5;


import android.os.Bundle;


import com.xiaofangfang.opensourceframeworkdemo.Dagger2.simple5.mode.Login;
import com.xiaofangfang.opensourceframeworkdemo.Dagger2.simple5.module.LoginModule;
import com.xiaofangfang.opensourceframeworkdemo.R;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Inject
    Login login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dagger_activity);

        /**
         * 生成 类的名字只Component的名字前面加上Dagger
         */
//        DaggerLoginComponent.builder().loginModule(new LoginModule("fang", "sddsdfh"));


        login.getPassword();

        

    }
}
