package com.delicloud.app.airkissdemo.model;

import java.io.Serializable;

/**
 * @author ChengXinPing
 * @time 2018/1/26 10:02
 */

public class UDPModel implements Serializable {
    /**
     * 包头 固定9个字节
     */
    private String packageName;
    /**
     * 广播类型	保留，1字节
     */
    private String type;
    /**
     * 负载长度 2字节(16进制)
     */
    private int loadLength;
    /**
     * 负载  与负载长度一致
     */
    private LoadContent loadContent;

    public int getLoadLength() {
        return loadLength;
    }

    public void setLoadLength(int loadLength) {
        this.loadLength = loadLength;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public LoadContent getLoadContent() {
        return loadContent;
    }

    public void setLoadContent(LoadContent loadContent) {
        this.loadContent = loadContent;
    }

    public static class LoadContent implements Serializable {
        /**
         * 设备ID长度 2字节
         */
        private int deviceIdLength;
        /**
         * 设备ID
         */
        private String deviceId;
        /**
         * 产品型号长度 2字节
         */
        private int deviceNameLength;
        /**
         * 产品型号
         */
        private String deviceName;
        /**
         * 设备状态	2字节
         */
        private Integer deviceStatus;
        /**
         * 错误消息长度  2字节
         */
        private int errMsgLength;
        /**
         * 错误消息
         */
        private String errMsg;


        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }


        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Integer getDeviceStatus() {
            return deviceStatus;
        }

        public int getDeviceIdLength() {
            return deviceIdLength;
        }

        public void setDeviceStatus(Integer deviceStatus) {
            this.deviceStatus = deviceStatus;
        }

        public void setDeviceIdLength(int deviceIdLength) {
            this.deviceIdLength = deviceIdLength;
        }

        public int getDeviceNameLength() {
            return deviceNameLength;
        }

        public void setDeviceNameLength(int deviceNameLength) {
            this.deviceNameLength = deviceNameLength;
        }

        public int getErrMsgLength() {
            return errMsgLength;
        }

        public void setErrMsgLength(int errMsgLength) {
            this.errMsgLength = errMsgLength;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

        @Override
        public String toString() {
            return "LoadContent{" +
                    "deviceIdLength='" + deviceIdLength + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    ", deviceNameLength='" + deviceNameLength + '\'' +
                    ", deviceName='" + deviceName + '\'' +
                    ", deviceStatus='" + deviceStatus + '\'' +
                    ", errMsgLength='" + errMsgLength + '\'' +
                    ", errMsg='" + errMsg + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UDPModel{" +
                "packageName='" + packageName + '\'' +
                ", type='" + type + '\'' +
                ", loadLength='" + loadLength + '\'' +
                ", loadContent=" + loadContent +
                '}';
    }
}
