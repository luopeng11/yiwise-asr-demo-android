### 注意事项
1. 可在assets文件夹下的config.properties文件中修改相关配置；
2. 编译时如果报错```More than one file was found with OS independent path 'META-INF/io.netty.versions.properties'```，在app的build.gradle中添加
    ```
    android {
        // 其它配置项
    
        packagingOptions {
            exclude 'META-INF/*'
            exclude 'META-INF/NOTICE'
            exclude 'META-INF/LICENSE'
            exclude 'META-INF/INDEX.LIST'
        }
    }
    ```

### SDK的Gradle地址
```
implementation('com.yiwise:asr-client-sdk:1.0.8-RELEASE') {
    exclude group: 'io.netty', module: 'netty-all'
}
```

### 客户端服务端交互文档
* [一知ASR实时语音识别使用说明文档](https://www.yuque.com/docs/share/d02243d2-c24e-4268-a7a8-3e1e090c4e03?#)
* [一知ASR录音文件识别使用说明文档](https://www.yuque.com/docs/share/a131e157-191b-4347-823c-c0ec1a515820?#)
