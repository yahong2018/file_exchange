package com.zhxh.imms.omron.backgroud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;

import com.zhxh.imms.utils.ByteUtil;
import com.zhxh.imms.utils.Logger;

public class TcpClient {
    private String ip;
    private int port;
    private int connectionTimeout = 3000;
    private Socket socket;
    private SocketAddress socketAddress;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LocalDateTime lastConnectTime = LocalDateTime.MIN;
    private LocalDateTime lastReadTime = LocalDateTime.MIN;
    private LocalDateTime lastWriteTime = LocalDateTime.MIN;
    private long totalReadBytes = 0;
    private long totalWriteBytes = 0;
    private boolean aborted = false;

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public synchronized boolean isConnected() {
        if (this.socket == null) {
            return false;
        }
        return !this.aborted && !this.socket.isClosed() && this.socket.isConnected();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public LocalDateTime getLastConnectTime() {
        return lastConnectTime;
    }

    public LocalDateTime getLastReadTime() {
        return lastReadTime;
    }

    public LocalDateTime getLastWriteTime() {
        return lastWriteTime;
    }

    public LocalDateTime getLastReadWriteTime() {
        if (this.lastReadTime.isAfter(this.lastWriteTime)) {
            return this.lastReadTime;
        }
        return this.lastWriteTime;
    }

    public long getTotalReadBytes() {
        return totalReadBytes;
    }

    public long getTotalWriteBytes() {
        return totalWriteBytes;
    }

    public TcpClient() {
    }

    public TcpClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String getName() {
        return this.ip + "(" + this.port + ")";
    }

    public boolean connect() {
        if (this.isConnected()) {
            return true;
        }
        
        if (this.socketAddress == null) {
            this.socketAddress = new InetSocketAddress(this.ip, this.port);
        }

        try {
            this.socket = new Socket();
            this.socket.connect(this.socketAddress, this.connectionTimeout);
            this.aborted = false;
            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();

            this.lastConnectTime = LocalDateTime.now();

            Logger.info("??????" + this.getName() + "??????");
            return true;
        } catch (Exception e) {
            Logger.error(this.getName() + "???????????????" + e.getMessage());
            Logger.debug(e);
        }
        return false;
    }

    public int read(byte[] buffer) {
        if (!this.isConnected() || buffer == null) {
            return 0;
        }

        try {
            int result = this.inputStream.read(buffer);
            this.lastReadTime = LocalDateTime.now();
            this.totalReadBytes += result;

            String hexString = ByteUtil.bytesToHex(buffer, result);
            Logger.debug(this.getName() + "???????????????" + hexString);

            return result;
        } catch (IOException e) {
            this.aborted = true;
            Logger.error(this.getName() + "?????????????????????" + e.getMessage());
            Logger.debug(e);
        }
        return 0;
    }

    public int write(byte[] buffer) {
        if (!this.isConnected() || buffer == null) {
            return 0;
        }
        int result = buffer.length;
        try {
            this.outputStream.write(buffer);
            this.lastWriteTime = LocalDateTime.now();
            this.totalWriteBytes += result;

            String hexString = ByteUtil.bytesToHex(buffer, result);
            Logger.debug(this.getName() + "???????????????" + hexString);
        } catch (IOException e) {
            this.aborted = true;
            Logger.error(this.getName() + "?????????????????????" + e.getMessage());
            Logger.debug(e);
            return 0;
        }

        return result;
    }

    public void disConnect() {
        try {
            if (this.isConnected()) {
                this.socket.close();
            }
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
            Logger.info(this.getName() + "?????????.");
        } catch (Exception e) {
            this.aborted = true;
            Logger.error(this.getName() + "???????????????" + e.getMessage());
            Logger.debug(e);
        }
    }
}
