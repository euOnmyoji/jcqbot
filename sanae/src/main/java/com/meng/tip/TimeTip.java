package com.meng.tip;

import com.meng.*;
import com.meng.bilibili.*;
import com.meng.config.*;
import com.meng.groupMsgProcess.*;
import com.sobte.cqp.jcq.entity.*;
import java.util.*;

public class TimeTip implements Runnable {

	private String[] goodMorning = new String[]{
		"早上好",
		"早安",
		"早",
		"大家早上好",
		"大家早上好啊.."
	};
	private String[] goodEvening = new String[]{
		"晚安",
		"大家晚安",
		"晚安....",
		"大家晚安....",
		"大家早点休息吧"
	};
    public TimeTip() {
    }

    @Override
    public void run() {
        while (true) {
            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.MINUTE) == 0) {
				if (c.get(Calendar.HOUR_OF_DAY) == 11) {
					for (long l : ConfigManager.instence.RanConfig.adminList) {
						Autoreply.CQ.sendLikeV2(l, 10);
					}
					for (long l : ConfigManager.instence.SanaeConfig.zanSet) {
						Autoreply.CQ.sendLikeV2(l, 10);
					}
				}
				if (c.get(Calendar.HOUR_OF_DAY) == 0) {
					for (BiliUser bm:ConfigManager.instence.SanaeConfig.biliMaster.values()) {
						for (BiliUser.FansInGroup fans:bm.fans) {
							ModuleFaith mf=((ModuleFaith)ModuleManager.instence.getModule(ModuleFaith.class));
							if (mf.getFaith(fans.qq) > 0) {
								mf.subFaith(fans.qq, 1);
							}
						}
					}
				}
				if (c.get(Calendar.HOUR_OF_DAY) == 22) {
					Autoreply.ins.threadPool.execute(new Runnable() {
							@Override
							public void run() {
								List<Group> groupList=Autoreply.CQ.getGroupList();
								for (Group g:groupList) {
									if (Autoreply.sendMessage(g.getId(), 0, goodEvening) < 0) {
										continue;
									}
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								Autoreply.sleeping = true;
							}
						});
				}
				if (c.get(Calendar.HOUR_OF_DAY) == 6) {
					Autoreply.ins.threadPool.execute(new Runnable() {
							@Override
							public void run() {
								Autoreply.sleeping = false;
								List<Group> groupList=Autoreply.CQ.getGroupList();
								for (Group g:groupList) {
									if (Autoreply.sendMessage(g.getId(), 0, goodMorning) < 0) {
										continue;
									}
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						});
				}          
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
}
