package file_op;

import java.io.File;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import file_op.AnyFile_Op.AnyFileElement;

/**
 * WRFileThread д�ļ�ϵͳ�������������ϵͳ�������У���ϵͳ�л�ûʹ��
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
		
		/*��������Ŀ¼*/
		File dir = aOp.CreateDir(savedir);
		/*��������Ŀ¼*/
		
		/*���������ļ�*/
		File wFile = aOp.CreateFile(aElement.dirname, aElement.filename);
		/*���������ļ�*/
		
		byte read_b[] = aOp.ReadFile(file);/*��ȡ�ϴ����ļ�������*/
		
		aOp.WriteFile(aElement,read_b,wFile);/*���ļ����浽������ָ��Ŀ¼��*/
	}

}
