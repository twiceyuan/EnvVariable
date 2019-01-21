package com.twiceyuan.envvariable.sample;

import android.app.Application;
import com.twiceyuan.envvariable.EnvVariable;

public class EnvApplication extends Application {

    private EnvConfig config;

    @Override
    public void onCreate() {
        super.onCreate();
        config = EnvVariable.register(this, EnvConfig.class);
        EnvVariable.registerRefreshAction(() -> {
            // TODO: 2019/1/21 更新 EnvVariable 相关的变量
        });
    }

    public EnvConfig getConfig() {
        return config;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        EnvVariable.syncCache();
    }
}
