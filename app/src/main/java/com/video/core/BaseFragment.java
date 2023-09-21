package com.video.core;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;


/**
 * @author liuguofeng
 * @date 2022/11/15 17:29
 **/
public class BaseFragment extends Fragment {


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

}
