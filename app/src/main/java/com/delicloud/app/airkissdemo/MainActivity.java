package com.delicloud.app.airkissdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.delicloud.app.airkissdemo.model.AirKissEncoder;
import com.delicloud.app.airkissdemo.utils.Str_Hex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private EditText mSSIDEditText;
    private EditText mPasswordEditText;
    private Subscription sendSubscribe;
    private Subscription receiveSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSSIDEditText = (EditText) findViewById(R.id.ssidEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);

        Context context = getApplicationContext();
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = connectionInfo.getSSID();
                if (Build.VERSION.SDK_INT >= 17 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.replaceAll("^\"|\"$", "");
                }
                mSSIDEditText.setText(ssid);
                mSSIDEditText.setEnabled(false);

            }
        }
    }

    public void onConnectBtnClick(View view) {
        if (sendSubscribe != null && sendSubscribe.isUnsubscribed()) {
            sendSubscribe.unsubscribe();
        }
        if (receiveSubscribe != null && receiveSubscribe.isUnsubscribed()) {
            receiveSubscribe.unsubscribe();
        }
        final String ssid = mSSIDEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();
        if (ssid.isEmpty() || password.isEmpty()) {
            Context context = getApplicationContext();
            CharSequence text = "请输入wifi密码";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

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

        ProgressDialog mDialog = mDialog = new ProgressDialog(MainActivity.this);

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
    }

    @Override
    protected void onDestroy() {
        if (sendSubscribe != null && sendSubscribe.isUnsubscribed()) {
            sendSubscribe.unsubscribe();
        }
        if (receiveSubscribe != null && receiveSubscribe.isUnsubscribed()) {
            receiveSubscribe.unsubscribe();
        }
        super.onDestroy();
    }
}
