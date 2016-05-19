package com.sangbo.autoupdate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;

import java.io.File;

import okhttp3.Call;

/**
 * app更新
 * Created by sangbo on 16-5-19.
 */
public class CheckVersion {


    private static int mAppVersionCode = 0;
    private static Context mContext;
    private static ProgressDialog mAlertDialog;
    public static String checkUrl = "";

    public static void update(Context context){
        mContext = context;
        mAppVersionCode = getVersionCode(mContext);

        if(TextUtils.isEmpty(checkUrl)){
            Toast.makeText(mContext, "url不能为空，请设置url", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpUtils.get().url(checkUrl).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {

                loadOnlineData(response);

            }
        });



    }

    private static void loadOnlineData(String json) {


        try {
            UpdateEntity updateEntity = new UpdateEntity(json);

            if(mAppVersionCode < updateEntity.versionCode){
                //启动更新
                AlertUpdate(updateEntity);
            }else{
                Log.d("TAG","当前版本已经是最新版本");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private static void AlertUpdate(final UpdateEntity updateEntity){

        TextView tvMsg = new TextView(mContext);
        tvMsg.setText("新版本:" + updateEntity.versionName + "\n"
                + "版本内容:" + "\n"
                + updateEntity.updateLog + "\n");
        AlertView mAlertViewEx = new AlertView("发现更新",null
                , "取消", new String[]{"更新"}, null, mContext,
                AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                switch (position){

                    case 0:
                        updateApp(updateEntity);
                        break;
                }
            }
        });
        tvMsg.setMaxLines(15);
        tvMsg.setMovementMethod(new ScrollingMovementMethod());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 0, 20, 0);
        tvMsg.setLayoutParams(lp);
        mAlertViewEx.addExtView(tvMsg);
        mAlertViewEx.show();

    }

    private static void updateApp(final UpdateEntity updateEntity) {



        mAlertDialog = new ProgressDialog(mContext);
        mAlertDialog.setTitle("提示信息");
        mAlertDialog.setMessage("正在下载ing,请稍后");
        mAlertDialog.setCancelable(false);
        mAlertDialog.setIndeterminate(true);
        mAlertDialog.show();

        Log.d("TAG","弹出对话框，显示更新");
        OkHttpUtils.get().url(updateEntity.downUrl).build().execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), updateEntity.versionName +".apk") {
            @Override
            public void inProgress(float progress, long total) {
                mAlertDialog.setMessage("当前进度:"+(int) (100 * progress)+"%");
            }

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(File response) {
                //下载成功，开始安装
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+updateEntity.versionName+".apk");
                install(mContext,file);
            }
        });


    }


    public static void install(Context context, File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获得apk版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            versionCode = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }


}
