不区分优先级

1.xfyun配置抽到springboot中 不要单独的config.properties

2.增加文件类型转换接口, 如传入 wmv格式视频 指定输出类型为HTML5_Compatible,
增加该接口的意义在于html5 video,audio控件播放有格式限制.

> video格式

| 格式 | IE | Firefox | Opera | Chrome | Safari |
| :------: | :------: | :------: | :------: | :------: | :------: |
| Ogg Theora| No | 3.5+ |10.5+ |5.0+|No |
| MPEG4 H.264 |9.0+|No|No|5.0+|3.0+|
| WebM VP8| No|4.0+|10.6+|6.0+|No|
         
> audio格式

| 格式 | IE | Firefox | Opera | Chrome | Safari |
| :------: | :------: | :------: | :------: | :------: | :------: |
| Ogg Vorbis | No | 3.5+ |10.5+ |5.0+|No |
| MP3 |9.0+|No|No|5.0+|3.0+|
| Wav | No|4.0+|10.6+|6.0+|No|
--------------------- 
