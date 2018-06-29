package com.superh.hot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.meituan.robust.PatchExecutor;
import com.superh.hot.hotfix.HotfixVersion;
import com.superh.hot.hotfix.PatchManipulateImp;
import com.superh.hot.hotfix.RobustCallBackImp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText versionEdit;
    private EditText pathEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        versionEdit =  findViewById(R.id.version_edit);
        pathEdit =  findViewById(R.id.path_edit);
        Button makeBtn =  findViewById(R.id.make_patch_btn);
        Button startBtn =  findViewById(R.id.start_btn);
        makeBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.make_patch_btn:
                if(TextUtils.isEmpty(versionEdit.getText().toString().trim()) || TextUtils.isEmpty(pathEdit.getText().toString().trim())){
                    return;
                }
                HotfixVersion hotfixVersion = new HotfixVersion();
                hotfixVersion.setName(versionEdit.getText().toString().trim());
                hotfixVersion.setUrl(pathEdit.getText().toString().trim());
                new PatchExecutor(this,new PatchManipulateImp(hotfixVersion),new RobustCallBackImp()).start();
                break;
            case R.id.start_btn:
                HotfixTestActivity.start(this);
                break;
        }
    }
}
