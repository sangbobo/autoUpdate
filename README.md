# autoUpdate

android自动更新

之前一直在用友盟的自动更新插件，很好用，简单方便，可是无奈10月份要关闭了，好吧，只能自己写一个了。

使用起来很方便，使autoUpdate作为module.

        CheckVersion.checkUrl = "http://www.xxx.com/api/versiontest.txt";     //定义服务器版本信息
        CheckVersion.update(this);                                            //在需要更新的位置，插入这句话

