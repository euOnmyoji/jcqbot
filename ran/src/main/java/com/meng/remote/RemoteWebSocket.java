package com.meng.remote;

import com.meng.*;
import com.meng.config.*;
import com.meng.config.javabeans.*;
import com.sobte.cqp.jcq.entity.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import org.java_websocket.*;
import org.java_websocket.handshake.*;
import org.java_websocket.server.*;

public class RemoteWebSocket extends WebSocketServer {
	BotDataPack msgPack;
	public static BotMsgInfo botInfoBean=new BotMsgInfo();
	public RemoteWebSocket() {
		super(new InetSocketAddress(8888));
		Autoreply.instance.threadPool.execute(new Runnable(){

				@Override
				public void run() {
					while (true) {
						msgPack = BotDataPack.encode(BotDataPack.onGroupMsg);
						try {
							Thread.sleep(1000);
							broadcast(msgPack.getData());
							BotDataPack bbbbb=BotDataPack.encode(BotDataPack.onPerSecMsgInfo);
							bbbbb.write(botInfoBean.sendTo).write(botInfoBean.recFrom).write(botInfoBean.msgPerSec).write(botInfoBean.msgCmdPerSec).write(botInfoBean.msgSendPerSec);
							broadcast(bbbbb.getData());
							botInfoBean.reset();
						} catch (Exception e) {}
					}
				}
			});
	}
	@Override
	public void onOpen(WebSocket p1, ClientHandshake p2) {
		System.out.println("remote connect");
	}

	@Override
	public void onClose(WebSocket p1, int p2, String p3, boolean p4) {
		System.out.println("remote disconnect");
	}

	@Override
	public void onMessage(WebSocket p1, String p2) {
		// TODO: Implement this method
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		BotDataPack rec=BotDataPack.decode(message.array());
		BotDataPack toSend=null;
		switch (rec.getOpCode()) {
			case BotDataPack.opLoginQQ:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.CQ.getLoginQQ());
				break;
			case BotDataPack.opLoginNick:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.CQ.getLoginNick());
				break;
			case BotDataPack.opPrivateMsg:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.sendMessage(0, rec.readLong(), rec.readString()));
				break;
			case BotDataPack.opGroupMsg:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.sendMessage(rec.readLong(), 0, rec.readString()));
				break;
			case BotDataPack.opDiscussMsg:
				//toSend = BotDataPack.encode(botDataPack.getOpCode());
				break;
			case BotDataPack.opDeleteMsg:
				Autoreply.CQ.deleteMsg(rec.readInt());
				break;
			case BotDataPack.opSendLike:
				Autoreply.CQ.sendLikeV2(rec.readLong(), rec.readInt());
				break;
			case BotDataPack.opCookies:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend .write(Autoreply.CQ.getCookies());
				break;
			case BotDataPack.opCsrfToken:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.CQ.getCsrfToken());
				break;
			case BotDataPack.opRecord:
				//	toSend = BotDataPack.encode(rec.getOpCode());
				break;
			case BotDataPack.opGroupKick:
				Autoreply.CQ.setGroupKick(rec.readLong(), rec.readLong(), rec.readBoolean());
				break;
			case BotDataPack.opGroupBan:
				Autoreply.CQ.setGroupBan(rec.readLong(), rec.readLong(), rec.readLong());
				break;
			case BotDataPack.opGroupAdmin:
				Autoreply.CQ.setGroupAdmin(rec.readLong(), rec.readLong(), rec.readBoolean());
				break;
			case BotDataPack.opGroupWholeBan:
				Autoreply.CQ.setGroupWholeBan(rec.readLong(), rec.readBoolean());
				break;
			case BotDataPack.opGroupAnonymousBan:
				Autoreply.CQ.setGroupAnonymousBan(rec.readLong(), rec.readString(), rec.readLong());
				break;
			case BotDataPack.opGroupAnonymous:
				Autoreply.CQ.setGroupAnonymous(rec.readLong(), rec.readBoolean());
				break;
			case BotDataPack.opGroupCard:
				Autoreply.CQ.setGroupCard(rec.readLong(), rec.readLong(), rec.readString());
				break;
			case BotDataPack.opGroupLeave:
				Autoreply.CQ.setGroupLeave(rec.readLong(), rec.readBoolean());
				break;
			case BotDataPack.opGroupSpecialTitle:
				Autoreply.CQ.setGroupSpecialTitle(rec.readLong(), rec.readLong(), rec.readString(), rec.readLong());
				break;
			case BotDataPack.opGroupMemberInfo:
				toSend = BotDataPack.encode(rec.getOpCode());
				Member m=Autoreply.CQ.getGroupMemberInfo(rec.readLong(), rec.readLong());
				toSend.
					write(m.getGroupId()).
					write(m.getQqId()).
					write(m.getNick()).
					write(m.getCard()).
					write(m.getGender()).
					write(m.getAge()).
					write(m.getArea()).
					write(m.getAddTime().getTime()).
					write(m.getLastTime().getTime()).
					write(m.getLevelName()).
					write(m.getAuthority()).
					write(m.getTitle()).
					write(m.getTitleExpire().getTime()).
					write(m.isBad()).
					write(m.isModifyCard());
				break;
			case BotDataPack.opDiscussLeave:
				toSend = BotDataPack.encode(rec.getOpCode());
				break;
			case BotDataPack.opFriendAddRequest:
				toSend = BotDataPack.encode(rec.getOpCode());
				Autoreply.CQ.setFriendAddRequest(rec.readString(), rec.readInt(), rec.readString());
				break;
			case BotDataPack.opGroupMemberList:
				toSend = BotDataPack.encode(rec.getOpCode());
				List<Member> ml=Autoreply.CQ.getGroupMemberList(rec.readLong());
				for (Member mlm:ml) {
					toSend.
						write(mlm.getGroupId()).
						write(mlm.getQqId()).
						write(mlm.getNick()).
						write(mlm.getCard()).
						write(mlm.getGender()).
						write(mlm.getAge()).
						write(mlm.getArea()).
						write(mlm.getAddTime().getTime()).
						write(mlm.getLastTime().getTime()).
						write(mlm.getLevelName()).
						write(mlm.getAuthority()).
						write(mlm.getTitle()).
						write(mlm.getTitleExpire().getTime()).
						write(mlm.isBad()).
						write(mlm.isModifyCard());
				}
				break;
			case BotDataPack.opGroupList:
				toSend = BotDataPack.encode(rec.getOpCode());
				List<Group> gl=Autoreply.CQ.getGroupList();
				for (Group g:gl) {
					toSend.write(g.getId()).write(g.getName());
				}
				break;
			case BotDataPack.getConfig:
				toSend = BotDataPack.encode(rec.getOpCode());
				toSend.write(Autoreply.gson.toJson(ConfigManager.instance.configJavaBean));
				break;
			case BotDataPack.opEnableFunction:
				ConfigManager.instance.setFunctionEnabled(rec.readLong(), rec.readInt(), rec.readInt() == 1);
				break;
			case BotDataPack.addGroup:
				GroupConfig g1c=new GroupConfig();
				g1c.n = rec.readLong();
				ConfigManager.instance.configJavaBean.groupConfigs.add(g1c);
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "添加群" + g1c.n);
				break;
			case BotDataPack.addNotReplyUser:
				ConfigManager.instance.configJavaBean.QQNotReply.add(rec.readLong());
				break;
			case BotDataPack.addNotReplyWord:
				ConfigManager.instance.configJavaBean.wordNotReply.add(rec.readString());
				break;
			case BotDataPack.addPersonInfo:
				ConfigManager.instance.configJavaBean.personInfo.add(Autoreply.gson.fromJson(rec.readString(), PersonInfo.class));
				break;
			case BotDataPack.addMaster:
				long master=rec.readLong();
				ConfigManager.instance.configJavaBean.masterList.add(master);
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "添加master" + master);
				break;
			case BotDataPack.addAdmin:
				long admin=rec.readLong();
				ConfigManager.instance.configJavaBean.adminList.add(admin);
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "添加admin" + admin);
				break;
			case BotDataPack.addGroupAllow:
				ConfigManager.instance.addAutoAllow(rec.readLong());
				break;
			case BotDataPack.addBlackQQ:
				ConfigManager.instance.configJavaBean.blackListQQ.add(rec.readLong());
				break;
			case BotDataPack.addBlackGroup:
				ConfigManager.instance.configJavaBean.blackListGroup.add(rec.readLong());
				break;
			case BotDataPack.removeGroup:
				long gcn=rec.readLong();
				Iterator<GroupConfig> iterator=ConfigManager.instance.configJavaBean.groupConfigs.iterator();
				while (iterator.hasNext()) {
					GroupConfig gc=iterator.next();
					if (gc.n == gcn) {
						iterator.remove();
						break;
					}
				}
				break;
			case BotDataPack.removeNotReplyUser:
				ConfigManager.instance.configJavaBean.QQNotReply.remove(rec.readLong());
				break;
			case BotDataPack.removeNotReplyWord:
				ConfigManager.instance.configJavaBean.wordNotReply.remove(rec.readString());
				break;
			case BotDataPack.removePersonInfo:
				ConfigManager.instance.configJavaBean.personInfo.remove(Autoreply.gson.fromJson(rec.readString(), PersonInfo.class));
				break;
			case BotDataPack.removeMaster:
				long rm=rec.readLong();
				ConfigManager.instance.configJavaBean.masterList.remove(rm);
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "移除master" + rm);
				break;
			case BotDataPack.removeAdmin:
				long ra=rec.readLong();
				ConfigManager.instance.configJavaBean.adminList.remove(ra);
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "移除admin" + ra);
				break;
			case BotDataPack.removeGroupAllow:
				ConfigManager.instance.removeAutoAllow(rec.readLong());
				break;
			case BotDataPack.removeBlackQQ:
				ConfigManager.instance.configJavaBean.blackListQQ.remove(rec.readLong());
				break;
			case BotDataPack.removeBlackGroup:
				ConfigManager.instance.configJavaBean.blackListGroup.remove(rec.readLong());
				break;
			case BotDataPack.setPersonInfo:
				PersonInfo oldPersonInfo = Autoreply.gson.fromJson(rec.readString(), PersonInfo.class);
				PersonInfo newPersonInfo = Autoreply.gson.fromJson(rec.readString(), PersonInfo.class);
				for (PersonInfo pi : ConfigManager.instance.configJavaBean.personInfo) {
					if (pi.name.equals(oldPersonInfo.name) && pi.qq == oldPersonInfo.qq && pi.bid == oldPersonInfo.bid && pi.bliveRoom == oldPersonInfo.bliveRoom) {
						ConfigManager.instance.configJavaBean.personInfo.remove(oldPersonInfo);
						break;
					}
				}
				ConfigManager.instance.configJavaBean.personInfo.add(newPersonInfo);
				break;	


				/*	case BotDataPack.opGroupInfo:
				 toSend = BotDataPack.encode(rec.getOpCode());
				 ArrayList<Group> gl=(ArrayList<Group>) Autoreply.ins.CQ.getGroupList();
				 long gid=rec.readLong();
				 for (Group g:gl) {
				 if (g.getId() == gid) {
				 toSend.write(g.getId()).write(g.getName());
				 break;
				 }
				 }
				 break;
				 */
		}
		if (toSend != null) {
			conn.send(toSend.getData());
		}
	}

	@Override
	public void onError(WebSocket p1, Exception p2) {
		// TODO: Implement this method
	}

	@Override
	public void onStart() {
		setConnectionLostTimeout(1800);
	}

	public void sendMsg(int type, long group, long qq, String msg, long msgId) {
		msgPack.write(type).write(group).write(qq).write(msg).write((int)msgId);
	}
}
