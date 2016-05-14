# cordova-plugin-baidutts
### 没时间解释了

# 操作步骤
```
1. 下载这个包到本地
2. 更改 src/android/Keys.java文件里的各种key
3. 安装插件 cordova plugin add /目录/cordova-plugin-baidutts
```

# 使用
```
// 播放声音
baiduTts.speak({text: text, volume: volumeVal, speed: rateVal, pitch: pitch});
// 监听开始事件
baiduTts.onStart(function(){//do});
// 监听结束事件
baiduTts.onStop(function(){//do});
// 停止播放
baiduTts.stop();
```
