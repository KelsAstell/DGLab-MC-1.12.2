# DG-Lab-Mod
一个可以连接到DG-Lab设备并进行操作的Minecraft mod  
可以根据扣血事件来调整强度
是https://github.com/YuuLuo/DG-Lab-Mod的1.12.2版本移植
请使用shadow进行打包
# 改动说明
### 2023-06-28
1. 更新Gradle构建器到4.9版本
2. 换用阿里云nexus maven / ls233 maven
3. 代码重构, 减少Debug信息
4. 修正了1个潜在的Bug, 虽然我也不知道是不是就顺手修了orz
5. 升级依赖版本
6. 引入`ModConfig.java`, 由https://github.com/KelsAstell/ChocolateMaker2-1.12.2提供(还是写一下吧, 毕竟对逻辑做改动了)
7. 我没测试！我没测试！我没测试！重要的话说三遍
# 连接
连接部分使用https://github.com/SakuraKoi/DgLabUnlocker 模块的“对外开放Websocket RPC服务端口”功能，所以需要先下载DG-Lab 1.3.2版本：https://dungeon-lab.cn/appDownload/dlab1.3.2.apk ，并安装好DgLabUnlocker模块，在右上角模块设置-功能设置中打开“开放RPC服务端口”，并确保该手机与电脑处于同一网络下。  
~其实是不想在Java里折腾蓝牙协议了~  
进入游戏，输入/dglab connect [ip] 来连接到设备，ip可以从手机上的wifi-设置-高级-IP地址中看到。
如成功连接，应该会看到提示“Connected to [ip]"
# 使用
以下是指令列表以及说明：  
`/dglab connect <ip>` &nbsp;&nbsp;    连接到对应ip的设备  
`/dglab disconnect`  &nbsp;&nbsp;   从设备断开连接  
`/dglab getStrength`  &nbsp;&nbsp;  获取当前A、B通道强度  
`/dglab setMaxStrength [20~276] [20~276]` &nbsp;&nbsp;  设置A、B通道最大强度，mod对强度的调整不会超过此强度限制，默认为100  
`/dglab getMaxStrength` &nbsp;&nbsp;  获取当前最大强度设置  
`/dglab addStrength <A/B> [value]`&nbsp;&nbsp; 对A/B通道增加 value 强度  
`/dglab setBaseStrength [value] [value]`  &nbsp;&nbsp;  设置基础强度，punish 模式下当 punishTime 结束后会回到此强度，默认为20  
`/dglab getBaseStrength` &nbsp;&nbsp;   获取当前基础强度设置  
`/dglab setPunishTime [seconds]`  &nbsp;&nbsp;  设置当前惩罚持续时间，单位为秒，默认为3  
`/dglab setPunishRate [value]` &nbsp;&nbsp;   设置当前惩罚倍数，默认为5  
`/dglab punish/ultraPunish <start/stop>` &nbsp;&nbsp;   开始或停止 punish/ultraPunish 模式
# Punish 模式
当 punish 启动后，mod将会开始监听受伤事件
当玩家受到 damage 点hp的伤害时（1颗心=2hp），将会将强度提升 (damage*punishRate)，持续 punishTime 秒
在 punishTime 持续期间，受到的其他伤害会被忽略，时间结束后强度会被恢复为基础强度
# UltraPunish模式
一个特殊的惩罚模式，当 ultraPunish 启动后，mod将会开始监听受伤事件
当玩家受到 damage 点hp的伤害时（1颗心=2hp），将会将强度提升 (damage*punishRate)，持续 punishTime 秒
在 punishTime 持续期间，受到的其他伤害会被叠加，并重新计算 punishTime ，时间结束后强度会被恢复为基础强度

注意：punish 模式不能和 ultraPunish 模式同时开启，如已经启动了一个模式，在启动另一个模式时将会自动关闭此模式
