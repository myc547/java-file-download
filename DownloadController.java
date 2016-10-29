package com.myc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DownloadController {
	
	@RequestMapping(value="/download")
	@ResponseBody
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		File dir = new File("F:/zip");
		if (!dir.exists()){
			dir.mkdirs();
		}
		File zipFile = new File(dir,"所有文件.zip");
		zipFile.createNewFile();
		
		Map<String,String> fileMap = new ConcurrentHashMap<String, String>();
		fileMap.put("F:/download/game/你好.exe", "你好.exe");
		fileMap.put("F:/download/game/mysql-5.7.13-linux-glibc2.5-x86_64.tar.gz", "mysql-5.7.13-linux-glibc2.5-x86_64.tar.gz");
		
		boolean flag = zipFile(zipFile, fileMap);
		if (!flag){
			return;
		}
		
		String fileName = zipFile.getName();
		/*File file = new File("F:/download/game/你好.rar");
		String fileName = file.getName();*/
		response.reset();   
		
		
		String userAgent = request.getHeader("User-Agent"); 
		//针对IE或者以IE为内核的浏览器：
		if (userAgent.contains("MSIE")||userAgent.contains("Trident")) {
		fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
		} else {
		//非IE浏览器的处理：
		fileName = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
		}
		response.setContentType("application/octet-stream");   
		response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", fileName));
        response.addHeader("Content-Length", "" + zipFile.length());
        response.setCharacterEncoding("UTF-8"); 
        InputStream fis = new BufferedInputStream(new FileInputStream(zipFile));   
        OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
        writeFile(fis, toClient);
        toClient.flush();   
        toClient.close();  
		
	}
	
	public static boolean zipFile(File zipFile , Map<String,String> fileMap) {
		boolean flag = true;
		try {
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			
			for (Map.Entry<String, String>  entry :fileMap.entrySet()){
				 InputStream in = new BufferedInputStream(new FileInputStream(new File(entry.getKey())));
				 ZipEntry zipEntry = new ZipEntry(entry.getValue());
				 zipOut.putNextEntry(zipEntry);
				 writeFile(in, zipOut);
			}
			zipOut.flush();   
			zipOut.close();  
		} catch (FileNotFoundException e) {
			flag = false;
			e.printStackTrace();
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		} 
		
		return flag;
	}
	
	public static void writeFile(InputStream in, OutputStream out) throws IOException{
		 byte[] buffer = new byte[1024 * 1024 * 4];   
         int i = -1;   
         while ((i = in.read(buffer)) != -1) {   
        	out.write(buffer, 0, i);  
         }   
         in.close();   
	}
	
	public static void deleteFile(File file){
		if (file.exists()){
			file.delete();
		}
	}
}
