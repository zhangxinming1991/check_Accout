package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import check_Asys.ConnectP_PayService;
import check_Asys.OpLog_Service;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.ConnectPerson;
import entity.PayRecord;
import entity.PayRecordCache;
import entity.PayRecordHistory;
import file_op.AnyFile_Op;
import httpexcutor.MediaDownloadRequestExecutor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * ConnectP_Pay_Controller 控制用户的付款信息上传
 * @author Simon
 *
 */
@Controller
public class ConnectP_Pay_Controller {
	
	private static Logger logger = LogManager.getLogger(ConnectP_Pay_Controller.class);
	/*全局变量*/
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public final static ConnectP_PayService cps = new ConnectP_PayService(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	/*全局变量*/
	
	/**
	 * upload_pay_2 上传付款信息
	 * @category app和web客户端上传付款信息接口
	 * @param request
	 * @param response
	 * @param mfile
	 * @author zhangxinming
	 */
	@RequestMapping(value="/upload_pay_2")
	public void upload_pay_2(HttpServletRequest request,HttpServletResponse response,@RequestParam("file") MultipartFile mfile){
		logger.info("***Get upload_pay_2 request***");
		JSONObject re_jsonobject = new JSONObject();
/*		String request_s = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				logger.info(request_s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		String username = null;
		String payer = null;
		String pay_money = null;
		String pay_way = null;
		String pay_account = null;
		String many_pay = null;
		String owner = null;
		String receiver = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			payer = cps.cDao.findById(ConnectPerson.class, username).getCompany();//获取客户名称
			pay_money = AES.aesDecrypt(request.getParameter("pay_money"),AES.key); //获取付款金额
			pay_way = new String(AES.aesDecrypt(request.getParameter("pay_way"),AES.key).getBytes("GBK"),"GBK");//获取付款方式
			pay_account = AES.aesDecrypt(request.getParameter("pay_account"),AES.key);//获取付款账号
			logger.info("付款账号为" + pay_account);
			many_pay = AES.aesDecrypt(request.getParameter("many_pay"),AES.key);//获取付款的合同及金额信息
			receiver = new String(AES.aesDecrypt(request.getParameter("receiver"),AES.key).getBytes("GBK"),"GBK");//获取款项接受人信息
			owner = AES.aesDecrypt(request.getParameter("owner"),AES.key);//获取付款记录所属代理商信息
			logger.info(owner);
			ED_Code.printHexString(owner.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("解密上传付款信息错误");
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay, OpLog_Service.result_failed);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "解密上传付款信息错误");
			Common_return(response,re_jsonobject);
			
			e.printStackTrace();
		}//获取用户名
		
		/*获取付款的合同及金额信息*/
		JSONArray jamany_pay = JSONArray.fromObject(many_pay);
		for(int i = 0;i<jamany_pay.size();i++){
			JSONObject jomany_pay = (JSONObject) jamany_pay.get(i);
			Double money = Double.parseDouble((String)jomany_pay.get("money"));//如果金额为空的话
			jomany_pay.put("money", money);
			jamany_pay.set(i, jomany_pay);
		}
		String new_many_pay = jamany_pay.toString();
		logger.info("付款合同及金额信息");
		/*获取付款的合同及金额信息*/

		String savedir = request.getServletContext().getRealPath("/" + "付款记录/" + owner + "/" + payer);
		
		Date date = new Date();
		SimpleDateFormat sFormatf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		SimpleDateFormat sFormatt = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
		sFormatf.format(date);
		String fileName = sFormatf.format(date) + mfile.getOriginalFilename().substring(mfile.getOriginalFilename().lastIndexOf("."));
		
		String caid = CreateCaid(owner);
		
		PayRecordCache ipayRecord = new PayRecordCache();
		ipayRecord.setPayer(payer);
		ipayRecord.setOwner(owner);
		ipayRecord.setManyPay(new_many_pay);
		ipayRecord.setPayWay(pay_way);
		ipayRecord.setReceiver(receiver);
		ipayRecord.setPayMoney(Double.parseDouble(pay_money));
		ipayRecord.setPayAccount(pay_account);
		ipayRecord.setPass(false);
		ipayRecord.setLinkCer("/check_Accout/" + "付款记录/" + owner + "/" + payer + "/" + fileName);
		ipayRecord.setUploadTime(sFormatt.format(date));
		ipayRecord.setConnPerson(username);
		ipayRecord.setIsconnect(false);
		ipayRecord.setCaid(caid);
		
		cps.Upload_Pay(ipayRecord,mfile,savedir,fileName);//调用上传服务
		
		oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay, OpLog_Service.result_success);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "上传成功");
		Common_return(response,re_jsonobject);
	}
	
	/**
	 * upload_pay_weixin 上传付款信息
	 * @category 微信段上传付款信息接口
	 * @param request
	 * @param response
	 * @param mfile
	 * @author zhangxinming
	 */
	@RequestMapping(value="/upload_pay_weixin")
	public void upload_pay_weixin(HttpServletRequest request,HttpServletResponse response){
		logger.info("***get upload_pay_weixin request***");
		JSONObject re_jsonobject = new JSONObject();
		
/*		String request_s = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				logger.info(request_s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		String username = null;
		String payer = null;
		String pay_money = null;
		String pay_way = null;
		String pay_account = null;
		String many_pay = null;
		String owner = null;
		String receiver = null;
		String imageUrl = null;
		try {
			imageUrl = AES.aesDecrypt(request.getParameter("imageUrl"),AES.key);
			logger.info("imageUrl：" + imageUrl);
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			payer = cps.cDao.findById(ConnectPerson.class, username).getCompany();//获取客户名称
			pay_money = AES.aesDecrypt(request.getParameter("pay_money"),AES.key); //获取付款金额
			pay_way = new String(AES.aesDecrypt(request.getParameter("pay_way"),AES.key).getBytes("GBK"),"GBK");//获取付款方式
			logger.info("pay_way:" + pay_way);
			pay_account = AES.aesDecrypt(request.getParameter("pay_account"),AES.key);//获取付款账号
			logger.info("pay_account：" + pay_account);
			many_pay = AES.aesDecrypt(request.getParameter("many_pay"),AES.key);//获取付款的合同及金额信息
			receiver = new String(AES.aesDecrypt(request.getParameter("receiver"),AES.key).getBytes("GBK"),"GBK");//获取款项接受人信息
			owner = AES.aesDecrypt(request.getParameter("owner"),AES.key);//获取付款记录所属代理商信息
			logger.info(owner);
			ED_Code.printHexString(owner.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("解密微信上传付款信息错误" + e);
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_failed);
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "解密微信上传付款信息错误");
			Common_return(response,re_jsonobject);
			
			e.printStackTrace();
			return;
		}//获取用户名
		
		/*获取付款的合同及金额信息*/
		JSONArray jamany_pay = JSONArray.fromObject(many_pay);
		for(int i = 0;i<jamany_pay.size();i++){
			JSONObject jomany_pay = (JSONObject) jamany_pay.get(i);
			Double money = Double.parseDouble((String)jomany_pay.get("money"));//如果金额为空的话
			jomany_pay.put("money", money);
			jamany_pay.set(i, jomany_pay);
		}
		String new_many_pay = jamany_pay.toString();
		logger.info("付款合同及金额信息");
		/*获取付款的合同及金额信息*/
		
		String savedir = request.getServletContext().getRealPath("/" + "付款记录/" + owner + "/" + payer);
		
		Date date = new Date();
		SimpleDateFormat sFormatf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		SimpleDateFormat sFormatt = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
		sFormatf.format(date);

		String fileName = sFormatf.format(date);
		
		String caid = CreateCaid(owner);
		
		PayRecordCache ipayRecord = new PayRecordCache();
		ipayRecord.setPayer(payer);
		ipayRecord.setOwner(owner);
		ipayRecord.setManyPay(new_many_pay);
		ipayRecord.setPayWay(pay_way);
		ipayRecord.setReceiver(receiver);
		ipayRecord.setPayMoney(Double.parseDouble(pay_money));
		ipayRecord.setPayAccount(pay_account);
		ipayRecord.setPass(false);
		ipayRecord.setUploadTime(sFormatt.format(date));
		ipayRecord.setConnPerson(username);
		ipayRecord.setIsconnect(false);
		ipayRecord.setCaid(caid);
		
		String newfilename = null;
		try {
			
			CloseableHttpClient client = HttpClients.createDefault();
			newfilename = new MediaDownloadRequestExecutor().Excute_post(client, null, imageUrl, fileName,savedir);
			ipayRecord.setLinkCer("/check_Accout/" + "付款记录/" + owner + "/" + payer + "/" + newfilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("获取微信服务器图片失败" + e);
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_failed);
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取微信服务器图片失败");
			Common_return(response,re_jsonobject);
			e.printStackTrace();
		}
		
		cps.Upload_Pay(ipayRecord,null,savedir,newfilename);
		
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "上传成功");
		oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_success);
		Common_return(response,re_jsonobject);
	}
	
	/**
	 * Get_CandA 获取客户的合同和账号信息
	 * @param request
	 * @param response
	 * @category 客户端接口
	 * @author zhangxinming
	 */
	@RequestMapping(value="/get_contractandaccout")
	public void Get_CandA(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get get_contractandaccout request***");
		// TODO Auto-generated constructor stub
		String username = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject re_object = cps.Get_CandA(username);
		
		Get_CandA_return(response,re_object);
	}
	
	/**
	 * Check_PayMes 查询客户付款信息
	 * @param request
	 * @param response
	 * @category 客户端接口
	 * @author zhangxinming
	 */
	@RequestMapping(value="/check_pay_mes")
	public void Check_PayMes(HttpServletRequest request,HttpServletResponse response){
		logger.info("***check_pay_mes request***");
		JSONObject re_jsonobject = new JSONObject();
		
		String username = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("获取提交参数失败" + e);
			e.printStackTrace();
			
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("获取提交参数失败", "上传成功");
			Common_return(response,re_jsonobject);
		}
		ConnectPerson check_cp = cps.cDao.findById(ConnectPerson.class, username);
		String companyid = check_cp.getCompany();
		List<PayRecordCache> payRecordCaches = cps.pCDao.GetPayRecordsTbByElment("payer", companyid);
		List<PayRecord> payRecords = cps.pDao.FindBySpeElement_S("payer", companyid);
		List<PayRecordHistory> payRecordHistories =  cps.pHDao.GetPrecordTbByElement("payer", companyid);
		
		LinkedList<Object> re_list = new LinkedList<Object>();
		for (int i = 0; i < payRecordHistories.size(); i++) {
			re_list.addFirst(payRecordHistories.get(i));
		}
		for (int i = 0; i < payRecords.size(); i++) {
			re_list.addFirst(payRecords.get(i));
		}
		for (int i = 0; i < payRecordCaches.size(); i++) {
			re_list.addFirst(payRecordCaches.get(i));
		}


		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "返回成功");
		OneKeyArray_return_enData(response,re_jsonobject,"data",re_list);

	}
		
	/**
	 * CreateCaid 产生特定的对账id,这个id区分了每个代理商及每个月的对账的不同
	 * @param owner
	 * @return
	 * @author zhangxinming
	 */
	public String CreateCaid(String owner){
		//生成新的对账id
		Date datey = new Date();
		Date datem = new Date();
		SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
		SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		String dateys = sdfy.format(datey);//本年
		String datems = sdfm.format(datem);//本月
		String caid = dateys + "-" + datems + "-" + owner;//对账id
		
		return caid;
	}

    /**
     * OneKeyArray_return_enData 带一个具体加密信息的返回，只加密具体信息
     * @param response
     * @param re_json 操作结果及具体信息
     * @param key 具体信息的key
     * @param data 具体信息
     */
    public void OneKeyArray_return_enData(HttpServletResponse response,JSONObject re_json,String key,Object data){
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
    	

		JSONArray aes_object = JSONArray.fromObject(data);
		logger.info(aes_object);
		String aes_object_s = aes_object.toString();
		String en_s = null;
		try {
			en_s = AES.aesEncrypt(aes_object_s,AES.key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		re_object.element(key, en_s);
		
		/*传递json数据给前台*/
		try {
			logger.info("send content：" + re_object.toString());
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }

    /**
     * Get_CandA_return 返回客户的合同和账号信息
     * @param response
     * @param re_object
     * @author zhagnxinming
     */
	public void Get_CandA_return(HttpServletResponse response,JSONObject re_object){
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setCharacterEncoding("utf-8");
		JSONObject new_object = new JSONObject();
		
		new_object.element("flag", 0);
		new_object.element("errmsg", "返回成功");
		
		try {
			logger.info(re_object.getString("many_pay"));
			logger.info(re_object.getString("accout"));
			String en_many_pay = AES.aesEncrypt(re_object.getString("many_pay"),AES.key);
			String en_accout = AES.aesEncrypt(re_object.getString("accout"),AES.key);
			new_object.element("many_pay", en_many_pay);
			new_object.element("accout", en_accout);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*传递json数据给前台*/
		System.out.println(new_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(new_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
	}

	/**
	 * Common_return 普通返回，没有带具体的data,只有成功或者失败
	 * @param response
	 * @param jsonObject
	 */
	public void Common_return(HttpServletResponse response,JSONObject jsonObject){
		response.setCharacterEncoding("utf-8");
		response.addHeader("Access-Control-Allow-Origin", "*");	
		
		JSONObject re_object =  jsonObject;//传递参数中的最外层对象
		
		/*传递json数据给前台*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
	}
}
