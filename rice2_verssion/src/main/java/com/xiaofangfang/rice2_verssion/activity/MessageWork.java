package com.xiaofangfang.rice2_verssion.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xiaofangfang.rice2_verssion.ParentActivity;
import com.xiaofangfang.rice2_verssion.R;
import com.xiaofangfang.rice2_verssion.view.CommandBar;

public class MessageWork extends ParentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.message_work);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initView() {
        CommandBar com = findViewById(R.id.commandBar);
        com.setTitle("营业厅");
    }
}
