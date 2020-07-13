
#### 视频中的运动截取

- 上家公司的业务需要,Java跑opencv还是挺勉强的
- 垃圾代码,不过好歹也有点心血

jar包
CMD输入以下指令运行：
> java -jar VideoHandle-1.0-SNAPSHOT.jar [配置文件videoHandle.properties地址]


- 基于JDK1.8运行前确认Java环境正确
- opencv的jar包，以及dll文件已经一起打入jar包
- 配置项在.properties文件中有具体说明
- 源码中opencv的jar包需要事先将推到仓库
> mvn install:install-file -Dfile=***/opencv-400.jar -DgroupId=org.opencv -DartifactId=opencv400 -Dversion=4.0.0 -Dpackaging=jar
