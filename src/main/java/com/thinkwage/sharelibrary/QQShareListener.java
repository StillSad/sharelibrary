package com.thinkwage.sharelibrary;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;


/**
 * Created by ICE on 2017/5/31.
 */

public class QQShareListener extends AShareListener {
    private Activity activity;


    public QQShareListener(Activity activity) {
        this.activity = activity;
    }


    @Override
    public void onComplete(Object o) {
        //分享到电脑{"ret":0}
        //分享到好友{"ret":0}
        //分享到空间{"ret":0}
//        ToastUtils.show(activity, "分享完成");
//        luckyAddShareChange(activity);
        sendReceiver(ShareUtil.SHARE_COMPLETE);
    }

    @Override
    public void onError(UiError uiError) {
        sendReceiver(ShareUtil.SHARE_ERROR);
//        ToastUtils.show(activity, "分享错误");
    }

    @Override
    public void onCancel() {
        sendReceiver(ShareUtil.SHARE_CANCEL);
//        ToastUtils.show(activity, "取消分享");
    }

    public void sendReceiver(String result) {
        Intent intent = new Intent();
        intent.setAction(ShareUtil.SHARE_RECEIVER + "." + activity.getClass().getSimpleName());
        intent.putExtra(ShareUtil.SHARE_TYPE, getType());
        intent.putExtra(ShareUtil.SHARE_RESULT, result);
        activity.sendBroadcast(intent);
    }
//    private void luckyAddShareChange(final Activity activity) {
//        String token = OtherTools.getInstance().getUserToken(activity);
//        String luckId = OtherTools.getInstance().getLuckyId(activity);
//        if (TextUtils.isEmpty(luckId)) return;
//        HttpClientClass.getInstance().getLuckyAddShareChange(token, luckId, new HttpListener() {
//            @Override
//            public void connectSuccess(Object object) {
//                OtherTools.getInstance().clearLuckyId(activity);
//                Intent intent = new Intent();
//                intent.setAction(StringConstant.SHARE);
//                activity.sendBroadcast(intent);
//            }
//
//            @Override
//            public void connectFailure(String failure) {
//                OtherTools.getInstance().clearLuckyId(activity);
//            }
//        });
//    }
}
