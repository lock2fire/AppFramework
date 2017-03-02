package com.yuxiao.buz.baseframework.common;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class UtilStorage {
	private ArrayList<String> cache = new ArrayList<String>();
	private File vold_fastb;
	private final static String HEAD = "dev_mount";
	private static UtilStorage storageUtil;
	private static boolean hasInnerSdCard;
	private static boolean hasExternalSdCard;
	private static String externalSdcard;
	private static String innerSdcard;
	private int mSDCardNum; // 当前卡数量 内、外置

	/**
	 * 是否有内置sd卡
	 */
	public boolean isHasInnerSdCard() {
		return hasInnerSdCard;
	}

	/**
	 * 是否有外置sd卡
	 */
	public boolean isHasExternalSdCard() {
		return hasExternalSdCard;
	}

	public static UtilStorage getInstance() {

		if (storageUtil == null) {
			storageUtil = new UtilStorage();
			storageUtil.init();
		}
		return storageUtil;
	}

	/**
	 * 获取内置sd卡容量
	 */
	public long getInnerSdCardCapacity() {
		if (hasInnerSdCard) {
			return getTotalStorage(innerSdcard);
		} else {
			return 0;
		}
	}

	/**
	 * 获取外置sd卡容量
	 */
	public long getExternalSdCardCapacity() {
		if (hasExternalSdCard) {
			if (getTotalStorage(externalSdcard) == getTotalStorage(innerSdcard)) {
				return 0;
			}
			return getTotalStorage(externalSdcard);
		} else {
			return 0;
		}
	}

	/**
	 * 获取内置sd卡路径
	 */
	public String getInnerSdCardPath() {
		if (innerSdcard != null) {
			return innerSdcard;
		} else {
			return null;
		}
	}

	/**
	 * 获取外置sd卡路径
	 */
	public String getExternalSdCardPath() {
		if (externalSdcard != null) {
			return externalSdcard;
		} else {
			return null;
		}
	}

	/**
	 * 获取内置sd卡可用容量
	 */
	public long getInnerSdCardAvalible() {
		if (hasInnerSdCard) {
			return getAvailableStorage(innerSdcard);
		} else {
			return 0;
		}
	}

	/**
	 * 获取外置sd卡可用容量
	 */
	public long getExternalSdCardAvalible() {
		if (hasExternalSdCard) {
			if (innerSdcard != null
					&& getAvailableStorage(externalSdcard) == getAvailableStorage(innerSdcard)) {
				return 0;
			}
			return getAvailableStorage(externalSdcard);
		} else {
			return 0;
		}
	}

	/**
	 * 获取内置d卡已用容量
	 */
	public long getInnerSdCardUsed() {
		return getInnerSdCardCapacity() - getInnerSdCardAvalible();
	}

	/**
	 * 获取外置d卡已用容量
	 */
	public long getExternalSdCardUsed() {
		return getExternalSdCardCapacity() - getExternalSdCardAvalible();
	}

	private void init() {
		vold_fastb = new File(Environment.getRootDirectory().getAbsoluteFile()
				+ File.separator + "etc" + File.separator + "vold.fstab");
		if (vold_fastb != null && vold_fastb.exists()) {
			try {
				initVoldFstabToCache();// 解析vold.fstab
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < cache.size(); i++) {
				String temp = cache.get(i);
				String[] info = temp.split(" ");
				Log.i("zeng", "总容量：" + getTotalStorage(info[2]));// 1024*1024*1024*8*16
				if (info[1].equals("extsdcard") || info[1].equals("sdcard2")) {
					// 有外置sd卡
					hasExternalSdCard = true;
					externalSdcard = info[2];
					Log.i("zeng", "externalSdcard:" + externalSdcard);
				}
				if (info[1].equals("sdcard")) {
					// 有sd卡 不知道是内置还是外置 但如果有外置sd卡的话，配置文件里肯定有路径
					hasInnerSdCard = true;
					innerSdcard = info[2];
					Log.i("zeng", "innerSdcard:" + innerSdcard);
				}
			}
			// 若没有明确标示有外置sd卡，且有sdcard，则这个卡的路径为外置sdcard的路径
			if (!hasExternalSdCard && hasInnerSdCard) {
				hasExternalSdCard = true;
				externalSdcard = innerSdcard;
				Log.i("zeng", "externalSdcard:" + externalSdcard);
				// 如果配置文件里的路径和api得到的路径不一致，则有内置sd卡
				if (!Environment.getExternalStorageDirectory().toString()
						.equals(externalSdcard)) {
					hasInnerSdCard = true;
					innerSdcard = Environment.getExternalStorageDirectory()
							.toString();
					Log.i("zeng", "innerSdcard:" + innerSdcard);
				} else// 否则，仅有外置sd卡
				{
					hasInnerSdCard = false;
					innerSdcard = null;
					Log.i("zeng", "innerSdcard:" + innerSdcard);
				}
			}
			//如果内外置都未识别出来，或者识别出来容量却为0
			if((hasInnerSdCard == false && hasExternalSdCard == false) || ((hasInnerSdCard == true && getTotalStorage(innerSdcard)==0) && (hasExternalSdCard == true && getTotalStorage(externalSdcard) == 0)))
			{
				hasInnerSdCard = false;
				hasExternalSdCard = true;
				externalSdcard = Environment.getExternalStorageDirectory()
						.toString();
			}
		} else {//没有配置文件，则通过系统API取得外置sd卡路径
			hasInnerSdCard = false;
			hasExternalSdCard = true;
			externalSdcard = Environment.getExternalStorageDirectory()
					.toString();
		}

	};

	private void initVoldFstabToCache() throws IOException {
		cache.clear();
		BufferedReader br = new BufferedReader(new FileReader(vold_fastb));
		String tmp = null;
		while ((tmp = br.readLine()) != null) {
			// the words startsWith "dev_mount" are the SD info
			if (tmp.startsWith(HEAD) || tmp.startsWith(" " + HEAD)) {
				if (tmp.startsWith(HEAD)) {
					cache.add(tmp);
				}
				if (tmp.startsWith(" " + HEAD))// 华为U9508手机开头多一个空格
				{
					cache.add(tmp.substring(1, tmp.length()));
				}

			}
		}
		br.close();
		cache.trimToSize();
		Log.i("zeng", "vold_fastb 内容:" + cache.toString());
	}

	/**
	 * 获取存储卡的剩余容量，单位为字节
	 * 
	 * @param pPath
	 *            存储卡路径
	 * @return 剩余容量 为0证明该卡不可访问
	 */
	public long getAvailableStorage(String pPath) {
		long availableStorage = 0;
		try {
			StatFs stat = new StatFs(pPath);
			availableStorage = (long) stat.getAvailableBlocks()
					* (long) stat.getBlockSize();
			Log.i("zeng", "getAvailableStorage:" + availableStorage);
		} catch (Exception e) {

		}

		return availableStorage;
	}

	private long getTotalStorage(String path) {
		String storageDirectory = path;
		if (path == null) {
			return 0;
		}
		StatFs stat = null;
		try {
			stat = new StatFs(storageDirectory);
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
		return (long) stat.getBlockCount() * (long) stat.getBlockSize();
	}

	/**
	 * 获取外置SD卡空闲容量的一半
	 * 
	 * @return 容量
	 */
	public int getExternalSdCardAvalibleHalf() {
		return (int) (getExternalSdCardAvalible() / (1024 * 1024 * 2));
	}

	/**
	 * 获取内置SD卡空闲容量的一半
	 * 
	 * @return 容量
	 */
	public int getInnerSdCardAvalibleHalf() {
		return (int) (getInnerSdCardAvalible() / (1024 * 1024 * 2));
	}

	/**
	 * 获取某个sd卡路径的一半容量
	 * 
	 * @param pPath
	 *            sd卡路径
	 * @return 容量
	 */
	public int getSdCardAvalibleHalf(String pPath) {
		return (int) (getAvailableStorage(pPath) / (1024 * 1024 * 2));
	}

	/**
	 * 获取sd卡数量 内、外置
	 * 
	 * @return 1 只有内置，2 有内、外置
	 */
	public int getSDCardNum() {
		if (this.getExternalSdCardPath() != null
				&& !this.getExternalSdCardPath().equals("")) {
			this.mSDCardNum = 2;
		} else if ((fetchDiskInfo().indexOf(this.getExternalSdCardPath()) == -1)
				|| (this.getExternalSdCardCapacity() == 0)) {
			this.mSDCardNum = 1;
		}

		return this.mSDCardNum;
	}

	// 获取磁盘信息
	public String fetchDiskInfo() {
		String result = null;
		UtilCMDExecute cmdexe = new UtilCMDExecute();
		try {
			String[] args = { "/system/bin/df" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/*
	 * 获取可用sd卡路径，貌似不好用public String[] getVolumePaths(Activity activity) {
	 * 
	 * if (activity != null) { mStorageManager = (StorageManager)activity
	 * .getSystemService(Activity.STORAGE_SERVICE); try { mMethodGetPaths =
	 * mStorageManager.getClass() .getMethod("getVolumePaths"); } catch
	 * (NoSuchMethodException e) { e.printStackTrace(); } }
	 * 
	 * String[] paths = null; try { paths = (String[])
	 * mMethodGetPaths.invoke(mStorageManager); } catch
	 * (IllegalArgumentException e) { e.printStackTrace(); } catch
	 * (IllegalAccessException e) { e.printStackTrace(); } catch
	 * (InvocationTargetException e) { e.printStackTrace(); } return paths; }
	 */
	
	/**
	 * 使用系统函数判断是否有SD卡
	 * 
	 * @return true 有 false 没有
	 */
	public boolean existSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}
	
	/**
	 * 使用系统函数得到SD卡剩余空间
	 * @return
	 */
	public long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		// return freeBlocks * blockSize; //单位Byte
		// return (freeBlocks * blockSize)/1024; //单位KB
		return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
	}
}



/*
 * http://blog.manhuahe.net/2013/01/android开发中如何获取外置tf卡的路径/
 * try {
	Runtime runtime = Runtime.getRuntime();
	Process proc = runtime.exec("mount");
	InputStream is = proc.getInputStream();
	InputStreamReader isr = new InputStreamReader(is);
	String line;
	String mount = new String();
	BufferedReader br = new BufferedReader(isr);
	while ((line = br.readLine()) != null) {
		if (line.contains("secure")) continue;
		if (line.contains("asec")) continue;
		
		if (line.contains("fat")) {
			String columns[] = line.split(" ");
			if (columns != null && columns.length > 1) {
				mount = mount.concat("*" + columns[1] + "\n");
			}
		} else if (line.contains("fuse")) {
			String columns[] = line.split(" ");
			if (columns != null && columns.length > 1) {
				mount = mount.concat(columns[1] + "\n");
			}
		}
	}
	txtView.setText(mount);
	
} catch (FileNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}*/
