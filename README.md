# 公告

项目地址:https://github.com/MartinxMax/S-Clustr

| 更新预告 | 内容 | 进度 |
| ---- | --- | --- |
| SIEMENS S7-200 SMART | 远程控制 | 进行中 |

| 开发人员 | Blog | 联系方式 | 提交时间 | 提交内容 | 授权情况 |
| ---- | ---- | ---- | ---- | ---- | ---- |
| ASH\_HH | https://blog.csdn.net/m0\_53711047/article/details/133691537?spm=1001.2014.3001.5502 | 匿名 | 2023-10-16 21:42:26 | STM32 | 已授权 |

| 类型 | 被控设备 | 有线 | 无线 | 4G | 安全加密 | 协议 |
| --- | ---- | --- | --- | --- | ---- | --- |
| 嵌入式 | Arduino | √ | × | √ | × | TCP/IP |
| 嵌入式 | 合宙AIR780e | × | × | √ | × | TCP/IP |
| 嵌入式 | ESP8266 | × | √ | × | × | TCP/IP |
| 嵌入式 | AT89C51 | × | × | √ | × | TCP/IP |
| 嵌入式 | STM32[103fc6t6] | × | × | √ | × | TCP/IP |
| PLC | SIEMENS S7-1200 | √ | × | × | x | TCP/IP |
| PLC | SIEMENS S7-200 | √ | × | × | x | TCP/IP |

| 被控PC平台 | 协议 | 安全加密 |
| ------ | --- | ---- |
| Windows | TCP/IP | 可选 |
| Linux | TCP/IP | 可选 |
| Mac OS | TCP/IP | 可选 |

| 服务端文件 | 解释 |
| ----- | --- |
| S-Clustr\_Server | 服务端 |
| S-Clustr\_Client | 黑客端 |
| DebugDevice | 用于模拟嵌入式设备接入服务端 |
| Generate | 一键生成嵌入式设备程序 |
| Testpc | Windows主机接入服务端 |
| blacklist.conf | 黑名单,标记禁止接入的IP组 |
| Server.conf | 服务端一些配置参数 |
| Version.conf | 版本信息 |
| Linux\_Installer.sh | Linux依环境赖安装程序 |
| Windows\_Installer.bat | Windows环境依赖安装程序 |
| Parameter\_Description-EN.xls | [英文]关于Server.conf内参数说明 |
| Parameter\_Description-ZH.xls | [中文]关于Server.conf内参数说明 |

# 问答解惑

> (匿名网友)问:S-Clustr是一款什么工具?
> 答:是一款集中化网络控制器,用于一对多的网络控制

> (柴郡)问:S-Clustr的使用场景和使用环境?
> 答:工业/智能控制、大/中/小型机房控制、工业/交通电源控制、Botnet控制

> (柴郡)问:流量通讯的隐蔽性如何?
> 答:固然过程是加密,但您的流量路径基本都是国内运营商线路,所以请遵守法律

> (柴郡)问:Arduino可不可以换成更便宜的开发版?
> 答:完全可以,您可以Server.conf中DEV\_TYPE、DEV\_ENCRYPTION\_Server分别添加您的开发板型号。或者联系作者[https://github.com/MartinxMax]进行更新您的开发板程序

![图片.png](https://image.3001.net/images/20231014/1697269501_652a46fd0bff1b2f41903.png!small)

> (柴郡)问:是否可以避免中间人和重放攻击?
> 答:身份验证数据流=时间戳+设备ID+设备状态+AES(时间戳+密钥),这样服务端验证黑客身份时,将尝试时间戳进行AES解密后与服务器时间戳进行对比。如果您受到中间人攻击导致数据包被获取,即使如此,在其他黑客未获取您的密钥前将无法对您设备进行重放攻击。

![图片.png](https://image.3001.net/images/20231014/1697266692_652a3c04d5600d52ca8c4.png!small)

> (匿名网友)问:控制PC端可以做什么?
> 答:例如您可以当命令下发时爬取xxx网站,打开xxx应用,执行xxx命令

> (匿名网友)问:设备端嵌入式设备都没有加密服务?
> 答:对的,暂时考虑到嵌入式设备端加入加密函数后影响性能。如果您对嵌入式设备安全性要求较高请不要与服务端处于相同局域网下

# S-Clustr 嵌入式设备端

## Arduino

### 有线局域网控制

#### 准备设备材料(合计 50￥ )

1.Arduino UNO (17￥)

![图片.png](https://image.3001.net/images/20231003/1696319500_651bc80cd29c5e6f7d8d1.png!small)

2.ENC28J60 (28￥)

![图片.png](https://image.3001.net/images/20231004/1696404809_651d15491408ad4740661.png!small)

3.1路继电器模块5V (3￥)

![图片.png](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

4.杜邦线 (2￥)
母转母

![图片.png](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

#### 接线原理图

![图片.png](https://image.3001.net/images/20231004/1696404864_651d15804a8adde1b9379.png!small)

![图片.png](https://image.3001.net/images/20231004/1696404881_651d1591885762a52c1a9.png!small)

### 4G无线公网远控

#### 准备设备材料(合计 48￥ )

1.Arduino UNO (17￥)

![图片.png](https://image.3001.net/images/20231003/1696319500_651bc80cd29c5e6f7d8d1.png!small)

2.SIM900A或SIM800A (26￥)

![图片.png](https://image.3001.net/images/20231003/1696320095_651bca5fdbb3a6a2b1941.png!small)

2.1路继电器模块5V (3￥)

![图片.png](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.杜邦线 (2￥)
母转母

![图片.png](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

4.移动卡

*这里需要移动卡是因为SIM800A与SIM900A支持移动的2G网,电信就不行,联通也不行...不过后面测试合宙AIR780e的板子应该联通可行,也就是不需要SIM系列了,内部集成好了*

![图片.png](https://image.3001.net/images/20231003/1696322442_651bd38a673737e609e44.png!small)

#### 接线原理图

这里将模拟Arduino收到信号后控制继电器行为

![图片.png](https://image.3001.net/images/20231003/1696321543_651bd0070660a10028e00.png!small)

![图片.png](https://image.3001.net/images/20231003/1696321799_651bd107e606ad635d9d9.png!small)

*不会写代码?...使用Generate.py生成Arduino代码就OK了*

## ESP8266 (WIFI局域网控制)

#### 准备设备材料(合计 18￥ )

1.ESP8266 (13￥)

![图片.png](https://image.3001.net/images/20231005/1696496352_651e7ae0eadb3f502abd5.png!small)

2.1路继电器模块5V (3￥)

![图片.png](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.杜邦线 (2￥)
母转母

![图片.png](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

#### 接线原理图

![图片.png](https://image.3001.net/images/20231005/1696496713_651e7c4961e1f66469f91.png!small)

## AIR780E (4G无线公网远控)[推荐]

### 注意事项

1.该开发板经测试后,的确比SIM系列来的更加快速稳定
2.安装Luatools：用于完成程序烧录
[https://doc.openluat.com/wiki/37?wiki\_page\_id=4489]

#### 准备设备材料(合计 47￥ )

1.Air780e开发板 (42￥)

*背面有一个sim卡槽*

![图片.png](https://image.3001.net/images/20231014/1697255772_652a115c4d9c45ee325b8.png!small)

![图片.png](https://image.3001.net/images/20231014/1697256035_652a1263e9098b9a5a733.png!small)

2.1路继电器模块5V (3￥)

![图片.png](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.杜邦线 (2￥)
母转母

![图片.png](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

#### 接线原理图

![图片.png](https://image.3001.net/images/20231014/1697256413_652a13ddced9ee29cc759.png!small)

#### 烧录程序

*将我们Generate生成的文件导入进Luatools*
![图片.png](https://image.3001.net/images/20231014/1697256986_652a161a395cbe1d00004.png!small)

*选择底层core,在我们的Output\AIR780E\LuatOS-SoC\_V1103\_EC618.soc有提供*

![图片.png](https://image.3001.net/images/20231014/1697257233_652a1711a164adde8dc73.png!small)

*根据提示完成烧录,注意板子上的三个键,分别为 启动 复位 BOOT*

![图片.png](https://image.3001.net/images/20231014/1697257441_652a17e10a6eb5ce11d70.png!small)

## AT89C51

### 准备设备材料(合计 42￥ )

1.51单片机最小系统开发板送CH340下载器 (11￥)

![图片.png](https://image.3001.net/images/20231016/1697385869_652c0d8d827cba5a5f0b6.png!small)

2.1路继电器模块5V (3￥)

![图片.png](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.杜邦线 (2￥)
母转母

![图片.png](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

4.SIM900A或SIM800A (26￥)

![图片.png](https://image.3001.net/images/20231003/1696320095_651bca5fdbb3a6a2b1941.png!small)

5.移动卡

![图片.png](https://image.3001.net/images/20231003/1696322442_651bd38a673737e609e44.png!small)

### 接线原理图

![图片.png](https://image.3001.net/images/20231016/1697386548_652c1034eb77af75eea6b.png!small)

## Generate一键生成烧录代码

`python3 Generate.py`

![图片.png](https://image.3001.net/images/20231016/1697386581_652c10555cac89556f026.png!small)

*填写完成参数,这里的127.0.0.1是错误的,你应该输入服务端的公网IP地址,也就是运行S-Clustr\_Server.py的服务器的IP*

![图片.png](https://image.3001.net/images/20231003/1696321930_651bd18a964982b1d7c4d.png!small)

*输出烧录代码将在目录.\Device\Output\型号 目录下*

# S-Clustr 服务端

注意:
1.服务端必须在公网,如果你的服务端在内网可以考虑端口映射,黑客端服务默认在端口9999,设备端服务默认在端口10000
2.服务端与黑客端是全程高度加密通信的,强制开启加密服务不可关闭。其次你可以通过Server.conf文件内的配置,来决定嵌入式设备或被控PC接入时是否提供加密服务
3.对于Server.conf文件内参数不理解,请详细阅读手册文档
4.服务端每次启动都将随机长度为12个字符作为密钥用作于黑客与被控设备的身份认证,防止其他黑客未授权接入控制设备。你可以手动指定密钥(python3 S-Clustr\_Server.py -keyh Maptnh -keyv Maptnh)
—这里黑客端与被控端各2个密钥,首次为明文密钥,如果你觉得过于敏感,可以把其次的临时TOKEN当作密钥,有效防止明文密钥被破解

![0OVQK\[M(I}1)2T\]1}$28AXK.png](https://image.3001.net/images/20231003/1696323112_651bd6286aa926034bd61.png!small)
*在该参数中,置1表示启动被控设备的加密服务,那么被控设备必须提供身份认证密钥才允许接入,否则拒绝*

![图片.png](https://image.3001.net/images/20231003/1696323168_651bd6608c798de5fd12d.png!small)

5.有效防止中间人(MITM)嗅探分析黑客端与服务端通信数据包,再进一步防止重放攻击与加密数据被破解

## 服务端脚本参数解析

`-lh`:绑定指定本地IP,默认0.0.0.0
`-lpv`:设置本地监听地址(设备端)默认10000端口
`-lph`:设置本地监听地址(黑客端)默认9999端口
`-keyh`:设置黑客端密钥,默认随机12位字符作为密钥
`-keyv`:设置设备端密钥,默认随机12位字符作为密钥

## 服务端运行

`python3 S-Clustr_Server.py`

![图片.png](https://image.3001.net/images/20231003/1696323448_651bd778647c82aa08cf5.png!small)

# S-Clustr 黑客端

注意:
客户端采用交互式运行,操作类似于Metasploit渗透测试框架

## 黑客端脚本参数解析

进入后输入 `help`或 `?`或 `options`来查看所需要设置的参数
`set rhosts <IP>`:设置服务端的IP
`set rport <Port>`:设置服务端的端口
`set id <number>`:选择所需要控制的设备ID编号,0表示选中所有设备
`set pwr <state>`:控制设备状态,启动[1],停止[2],查询状态[3]

## 黑客端连接服务端

`python3 S-Clustr_Server.py`

![图片.png](https://image.3001.net/images/20231003/1696323920_651bd950229e1881fffdc.png!small)

*查询全部设备当前状态*

```
S-Clustr(V1.0.0)> set rhost 127.0.0.1
[*] rhost => 127.0.0.1
S-Clustr(V1.0.0)> set id 0
[*] id => 0
S-Clustr(V1.0.0)> set pwr 3
[*] pwr => 3
```

PS:这里必须填写Key,这将决定你是否有权限接入服务器的关键要素。
在服务端中,你可以任意挑选一个作为密钥

![图片.png](https://image.3001.net/images/20231003/1696324886_651bdd16be48fe704fe63.png!small)

```
S-Clustr(V1.0.0)> set key cf5cdc4798a72283a4c0c0b1ef2ef5da
[*] key => cf5cdc4798a72283a4c0c0b1ef2ef5da
```

## 查询全部设备状态

```
S-Clustr(V1.0.0)> set id 0
[*] id => 0
S-Clustr(V1.0.0)> set pwr 3
[*] pwr => 3
S-Clustr(V1.0.0)> run
[*] Connecting to the server...
[*] Attempting to authenticate to the server [127.0.0.1:9999]
|   Device ID   |  Device Type  | Device State | Device Network |
|:-------------:|:-------------:|:-------------:|:---------------:|
|       1        |      None      |    Stopped     |   Disconnected   |
|       2        |      None      |    Stopped     |   Disconnected   |
|       3        |      None      |    Stopped     |   Disconnected   |
|       4        |      None      |    Stopped     |   Disconnected   |
|       5        |      None      |    Stopped     |   Disconnected   |
|       6        |      None      |    Stopped     |   Disconnected   |
|       7        |      None      |    Stopped     |   Disconnected   |
|       8        |      None      |    Stopped     |   Disconnected   |
|       9        |      None      |    Stopped     |   Disconnected   |
|       10       |      None      |    Stopped     |   Disconnected   |
|:-------------:|:-------------:|:-------------:|:---------------:|
```

*我们可以看到已经成功接入服务器了,身份认证成功*

*我们设置错误的Key时,服务器将无法对你进行授权*

![图片.png](https://image.3001.net/images/20231003/1696325026_651bdda20a3ade75729c8.png!small)

![图片.png](https://image.3001.net/images/20231003/1696325038_651bddae4348ef54a6219.png!small)

*【模拟被控设备连接服务端】通过黑客端控制全部设备*

![图片.png](https://image.3001.net/images/20231003/1696325230_651bde6ea06aa09ce29dc.png!small)

*设备全部上线*

![图片.png](https://image.3001.net/images/20231003/1696325329_651bded1da4510ff2bf07.png!small)

## 控制全部设备启动

```
S-Clustr(V1.0.0)> set id 0
[*] id => 0
S-Clustr(V1.0.0)> set pwr 1
[*] pwr => 1
S-Clustr(V1.0.0)> run
```

![图片.png](https://image.3001.net/images/20231003/1696325463_651bdf570f2bc9ff3a606.png!small)

![图片.png](https://image.3001.net/images/20231003/1696325476_651bdf6439c9cef5bf0ab.png!small)

## 控制全部设备停止

```
S-Clustr(V1.0.0)> set id 0
[*] id => 0
S-Clustr(V1.0.0)> set pwr 2
[*] pwr => 2
S-Clustr(V1.0.0)> run
```

![图片.png](https://image.3001.net/images/20231003/1696325887_651be0ff5a3bff29a4fc3.png!small)

# 案例:被控端收到命令后,访问www.bing.com，并且打开计算器

![图片.png](https://image.3001.net/images/20231003/1696327117_651be5cdcfd7f72eff618.png!small)

![图片.png](https://image.3001.net/images/20231003/1696327549_651be77d5f1405dbdd832.png!small)

*输入我们的被控端KEY*

![图片.png](https://image.3001.net/images/20231003/1696327337_651be6a9304ba513e44c1.png!small)
*被控端成功接入*

*我们的黑客端也成功查询到设备*

![图片.png](https://image.3001.net/images/20231003/1696327393_651be6e18f1a83c55f71d.png!small)

*控制设备完成题目要求*

![image](https://image.3001.net/images/20231003/1696328517_651beb45dd05d703258bc.gif)


# SIEMENS S7-12XX 后门攻击


![image.png](https://image.3001.net/images/20231116/1700064368_6554ec700ef08120c4bc0.png!small)

![image.png](https://image.3001.net/images/20231116/1700064386_6554ec8260cd410d68837.png!small)

![image.png](https://image.3001.net/images/20231116/1700064514_6554ed02623a5f958fb2c.png!small)


# 手册文档

*Parameter\_Description-ZH.xls*

![图片.png](https://image.3001.net/images/20231003/1696326111_651be1dfb5a553110354d.png!small)

*Parameter\_Description-EN.xls*

![图片.png](https://image.3001.net/images/20231003/1696326136_651be1f8121b3eaf709ac.png!small)

