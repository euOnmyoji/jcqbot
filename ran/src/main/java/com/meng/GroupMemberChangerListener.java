package com.meng;

import com.meng.config.*;
import com.meng.config.javabeans.*;
import com.meng.modules.*;
import com.meng.tools.*;
import com.sobte.cqp.jcq.entity.*;

import static com.meng.Autoreply.sendMessage;

public class GroupMemberChangerListener {

    public GroupMemberChangerListener() {
    }

    public void checkIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        if (ConfigManager.instance.isBlackQQ(beingOperateQQ)) {
            Tools.CQ.ban(fromGroup, fromQQ, 300);
        }
        PersonInfo personInfo = ConfigManager.instance.getPersonInfoFromQQ(beingOperateQQ);
        if (personInfo != null && personInfo.name.equals("熊哥")) {
            sendMessage(959615179L, 0, Autoreply.instance.CC.at(-1) + "熊加入了群" + fromGroup, true);
            return;
        }
        if (!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_MainSwitch)) {
            return;
        }
        if (personInfo != null) {
            sendMessage(fromGroup, 0, "欢迎" + ConfigManager.instance.getNickName(beingOperateQQ), true);
        } else {
            sendMessage(fromGroup, 0, "欢迎新大佬", true);
        }
		ConfigManager.instance.addAutoAllow(beingOperateQQ);
		/*  if (fromGroup == 859561731L) { // 台长群
		 sendMessage(859561731L, 0, "芳赛服务器炸了", true);
		 try { sendMessage(859561731L, 0, CC.image(new File(appDirectory +
		 "pic/sjf9961.jpg"))); } catch (IOException e) {
		 e.printStackTrace(); }
		 } */
    }

    public void checkDecrease(int subtype, int sendTime, final long fromGroup, final long fromQQ, long beingOperateQQ) {
        if (subtype == 1) {
			//  if (beingOperateQQ == 2856986197L) {
			//	if(fromGroup==Autoreply.mainGroup){
			//		return;
			//	}
			//     Autoreply.CQ.setGroupLeave(fromGroup, false);
			//    }
            if (!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_MainSwitch)) {
                return;
            }
            if (ConfigManager.instance.isBlackQQ(beingOperateQQ)) {
                return;
            }
            QQInfo qInfo = Autoreply.CQ.getStrangerInfo(beingOperateQQ);
            sendMessage(fromGroup, 0, ConfigManager.instance.getNickName(beingOperateQQ)  + "(" + qInfo.getQqId() + ")" + "跑莉", true);
        } else if (subtype == 2) {
            if (beingOperateQQ == 2856986197L) {
                Autoreply.instance.threadPool.execute(new Runnable() {
						@Override
						public void run() {
							ConfigManager.instance.addBlack(fromGroup, fromQQ);
							Autoreply.CQ.setGroupLeave(fromGroup, false);
						}
					});
                return;
            }
            if (beingOperateQQ == 2558395159L) {
                Autoreply.CQ.setGroupLeave(fromGroup, false);
                return;
            }
            if (beingOperateQQ == Autoreply.CQ.getLoginQQ()) {
                ConfigManager.instance.addBlack(fromGroup, fromQQ);
                return;
            }
            if (!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_MainSwitch)) {
                return;
            }
            if (ConfigManager.instance.isNotReplyQQ(beingOperateQQ)) {
                return;
            }
            QQInfo qInfo = Autoreply.CQ.getStrangerInfo(beingOperateQQ);
            QQInfo qInfo2 = Autoreply.CQ.getStrangerInfo(fromQQ);
            ConfigManager.instance.removeAutoAllow(beingOperateQQ);
            sendMessage(fromGroup, 0, ConfigManager.instance.getNickName(beingOperateQQ) + "(" + qInfo.getQqId() + ")" + "被" + ConfigManager.instance.getNickName(fromQQ) + "(" + qInfo2.getQqId() + ")" + "玩完扔莉", true);
        }
    }


}
