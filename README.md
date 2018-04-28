# AirKissDemo
### AirKiss原理
> 本章只大致讲解一下什么是air kiss,具体通讯协议介绍请去文末链接查看
#### 一、AirKiss简介
AirKiss技术是一种通过手机发送的SSID和密码经过路由转发出去，被目前wifi设备所检测并截获到。从而达到通过手机客户端给智能设备配网wifi网络的一种新型技术。
#### 二、AirKiss配网基本流程 
1. wifi智能设备以station混杂模式运行 
2. 手机客户端通过AirKiss发送家里的路由器ssid和密码 
3. wifi设备通过抓包获取到ssid和密码，然后连接到家里的路由器 
#### 三、Demo实现
> demo通过`RxJava`实现异步任务 
- 客户端发送wifi ssid 以及密码 关键代码
``` java
//发送AirKiss
        sendSubscribe = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                byte DUMMY_DATA[] = new byte[1500];
                AirKissEncoder airKissEncoder = new AirKissEncoder(ssid, password);
                DatagramSocket sendSocket = null;
                try {
                    sendSocket = new DatagramSocket();
                    sendSocket.setBroadcast(true);
                    int encoded_data[] = airKissEncoder.getEncodedData();
                    for (int i = 0; i < encoded_data.length; ++i) {
                        DatagramPacket pkg = new DatagramPacket(DUMMY_DATA,
                                encoded_data[i],
                                InetAddress.getByName("255.255.255.255"),
                                10000);
                        sendSocket.send(pkg);
                        Thread.sleep(4);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                } finally {
                    sendSocket.close();
                    sendSocket.disconnect();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String string) {

                    }
                });
```
> 这时智能设备已经连上了wifi,一般来说客户端和硬件交互时，智能设备连上了wifi可能会发送一段UDP包，来告诉客户端联网成功，客户端再来进行接下来的操作（要与硬件规定规范）
- 客户端处理智能设备发送的UDP包（根据实际格式进行解码，本文直接解码成String）
``` java
 //接收udp包
        receiveSubscribe = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                byte[] buffer = new byte[15000];
                DatagramSocket udpServerSocket = null;
                try {
                    udpServerSocket = new DatagramSocket(24333);
                    udpServerSocket.setSoTimeout(1000 * 60);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    while (true) {
                        Log.d("status", "running");
                        udpServerSocket.receive(packet);
                        buffer = packet.getData();
                        String hexString = Str_Hex.byte2hex(buffer);
                        //对收到的UDP包进行解码
                        //各个设备返回的UDP包格式不一样  将解码的UDP包通过RxJava发送到主线程 进行UI处理
                        if (!TextUtils.isEmpty(hexString)) {
                            Log.d("received:", hexString);
                            subscriber.onNext(hexString);
                            break;
                        }
                    }

                    subscriber.onCompleted();
                } catch (SocketException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                } finally {
                    udpServerSocket.close();
                    udpServerSocket.disconnect();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    ProgressDialog mDialog = mDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    public void onStart() {
                        super.onStart();
                        mDialog.setMessage("正在连接...");
                        mDialog.setCancelable(false);
                        mDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onNext(String s) {
                        Toast.makeText(MainActivity.this, "收到的UDP包：" + s, Toast.LENGTH_SHORT).show();
                    }

                });
```
> **注意：** 由于使用了`RxJava`，要在`onDestroy()`里面解除订阅，避免内存泄露

#### 四、相关说明
本协议是基于https://github.com/zhchbin/WeChatAirKiss 修改的，感谢@zhchbin的分享，上面地址完整的实现了airkiss协议

***声明：*** 目前AirKiss协议已经发展到3.0版本（详见微信硬件平台），此版本应该还是基于最初的设计实现，仅供学习研究使用，不建议作为商业产品

AirKiss是微信硬件平台提供的一种WIFI设备快速入网配置技术。WeChatAirKiss是通过分析微信客户端相关的网络包实现的Android客户端，实现了相同的功能，使用者能够摆脱微信客户端的限制使用AirKiss技术进行物联网模块的联网配置。