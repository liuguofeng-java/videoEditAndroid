package com.video.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.video.activity.MainActivity;


/**
 * @author liuguofeng
 * @date 2022/11/15 10:03
 **/
public abstract class BaseActivity extends AppCompatActivity {

    private long timeMillis;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * 跳转页面
     *
     * @param packageContext A Context of the application package implementing
     *                       this class.
     * @param cls            The class name to set, equivalent to
     *                       <code>setClassName(context, cls.getName())</code>.
     */
    protected void startActivity(Context packageContext, Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(packageContext, cls);
        startActivity(intent);
    }

    /**
     * 提示框
     *
     * @param text 提示内容
     */
    protected void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 按两下返回退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!(this.getBaseContext() instanceof MainActivity)) {
            return super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - timeMillis) > 2000) {
                timeMillis = System.currentTimeMillis();
            } else {
                moveTaskToBack(true);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
