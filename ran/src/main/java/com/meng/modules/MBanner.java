package com.meng.modules;

import com.meng.*;
import com.meng.config.*;
import com.meng.modules.*;
import com.meng.tools.*;
import com.sobte.cqp.jcq.entity.*;
import java.io.*;
import java.util.*;

public class MBanner extends BaseModule {
    public HashMap<Long, HashMap<Long, BanType>> banMap = new HashMap<>();

	@Override
	public BaseModule load() {
		enable = true;
		return this;
	}

	@Override
	protected boolean processMsg(long fromGroup, long fromQQ, String msg, int msgId, File[] imgs) {
		if(!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_Banner)){
			return false;
		}
		if (!ConfigManager.instance.isAdmin(fromQQ)) {
			return false;
		}
		String[] strs = msg.split("\\.");
		switch (strs.length) {
            case 1:
				if (msg.equalsIgnoreCase("allban")) {
					Autoreply.CQ.setGroupWholeBan(fromGroup, true);
					return true;
				}
				if (msg.equalsIgnoreCase("allrelease")) {
					Autoreply.CQ.setGroupWholeBan(fromGroup, false);
					return true;
				}
                break;
            case 2:
                try {
					if (strs[0].equalsIgnoreCase("allban")) {
						Autoreply.CQ.setGroupWholeBan(Long.parseLong(strs[1]), true);
						return true;
					}
					if (strs[0].equalsIgnoreCase("allrelease")) {
						Autoreply.CQ.setGroupWholeBan(Long.parseLong(strs[1]), false);
						return true;
					}
				} catch (Exception e) {
					Autoreply.sendMessage(fromGroup, fromQQ, e.toString());
				}
                break;
            case 3:
                if (strs[0].equals("ban")) {
                    long targetQQ;
                    int time;
                    try {
                        targetQQ = Long.parseLong(strs[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        targetQQ = Autoreply.instance.CC.getAt(strs[1]);
                        if (targetQQ == -1) {
                            return false;
                        }
                    }
                    try {
                        time = Integer.parseInt(strs[2]);
                    } catch (Exception e) {
                        targetQQ = fromQQ;
                        time = 2592000;
                        e.printStackTrace();
                    }
                    HashMap<Long, BanType> targetQQAndType = banMap.get(fromGroup);
					if (targetQQAndType == null) {
						targetQQAndType = new HashMap<>();
					}
                    BanType lastOp = targetQQAndType.get(targetQQ);
					if (lastOp == null) {
						lastOp = BanType.ByUser;
					}
                    BanType thisOp = getType(fromGroup, fromQQ);
                    if (thisOp.getPermission() - lastOp.getPermission() < 0) {
                        Autoreply.sendMessage(fromGroup, fromQQ, "你无法修改等级比你高的人进行的操作");
                        return true;
                    }
                    if (time == 0) {
                        targetQQAndType.remove(targetQQ);
                    } else {
                        targetQQAndType.put(targetQQ, thisOp);
                    }
                    if (checkBan(fromGroup, targetQQ, fromQQ, time)) {
                        return true;
                    }
                }
                break;
            case 4:
                if (strs[0].equals("ban")) {
                    long targetGroup;
                    long targetQQ;
                    int time;
                    try {
                        targetGroup = Long.parseLong(strs[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Autoreply.sendMessage(fromGroup, fromQQ, "群号错误");
                        return false;
                    }
                    try {
                        targetQQ = Long.parseLong(strs[2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        targetQQ = Autoreply.instance.CC.getAt(strs[2]);
                        if (targetQQ == -1) {
                            return false;
                        }
                    }
                    try {
                        time = Integer.parseInt(strs[3]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        targetQQ = fromQQ;
                        time = 2592000;
                    }
                    HashMap<Long, BanType> targetQQAndType = banMap.get(fromGroup);
					if (targetQQAndType == null) {
						targetQQAndType = new HashMap<>();
					}
                    BanType lastOp = targetQQAndType.get(targetQQ);
					if (lastOp == null) {
						lastOp = BanType.ByUser;
					}
                    BanType thisOp = getType(fromGroup, fromQQ);
                    if (thisOp.getPermission() - lastOp.getPermission() < 0) {
                        Autoreply.sendMessage(fromGroup, fromQQ, "你无法修改等级比你高的人进行的操作");
                        return true;
                    }
                    if (time == 0) {
                        targetQQAndType.remove(targetQQ);
                    } else {
                        targetQQAndType.put(targetQQ, thisOp);
                    }
                    if (checkBan(targetGroup, targetQQ, fromQQ, time)) {
                        return true;
                    }
                }
        }
        return checkSleep(fromGroup, fromQQ, strs);
    }

    private boolean checkSleep(long fromGroup, long fromQQ, String[] str) {
        if (str.length == 3 && str[0].equals("sleep")) {
            if (ConfigManager.instance.isAdmin(fromQQ)) {
                return true;
            }
            int time;
            switch (str[1]) {
                case "s":
                case "second":
                case "sec":
                    time = 1;
                    break;
                case "min":
                case "minute":
                    time = 60;
                    break;
                case "h":
                case "hour":
                    time = 3600;
                    break;
                case "d":
                case "day":
                    time = 86400;
                    break;
                case "w":
                case "week":
                    time = 604800;
                    break;
                case "m":
                case "month":
                    time = 2592000;
                    break;
                default:
                    time = 5400;
                    break;
            }
            int sleepTime;
            try {
                sleepTime = Integer.parseInt(str[2]) * time;
                if (sleepTime > 2592000) {
                    sleepTime = 2592000;
                }
            } catch (Exception e) {
                sleepTime = 2592000;
            }
            if (sleepTime <= 0) {
                Tools.CQ.ban(fromGroup, fromQQ, 5400);
                return true;
            } else {
                Tools.CQ.ban(fromGroup, fromQQ, sleepTime);
                return true;
            }
        }
        return false;
    }

    private boolean checkBan(long targetGroup, long targetQQ, long fromQQ, int time) {
        try {
            if (ConfigManager.instance.isAdmin(fromQQ)) {
                if (time > 2592000) {
                    time = 2592000;
                }
            } else {
                if (time > 120) {
                    time = 120;
                }
            }
            if (time < 0) {
                time = 0;
            }
        } catch (Exception e) {
            time = 2592000;
            targetQQ = fromQQ;
        }
        if (ConfigManager.instance.isMaster(targetQQ)) {
            if (!ConfigManager.instance.isMaster(fromQQ)) {
                Tools.CQ.ban(targetGroup, fromQQ, time);
                return true;
            }
        } else {
            Tools.CQ.ban(targetGroup, targetQQ, time);
            return true;
        }
        return false;
    }

    private BanType getType(long fromGroup, long fromQQ) {
        if (ConfigManager.instance.isMaster(fromQQ)) {
            return BanType.ByMaster;
        }
        Member member = Autoreply.CQ.getGroupMemberInfoV2(fromGroup, fromQQ);
        if (member.getAuthority() == 3) {
            return BanType.ByGroupMaster;
        }
        if (ConfigManager.instance.isAdmin(fromQQ)) {
            return BanType.ByAdmin;
        }
        if (member.getAuthority() == 2) {
            return BanType.ByGroupAdmin;
        }
        return BanType.ByUser;
    }

	public enum BanType {
		ByMaster(4),
		ByGroupMaster(3),
		ByAdmin(2),
		ByGroupAdmin(1),
		ByUser(0);

		private int permission;

		BanType(int permission) {
			this.permission = permission;
		}

		public int getPermission() {
			return permission;
		}
	}
}
