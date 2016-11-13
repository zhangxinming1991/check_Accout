package file_op;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import en_de_code.ED_Code;

/**
 * AnyFile_Op 文件的常用操作:读写文件，创建目录等
 * @author zhangxinming
 * @version 1.0.0
 */
public class AnyFile_Op{

	public byte [] ReadFile(MultipartFile mfile){
		if (mfile.getOriginalFilename().equals("")) {
			return null;
		}
		
		long filesize = mfile.getSize();
		byte read_b[] = new byte[(int)filesize];
        /*读取上传的文件的内容*/
		InputStream inputStream;
		try {
			inputStream = mfile.getInputStream();
			inputStream.read(read_b); 
			inputStream.close();
			System.out.println(ED_Code.ByteArray_Act_Len(read_b));
		/*读取上传的文件的内容*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return read_b;
	}
	
	public boolean WriteFile(AnyFileElement aF_Element,byte []read_b,File file){
		FileOutputStream out = null;
		
		/*将文件保存到服务器指定目录中*/
		System.out.println(aF_Element.filename);
		try {
			out = new FileOutputStream(file);
			out.write(read_b,0,aF_Element.filesize);
			out.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		/*将文件保存到服务器指定目录中*/		
	}
	
	public  File CreateDir(String dirname) {
		/*判断文件夹是否存在*/
		File dir = new File(dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			System.out.println(dirname + "不存在");
			dir.mkdirs();
		}
		/*判断文件夹是否存在*/
		return dir;
	}
	
	public File CreateFile(String dirname,String filename) {
		/*判断文件是否存在*/
		boolean exitflag = true;
		File file = new File(dirname + "/" + filename);
		if (!file.exists()) {
			exitflag = false;
			System.out.println(filename + "don't exit"); 
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{//重新创建,在linux下是否存在权限问题
			System.out.println(filename + "delet and create again");
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*判断文件是否存在*/
		return file;
	}
	
	public class AnyFileElement{
		public String filename;
		public String dirname;
		public int filesize;

		public AnyFileElement(String filename,String dirname,int filesize) {
			this.filename = filename;
			this.dirname = dirname;
			this.filesize = filesize;
	}
		// TODO Auto-generated constructor stub
	}
}
