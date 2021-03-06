package com.meng.modules;
import com.meng.*;
import com.meng.config.*;
import com.meng.tools.*;
import com.sobte.cqp.jcq.entity.*;
import java.io.*;
import java.util.*;

public class MSetu extends BaseModule {

	@Override
	public BaseModule load() {
		enable = true;
		return this;
	}

	@Override
	protected boolean processMsg(long fromGroup, long fromQQ, String msg, int msgId, File[] imgs) {
		if(!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_Setu)){
			return false;
		}
		if (msg.equals("色图")) {
			String[] strings = (new File(Autoreply.appDirectory + "setu/")).list();
			StringBuilder sBuilder = new StringBuilder("现在有");
			for (String s : strings) {
				sBuilder.append(" ").append(s);
			}
			sBuilder.append("的色图");
			Autoreply.sendMessage(fromGroup, fromQQ, sBuilder.toString());
			return true;
		} else if (msg.equals("随机色图")) {
			File[] files = (new File(Autoreply.appDirectory + "setu/")).listFiles();
			File folder = (File) Tools.ArrayTool.rfa(files);
			File[] pics = folder.listFiles();
			Autoreply.instance.threadPool.execute(new DeleteMessageRunnable(Autoreply.sendMessage(fromGroup, fromQQ, Autoreply.instance.CC.image((File) Tools.ArrayTool.rfa(pics)))));
			ModuleManager.instance.getModule(MUserCounter.class).incSetu(fromQQ);
			ModuleManager.instance.getModule(MGroupCounter.class).incSetu(fromGroup);
			ModuleManager.instance.getModule(MUserCounter.class).incSetu(Autoreply.CQ.getLoginQQ());
		} else if (msg.endsWith("色图")) {
			File[] files = (new File(Autoreply.appDirectory + "setu/" + msg.replace("色图", ""))).listFiles();
			if (files != null && files.length > 0) {
				Autoreply.instance.threadPool.execute(new DeleteMessageRunnable(Autoreply.sendMessage(fromGroup, fromQQ, Autoreply.instance.CC.image((File) Tools.ArrayTool.rfa(files)))));
				ModuleManager.instance.getModule(MUserCounter.class).incSetu(fromQQ);
				ModuleManager.instance.getModule(MGroupCounter.class).incSetu(fromGroup);
				ModuleManager.instance.getModule(MUserCounter.class).incSetu(Autoreply.CQ.getLoginQQ());
			}
			return true;
		}
		if (!ConfigManager.instance.isAdmin(fromQQ)) {
			return false;
		}
		if (msg.contains("色图[CQ:image")) {
			String setuName = msg.substring(0, msg.indexOf("[CQ:image") - 2);
			List<CQImage> imgList = Autoreply.instance.CC.getCQImages(msg);
			for (CQImage cqImage : imgList) {
				try {
					Autoreply.instance.fileTypeUtil.checkFormat(cqImage.download(Autoreply.appDirectory + File.separator + "setu/" + setuName, cqImage.getMd5()));
				} catch (IOException e) {
					e.printStackTrace();
					Autoreply.sendMessage(fromGroup, fromQQ, e.toString());
					return false;
				}
			}
			Autoreply.sendMessage(fromGroup, fromQQ, imgList.size() + "张图添加成功");
			return true;
		}


		return false;
	}
}

	
