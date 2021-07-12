import * as Utils from './Utils';

export class WebSocketProxy {
    constructor() {
        this.server_time = null;
        this.network_status = 4;

        this.cancelAutoReconnect = this.cancelAutoReconnect.bind(this);
        this.init = this.init.bind(this);
        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.send = this.send.bind(this);
        this.reconnect = this.reconnect.bind(this);
    }

    cancelAutoReconnect() {
        this.autoReconnect = false;
        if (this.auto_reconect_timer) {
            clearInterval(this.auto_reconect_timer);
            this.auto_reconect_timer = null;
        }
    }

    init(config) {
        config = config || {};

        this.url = config.url;
        this.onConnected = config.onConnected;
        this.onError = config.onError;
        this.onClose = config.onClose;
        this.onMessage = config.onMessage;

        this.autoReconnect = config.autoReconnect;        
        if (config.autoReconnect !== false) {
            this.autoReconnect = true;
        }

        this.reconnectTime = config.reconnectTime;
        if (this.reconnectTime == null) {
            this.reconnectTime = 10;
        }
        this.reconnectTime = this.reconnectTime * 1000;

        if (this.autoReconnect) {
            const me = this;
            const cycleTime = this.reconnectTime * 1.5;

            this.auto_reconect_timer = setInterval(function () {
                if (me.waitTime === -1) {
                    return;
                }
                const now = +new Date();
                const result = now - me.waitTime;
                if (result >= (me.reconnectTime)) {
                    console.log("connection timeout,closing the socket...")
                    me.close();
                    me.reconnect();
                }
            }, cycleTime);
        }
    }

    open(config) {        
        try {
            console.log("start connect...");
            config = config || {};

            const scheme = config.scheme || (document.location.protocol === "https:" ? "wss" : "ws");
            const wsUrl = config.wsUrl || (scheme + "://" + Utils.getApiServerHost() + "/" + this.url);

            this.openConfig = config;

            this.waitTime = +new Date();
            const websocket = new WebSocket(wsUrl);
            this.websocket = websocket;

            const me = this;
            websocket.onopen = function () {
                me.waitTime = +new Date();
                me.network_status = 0;

                me.ping = setInterval(function () {
                    me.send.call(me, 'com.zhxh.imms.web.webSocket.WebSocketPingMessage', {});
                }, 10 * 1000);

                console.log("websocket connected");
                if (me.onConnected) {
                    me.onConnected();
                }
            };
            websocket.onerror = function (evt) {
                console.log("web socked error");

                if (me.onError) {
                    me.onError(evt);
                }
            };

            websocket.onclose = function () {
                console.log("websocket closed.");
                me.network_status = 4;
                if (me.ping) {
                    clearInterval(me.ping);
                }

                if (me.onClose) {
                    me.onClose();
                }
                me.websocket = null;
                me.waitTime = +new Date();
            };

            websocket.onmessage = function (evt) {
                console.log("websocket received message...");
                me.network_status = 0;
                me.waitTime = +new Date();
                try {
                    const webSoccketMessage = eval("(" + evt.data + ")");
                    const body = eval("(" + webSoccketMessage.messageBody + ")");
                    if (webSoccketMessage.messageType === "com.zhxh.imms.web.webSocket.WebSocketPongMessage") {
                        me.server_time = body.currentDateTime;
                    } else if (me.onMessage) {
                        me.onMessage(body);
                    }
                } catch (e) {
                    console.error(e);
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    close() {
        if (this.websocket) {
            this.websocket.close();
            this.websocket = null;
        }
    }

    send(msgType, msg) {
        const webSocketMessage = {
            messageType: msgType,
            messageBody: JSON.stringify(msg)
        };
        const strMsg = JSON.stringify(webSocketMessage);
        if (msgType === "com.zhxh.imms.web.webSocket.WebSocketPingMessage") {
            console.log("ping to server ...");
        } else {
            console.log("sending to server:" + strMsg);
        }
        try {
            this.websocket.send(strMsg);
        } catch (e) {
            console.log(e);
        }
        console.log("message sent.");
    }

    reconnect() {
        console.log("ready to reconnect ...");
        const me = this;
        me.waitTime = +new Date();
        setTimeout(function () {
            me.open(me.openConfig);
        }, me.reconnectTime);
    }
}
