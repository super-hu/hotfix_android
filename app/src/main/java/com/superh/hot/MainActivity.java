package com.superh.hot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText versionEdit;

    private EditText pathEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        versionEdit =  findViewById(R.id.version_edit);
        pathEdit =  findViewById(R.id.path_edit);
        Button switchBtn = findViewById(R.id.switch_btn);
        Button makeBtn =  findViewById(R.id.make_patch_btn);
        Button startBtn =  findViewById(R.id.start_btn);
        makeBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        switchBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.make_patch_btn:

                break;
            case R.id.start_btn:
                HotfixTestActivity.start(this);
                break;
            case R.id.switch_btn:
                versionEdit.setText("");
                pathEdit.setText("");

                break;
        }
    }
}
