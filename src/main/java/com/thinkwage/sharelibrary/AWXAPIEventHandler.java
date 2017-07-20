package com.thinkwage.sharelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by ICE on 2017/6/22.
 */

public abstract class AWXAPIEventHandler extends Activity implements IWXAPIEventHandler {
    public static String type;
    public static String activityName;
    private IWXAPI iwxapi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        iwxapi = WXAPIFactory.createWXAPI(this, ShareUtil.getInstance().getMetaData("WX_APP_ID"), false);
        iwxapi.handleIntent(getIntent(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResp(BaseResp arg0) {
        // TODO Auto-generated method stub
        switch (arg0.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                // 分享成功
                sendReceiver(ShareUtil.SHARE_COMPLETE);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                // 分享取消
                sendReceiver(ShareUtil.SHARE_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                //分享失败
                sendReceiver(ShareUtil.SHARE_ERROR);
                break;
        }

    }

    public void sendReceiver(String result) {
        Intent intent = new Intent();
        intent.setAction(ShareUtil.SHARE_RECEIVER + "." + activityName);
        intent.putExtra(ShareUtil.SHARE_TYPE, type);
        intent.putExtra(ShareUtil.SHARE_RESULT, result);
        sendBroadcast(intent);
        finish();
    }
}
