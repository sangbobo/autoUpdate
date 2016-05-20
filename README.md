# autoUpdate

android自动更新

之前一直在用友盟的自动更新插件，很好用，简单方便，可是无奈10月份要关闭了，好吧，只能自己写一个了。

android studio导入方式

        compile 'com.sangbo.autoupdate:autoUpdate:1.0.1'
        
使用方法

        CheckVersion.checkUrl = "http://www.xxx.com/api/versiontest.txt";     //定义服务器版本信息
        CheckVersion.update(this);                                            //更新，默认更新不显示处理消息（一般自动更新时使用）
        or
        CheckVersion.update(this,true);                                       //更新，并显示处理结果（一般手打更新时使用）


服务器json信息

        {
            "versionCode": 2,                   //app版本
            "isForceUpdate": 0,                 //是否更新
            "preBaselineCode": 0,               //最低运行版本
            "versionName": "1.1.0",             //app版本名称
            "downUrl": "http://xx.apk",         //下载地址
            "md5":"xxxxxxxxx",                  //md5
            "updateLog": "xxx,xxx,xx"           //更新公告
        }
        