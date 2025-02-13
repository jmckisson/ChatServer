package com.presence.chat.socket;

import com.presence.chat.protocol.ChatCommand;

public class DecodedMsg {

    private ChatCommand cmd;
    private String msg;
    private byte endByte;

    public void setCmd(ChatCommand cmd) {
        this.cmd = cmd;
    }

    public ChatCommand getCmd() {
        return cmd;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }

    public void setEndByte(byte b) {
        endByte = b;
    }

    public byte getEndByte() {
        return endByte;
    }
}
