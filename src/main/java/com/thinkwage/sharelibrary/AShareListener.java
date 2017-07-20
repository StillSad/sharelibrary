package com.thinkwage.sharelibrary;

import com.tencent.tauth.IUiListener;

/**
 * Created by ICE on 2017/6/22.
 */

public abstract class AShareListener implements IUiListener {
    private String type;
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
