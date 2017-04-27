package com.sangbo.autoupdate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import okhttp3.Call;

/**
 * app更新
 * Created by 桑博 on 16-5-19.
 */
public class CheckVersion {


    private static int mAppVersionCode = 0;
    private static Context mContext;
    private static ProgressDialog mAlertDialog;
    private static boolean mIsEnforceCheck = false;
    public static String checkUrl = "";
    public static UpdateEntity mUpdateEntity;

    public static void update(Context context){
        update(context,mIsEnforceCheck);
    }

    public static void update(Context context, final boolean isEnforceCheck){
        mContext = context;
        mIsEnforceCheck = isEnforceCheck;
        mAppVersionCode = getVersionCode(mContext);
        if(TextUtils.isEmpty(checkUrl)){
            Toast.makeText(mContext, "url不能为空，请设置url", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpUtils.get().url(checkUrl).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                if(mIsEnforceCheck)
                    Toast.makeText(mContext, "更新失败，请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                loadOnlineData(response);
            }

        });
    }

    private static void loadOnlineData(String json) {


        try {
            UpdateEntity updateEntity = new UpdateEntity(json);
            if(updateEntity == null){
                if(mIsEnforceCheck)
                    Toast.makeText(mContext, "网络信息获取失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }
            mUpdateEntity = updateEntity;
            Log.d("versionInfo",json);
            if(mAppVersionCode < mUpdateEntity.versionCode){
                //启动更新
                AlertUpdate();
            }else{
                if(mIsEnforceCheck)
                    Toast.makeText(mContext, "当前版本已经是最新版本", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    private static void AlertUpdate(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("发现更新");
        builder.setMessage("新版本:" + mUpdateEntity.versionName + "\n"
                + "版本内容:" + "\n"
                + mUpdateEntity.updateLog + "\n");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateApp();
            }
        });
        if(mAppVersionCode < mUpdateEntity.preBaselineCode){
            builder.setCancelable(false);
        }else{
            builder.setNegativeButton("取消",null);
        }
        builder.show();

    }
    private static void updateApp() {

        updateApp(false);

    }
    private static void updateApp(boolean isEnforceDown) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = getPackgeName(mContext)+mUpdateEntity.versionName +".apk";

        if(!isEnforceDown){
            File file = new File(filePath+"/"+fileName);
            if(file.exists()){
                install(file);
                return;
            }
        }

        mAlertDialog = new ProgressDialog(mContext);
        mAlertDialog.setTitle("更新ing");
        mAlertDialog.setMessage("正在下载最新版本,请稍后");
        mAlertDialog.setCancelable(false);
        mAlertDialog.setIndeterminate(true);
        mAlertDialog.show();

        OkHttpUtils.get().url(mUpdateEntity.downUrl).build().execute(
                new FileCallBack(
                        filePath,
                        fileName) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        resterAlert();
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        install(file);
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        mAlertDialog.setMessage("当前下载进度:"+(int) (100 * progress)+"%");
                    }

                    @Override
                    public void onAfter(int id) {
                        mAlertDialog.dismiss();
                    }
                });
    }


    public static void install(File file) {

        if(!checkMD5(file)){
            md5Alert();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private static void md5Alert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("\nmd5不一致，是否重新下载\n");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateApp(true);
            }
        });
        if(mAppVersionCode < mUpdateEntity.preBaselineCode){
            builder.setCancelable(false);
        }else{
            builder.setNegativeButton("取消",null);
        }
        builder.show();

    }
    private static void resterAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("\n文件下载失败，是否重试？\n");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateApp();
            }
        });
        if(mAppVersionCode < mUpdateEntity.preBaselineCode){
            builder.setCancelable(false);
        }else{
            builder.setNegativeButton("取消",null);
        }
        builder.show();
    }

    private static boolean checkMD5(File file) {

        String md5Value;
        try {
            md5Value = getMd5ByFile(file);
        } catch (FileNotFoundException e) {
            md5Value = "-1";
        }
        Log.d("md5:",md5Value);
        return md5Value.equals(mUpdateEntity.md5);


    }

    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * 获得apk版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageInfo packInfo = getPackInfo(context);
        if(packInfo!=null){
            versionCode = packInfo.versionCode;
        }
        return versionCode;
    }


    /**
     * 获得apkPackgeName
     * @param context
     * @return
     */
    public static String getPackgeName(Context context) {
        String packName = "";
        PackageInfo packInfo = getPackInfo(context);
        if(packInfo!=null){
            packName = packInfo.packageName;
        }
        return packName;
    }

    /**
     * 获得apkinfo
     * @param context
     * @return
     */
    public static PackageInfo getPackInfo(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo;
    }


}
