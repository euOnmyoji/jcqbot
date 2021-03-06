package com.meng.tip;

import com.meng.*;
import com.meng.config.*;
import com.meng.config.javabeans.*;
import com.meng.modules.*;
import com.meng.tools.*;
import java.io.*;
import java.util.*;

public class MTimeTip extends BaseModule implements Runnable {
    private final long groupYuTang = 617745343L;
    private final long groupDNFmoDao = 424838564L;
    private final long groupXueXi = 312342896L;
    private final long alice = 1326051907L;
    private final long YYS = 1418780411L;
    private boolean tipedYYS = true;
    private boolean tipedAlice = true;

	@Override
	public BaseModule load() {
		enable = true;
		return this;
	}

    @Override
    public void run() {
        while (true) {
            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.MINUTE) == 0) {
                tipedAlice = false;
                if (c.get(Calendar.HOUR_OF_DAY) == 23) {
                    Autoreply.instance.threadPool.execute(new Runnable() {
							@Override
							public void run() {
								for (GroupConfig groupConfig : ConfigManager.instance.configJavaBean.groupConfigs) {
									if ((groupConfig.f1 & (1 << ModuleManager.ID_MainSwitch)) != 0) {
										if (Autoreply.sendMessage(groupConfig.n, 0, "少女休息中...", true) < 0) {
											continue;
										}
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
								Autoreply.sleeping = true;
							}
						});
                }
                if (c.get(Calendar.HOUR_OF_DAY) == 6) {
                    Autoreply.sleeping = false;
                }
                if (c.get(Calendar.HOUR_OF_DAY) % 3 == 0) {
                    tipedYYS = false;
                }
                if ((c.get(Calendar.HOUR_OF_DAY) == 11) && c.get(Calendar.MINUTE) == 0) {
                    Autoreply.instance.zanManager.sendZan();
                }
                if (getTipHour(c)) {
                    if (c.getActualMaximum(Calendar.DAY_OF_MONTH) == c.get(Calendar.DATE)) {
                        Autoreply.sendMessage(groupDNFmoDao, 0, "最后一天莉，，，看看冒险团商店");
                        Autoreply.sendMessage(groupXueXi, 0, "最后一天莉，，，看看冒险团商店");
                    }
                    if (c.get(Calendar.DAY_OF_WEEK) == 4) {
						Autoreply.sendMessage(groupDNFmoDao, 0, "星期三莉，，，看看成长胶囊");
						Autoreply.sendMessage(groupXueXi, 0, "星期三莉，，，看看成长胶囊");
                    }
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean getTipHour(Calendar c) {
        return (c.get(Calendar.HOUR_OF_DAY) == 12 || c.get(Calendar.HOUR_OF_DAY) == 16 || c.get(Calendar.HOUR_OF_DAY) == 22);
    }

	@Override
	protected boolean processMsg(long fromGroup, long fromQQ, String msg, int msgId, File[] imgs) {
		if (!tipedYYS && fromGroup == groupYuTang && fromQQ == YYS) {
            String[] strings = new String[]{"想吃YYS", "想食YYS", "想上YYS", Autoreply.instance.CC.at(1418780411L) + "老婆"};
            Autoreply.sendMessage(groupYuTang, 0, (String) Tools.ArrayTool.rfa(strings));
            tipedYYS = true;
            return true;
        }
        if (!tipedAlice && fromQQ == alice) {
            Autoreply.sendMessage(fromGroup, 0,
								  Autoreply.instance.CC.image(new File(Autoreply.appDirectory + "pic\\alice.jpg")));
            tipedAlice = true;
            return true;
        }
        return false;
    }

}
