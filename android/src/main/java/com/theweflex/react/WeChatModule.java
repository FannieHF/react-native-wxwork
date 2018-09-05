package com.theweflex.react;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Files;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXFileObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import com.tencent.wework.api.IWWAPI;
import com.tencent.wework.api.IWWAPIEventHandler;
import com.tencent.wework.api.WWAPIFactory;
import com.tencent.wework.api.model.BaseMessage;
import com.tencent.wework.api.model.WWAuthMessage;
import com.tencent.wework.api.model.WWMediaConversation;
import com.tencent.wework.api.model.WWMediaFile;
import com.tencent.wework.api.model.WWMediaImage;
import com.tencent.wework.api.model.WWMediaLink;
import com.tencent.wework.api.model.WWMediaMergedConvs;
import com.tencent.wework.api.model.WWMediaText;
import com.tencent.wework.api.model.WWMediaVideo;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class WeChatModule extends ReactContextBaseJavaModule implements IWWAPIEventHandler {
    private String appId;
    private String agentId;
    private String schema;

    private IWWAPI wwapi = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "WeChat API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";

    public WeChatModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "react-native-wxwork";
    }

    /**
     * fix Native module WeChatModule tried to override WeChatModule for module name react-native-wxwork.
     * If this was your intention, return true from WeChatModule#canOverrideExistingModule() bug
     * @return
     */
    public boolean canOverrideExistingModule(){
        return true;
    }

    private static ArrayList<WeChatModule> modules = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
        modules.add(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (wwapi != null) {
            wwapi = null;
        }
        modules.remove(this);
    }

    public static void handleIntent(Intent intent) {
        for (WeChatModule mod : modules) {
            mod.wwapi.handleIntent(intent, mod);
        }
    }
//
//    @ReactMethod
//    public void registerApp(String appid, Callback callback) {
//        this.appId = appid;
//        wxapi = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), appid, true);
//        callback.invoke(null, wxapi.registerApp(appid));
//    }

    @ReactMethod
    public void registerWWApp(String schema, Callback callback) {
        this.schema = schema;
        wwapi = WWAPIFactory.createWWAPI(this.getReactApplicationContext().getBaseContext());
        callback.invoke(null, wwapi.registerApp(schema));
    }

    @ReactMethod
    public void isAppInstalled(Callback callback) {
        if (wwapi == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, wwapi.isWWAppInstalled());
    }

    @ReactMethod
    public void sendAuthRequest(String schema, String appid, String agentid, Callback callback) {
        if (wwapi == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        WWAuthMessage.Req req = new WWAuthMessage.Req();
        req.sch = schema;
        req.appId = appid;
        req.agentId = agentid;
        req.state = "dd";
        callback.invoke(null, wwapi.sendMessage(req));
    }

    @Override
    public void handleResp(BaseMessage baseResp) {
        WritableMap map = Arguments.createMap();
//        map.putInt("errCode", baseResp.errCode);
//        map.putString("errStr", baseResp.errStr);
//        map.putString("openId", baseResp.openId);
//        map.putString("transaction", baseResp.transaction);

        if (baseResp instanceof WWAuthMessage.Resp) {
            WWAuthMessage.Resp resp = (WWAuthMessage.Resp) (baseResp);

            map.putString("type", "SendAuth.Resp");
            map.putInt("errCode", resp.errCode);
            map.putString("code", resp.code);
            map.putString("state", resp.state);
        }

        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("SendAuth.Resp", map);
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

    private interface MediaObjectCallback {
        void invoke(@Nullable WWMediaText.WWMediaObject mediaObject);
    }

}
