NIO Channel、Selector的原理解析
------------------------------
Selector许多书上讲解是选择器，为了更加具象,我们将其称为多路复用器，在一次的使用通过打开一个多路复用器实现多路复用，具体步骤如下:
* 打开Selector
* 打开通道channel
* 将通道channel注册到selector中，并设置标志key
* 设置selector的轮询时间,selector会定时的遍历在上注册的channel
* 检测到有channel时，对channel进行处理，其实本质上，channel只是一个通信通道，真正的处理是其上的标志key,针对不用的key进行不同的处理

****
**案例解析**
    该例子中实现的是一个多路复用器链接通信的问题，服务器端打开一个selector进行轮询，客户端同时也打开一个selector进行论寻，针对不同的key提供不同的处理方式