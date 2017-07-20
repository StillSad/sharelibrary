package com.thinkwage.sharelibrary;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;


import java.util.ArrayList;

/**
 * Created by ICE on 2017/6/16.
 */

public class ShareUtil {
    //广播
    public static final String SHARE_RECEIVER = "share.receiver";
    //分享结果
    public static final String SHARE_RESULT = "share.result";
    //分享成功
    public static final String SHARE_COMPLETE = "share.complete";
    //分享失败
    public static final String SHARE_ERROR = "share.error";
    //分享取消
    public static final String SHARE_CANCEL = "share.cancel";
    //分享类型
    public static final String SHARE_TYPE = "share.type";
    //分享到QQ
    public static final String SHARE_TO_QQ = "share.qq";
    //分享到QQ空间
    public static final String SHARE_TO_QZONE = "share.qzone";
    //分享到微信好友（聊天界面）
    public static final String SHARE_TO_SESSION = "share.session";
    //分享到朋友圈
    public static final String SHARE_TO_TIMELINE = "share.timeline";
    private static ShareUtil mShareUtil;
    private IWXAPI mIwxapi;
    private Application mApplication;
    private Tencent mTencent;
    private String logoUrl;
    private Bundle mMetaData;
    private ShareUtil() {
    }

    public static ShareUtil getInstance() {
        if (mShareUtil == null) {
            synchronized ( ShareUtil.class) {
                if (mShareUtil == null) {
                    mShareUtil = new ShareUtil();
                }
            }
        }
        return mShareUtil;
    }

    public void initialize(Application application) {
        this.mApplication = application;
        //微信分享初始化
        String wxAppId = getMetaData("WX_APP_ID");
        mIwxapi = WXAPIFactory.createWXAPI(application, wxAppId, true);
        mIwxapi.registerApp(wxAppId);
        //QQ分享初始化
        String qqAppId = getMetaData("QQ_APP_ID");
        mTencent = Tencent.createInstance(qqAppId, application);

        logoUrl = getMetaData("SHARE_LOGO_URL");
    }


    public String getMetaData(String key) {
        String msg = "";
        try {
            if (mMetaData == null) {
                ApplicationInfo appInfo = mApplication.getPackageManager().getApplicationInfo(
                        mApplication.getPackageName(), PackageManager.GET_META_DATA);
                mMetaData = appInfo.metaData;
            }
            msg = mMetaData.getString(key).replace("sharelib","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            return msg;
        }
    }

    //分享窗口
    public void showShareDialog(final Activity activity, final String title, final String info, final String url, final AShareListener aShareListener) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_share, null);
        final Dialog dialog = new Dialog(activity, R.style.common_dialog);
//        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.linearLayout_cancel);
        LinearLayout linearLayout_qq = (LinearLayout) view.findViewById(R.id.linearLayout_qq);
        LinearLayout linearLayout_qq_room = (LinearLayout) view.findViewById(R.id.linearLayout_qq_empty);
        LinearLayout linearLayout_wechat = (LinearLayout) view.findViewById(R.id.linearLayout_wechat);
        LinearLayout linearLayout_wechat_friend = (LinearLayout) view.findViewById(R.id.linearLayout_wechat_friend);
        dialog.setContentView(view);

        linearLayout_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aShareListener.setType(ShareUtil.SHARE_TO_QQ);
                ShareUtil.getInstance().shareToQQ(activity, title, info, url,aShareListener);
//                clearLuckyId(activity);
                dialog.dismiss();
            }
        });
        linearLayout_qq_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aShareListener.setType(ShareUtil.SHARE_TO_QQ);
                ShareUtil.getInstance().shareToQzone(activity, title, info, url,aShareListener);
//                clearLuckyId(activity);
                dialog.dismiss();
            }
        });
        linearLayout_wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWXAPIEventHandler.type = ShareUtil.SHARE_TO_SESSION;
                AWXAPIEventHandler.activityName = activity.getClass().getSimpleName();
                ShareUtil.getInstance().sendWXShare(SendMessageToWX.Req.WXSceneSession, activity, title, info, url);
//                clearLuckyId(activity);
                dialog.dismiss();
            }
        });
        linearLayout_wechat_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWXAPIEventHandler.type = ShareUtil.SHARE_TO_TIMELINE;
                AWXAPIEventHandler.activityName = activity.getClass().getSimpleName();
                ShareUtil.getInstance().sendWXShare(SendMessageToWX.Req.WXSceneTimeline, activity, title, info, url);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = AbsListView.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    //分享窗口2
    public void showShareDeclareDialog(final Activity activity, final String title, final String info, final String url, final AShareListener aShareListener) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_share, null);
        final Dialog dialog = new Dialog(activity, R.style.common_dialog);
        dialog.setCancelable(false);
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.linearLayout_cancel);
        LinearLayout linearLayout_qq = (LinearLayout) view.findViewById(R.id.linearLayout_qq);
        LinearLayout linearLayout_qq_room = (LinearLayout) view.findViewById(R.id.linearLayout_qq_empty);
        LinearLayout linearLayout_wechat = (LinearLayout) view.findViewById(R.id.linearLayout_wechat);
        LinearLayout linearLayout_wechat_friend = (LinearLayout) view.findViewById(R.id.linearLayout_wechat_friend);
        dialog.setContentView(view);
        linearLayout_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtil.getInstance().shareToQQ(activity, title, info, url,aShareListener);
                dialog.dismiss();
            }
        });
        linearLayout_qq_room.setVisibility(View.GONE);
        linearLayout_wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtil.getInstance().sendWXShare(SendMessageToWX.Req.WXSceneSession, activity, title, info, url);
                dialog.dismiss();
            }
        });
        linearLayout_wechat_friend.setVisibility(View.GONE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = AbsListView.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }



    /*
   * 腾讯分享发送内容
   */
    public void shareToQQ(final Activity activity, String title, String info, String url, IUiListener iUiListener) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//必填
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, info);//选填
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);//必填
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, logoUrl);
        // params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
        // QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        if (iUiListener != null) {
            mTencent.shareToQQ(activity, params, iUiListener);
        } else {
            mTencent.shareToQQ(activity, params, new IUiListener() {

                @Override
                public void onError(UiError arg0) {
//                    ToastUtils.show(activity, "取消分享");
                }

                @Override
                public void onComplete(Object arg0) {
//                    ToastUtils.show(activity, "分享完成");
                }

                @Override
                public void onCancel() {
//                    ToastUtils.show(activity, "分享错误");
                }
            });
        }


    }


    // 分享到QQ空间
    public void shareToQzone(final Activity activity, String title, String info, String url, IUiListener iUiListener) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, info);//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);//必填
        ArrayList<String> mList = new ArrayList<String>();
        mList.add(logoUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mList);
        if (iUiListener != null) {
            mTencent.shareToQzone(activity, params, iUiListener);
        } else {
            mTencent.shareToQzone(activity, params, new IUiListener() {

                @Override
                public void onError(UiError arg0) {
//                    ToastUtils.show(activity, "取消分享");
                }

                @Override
                public void onComplete(Object arg0) {
//                    ToastUtils.show(activity, "分享完成");
                }

                @Override
                public void onCancel() {
//                    ToastUtils.show(activity, "分享错误");
                }
            });
        }


    }

    /*
    * 微信实现分享
    */
    public void sendWXShare(int scene, Context context, String title, String info, String url) {
        WXWebpageObject textObject = new WXWebpageObject();
        textObject.webpageUrl = url;
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.title = title;
        mediaMessage.description = info;
        mediaMessage.mediaObject = textObject;
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.share);
        mediaMessage.setThumbImage(thumb);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = System.currentTimeMillis() + "个税管家";
        req.message = mediaMessage;
        req.scene = scene;
        mIwxapi.sendReq(req);
    }


    public QQShareListener getShareListener(Activity activity) {
        return new QQShareListener(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data,AShareListener aShareListener) {
        Tencent.onActivityResultData(requestCode, resultCode, data, aShareListener);
    }
}
