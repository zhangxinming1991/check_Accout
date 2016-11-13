package check_Asys;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.web.multipart.MultipartFile;

import check_Asys.CheckAcManage.Dao_List;
import dao.Assistance_Dao;
import dao.BankInput_Dao;
import dao.ConnectPerson_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.PayRecord;
import entity.PayRecordCache;
import file_op.AnyFile_Op;
import file_op.AnyFile_Op.AnyFileElement;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 客户上传信息服务类，这个类提供接受客户上传付款信息的服务
 * @author zhangxinming 
 * @version 1.0.0
 *
 */
public class ConnectP_PayService {
	private static Logger logger = LogManager.getLogger(ConnectP_PayService.class);
	public PayRecord_Dao pDao;
	public ConnectPerson_Dao cDao;
	public PayRecordCache_Dao pCDao;
	public PayRecordHistory_Dao pHDao;
	public SendStore_Dao sDao;
	
	public ConnectP_PayService(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		pDao = new PayRecord_Dao(wFactory);
		cDao = new ConnectPerson_Dao(wFactory);
		pCDao = new PayRecordCache_Dao(wFactory);
		pHDao = new PayRecordHistory_Dao(wFactory);
		sDao = new SendStore_Dao(wFactory);
	}
	
	/**
	 * Upload_Pay 接受客户上传的付款信息
	 * @param pRecord 付款信息的文字部分
	 * @param mfile 付款信息的凭证部分
	 * @param savedir 凭证保存的路径
	 * @param fileName 凭证保存的文件名
	 * @author zhangxinming
	 */
	public void Upload_Pay(PayRecordCache pRecord,MultipartFile mfile,String savedir,String fileName){
		
		if (mfile != null) {
			AnyFile_Op aOp= new AnyFile_Op();
			long filesize = mfile.getSize();
		
			AnyFileElement aElement = aOp.new AnyFileElement(fileName, savedir, (int)filesize);
			
			/*读取并保存文件*/
			aOp.CreateDir(aElement.dirname);
			File upload_file = aOp.CreateFile(aElement.dirname, aElement.filename);
			byte read_b[] = aOp.ReadFile(mfile);
			aOp.WriteFile(aElement, read_b, upload_file);
			/*读取并保存文件*/	
		}
		
		pRecord.setPass(false);
		pRecord.setLinkCer("/check_Accout/" + "付款记录/" + pRecord.getOwner() + "/" + pRecord.getPayer() + "/" + fileName);
		pCDao.add(pRecord);
	}
	
	/**
	 * Upload_PayToWArea 该函数被弃用
	 * @param pRecord
	 * @param mfile
	 * @param savedir
	 * @param fileName
	 * @deprecated
	 * @author zhangxinming
	 */
	public void Upload_PayToWArea(PayRecord pRecord,MultipartFile mfile,String savedir,String fileName){
		
		AnyFile_Op aOp= new AnyFile_Op();
		long filesize = mfile.getSize();
		AnyFileElement aElement = aOp.new AnyFileElement(fileName, savedir, (int)filesize);
		
		/*读取并保存文件*/
		aOp.CreateDir(aElement.dirname);
		File upload_file = aOp.CreateFile(aElement.dirname, aElement.filename);
		byte read_b[] = aOp.ReadFile(mfile);
		aOp.WriteFile(aElement, read_b, upload_file);
		/*读取并保存文件*/
		
		pRecord.setPass(false);
		pRecord.setLinkCer("/check_Accout/" + "付款记录/" + pRecord.getOwner() + "/" + pRecord.getPayer() + "/" + fileName);
		pDao.add(pRecord);
	}

	/**
	 * Get_CandA 获取客户的合同号和付款账号信息，在客户上传的时候提供给用户选择，不用客户输入
	 * @param username 对账联系人的用户名
	 * @return
	 * @author zhangxinming
	 */
	public JSONObject Get_CandA(String username){
		ConnectPerson fPerson = cDao.findById(ConnectPerson.class, username);
		CusSecondstore customs = sDao.findById(CusSecondstore.class, fPerson.getCompany());
		
		/*获取合同信息*/
		JSONObject jCandA = new JSONObject();

		JSONArray jmany_pay = new JSONArray();
		String many_pay = customs.getContractMes();
		if (many_pay == null) {
			jCandA.element("many_pay", jmany_pay);//合同信息
		}
		else{
				jmany_pay = JSONArray.fromObject(many_pay);
				jCandA.element("many_pay", jmany_pay);//合同信息
		}
		/*获取合同信息*/
		
		/*获取账号信息*/
		JSONArray jaccout = new JSONArray();
		String accout = customs.getAccoutMes();
		if (accout == null) {
			jCandA.element("accout", jaccout);
		}
		else{
			jaccout = JSONArray.fromObject(accout);
			jCandA.element("accout", jaccout);
		}

		logger.info(jaccout.size());
		/*获取账号信息*/
		
		return jCandA;
	}
}
