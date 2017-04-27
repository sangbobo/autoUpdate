package com.sangbo.autoupdate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sangbo on 16-5-19.
 */
public class UpdateEntity {

    public int versionCode = 0;
    public int isForceUpdate = 0;
    public int preBaselineCode = 0;
    public String versionName = "";
    public String downUrl = "";
    public String updateLog = "";
    public String md5 = "";

    public UpdateEntity() {
    }

    public UpdateEntity(String json) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);
        this.versionCode = jsonObject.getInt("versionCode");
        this.versionName = jsonObject.getString("versionName");
        this.isForceUpdate = jsonObject.getInt("isForceUpdate");
        this.downUrl = jsonObject.getString("downUrl");
        this.preBaselineCode = jsonObject.getInt("preBaselineCode");
        this.updateLog = jsonObject.getString("updateLog");
        this.md5 = jsonObject.getString("md5");

    }
}
