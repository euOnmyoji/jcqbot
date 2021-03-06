package com.meng.modules;

import com.google.gson.*;
import com.meng.*;
import com.meng.bilibili.live.*;
import com.meng.bilibili.main.*;
import com.meng.config.*;
import com.meng.config.javabeans.*;
import com.meng.remote.*;
import com.meng.tools.*;
import com.meng.tools.override.*;
import com.sobte.cqp.jcq.entity.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.jsoup.*;

public class MAdminMsg extends BaseModule {
	private MyLinkedHashMap<String,String> masterPermission=new MyLinkedHashMap<>();
	private MyLinkedHashMap<String,String> adminPermission=new MyLinkedHashMap<>();
	public MyLinkedHashMap<String,String> userPermission=new MyLinkedHashMap<>();

	@Override
    public BaseModule load() {
		masterPermission.put("小律影专用指令:setconnect", "");
		masterPermission.put(".start|.stop", "总开关");
		masterPermission.put("find:[QQ号]", "在配置文件中查找此人");
		masterPermission.put("z.add[艾特至少一人]", "点赞列表");
		masterPermission.put("zan-now", "立即启动点赞线程,尽量不要用");
		masterPermission.put("block[艾特一人]", "屏蔽列表");
		masterPermission.put("black[艾特一人]", "黑名单");
		masterPermission.put("System.gc();", "System.gc();");
		masterPermission.put("-live.[start|stop]", "开关直播(hina)");
		masterPermission.put("-live.rename.[字符串]", "直播改名(hina)");
		masterPermission.put("blackgroup [群号]", "群加入黑名单,多群用空格隔开");
		masterPermission.put("av更新时间:[UID]", "用户最新后更新视频时间");
		masterPermission.put("avJson:[AV号]", "av信息");
		masterPermission.put("cv更新时间:[UID]", "用户最后更新文章时间");
		masterPermission.put("cvJson:[CV号]", "cv信息");
		masterPermission.put("直播状态lid:[直播间号]", "直播间状态");
		masterPermission.put("直播状态bid:[UID]", "从UID获取直播间状态");
		masterPermission.put("获取直播间:[UID]", "从UID获取直播间ID");
		masterPermission.put("直播时间统计", "统计的直播时间");
		masterPermission.put("群广播:[字符串]", "在所有回复的群里广播");
		masterPermission.put("nai.[称呼|直播间号].[内容]", "三月精账号发送弹幕");
		masterPermission.put("bav:[AV号]", "视频信息");
		masterPermission.put("bcv:[CV号]", "文章信息");
		masterPermission.put("blv:[直播间号]", "直播间信息");
		masterPermission.put("精神支柱[图片]|神触[图片]", "使用图片生成表情包");
		masterPermission.put("cookie.[称呼].[cookie字符串]", "设置cookie,可选值Sunny,Luna,Star,XingHuo,Hina,grzx");
		masterPermission.put("send.[群号].[内容]", "内容转发至指定群");
		masterPermission.put("mother.[字符串]", "直播间点歌问候");
		masterPermission.put("lban.[直播间号|直播间主人].[被禁言UID|被禁言者称呼].[时间]", "直播间禁言,单位为小时");
		masterPermission.put("移除成就 [成就名] [艾特一人]", "移除此人的该成就");

		adminPermission.put("findInAll:[QQ号]", "查找共同群");
		adminPermission.put("ban.[QQ号|艾特].[时间]|ban.[群号].[QQ号].[时间]", "禁言,单位为秒");
		adminPermission.put("加图指令懒得写了", "色图迫害图女装");
		adminPermission.put("蓝统计", "蓝发言统计");
		adminPermission.put("线程数", "线程池信息");
		adminPermission.put(".on|.off", "不修改配置文件的单群开关");
		adminPermission.put(".admin enable|.admin disable", "修改配置文件的单群开关");
		adminPermission.put(".live", "不管配置文件如何,都回复直播列表");

		userPermission.put(".live", "正在直播列表");
		userPermission.put(".nn [名字]", "设置蓝对你的称呼,如果不设置则恢复默认称呼");
		userPermission.put("-int [int] [+|-|*|/|<<|>>|>>>|%|^|&||] [int]", "int运算(溢出)");
		userPermission.put("-uint [int]", "int字节转uint(boom)");
		userPermission.put("抽卡", "抽卡");
		userPermission.put("给蓝master幻币转账", "抽卡，1币3卡");
		userPermission.put("查看成就", "查看成就列表");
		userPermission.put("查看符卡", "查看已获得的符卡,会刷屏，少用");
		userPermission.put("成就条件 [成就名]", "查看获得条件");
		userPermission.put("幻币兑换 [整数]", "本地幻币兑换至小律影");
		userPermission.put("~coins", "查看幻币数量");
		userPermission.put("幻币抽卡 [整数]", "使用本地幻币抽卡");
		userPermission.put("购买符卡 [符卡名]", "购买指定符卡,除lastword");
		userPermission.put("原曲认知 [E|N|H|L]", "原曲认知测试,回答时用\"原曲认知回答 答案\"进行回答，只能回答自己的问题");

		masterPermission.putAll(adminPermission);
		masterPermission.putAll(userPermission);
		adminPermission.putAll(userPermission);
		enable = true;
		return this;
	}

	@Override
	protected boolean processMsg(long fromGroup, long fromQQ, String msg, int msgId, File[] imgs) {
		if (fromQQ == 2856986197L || fromQQ == 2528419891L) {
			if (msg.startsWith("bchat.")) {
				String[] strs=msg.split("\\.", 3);
				PersonInfo pi=ConfigManager.instance.getPersonInfoFromName(strs[1]);
				String resu;
				if (pi == null) {
					resu = Autoreply.instance.naiManager.sendChat(strs[1], strs[2]);
				} else {
					resu = Autoreply.instance.naiManager.sendChat(pi.bliveRoom + "", strs[2]);
				}	
				if (!resu.equals("")) {
					Autoreply.sendMessage(fromGroup, 0, resu);
				}
				return true;
			}
			if (msg.startsWith("blink.")) {
				String[] strs=msg.split("\\.", 2);
				PersonInfo pi=ConfigManager.instance.getPersonInfoFromName(strs[1]);
				if (pi == null) {	  
					JsonParser parser = new JsonParser();
					JsonObject obj = parser.parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/room/v1/Room/playUrl?cid=" + strs[1] + "&quality=4&platform=web")).getAsJsonObject();
					JsonArray ja = obj.get("data").getAsJsonObject().get("durl").getAsJsonArray();
					Autoreply.sendMessage(fromGroup, 0, ja.get(0).getAsJsonObject().get("url").getAsString());
				} else {
					JsonParser parser = new JsonParser();
					JsonObject obj = parser.parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/room/v1/Room/playUrl?cid=" + pi.bliveRoom + "&quality=4&platform=web")).getAsJsonObject();
					JsonArray ja = obj.get("data").getAsJsonObject().get("durl").getAsJsonArray();
					Autoreply.sendMessage(fromGroup, 0, ja.get(0).getAsJsonObject().get("url").getAsString());			  
				}	
				return true;
			}
		}
		if (!ConfigManager.instance.isAdmin(fromQQ) && Autoreply.CQ.getGroupMemberInfo(fromGroup, fromQQ).getAuthority() < 2) {
			return false;
		}
		if (msg.equals(".on")) {
            GroupConfig groupConfig =ConfigManager.instance.getGroupConfig(fromGroup);
            if (groupConfig == null) {
                Autoreply.sendMessage(fromGroup, fromQQ, "本群没有默认配置");
                return true;
            }
            ConfigManager.instance.setFunctionEnabled(fromGroup,ModuleManager.ID_MainSwitch,true);
            Autoreply.sendMessage(fromGroup, fromQQ, "已启用");
            ConfigManager.instance.saveConfig();
			return true;
            }
 
        if (msg.equals(".off")) {
			ConfigManager.instance.setFunctionEnabled(fromGroup,ModuleManager.ID_MainSwitch,false);
			Autoreply.sendMessage(fromGroup, 0, "已停用");
            return true;
        }
		if (msg.equals(".live")) {
			String msgSend;
			StringBuilder stringBuilder = new StringBuilder();
			for (Map.Entry<Integer,LivePerson> entry:Autoreply.instance.liveListener.livePersonMap.entrySet()) {	
				if (entry.getValue().lastStatus) {
					stringBuilder.append(ConfigManager.instance.getPersonInfoFromBid(entry.getKey()).name).append("正在直播").append(entry.getValue().liveUrl).append("\n");
				}
			}
			msgSend = stringBuilder.toString();
			Autoreply.sendMessage(fromGroup, fromQQ, msgSend.equals("") ? "居然没有飞机佬直播" : msgSend);
			return true;
		}
		if (msg.startsWith("findInAll:")) {
			Tools.CQ.findQQInAllGroup(fromGroup, fromQQ, msg);
			return true;
		}
        if (!ConfigManager.instance.isMaster(fromQQ) && Autoreply.CQ.getGroupMemberInfo(fromGroup, fromQQ).getAuthority() < 3) {
			return false;
		}
		if (msg.startsWith("&#91;接收到新的加群申请&#93")) {
			String num=msg.substring(msg.indexOf("申请编号：") + 5, msg.indexOf("已注册快捷") - 2);
			long qqNum=Long.parseLong(msg.substring(msg.indexOf("用户：") + 3, msg.indexOf("验证消息") - 2));
			PersonInfo pi=ConfigManager.instance.getPersonInfoFromQQ(qqNum);
			if (pi != null) {
				Autoreply.sendMessage(Autoreply.mainGroup, 0, "~申请审核 " + num + " True");
				Autoreply.sendMessage(fromGroup, 0, "欢迎" + ConfigManager.instance.getNickName(qqNum));
			} else if (ConfigManager.instance.isGroupAutoAllow(qqNum)) {
				Autoreply.sendMessage(Autoreply.mainGroup, 0,  "~申请审核 " + num + " True");
				Autoreply.sendMessage(fromGroup, 0, "此账号在自动同意列表中，已同意进群");
				Autoreply.sendMessage(fromGroup, 0, "欢迎" + ConfigManager.instance.getNickName(qqNum));
			} else if (ConfigManager.instance.isBlackQQ(qqNum)) {
				Autoreply.sendMessage(fromGroup, 0, "~申请审核 " + num + " False 黑名单用户");
			}
			return true;
		}
		if (msg.startsWith("群广播:")) {
			if (msg.contains("~") || msg.contains("～")) {
				Autoreply.sendMessage(fromGroup, 0, "包含屏蔽的字符");
				return true;
			}
			String broadcast=msg.substring(4);
			HashSet<Group> hs=new HashSet<>();
			List<Group> glist=Autoreply.CQ.getGroupList();
			for (Group g:glist) {
				GroupConfig gc=ConfigManager.instance.getGroupConfig(g.getId());
				if (!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_MainSwitch)) {
					continue;
				}
				Autoreply.sendMessage(gc.n, 0, broadcast, true);
				hs.add(g);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
			}
			String result="在以下群发送了广播:";
			for (Group g:hs) {
				result += "\n";
				result += g.getId();
				result += ":";
				result += g.getName();
			}
			Autoreply.sendMessage(fromGroup, 0, result);
			return true;
		}
		if (msg.equals(".stop")) {
			Autoreply.sendMessage(fromGroup, 0, "disabled");
			Autoreply.sleeping = true;
			return true;
		}
		if (msg.equals(".start")) {
			Autoreply.sleeping = false;
			Autoreply.sendMessage(fromGroup, 0, "enabled");
			return true;
		}
		if (msg.startsWith("-live")) {
			String[] str=msg.split("\\.");
			PersonInfo pi=ConfigManager.instance.getPersonInfoFromQQ(fromQQ);
			String name;
			if (pi == null) {
				name = "" + fromQQ;
			} else {
				name = pi.name;
			}
			switch (str[1]) {
				case "start":
					try {
						Autoreply.sendMessage(fromGroup, 0, start(9721948, Autoreply.instance.cookieManager.cookie.Hina));
						Autoreply.sendMessage(Autoreply.mainGroup, 0, name + "开启了直播");
					} catch (IOException e) {}
					break;
				case "stop":
					try {
						Autoreply.sendMessage(fromGroup, 0, stop(9721948, Autoreply.instance.cookieManager.cookie.Hina));
						Autoreply.sendMessage(Autoreply.mainGroup, 0, name + "关闭了直播");
					} catch (IOException e) {}
					break;
				case "rename":
					try {
						Autoreply.sendMessage(fromGroup, 0, rename(9721948, Autoreply.instance.cookieManager.cookie.Hina, str[2]));
						Autoreply.sendMessage(Autoreply.mainGroup, 0, name + "为直播改了名:" + str[2]);
					} catch (IOException e) {}
			}	
			return true;
		}
		if (msg.startsWith("lban.")) {
			String[] ss=msg.split("\\.");
			String rid=ss[1];
			String uid=ss[2];
			PersonInfo mas=ConfigManager.instance.getPersonInfoFromName(ss[1]);
			if (mas != null) {
				rid = mas.bliveRoom + "";
			}
			PersonInfo ban=ConfigManager.instance.getPersonInfoFromName(ss[2]);
			if (ban != null) {
				uid = ban.bid + "";
			}
			Autoreply.instance.liveListener.setBan(fromGroup, rid, uid, ss[3]);
			return true;
		}
		if (msg.startsWith("mother.")) {
			if (msg.length() > 7) {
				if (Autoreply.instance.danmakuListenerManager.addMotherWord(msg.substring(7))) {
					Autoreply.sendMessage(fromGroup, 0, msg.substring(7) + "已添加");
				} else {
					Autoreply.sendMessage(fromGroup, 0, "添加失败");
				}
			} else {
				Autoreply.sendMessage(fromGroup, 0, "参数有误");
			}
			return true;
		}
		if (msg.startsWith("block[CQ:at")) {
			StringBuilder sb = new StringBuilder();
			List<Long> qqs = Autoreply.instance.CC.getAts(msg);
			sb.append("屏蔽列表添加:");
			for (int i = 0, qqsSize = qqs.size(); i < qqsSize; i++) {
				long qq = qqs.get(i);
				sb.append(qq).append(" ");
			}
			ConfigManager.instance.configJavaBean.QQNotReply.addAll(qqs);
			ConfigManager.instance.saveConfig();
			Autoreply.sendMessage(fromGroup, fromQQ, sb.toString());
			return true;
		}
		if (msg.startsWith("black[CQ:at")) {
			StringBuilder sb = new StringBuilder();
			List<Long> qqs = Autoreply.instance.CC.getAts(msg);
			sb.append("黑名单添加:");
			for (int i = 0, qqsSize = qqs.size(); i < qqsSize; i++) {
				long qq = qqs.get(i);
				sb.append(qq).append(" ");
			}
			ConfigManager.instance.configJavaBean.blackListQQ.addAll(qqs);
			ConfigManager.instance.saveConfig();
			Autoreply.sendMessage(fromGroup, fromQQ, sb.toString());
			return true;
		}
		if (msg.startsWith("blackgroup")) {
			StringBuilder sb = new StringBuilder();
			String[] groups = msg.split(" ");
			sb.append("黑名单群添加:");
			int le = groups.length;
			for (int i = 1; i < le; ++i) {
				sb.append(groups[i]).append(" ");
				ConfigManager.instance.configJavaBean.blackListGroup.add(Long.parseLong(groups[i]));
			}
			ConfigManager.instance.saveConfig();
			Autoreply.sendMessage(fromGroup, fromQQ, sb.toString());
			return true;
		}
		if (msg.startsWith("av更新时间:")) {
			Autoreply.sendMessage(fromGroup, fromQQ, String.valueOf(ModuleManager.instance.getModule(MBiliUpdate.class).getAVLastUpdateTime(msg.substring(7))));
			return true;
		}
		if (msg.startsWith("avJson:")) {
			Autoreply.sendMessage(fromGroup, fromQQ, ModuleManager.instance.getModule(MBiliUpdate.class).getAVJson(msg.substring(7)));
			return true;
		}
		if (msg.startsWith("cv更新时间:")) {
			Autoreply.sendMessage(fromGroup, fromQQ, String.valueOf(ModuleManager.instance.getModule(MBiliUpdate.class).getCVLastUpdateTime(msg.substring(7))));
			return true;
		}
		if (msg.startsWith("cvJson:")) {
			Autoreply.sendMessage(fromGroup, fromQQ, ModuleManager.instance.getModule(MBiliUpdate.class).getCVJson(msg.substring(7)));
			return true;
		}
		if (msg.startsWith("直播状态lid:")) {
			String html = Tools.Network.getSourceCode("https://live.bilibili.com/" + msg.substring(8));
			String jsonInHtml = html.substring(html.indexOf("{\"roomInitRes\":"), html.lastIndexOf("}") + 1);
			JsonObject data = new JsonParser().parse(jsonInHtml).getAsJsonObject().get("baseInfoRes").getAsJsonObject().get("data").getAsJsonObject();
			Autoreply.sendMessage(fromGroup, fromQQ, data.get("live_status").getAsInt() == 1 ? "true" : "false");
			return true;
		}
		if (msg.startsWith("直播状态bid:")) {
			SpaceToLiveJavaBean sjb = Autoreply.gson.fromJson(Tools.Network.getSourceCode("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + msg.substring(8)), SpaceToLiveJavaBean.class);
			Autoreply.sendMessage(fromGroup, fromQQ, sjb.data.liveStatus == 1 ? "true" : "false");
			return true;
		}
		if (msg.startsWith("获取直播间:")) {
			Autoreply.sendMessage(fromGroup, fromQQ, Tools.Network.getSourceCode("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + msg.substring(6)));
			return true;
		}
		if (msg.startsWith("add{")) {
			PersonInfo personInfo;
			try {
				personInfo = Autoreply.gson.fromJson(msg.substring(3), PersonInfo.class);
			} catch (Exception e) {
				Autoreply.sendMessage(fromGroup, fromQQ, e.toString());
				return true;
			}
			if (personInfo != null) {
				ConfigManager.instance.configJavaBean.personInfo.add(personInfo);
				ConfigManager.instance.saveConfig();
				Autoreply.sendMessage(fromGroup, fromQQ, msg + "成功");
			} else {
				Autoreply.sendMessage(fromGroup, fromQQ, "一个玄学问题导致了失败");
			}
			return true;
		}
		if (msg.startsWith("del{")) {
			PersonInfo p;
			try {
				p = Autoreply.gson.fromJson(msg.substring(3), PersonInfo.class);
			} catch (Exception e) {
				Autoreply.sendMessage(fromGroup, fromQQ, e.toString());
				return true;
			}
			if (p != null) {
				ConfigManager.instance.configJavaBean.personInfo.remove(p);
				ConfigManager.instance.saveConfig();
				Autoreply.sendMessage(fromGroup, fromQQ, msg + "成功");
			} else {
				Autoreply.sendMessage(fromGroup, fromQQ, "一个玄学问题导致了失败");
			}
			return true;
		}
		if (msg.startsWith("find:")) {
			String name = msg.substring(5);
			HashSet<PersonInfo> hashSet = new HashSet<>();
			for (PersonInfo personInfo : ConfigManager.instance.configJavaBean.personInfo) {
				if (personInfo.name.contains(name)) {
					hashSet.add(personInfo);
				}
				if (personInfo.qq != 0 && String.valueOf(personInfo.qq).contains(name)) {
					hashSet.add(personInfo);
				}
				if (personInfo.bid != 0 && String.valueOf(personInfo.bid).contains(name)) {
					hashSet.add(personInfo);
				}
				if (personInfo.bliveRoom != 0 && String.valueOf(personInfo.bliveRoom).contains(name)) {
					hashSet.add(personInfo);
				}
			}
			Autoreply.sendMessage(fromGroup, fromQQ, Autoreply.gson.toJson(hashSet));
			return true;
		}
		if (msg.equals("线程数")) {
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Autoreply.instance.threadPool;
			String s = "taskCount：" + threadPoolExecutor.getTaskCount() + "\n" +
				"completedTaskCount：" + threadPoolExecutor.getCompletedTaskCount() + "\n" +
				"largestPoolSize：" + threadPoolExecutor.getLargestPoolSize() + "\n" +
				"poolSize：" + threadPoolExecutor.getPoolSize() + "\n" +
				"activeCount：" + threadPoolExecutor.getActiveCount();
			Autoreply.sendMessage(fromGroup, fromQQ, s);
			return true;
		}
		if (msg.equalsIgnoreCase("System.gc();")) {
			System.gc();
			Autoreply.sendMessage(fromGroup, fromQQ, "gc start");
			return true;
		}
		if (msg.equals("zan-now")) {
			Autoreply.sendMessage(fromGroup, fromQQ, "start");
			Autoreply.instance.zanManager.sendZan();
			Autoreply.sendMessage(fromGroup, fromQQ, "finish");
			return true;
		}
		if (Autoreply.instance.zanManager.checkAdd(fromGroup, fromQQ, msg)) {
			return true;
		}
		if (msg.equals("直播时间统计")) {
			Autoreply.sendMessage(fromGroup, 0, Autoreply.instance.liveListener.getLiveTimeCount());
			return true;
		}
		if (msg.startsWith("nai.")) {
			String[] sarr = msg.split("\\.", 3);
			PersonInfo pInfo = ConfigManager.instance.getPersonInfoFromName(sarr[1]);
			if (pInfo != null) {
				Autoreply.instance.naiManager.check(fromGroup, pInfo.bliveRoom + "", fromQQ, sarr[2]);
			} else {
				Autoreply.instance.naiManager.check(fromGroup, sarr[1], fromQQ, sarr[2]);
			}
			return true;
		}
		if (msg.equals("精神支柱")) {
			Autoreply.sendMessage(fromGroup, 0, Autoreply.instance.CC.image(new File(Autoreply.appDirectory + "pic\\alice.png")));
			return true;
		}
		if (msg.startsWith("生成位置")) {
			String[] args = msg.split(",");
			if (args.length == 6) {
				try {
					Autoreply.sendMessage(fromGroup, 0,
										  Autoreply.instance.CC.location(
											  Double.parseDouble(args[2]),
											  Double.parseDouble(args[1]),
											  Integer.parseInt(args[3]),
											  args[4],
											  args[5]));
					return true;
				} catch (Exception e) {
					Autoreply.sendMessage(fromGroup, fromQQ, "参数错误,生成位置.经度double.纬度double.倍数int.名称string.描述string");
					return true;
				}
			}
		}

		String[] strings = msg.split("\\.", 3);
		if (strings[0].equals("send")) {
			if (msg.contains("~") || msg.contains("～")) {
				Autoreply.sendMessage(fromGroup, 0, "包含屏蔽的字符");
				return true;
			}
			switch (strings[2]) {
				case "喵":
					Autoreply.instance.threadPool.execute(new DeleteMessageRunnable(Autoreply.sendMessage(Long.parseLong(strings[1]), 0, Autoreply.instance.CC.record("miao.mp3"))));
					break;
				case "娇喘":
					Autoreply.instance.threadPool.execute(new DeleteMessageRunnable(Autoreply.sendMessage(Long.parseLong(strings[1]), 0, Autoreply.instance.CC.record("mmm.mp3"))));
					break;
				default:
					Autoreply.sendMessage(Long.parseLong(strings[1]), 0, strings[2]);
					break;
			}
			return true;
		}
		if (strings[0].equals("cookie")) {
			if (!Autoreply.instance.cookieManager.setCookie(strings[1], strings[2])) {
				Autoreply.sendMessage(fromGroup, 0, "添加失败");
				return true;
			}
			Autoreply.sendMessage(fromGroup, 0, "已为" + strings[1] + "设置cookie");
			return true;
		}
		if (msg.startsWith("精神支柱[CQ:image")) {
			ModuleManager.instance.getModule(MPicEdit.class).jingShenZhiZhuByPic(fromGroup, fromQQ, msg);
			return true;
		}
		if (msg.startsWith("神触[CQ:image")) {
			ModuleManager.instance.getModule(MPicEdit.class).shenChuByAt(fromGroup, fromQQ, msg);
			return true;
		}
		if (msg.startsWith("设置群头衔[CQ:at")) {
			String title = msg.substring(msg.indexOf("]") + 1);
			System.out.println(Autoreply.CQ.setGroupSpecialTitle(fromGroup, Autoreply.instance.CC.getAt(msg), title, -1));
			return true;
		}
		if (msg.startsWith("设置群名片[CQ:at")) {
			String title = msg.substring(msg.indexOf("]") + 1);
			System.out.println(Autoreply.CQ.setGroupCard(fromGroup, Autoreply.instance.CC.getAt(msg), title));
			return true;
		}
        return false;
	}

	public String start(int roomID, String cookie) throws IOException {
        Connection connection = Jsoup.connect("https://api.live.bilibili.com/room/v1/Room/startLive");
        String csrf = Tools.Network.cookieToMap(cookie).get("bili_jct");
        Map<String, String> liveHead = new HashMap<>();
        liveHead.put("Host", "api.live.bilibili.com");
        liveHead.put("Accept", "application/json, text/javascript, */*; q=0.01");
        liveHead.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        liveHead.put("Connection", "keep-alive");
        liveHead.put("Origin", "https://link.bilibili.com");
        connection.
			userAgent(Autoreply.instance.userAgent).
			headers(liveHead).
			ignoreContentType(true).
			referrer("https://link.bilibili.com/p/center/index").
			cookies(Tools.Network.cookieToMap(cookie)).
			method(Connection.Method.POST).
			data("room_id", String.valueOf(roomID)).
			data("platform", "pc").
			data("area_v2", "235").
			data("csrf_token", csrf).
			data("csrf", csrf);
        Connection.Response response = connection.execute();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(response.body()).getAsJsonObject();
		if (obj.get("code").getAsInt() == 0) {
			return "开播成功";
		}
		return obj.get("message").getAsString();
    }

    public String stop(int roomID, String cookie) throws IOException {
        Connection connection = Jsoup.connect("https://api.live.bilibili.com/room/v1/Room/stopLive");
        String csrf = Tools.Network.cookieToMap(cookie).get("bili_jct");
        Map<String, String> liveHead = new HashMap<>();
        liveHead.put("Host", "api.live.bilibili.com");
        liveHead.put("Accept", "application/json, text/javascript, */*; q=0.01");
        liveHead.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        liveHead.put("Connection", "keep-alive");
        liveHead.put("Origin", "https://link.bilibili.com");
        connection.
			userAgent(Autoreply.instance.userAgent).
			headers(liveHead).
			ignoreContentType(true).
			referrer("https://link.bilibili.com/p/center/index").
			cookies(Tools.Network.cookieToMap(cookie)).
			method(Connection.Method.POST).
			data("room_id", String.valueOf(roomID)).
			data("csrf_token", csrf).
			data("csrf", csrf);
        Connection.Response response = connection.execute();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(response.body()).getAsJsonObject();
		if (obj.get("code").getAsInt() == 0) {
			return "关闭成功";
		}
		return obj.get("message").getAsString();
    }

    public String rename(int roomID, String cookie, String newName) throws IOException {
        Connection connection = Jsoup.connect("https://api.live.bilibili.com/room/v1/Room/update");
        String csrf = Tools.Network.cookieToMap(cookie).get("bili_jct");
        Map<String, String> liveHead = new HashMap<>();
        liveHead.put("Host", "api.live.bilibili.com");
        liveHead.put("Accept", "application/json, text/javascript, */*; q=0.01");
        liveHead.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        liveHead.put("Connection", "keep-alive");
        liveHead.put("Origin", "https://link.bilibili.com");
        connection.
			userAgent(Autoreply.instance.userAgent).
			headers(liveHead).
			ignoreContentType(true).
			referrer("https://link.bilibili.com/p/center/index").
			cookies(Tools.Network.cookieToMap(cookie)).
			method(Connection.Method.POST).
			data("room_id", String.valueOf(roomID)).
			data("title", newName).
			data("csrf_token", csrf).
			data("csrf", csrf);
        Connection.Response response = connection.execute();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(response.body()).getAsJsonObject();
        return obj.get("message").getAsString();
    }
}
