package com.yuxiao.buz.baseframework.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 执行shell命令工具类
 * @author Rocky
 *
 */
public class UtilCMDExecute {
	
	/**
	 * 执行命令
	 * @param pCmd shell 命令 如"ls" 或"ls -1"
	 * @param pWorkDirectory 命令执行路径 如"/"
	 * @return 命令执行后的结果
	 * @throws IOException
	 */
	public String run(String[] pCmd, String pWorkDirectory)
			throws IOException {
		String result = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(pCmd);
			// set working directory
			if (pWorkDirectory != null)
				builder.directory(new File(pWorkDirectory));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while (in.read(re) != -1) {
				System.out.println(new String(re));
				result = result + new String(re);
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
