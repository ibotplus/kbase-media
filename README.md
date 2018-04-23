This a api to easy converter multipartFile(video audio image) to text.
There is [api-docs](http://172.16.23.12/kbase-media/swagger-ui.html#) use Swagger2.

**配置文件说明**
(配置文件为application*.yml) 

``` yaml
# convert部分配置
convert:
  # 是否开启异步接口
  enable-async: false
  # 文件默认上传路径
  outputFolder: /tmp/convert/
  #  异步接口文件默认上传路径
  outputFolder-async: /tmp/convert/async/
  video:
    vca:
	  # 项目依赖于ffmpeg,必须要安装,默认即可
      default: ffmpeg
      ffmpeg:
        # ffmpeg的安装路径
        path: /opt/ffmpeg/ffmpeg-3.0/
        toImage:
          # ffmpeg视频切割图片默认为1帧/5s
          fps: 0.2                                           
  audio:
    # default asr tool
    asr:
	  # 可选值:shhan:声瀚引擎(私有化部署),baidu:百度asr
      default: shhan
      # asr接口对音频时间长度有限制,所以此值为切割文件的长度,声瀚为20s/seg,百度为60s/seg 
      seg-duration: 20 
      #baidu asr config 
      baidu:
        appId: 11067243
        apiKey: iDEvPvY4zT9CzFgYKMQY6eAi
        secretKey: Wkeh8gIbB2LrNBtGwuechG8TUkLlB2TY
      shhan:
	    # 声瀚引擎base-url
        base-url: http://172.16.8.103:8177/shRecBase/
  image:
    # 可选值 youtu|abbyy 私有化部署设置abbyy
    ocr:
      default: abbyy
      #tencent youtu ocr tool config
      youtu:
        appId: 10125304
        secretId: AKIDVs45xejwtvmW5SpdkjYGpDUZTIwOp0Hn
        secretKey: a0EHCwgHhgnogMCvUr33uhKl195qSwip
        userId: 1071552744
      # abbyy fineReader engine config
      abbyy:
        path: /opt/ABBYY/FREngine11/Bin
        license: SWTT-1101-1006-4491-7660-4166
```
### Restful Apis
[http://172.16.23.12/kbase-media/swagger-ui.html](http://172.16.23.12/kbase-media/swagger-ui.html)

### Thanks For
[Tencent-YouTu](https://github.com/Tencent-YouTu/java_sdk)

[Baidu-AIP](https://ai.baidu.com/docs#/ASR-Online-Java-SDK/top)

[bramp/ffmpeg-cli-wrapper](https://github.com/bramp/ffmpeg-cli-wrapper)

[apache/rocketmq](https://github.com/apache/rocketmq)

[ekoz/ocr-api](https://github.com/ekoz/ocr-api)