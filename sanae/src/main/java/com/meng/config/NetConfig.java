package com.meng.config;

import com.google.gson.reflect.*;
import com.meng.*;
import com.meng.groupMsgProcess.*;
import com.meng.remote.*;
import com.meng.tools.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import org.java_websocket.client.*;
import org.java_websocket.exceptions.*;
import org.java_websocket.handshake.*;

public class NetConfig extends WebSocketClient {

	private HashMap<Integer,TaskResult> resultMap=new HashMap<>();
	public NetConfig(URI uri) {
		super(uri);
	}

	@Override
	public void onMessage(String p1) {
		System.out.println("strMsg:" + p1);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		send(SanaeDataPack.encode(SanaeDataPack.opConfigFile));
		System.out.println("连接到蓝");
		Autoreply.ins.threadPool.execute(new Runnable(){

				@Override
				public void run() {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						send(SanaeDataPack.encode(SanaeDataPack.opHeartBeat));
					} catch (WebsocketNotConnectedException e) {
						System.out.println("和蓝的连接已断开");
						e.printStackTrace();
						reconnect();
					}
				}
			});
	}

	@Override
	public void onMessage(ByteBuffer bs) {
		++RemoteWebSocket.botInfoBean.recFrom;
		SanaeDataPack dataRec=SanaeDataPack.decode(bs.array());
		switch (dataRec.getOpCode()) {
			case SanaeDataPack.opConfigFile:
				Type type = new TypeToken<RanConfigBean>() {
				}.getType();
				ConfigManager.instence.RanConfig = Autoreply.gson.fromJson(dataRec.readString(), type);
				ModuleManager.instence.getModule(ModuleSeq.class.getSimpleName()).load();
				break;
			case SanaeDataPack.opGameOverSpell:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;
			case SanaeDataPack.opGameOverPersent:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readInt()));
				break;
			case SanaeDataPack.opGrandma:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;
			case SanaeDataPack.opMusicName:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;
			case SanaeDataPack.opGotSpells:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;
			case SanaeDataPack.opNeta:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;			
			case SanaeDataPack.opSeqContent:
				resultMap.put(dataRec.getOpCode(), new TaskResult(dataRec.readString()));
				break;
			case SanaeDataPack.opSendMsg:
				Autoreply.sendMessage(dataRec.readLong(), dataRec.readLong(), dataRec.readString());
				break;
			case SanaeDataPack.opLiveList:
				StringBuilder sb=new StringBuilder();
				while (dataRec.hasNext()) {
					sb.append(dataRec.readString()).append("正在直播:").append(dataRec.readLong()).append("\n");
				}
				resultMap.put(dataRec.getOpCode(), new TaskResult(sb.toString()));
				break;
			case SanaeDataPack.opLiveStart:
				Autoreply.sendMessage(Autoreply.mainGroup, 0, dataRec.readString() + "开始直播" + dataRec.readLong());
				break; 
			case SanaeDataPack.opLiveStop:
				Autoreply.sendMessage(Autoreply.mainGroup, 0, dataRec.readString() + "停止直播" + dataRec.readLong());
				break;
			case SanaeDataPack.opSpeakInliveRoom:
				//直播间说话 string(主播称呼) long(blid) string(说话者称呼) long(说话者bid)
				break; 
			case SanaeDataPack.opNewVideo:
				Autoreply.sendMessage(Autoreply.mainGroup, 0, dataRec.readString() + "发布新视频:" + dataRec.readString() + "(av" + dataRec.readLong() + ")");
				break;
			case SanaeDataPack.opNewArtical:
				Autoreply.sendMessage(Autoreply.mainGroup, 0, dataRec.readString() + "发布新专栏:" + dataRec.readString() + "(cv" + dataRec.readLong() + ")");
				break;
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {

	}

	@Override
	public void onError(Exception e) {
		e.printStackTrace();
	}

	public String getOverSpell(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opGameOverSpell).write(fromQQ));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opGameOverSpell).data);
	}

	public int getOverPersent(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opGameOverPersent).write(fromQQ));
		return Tools.BitConverter.toInt(getTaskResult(SanaeDataPack.opGameOverPersent).data);
	}

	public String getGrandma(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opGrandma).write(fromQQ));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opGrandma).data);
	}

	public String getMusicName(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opMusicName).write(fromQQ));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opMusicName).data);
	}

	public String getSpells(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opGotSpells).write(fromQQ));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opGotSpells).data);
	}

	public String getNeta(long fromQQ) {
		send(SanaeDataPack.encode(SanaeDataPack.opNeta).write(fromQQ));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opNeta).data);
	}

	public String getSeq() {
		send(SanaeDataPack.encode(SanaeDataPack.opSeqContent));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opSeqContent).data);
	}

	public String getLiveList() {
		send(SanaeDataPack.encode(SanaeDataPack.opLiveList));
		return Tools.BitConverter.toString(getTaskResult(SanaeDataPack.opLiveList).data);
	}

	public void setWelcome(long group, String welcome) {
		ConfigManager.instence.SanaeConfig.welcomeMap.put(group, welcome);
		ConfigManager.instence.saveSanaeConfig();
	}

	public String getWelcome(long group) {
		if (ConfigManager.instence.SanaeConfig.welcomeMap.get(group) == null) {
			return null;
		}
		return ConfigManager.instence.SanaeConfig.welcomeMap.get(group);
	}

	private TaskResult getTaskResult(int opCode) {
		int time=3000;
		while (resultMap.get(opCode) == null && time-- > 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		TaskResult tr=resultMap.get(opCode);
		resultMap.remove(opCode);
		return tr;
	}

	public void setNickName(long qq, String nickname) {
		if (nickname != null) {
			ConfigManager.instence.RanConfig.nicknameMap.put(qq, nickname);
			send(SanaeDataPack.encode(SanaeDataPack.opSetNick).write(qq).write(nickname));
		} else {
			ConfigManager.instence.RanConfig.nicknameMap.remove(qq);
			send(SanaeDataPack.encode(SanaeDataPack.opSetNick).write(qq));
		}
	}

	public String getNickName(long qq) {
		String nick=null;
		nick = ConfigManager.instence.RanConfig.nicknameMap.get(qq);
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
		nick = ConfigManager.instence.RanConfig.nicknameMap.get(qq);
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
        return fromQQ == 1594703250L || fromQQ == 2856986197L || fromQQ == 8255053L || fromQQ == 1592608126L || fromQQ == 1620628713L || fromQQ == 2565128043L;
    }

    public boolean isAdmin(long fromQQ) {
        return ConfigManager.instence.RanConfig.adminList.contains(fromQQ) || isMaster(fromQQ);
    }

    public boolean isNotReplyQQ(long qq) {
        return ConfigManager.instence.RanConfig.QQNotReply.contains(qq) || ConfigManager.instence.RanConfig.blackListQQ.contains(qq);
    }

    public boolean isBlackQQ(long qq) {
        return ConfigManager.instence.RanConfig.blackListQQ.contains(qq);
    }

    public boolean isBlackGroup(long qq) {
        return ConfigManager.instence.RanConfig.blackListGroup.contains(qq);
    }

    public boolean isNotReplyWord(String word) {
        for (String nrw : ConfigManager.instence.RanConfig.wordNotReply) {
            if (word.contains(nrw)) {
                return true;
            }
        }
        return false;
    }

    public PersonInfo getPersonInfoFromQQ(long qq) {
        for (PersonInfo pi : ConfigManager.instence.RanConfig.personInfo) {
            if (pi.qq == qq) {
                return pi;
            }
        }
        return null;
    }

    public PersonInfo getPersonInfoFromName(String name) {
        for (PersonInfo pi : ConfigManager.instence.RanConfig.personInfo) {
            if (pi.name.equals(name)) {
                return pi;
            }
        }
        return null;
    }

    public PersonInfo getPersonInfoFromBid(long bid) {
        for (PersonInfo pi : ConfigManager.instence.RanConfig.personInfo) {
            if (pi.bid == bid) {
                return pi;
            }
        }
        return null;
    }

	public PersonInfo getPersonInfoFromLiveId(long lid) {
        for (PersonInfo pi : ConfigManager.instence.RanConfig.personInfo) {
            if (pi.bliveRoom == lid) {
                return pi;
			}
		}
        return null;
	}

	public PersonConfig getPersonCfg(long fromQQ) {
		PersonConfig pcfg=ConfigManager.instence.SanaeConfig.personCfg.get(fromQQ);
		if (pcfg == null) {
			pcfg = new PersonConfig();
			ConfigManager.instence.SanaeConfig.personCfg.put(fromQQ, pcfg);
		}
		return pcfg;
	}

    public void addBlack(long group, final long qq) {
        ConfigManager.instence.RanConfig.blackListQQ.add(qq);
        ConfigManager.instence.RanConfig.blackListGroup.add(group);
		send(SanaeDataPack.encode(SanaeDataPack.opAddBlack).write(group).write(qq));
        Autoreply.sendMessage(Autoreply.mainGroup, 0, "已将用户" + qq + "加入黑名单");
        Autoreply.sendMessage(Autoreply.mainGroup, 0, "已将群" + group + "加入黑名单");
		Autoreply.CQ.setGroupLeave(group, false);
    }

	public void addReport(long fromGroup, long fromQQ, String content) {
		ConfigManager.instence.SanaeConfig.addReport(fromGroup, fromQQ, content.substring(4));
		ConfigManager.instence.saveSanaeConfig();
	}

	public void addBugReport(long fromGroup, long fromQQ, String content) {
		ConfigManager.instence.SanaeConfig.addBugReport(fromGroup, fromQQ, content.substring(6));
		ConfigManager.instence.saveSanaeConfig();
	}

	public SanaeConfigJavaBean.ReportBean getReport() {
		return ConfigManager.instence.SanaeConfig.getReport();
	}

	public SanaeConfigJavaBean.ReportBean removeReport() {
		return ConfigManager.instence.SanaeConfig.removeReport();
	}

	public void reportToLast() {
		ConfigManager.instence.SanaeConfig.reportToLast();
	}

	public SanaeConfigJavaBean.BugReportBean removeBugReport() {
		return ConfigManager.instence.SanaeConfig.removeBugReport();
	}

	public void bugReportToLast() {
		ConfigManager.instence.SanaeConfig.bugReportToLast();
	}

	public SanaeConfigJavaBean.BugReportBean getBugReport() {
		return ConfigManager.instence.SanaeConfig.getBugReport();
	}

	public void send(SanaeDataPack sdp) {
		++RemoteWebSocket.botInfoBean.sendTo;
		send(sdp.getData());
    }
}
