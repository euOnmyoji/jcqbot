package com.meng.modules;

import com.madgag.gif.fmsware.*;
import com.meng.*;
import com.meng.config.javabeans.*;
import com.meng.messageProcess.*;
import com.meng.tools.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.meng.modules.*;
import com.meng.config.*;


public class MRepeater extends BaseModule {

    private HashMap<Long, RepeaterBanner> repeaters = new HashMap<>();

	@Override
	public BaseModule load() {
		enable = true;
		return this;
	}

	@Override
	protected boolean processMsg(long fromGroup, long fromQQ, String msg, int msgId, File[] imgs) {
		if(!ConfigManager.instance.isFunctionEnable(fromGroup,ModuleManager.ID_Repeater)){
			return false;
		}
		RepeaterBanner repeaterBanner = repeaters.get(fromGroup);
        if (repeaterBanner == null) {
            repeaterBanner = new RepeaterBanner(fromGroup);
			repeaters.put(fromGroup, new RepeaterBanner(fromGroup));
        }
        return repeaterBanner.check(fromGroup, fromQQ, msg, imgs);
    }

	private class RepeaterBanner {
		private int repeatCount = 0;
		private int banCount = 6;
		private String lastMessageRecieved = "";
		private int reverseFlag = Autoreply.instance.random.nextInt(4);
		private boolean lastStatus = false;
		private FingerPrint[] thisFp;
		private FingerPrint[] lastFp;
		long groupNumber = 0;
		private MWarnMsg warnMessageProcessor;

		public RepeaterBanner(long groupNumber) {
			this.groupNumber = groupNumber;
			warnMessageProcessor = new MWarnMsg();
		}

		public boolean check(long fromGroup, long fromQQ, String msg, File[] imageFiles) {
			GroupConfig groupConfig = ConfigManager.instance.getGroupConfig(fromGroup);
			if (groupConfig == null) {
				return false;
			}
			if (msg.contains("~转账") || msg.contains("～转账")) {
				return true;
			}
			boolean b = false;
			try {
				if (msg.contains("禁言") || fromGroup != groupNumber) {
					return true;
				}
				float simi = getPicSimilar(imageFiles);
				switch (groupConfig.repeatMode) {
					case 0:

						break;
					case 1:
						if (lastMessageRecieved.equals(msg) || isPicMsgRepeat(lastMessageRecieved, msg, simi)) {
							if (Autoreply.instance.random.nextInt() % banCount == 0) {
								int time = Autoreply.instance.random.nextInt(60) + 1;
								banCount = 6;
								if (Tools.CQ.ban(fromGroup, fromQQ, time)) {
									Autoreply.sendMessage(0, fromQQ, String.format("你从“群复读轮盘”中获得了%d秒禁言套餐", time));
								}
							}
						}
						break;
					case 2:
						if (lastMessageRecieved.equals(msg) || isPicMsgRepeat(lastMessageRecieved, msg, simi)) {
							int time = Autoreply.instance.random.nextInt(60) + 1;
							if (Tools.CQ.ban(fromGroup, fromQQ, time)) {
								Autoreply.sendMessage(0, fromQQ, String.format("你因复读获得了%d秒禁言套餐", time));
							}
						}
						lastMessageRecieved = msg;
						return true;
				}
				b = checkRepeatStatu(fromGroup, fromQQ, msg, imageFiles, simi);
				lastMessageRecieved = msg;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return b;
		}

		// 复读状态
		private boolean checkRepeatStatu(long group, long qq, String msg, File[] imageFiles, float simi) {
			boolean b = false;
			if (!lastStatus && (lastMessageRecieved.equals(msg) || isPicMsgRepeat(lastMessageRecieved, msg, simi))) {
				b = repeatStart(group, qq, msg, imageFiles);
			}
			if (lastStatus && (lastMessageRecieved.equals(msg) || isPicMsgRepeat(lastMessageRecieved, msg, simi))) {
				b = repeatRunning(group, qq, msg);
			}
			if (lastStatus && !lastMessageRecieved.equals(msg) && !isPicMsgRepeat(lastMessageRecieved, msg, simi)) {
				b = repeatEnd(group, qq, msg);
			}
			lastStatus = lastMessageRecieved.equals(msg) || isPicMsgRepeat(lastMessageRecieved, msg, simi);
			return b;
		}

		private boolean repeatEnd(long group, long qq, String msg) {
			ModuleManager.instance.getModule(MUserCounter.class).incRepeatBreaker(qq);
			ModuleManager.instance.getModule(MGroupCounter.class).incRepeatBreaker(group);
			return false;
		}

		private boolean repeatRunning(long group, long qq, String msg) {
			ModuleManager.instance.getModule(MUserCounter.class).incFudu(qq);
			ModuleManager.instance.getModule(MGroupCounter.class).incFudu(group);
			banCount--;
			return false;
		}

		private boolean repeatStart(final long group, final long qq, final String msg, final File[] imageFiles) {
			banCount = 6;
			ModuleManager.instance.getModule(MUserCounter.class).incFudujiguanjia(qq);
			ModuleManager.instance.getModule(MGroupCounter.class).incFudu(group);
			Autoreply.instance.threadPool.execute(new Runnable() {
					@Override
					public void run() {
					reply(group, qq, msg, imageFiles);
					}
				});
			ModuleManager.instance.getModule(MUserCounter.class).incFudu(Autoreply.CQ.getLoginQQ());
			return true;
		}

		// 回复
		private boolean reply(long group, long qq, String msg, File[] imageFiles) {
			if (msg.contains("蓝") || msg.contains("藍")) {
				return true;
			}
			if (imageFiles == null) {
				replyText(group, qq, msg);
			} else {
				try {
					replyPic(group, qq, msg, imageFiles);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		// 如果是图片复读
		private void replyPic(long group, long qq, String msg, File[] imageFiles) throws IOException {
			int index = 0;
			StringBuilder stringBuilder = new StringBuilder(msg);
			for (File imageFile : imageFiles) {
				index = msg.indexOf("[CQ:image,file=", index);
				if (Autoreply.instance.fileTypeUtil.getFileType(imageFile).equals("gif")) {
					stringBuilder.replace(index, index + 52, new StringBuilder(Autoreply.instance.CC.image(reverseGIF(imageFile))).reverse().toString());
				} else {
					stringBuilder.replace(index, index + 52, new StringBuilder(Autoreply.instance.CC.image(reversePic(imageFile))).reverse().toString());
				}
				index += 52;
			}
			Autoreply.sendMessage(group, 0, stringBuilder.reverse().toString());
			repeatCount = repeatCount > 2 ? 0 : repeatCount + 1;
			reverseFlag++;
		}

		// 如果是文本复读
		private void replyText(Long group, long qq, String msg) {
			if (msg.contains("此生无悔入东方")) {
				Autoreply.sendMessage(group, 0, msg);
				return;
			}
			if (repeatCount < 3) {
				Autoreply.sendMessage(group, 0, msg);
				repeatCount++;
			} else if (repeatCount == 3) {
				Autoreply.sendMessage(group, 0, "你群天天复读");
				repeatCount++;
			} else {
				String newmsg = new StringBuilder(msg).reverse().toString();
				Autoreply.sendMessage(group, 0, newmsg.equals(msg) ? newmsg + " " : newmsg);
				repeatCount = 0;
			}
		}
		// 反转图片
		private File reversePic(File file) throws IOException {
			Image im = ImageIO.read(file);
			int w = im.getWidth(null);
			int h = im.getHeight(null);
			int size = w * h;
			BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			b.getGraphics().drawImage(im.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			int[] rgb1 = b.getRGB(0, 0, w, h, new int[size], 0, w);
			int[] rgb2 = new int[size];
			switch (reverseFlag % 4) {
				case 0:
					for (int y = 0; y < h; ++y) {
						int yw = y * w;
						for (int x = 0; x < w; ++x) {
							rgb2[(w - 1 - x) + yw] = rgb1[x + yw]; // 镜之国
						}
					}
					break;
				case 1:
					for (int y = 0; y < h; y++) {
						// 天地
						if (w >= 0) {
							System.arraycopy(rgb1, y * w, rgb2, (h - 1 - y) * w, w);
						}
					}
					break;
				case 2:
					int halfH = h / 2;
					for (int y = 0; y < h; y++) {
						// 天壤梦弓
						if (w >= 0) {
							System.arraycopy(rgb1, y * w, rgb2, (y < halfH ? y + halfH : y - halfH) * w, w);
						}
					}
					break;
				case 3:
					for (int y = 0; y < h; y++) {
						for (int x = 0; x < w; x++) {
							rgb2[(w - 1 - x) + (h - 1 - y) * w] = rgb1[x + y * w]; // Reverse_Hierarchy
						}
					}
					break;
			}
			b.setRGB(0, 0, w, h, rgb2, 0, w);
			ImageIO.write(b, "png", file);
			return file;
		}

		private File reverseGIF(File gifFile) throws FileNotFoundException {
			GifDecoder gifDecoder = new GifDecoder();
			FileInputStream fis = new FileInputStream(gifFile);
			gifDecoder.read(fis);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
			BufferedImage cacheImage = gifDecoder.getFrame(0);
			int tw = cacheImage.getWidth(null) - 1;
			int th = cacheImage.getHeight(null) - 1;
			if (cacheImage.getRGB(0, 0) == 0 &&
                cacheImage.getRGB(tw, 0) == 0 &&
                cacheImage.getRGB(0, th) == 0 &&
                cacheImage.getRGB(tw, th) == 0) {
				localAnimatedGifEncoder.setTransparent(new Color(0, 0, 0, 0));
			}
			localAnimatedGifEncoder.start(baos);// start
			localAnimatedGifEncoder.setRepeat(0);// 设置生成gif的开始播放时间。0为立即开始播放
			float fa = (float) cacheImage.getHeight() / (gifDecoder.getFrameCount());
			switch (reverseFlag % 4) {
				case 0:// 镜之国
					cacheImage = spell1(gifDecoder.getFrame(0));
					for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
						localAnimatedGifEncoder.setDelay(gifDecoder.getDelay(i));
						localAnimatedGifEncoder.addFrame(spell1(gifDecoder.getFrame(i), cacheImage));
					}
					break;
				case 1:// 天地
					cacheImage = spell2(gifDecoder.getFrame(0));
					for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
						localAnimatedGifEncoder.setDelay(gifDecoder.getDelay(i));
						localAnimatedGifEncoder.addFrame(spell2(gifDecoder.getFrame(i), cacheImage));
					}
					break;
				case 2:// 天壤梦弓
					for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
						localAnimatedGifEncoder.setDelay(gifDecoder.getDelay(i));
						localAnimatedGifEncoder.addFrame(spell3(gifDecoder.getFrame(i), (int) (fa * (gifDecoder.getFrameCount() - i)), cacheImage));
					}
					break;
				case 3:// Reverse Hierarchy
					cacheImage = spell4(gifDecoder.getFrame(0));
					for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
						localAnimatedGifEncoder.setDelay(gifDecoder.getDelay(i));
						localAnimatedGifEncoder.addFrame(spell4(gifDecoder.getFrame(i), cacheImage));
					}
					break;
			}
			localAnimatedGifEncoder.finish();
			try {
				FileOutputStream fos = new FileOutputStream(gifFile);
				baos.writeTo(fos);
				baos.flush();
				fos.flush();
				baos.close();
				fos.close();
			} catch (Exception e) {
			}
			return gifFile;
		}

		private BufferedImage spell1(BufferedImage current, BufferedImage cache) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = current.getRGB(x, y);
					bmp.setRGB(w - 1 - x, y, i == 0 ? 1 : i);// 镜之国
				}
			}
			cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			return bmp;
		}

		private BufferedImage spell1(BufferedImage current) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = current.getRGB(x, y);
					bmp.setRGB(w - 1 - x, y, i == 0 ? 1 : i);// 镜之国
				}
			}
			return bmp;
		}

		private BufferedImage spell2(BufferedImage current, BufferedImage cache) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = current.getRGB(x, y);
					bmp.setRGB(x, h - 1 - y, i == 0 ? 1 : i);// 天地
				}
			}
			cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			return bmp;
		}

		private BufferedImage spell2(BufferedImage current) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = current.getRGB(x, y);
					bmp.setRGB(x, h - 1 - y, i == 0 ? 1 : i);// 天地
				}
			}
			return bmp;
		}

		private BufferedImage spell3(BufferedImage current, int px, BufferedImage cache) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			bmp.getGraphics().drawImage(spell3_at1(cache, px).getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			spell3Process(current, px, w, h, bmp);
			cache.getGraphics().drawImage(spell3_at1(bmp, -px).getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			return bmp;
		}

		private BufferedImage spell3_at1(BufferedImage cache, int px) {
			int w = cache.getWidth(null);
			int h = cache.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			int j;
			if (px > 0) {
				spell3Process(cache, px, w, h, bmp);
			} else {
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						j = y + px;
						int i = cache.getRGB(x, y);
						if (j >= 0) {
							bmp.setRGB(x, j, i == 0 ? 1 : i);
						} else {
							bmp.setRGB(x, j + h, i == 0 ? 1 : i);
						}
					}
				}
			}
			return bmp;
		}

		private void spell3Process(BufferedImage cache, int px, int w, int h, BufferedImage bmp) {
			int j;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					j = y + px;
					int i = cache.getRGB(x, y);
					if (j < h) {
						bmp.setRGB(x, j, i == 0 ? 1 : i);
					} else {
						bmp.setRGB(x, j - h, i == 0 ? 1 : i);
					}
				}
			}
		}

		private BufferedImage spell4(BufferedImage current, BufferedImage cache) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			spell4Process(current, w, h, bmp);
			cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			return bmp;
		}

		private BufferedImage spell4(BufferedImage current) {
			int w = current.getWidth(null);
			int h = current.getHeight(null);
			BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			spell4Process(current, w, h, bmp);
			return bmp;
		}

		private void spell4Process(BufferedImage current, int w, int h, BufferedImage bmp) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int i = current.getRGB(x, y);
					bmp.setRGB(w - 1 - x, h - 1 - y, i == 0 ? 1 : i);// Reverse_Hierarchy
				}
			}
		}

		// 图片相似度判断
		private float getPicSimilar(File[] imageFiles) {
			if (imageFiles == null) {
				thisFp = null;
				lastFp = null;
				return 0;
			}
			if (lastFp == null) {
				return 0;
			}
			if (lastFp.length != imageFiles.length) {
				return 0;
			}
			float simi = 100;
			float[] tmp = new float[imageFiles.length];
			try {
				for (File imageFile : imageFiles) {
					if (thisFp != null) {
						lastFp = thisFp;
					}
					thisFp = new FingerPrint[imageFiles.length];
					for (int ii = 0; ii < imageFiles.length; ++ii) {
						thisFp[ii] = new FingerPrint(ImageIO.read(imageFile));
					}
					for (int iii = 0; iii < imageFiles.length; ++iii) {
						if (lastFp != null) {
							tmp[iii] = thisFp[iii].compare(lastFp[iii]);
						}
					}
				}
				for (float f : tmp) {
					if (simi > f) {
						simi = f;
					}
				}
			} catch (Exception e) {
				return 0;
			}
			return simi;
		}

		private String getMsgText(String msg) {
			return msg.replaceAll("\\[.*]", "");
		}

		private boolean isPicMsgRepeat(String lastMsg, String msg, float simi) {
			return getMsgText(msg).equals(getMsgText(lastMsg)) && msg.length() == lastMsg.length() && simi > 0.97f;
		}
	}
}
