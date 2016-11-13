package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sun.org.apache.bcel.internal.generic.NEW;

import file_op.Excel_RW;

/**
 * FormManagerController
 * @author zhangxinming
 * @deprecated
 *
 */
@Controller
public class FormManagerController {

	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public String totalOri_Path;
	public String dirname;
	@RequestMapping(value="/TotalAccount")
	public String GetTotalAccount(HttpServletRequest request,HttpServletResponse response){
		System.out.println("get TotalAccount");
		HttpSession session = request.getSession(false);
		if (session == null) {
			try {
				response.sendRedirect("index.jsp");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String userType = (String) session.getAttribute("usertype");
		OwerAtr oAtr = new OwerAtr(work_id, userType);

		
		dirname = request.getServletContext().getRealPath("/报表中心/");
		totalOri_Path = "2016" + work_id + "总账单报表.xlsx";
		String sheetname = "八月";
		ExcelFileAtr eAtr = new ExcelFileAtr(dirname,totalOri_Path,sheetname);
		
		List<String> list = Excel_RW.WriteExcel(eAtr,oAtr,wFactory);
		
		request.setAttribute("links_name", list.get(0));
		request.setAttribute("links", list.get(1));
		return "/FormCenter.jsp";
	}
	
	@RequestMapping(value="/Export_All_Result")
	public void  Export_ALL_Result(HttpServletRequest request) {
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date_s = sFormat.format(new Date());
		
		
		String allresultdir = request.getServletContext().getRealPath("/" + date_s + "对账结果");
		File file_dir = new File(allresultdir);
		if (!file_dir.exists() && !file_dir.isDirectory()) {
			System.out.println("目录不存在");
			file_dir.mkdirs();
		}
		ZipOutputStream outputStream;
		try {
			String zipfile_name = date_s + ".zip";
			outputStream = new ZipOutputStream(new FileOutputStream(allresultdir + "/" + zipfile_name));
			
			File tol_file = new File(dirname + "/" + totalOri_Path);
			System.out.println(tol_file.getName()+":" + tol_file.length());
			
			FileInputStream inputStream = new FileInputStream(tol_file);
			byte[] rd = new byte[(int)tol_file.length()];
			inputStream.read(rd);
			
			outputStream.putNextEntry(new ZipEntry(tol_file.getName()));
			outputStream.write(rd);
			
			inputStream.close();			
			outputStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
	public class ExcelFileAtr{
		public String dirname;
		public String filename;
		public String sheetname;
		public ExcelFileAtr(String dirname,String filename,String sheetname) {
			this.dirname = dirname;
			this.filename = filename;
			this.sheetname = sheetname;
		}
	}
	
	public class OwerAtr{
		public 	String work_id;
		public String userType;
		public OwerAtr (String work_id,String userType) {
			this.work_id = work_id;
			this.userType = userType;
		}
	}
		
		// TODO Auto-generated constructor stub
}
