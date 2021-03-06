package com.meng.config;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.meng.*;
import com.meng.config.javabeans.*;
import com.meng.config.sanae.*;
import com.meng.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;

public class ConfigManager {
	public static ConfigManager instance;
    public RanCfgBean configJavaBean = new RanCfgBean();
    public PortConfig portConfig = new PortConfig();

    public ConfigManager() {
		instance = this;
        portConfig = Autoreply.gson.fromJson(Tools.FileTool.readString(Autoreply.appDirectory + "grzxEditConfig.json"), PortConfig.class);
        File jsonBaseConfigFile = new File(Autoreply.appDirectory + "configV3.json");
        if (!jsonBaseConfigFile.exists()) {
            saveConfig();
        }
        Type type = new TypeToken<RanCfgBean>() {
        }.getType();
        configJavaBean = Autoreply.gson.fromJson(Tools.FileTool.readString(Autoreply.appDirectory + "configV3.json"), type);
        Autoreply.instance.threadPool.execute(new SocketConfigManager(this));
        Autoreply.instance.threadPool.execute(new SocketDicManager(this));
    }

	public boolean containsGroup(long group) {
		for (GroupConfig gf:configJavaBean.groupConfigs) {
			if (gf.n == group) {
				return true;
			}
		}
		return false;
	}

	public boolean isFunctionEnable(long fromGroup, int functionID) {
		GroupConfig gc=getGroupConfig(fromGroup);
		if (gc == null) {
			return false;
		}
		return (gc.f1 & (1 << functionID)) != 0;
	}

	public void setFunctionEnabled(long fromGroup, int functionID, boolean enable) {
		GroupConfig gc=getGroupConfig(fromGroup);
		if (enable) {
			gc.f1 |= (1 << functionID);
		} else {
			gc.f1 &= ~(1 << functionID);
		}
		saveConfig();
	}

	public void setNickName(long qq, String nickname) {
		if (nickname != null) {
			configJavaBean.nicknameMap.put(qq, nickname);
		} else {
			configJavaBean.nicknameMap.remove(qq);
		}
		saveConfig();
	}

	public String getNickName(long qq) {
		String nick=null;
		nick = configJavaBean.nicknameMap.get(qq);
		if (nick == null) {
			PersonInfo pi=getPersonInfoFromQQ(qq);
			if (pi == null) {
				nick = Autoreply.CQ.getStrangerInfo(qq).getNick();
			} else {
				nick = pi.name;
			}
		}
		return nick;
	}

	public String getNickName(long group, long qq) {
		String nick=null;
		nick = configJavaBean.nicknameMap.get(qq);
		if (nick == null) {
			PersonInfo pi=getPersonInfoFromQQ(qq);
			if (pi == null) {
				nick = Autoreply.CQ.getGroupMemberInfo(group, qq).getNick();
			} else {
				nick = pi.name;
			}
		}
		return nick;
	}

    public boolean isMaster(long fromQQ) {
        return configJavaBean.masterList.contains(fromQQ);
    }

    public boolean isAdmin(long fromQQ) {
        return configJavaBean.adminList.contains(fromQQ) || configJavaBean.masterList.contains(fromQQ);
    }

    public boolean isGroupAutoAllow(long fromQQ) {
        return configJavaBean.groupAutoAllowList.contains(fromQQ) || configJavaBean.adminList.contains(fromQQ) || configJavaBean.masterList.contains(fromQQ);
    }

    public GroupConfig getGroupConfig(long fromGroup) {
        for (GroupConfig gc : configJavaBean.groupConfigs) {
            if (fromGroup == gc.n) {
                return gc;
            }
        }
        return null;
    }

	public void addAutoAllow(long qq) {
		configJavaBean.groupAutoAllowList.add(qq);
		Autoreply.sendMessage(Autoreply.mainGroup, 0, "自动同意列表添加用户" + qq);
		saveConfig();
	}

	public void removeAutoAllow(long qq) {
		configJavaBean.groupAutoAllowList.remove(qq);
		Autoreply.sendMessage(Autoreply.mainGroup, 0, "自动同意列表移除用户" + qq);
		saveConfig();
	}

    public boolean isNotReplyQQ(long qq) {
        return configJavaBean.QQNotReply.contains(qq) || configJavaBean.blackListQQ.contains(qq);
    }

    public boolean isBlackQQ(long qq) {
        return configJavaBean.blackListQQ.contains(qq);
    }

    public boolean isBlackGroup(long qq) {
        return configJavaBean.blackListGroup.contains(qq);
    }

    public boolean isNotReplyWord(String word) {
        for (String nrw : configJavaBean.wordNotReply) {
            if (word.contains(nrw)) {
                return true;
            }
        }
        return false;
    }

    public PersonInfo getPersonInfoFromQQ(long qq) {
        for (PersonInfo pi : configJavaBean.personInfo) {
            if (pi.qq == qq) {
                return pi;
            }
        }
        return null;
    }

    public PersonInfo getPersonInfoFromName(String name) {
        for (PersonInfo pi : configJavaBean.personInfo) {
            if (pi.name.equals(name)) {
                return pi;
            }
        }
        return null;
    }

    public PersonInfo getPersonInfoFromBid(long bid) {
        for (PersonInfo pi : configJavaBean.personInfo) {
            if (pi.bid == bid) {
                return pi;
            }
        }
        return null;
    }

	public PersonInfo getPersonInfoFromLiveId(long lid) {
        for (PersonInfo pi : configJavaBean.personInfo) {
            if (pi.bliveRoom == lid) {
                return pi;
			}
		}
        return null;
	}

    public void addBlack(long group, final long qq) {
        configJavaBean.blackListQQ.add(qq);
        configJavaBean.blackListGroup.add(group);
        for (GroupConfig groupConfig : configJavaBean.groupConfigs) {
            if (groupConfig.n == group) {
                configJavaBean.groupConfigs.remove(groupConfig);
                break;
            }
        }
        saveConfig();
        Autoreply.instance.threadPool.execute(new Runnable() {
				@Override
				public void run() {
					//    HashSet<Group> groups = Tools.CQ.findQQInAllGroup(qq);
					//   for (Group g : groups) {
                    // if (Tools.CQ.ban(g.getId(), qq, 300)) {
                    //    sendMessage(g.getId(), 0, "不要问为什么你会进黑名单，你干了什么自己知道");
                    //   }
					//    }
				}
			});
        Autoreply.sendMessage(Autoreply.mainGroup, 0, "已将用户" + qq + "加入黑名单");
        Autoreply.sendMessage(Autoreply.mainGroup, 0, "已将群" + group + "加入黑名单");
    }

	public void setOgg(long qqNum) {
		configJavaBean.ogg = qqNum;
		saveConfig();
	}

	class PortConfig {
		public int configPort = 0;
		public int dicPort = 0;
	}

    public void saveConfig() {
		SanaeDataPack sdp = SanaeDataPack.encode(SanaeDataPack.opConfigFile);
		sdp.write(Autoreply.gson.toJson(ConfigManager.instance.configJavaBean));
		Autoreply.instance.sanaeServer.send(sdp);
        try {
            File file = new File(Autoreply.appDirectory + "configV3.json");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(Autoreply.gson.toJson(configJavaBean));
            writer.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
