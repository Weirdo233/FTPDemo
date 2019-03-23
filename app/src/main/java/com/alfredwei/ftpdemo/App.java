package com.alfredwei.ftpdemo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.guoxiaoxing.phoenix.core.listener.ImageLoader;
import com.guoxiaoxing.phoenix.picker.Phoenix;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Phoenix.config()
                .imageLoader(new ImageLoader() {
                    @Override
                    public void loadImage(Context context, ImageView imageView, String imagePath, int type) {
                        Glide.with(context)
                                .load(imagePath)
                                .into(imageView);
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
