package com.delicloud.app.airkissdemo.utils;

import com.delicloud.app.airkissdemo.model.UDPModel;

/**
 * @author ChengXinPing
 * @time 2018/1/26 10:01
 */

public class UDPUtils {
    public static UDPModel decodeUdp(String hex) {
        UDPModel udpModel = new UDPModel();
        try {
            String packageName = Str_Hex.hexStringToString(hex.substring(0, 18), 2);

            String type = Str_Hex.hexStringToString(hex.substring(18, 20), 2);
            int loadLength = Str_Hex.hexStringToAlgorism(hex.substring(20, 24));
            udpModel.setPackageName(packageName);
            udpModel.setType(type);
            udpModel.setLoadLength(loadLength);
            UDPModel.LoadContent loadContent = new UDPModel.LoadContent();
            String contentHex = hex.substring(24, 24 + loadLength * 2);

            int deviceIdLength = Str_Hex.hexStringToAlgorism(contentHex.substring(0, 4));


            String deviceId = Str_Hex.hexStringToString(contentHex.substring(4, 4 + deviceIdLength * 2), 2);

            int deviceNameLength = Str_Hex.hexStringToAlgorism(contentHex.substring(4 + deviceIdLength * 2, 8 + deviceIdLength * 2));

            String deviceName = Str_Hex.hexStringToString(contentHex.substring(8 + deviceIdLength * 2, 8 + deviceIdLength * 2 + deviceNameLength * 2), 2);

            int deviceStatus = Str_Hex.hexStringToAlgorism(contentHex.substring(8 + deviceIdLength * 2 + deviceNameLength * 2, 12 + deviceIdLength * 2 + deviceNameLength * 2));

            int errMsgLength = Str_Hex.hexStringToAlgorism(contentHex.substring(12 + deviceIdLength * 2 + deviceNameLength * 2, 16 + deviceIdLength * 2 + deviceNameLength * 2));
            String errMsg = "";
            if (errMsgLength > 0) {
                errMsg = Str_Hex.hexStringToString(contentHex.substring(16 + deviceIdLength * 2 + deviceNameLength * 2, 16 + deviceIdLength * 2 + deviceNameLength * 2 + errMsgLength * 2), 2);
            }

            if (deviceStatus == 65535) {
                deviceStatus = -1;
            }
            loadContent.setDeviceStatus(deviceStatus);
            loadContent.setDeviceId(deviceId);
            loadContent.setDeviceIdLength(deviceIdLength);
            loadContent.setDeviceName(deviceName);
            loadContent.setDeviceNameLength(deviceNameLength);
            loadContent.setErrMsg(errMsg);
            loadContent.setErrMsgLength(errMsgLength);
            udpModel.setLoadContent(loadContent);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return udpModel;
    }
}
