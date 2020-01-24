package com.meng.groupMsgProcess;

import com.meng.*;
import com.meng.config.*;
import com.meng.game.TouHou.*;
import com.meng.groupMsgProcess.*;
import com.meng.tools.*;

public class MsgRunnable implements Runnable {
    private int msgId = 0;
    private long fromGroup = 0;
    private long fromQQ = 0;
    private String msg = null;

    public MsgRunnable(long fromGroup, long fromQQ, String msg, int msgId) {
        this.fromGroup = fromGroup;
        this.fromQQ = fromQQ;
        this.msg = msg;
        this.msgId = msgId;
    }

    @Override
    public synchronized void run() {
        ModuleManager.instence.processMsg(fromGroup, fromQQ, msg, msgId);
    }
}