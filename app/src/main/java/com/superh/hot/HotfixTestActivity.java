package com.superh.hot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.meituan.robust.patch.annotaion.Add;
import com.meituan.robust.patch.annotaion.Modify;

/**
 * 2018/6/29.
 *
 * @author huchao
 */
public class HotfixTestActivity extends AppCompatActivity {


    public static void start(Context context){
        Intent intent = new Intent(context,HotfixTestActivity.class);
        context.startActivity(intent);
    }

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textView =  findViewById(R.id.text_view);

        initData();
    }


//    public void initData() {
//        textView.setText("我还没有打补丁");
//    }

    @Modify
    public void initData() {
        textView.setText(getTextString());
        textView.setTextColor(Color.parseColor("#990033"));
    }

    @Add
    public String getTextString(){
        return "第二次打补丁了";
    }
}
