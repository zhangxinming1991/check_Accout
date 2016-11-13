package file_op;

import java.io.File;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import file_op.AnyFile_Op.AnyFileElement;

/**
 * WRFileThread 写文件系统，后期用于提高系统，测试中，在系统中还没使用
 * @author zhangxinming
 * @category debug
 * @version 1.0.0
 *
 */
public class WRFileThread implements Runnable{
	
	private AnyFile_Op aOp;
	private MultipartFile file;
	private String savedir;
	private String filename;
	public WRFileThread(MultipartFile file,String savedir,String filename) {
		// TODO Auto-generated constructor stub
		this.file = file;
		this.aOp = new AnyFile_Op();
		this.savedir = savedir;
		this.filename = filename;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		long filesize = file.getSize();
		
		AnyFileElement aElement = aOp.new AnyFileElement(filename, savedir, (int)filesize);
		
		/*创建保存目录*/
		File dir = aOp.CreateDir(savedir);
		/*创建保存目录*/
		
		/*创建保存文件*/
		File wFile = aOp.CreateFile(aElement.dirname, aElement.filename);
		/*创建保存文件*/
		
		byte read_b[] = aOp.ReadFile(file);/*读取上传的文件的内容*/
		
		aOp.WriteFile(aElement,read_b,wFile);/*将文件保存到服务器指定目录中*/
	}

}
