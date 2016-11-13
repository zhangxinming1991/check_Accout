package controller;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sun.jndi.url.corbaname.corbanameURLContextFactory;
import com.sun.org.apache.bcel.internal.generic.NEW;

import check_Asys.AutoCheckAuccount;
import check_Asys.CheckAcManage;
import check_Asys.CheckAcManage.Export_CAResObject;
import check_Asys.CheckAcManage.Import_Object;
import check_Asys.CheckAcManage.Map_Object;
import check_Asys.CheckAcManage.Owner;
import check_Asys.CheckAcManage.Watch_CAResObject;
import check_Asys.CheckAcManage.Watch_Object;
import check_Asys.OpLog_Service;
import controller.FormManagerController.OwerAtr;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.Assistance;
import entity.BankInput;
import entity.CaresultHistory;
import entity.CusSecondstore;
import entity.OriOrder;
import entity.PayRecord;
import entity.PayRecordCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Check_MainController 对账业务控制类，这个类提供了对账的所有业务，包括进入对账流程（上传货款表和出纳表，审阅付款信息，执行对账操作，查看对账结果
 * 以及重新对账），预览新上传的付款信息，查看历史对账结果
 * @author zhangxinming
 * @version 1.0.0
 */
@Controller
public class Check_MainController {
	private static Logger logger = LogManager.getLogger(Check_MainController.class);
	private static Logger logger_error = LogManager.getLogger("error");
	/*全局变量*/
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public final static CheckAcManage cOp = new CheckAcManage(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	/*全局变量*/
	
	/**
	 * Enter_CaModel 进入对账模式
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="enter_camodel")
	public void Enter_CaModel(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		String who = (String)session.getAttribute("workId");//获取用户名
		String agentid = (String)session.getAttribute("agentid");//设置操作者的所属代理商id
		Owner owner = cOp.new Owner();
		owner.work_id = agentid;
		
		String caid = (String) cOp.OpSelect(CheckAcManage.ENTRER_CaModel, null,owner);//处理进入对账就模式操作
		if (caid != null) {
			oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.ENTRER_CaModel, OpLog_Service.result_success);//插入操作日志
			jsonObject.element("flag", 0);
			jsonObject.element("errmsg", "进入对账模式成功");
			OneKeyData_return(response, jsonObject, "caid", caid);;//返回进入对账模式操作的结果
		}
		else{
			oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.ENTRER_CaModel, OpLog_Service.result_failed);
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "产生对账id失败，进入对账模式失败");
			Common_return_en(response,jsonObject);
		}
		
		
	}
	
	/**
	 * GoToMain 返回主菜单,该函数已经被舍弃
	 * @param request
	 * @param response
	 * @deprecated
	 * @author zhangxinming
	 */
	@RequestMapping(value="goto_main")
	public void GoToMain(HttpServletRequest request,HttpServletResponse response){
		logger.info("goto_main request");
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String agent_id = (String)session.getAttribute("agentid");
		//返回付款缓冲区的付款记录数目
		int newpay_num = 0;
		newpay_num = cOp.dao_List.pCDao.GetPayRecordsTb(agent_id).size();
		
		Goto_Main_return(response,newpay_num);
	}
	
	/**
	 * Upload_file 上传货款表及出纳
	 * @param request
	 * @param mfileA (A:accout)货款excel表
	 * @param mfileB (B:bankinput)出纳excel表
	 * @param response
	 */
	@RequestMapping(value="/upload")
	public void Upload_file(HttpServletRequest request,@RequestParam("fileA") MultipartFile mfileA,@RequestParam("fileB") MultipartFile mfileB,HttpServletResponse response){
		logger.info("***Get upload request***");
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		String workId = (String)session.getAttribute("workId");//获取用户名
		String agentid = (String)session.getAttribute("agentid");//或者操作者所属代理商id
		
		String caid = null;
		try {
			caid = AES.aesDecrypt(request.getParameter("caid"), AES.key);
			logger.info("对账id为" + caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*初始化工作*/
		cOp.dao_List.tDao.DeleteOoriderByElement("owner", agentid);//清空属于自己的货款表
		cOp.dao_List.bDao.DeleteBinputTbByElement("owner", agentid);//请款属于自己的出纳表
		cOp.auccount.ResetPayRecord(agentid);//重置付款工作区的记录
		/*初始化工作*/
		
		String orifilename = caid + CheckAcManage.FileName_Orider;//货款excel保存文件名
		String binputfilename = caid + CheckAcManage.FileName_BankInput;//出纳excel保存文件名
		if (mfileA.getOriginalFilename().equals("") || mfileB.getOriginalFilename().equals("") ) {//文件的合法性判断
			logger.error("两个文件必须同时存在");
			oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_failed);
			
			/*返回*/
			JSONObject re_json = new JSONObject();
			re_json.element("flag", -1);
			re_json.element("errmsg", "两个文件必须同时存在");
			Common_return_en(response, re_json);
			/*返回*/
		}
		else{
				String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
				Import_Object iObjecta = cOp.new Import_Object('A', mfileA,agentid,savedir_A,orifilename);
				cOp.OpSelect(CheckAcManage.IMPORT,iObjecta,null);//上传货款表
				
				String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
				Import_Object iObjectb = cOp.new Import_Object('B', mfileB,agentid,savedir_B,binputfilename);
				cOp.OpSelect(CheckAcManage.IMPORT,iObjectb,null);//上传出纳表

				oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_success);
				/*返回*/
				JSONObject re_json = new JSONObject();
				re_json.element("flag", 0);
				re_json.element("errmsg", "上传成功");
				Common_return_en(response, re_json);
				/*返回*/
		}
	}
	
	/**
	 * map 关联付款记录和出纳记录
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Map")
	public void map(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Map request***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		String agent_id = (String)session.getAttribute("agentid");

		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		
		String map_op = null;
		int pay_id;
		int bank_id;
		Map_Object map_Object = null;
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//解密数据
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		pay_id = jstr.getInt("pay_id");
		map_op = jstr.getString("map_op");
		
		if (map_op.equals("cer_map")) {//确定匹配
			bank_id = jstr.getInt("bank_id");
			map_Object = cOp.new Map_Object(map_op, pay_id,bank_id);
		}
		else if(map_op.equals("find_map")){//查找匹配
			map_Object = cOp.new Map_Object(map_op, pay_id);
		}
		else if(map_op.equals("cancel_map")){//取消匹配
			bank_id = jstr.getInt("bank_id");
			map_Object = cOp.new Map_Object(map_op, pay_id,bank_id);
		}
		else {
			System.out.println("unknow map_op");
			return;
		}
		
		List<BankInput> fBankInputs = (List<BankInput>)cOp.OpSelect(CheckAcManage.MAP, map_Object,owner);

		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "操作成功");
		OneKeyData_return(response, jsonObject, "data", fBankInputs);
	}
	
	/**
	 * Watch_Mes 查看订单，出纳及付款记录信息
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Watch")
	public void Watch_Mes(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Watch Request***");
		response.setCharacterEncoding("utf-8");//设置编码格式
		response.setContentType("text/html; charset=UTF-8"); 
		JSONObject re_jsonobject = new JSONObject();
		HttpSession session = request.getSession(false);
		if (session == null) {
			logger.info("session is null");
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		Owner owner = cOp.new Owner();
		String work_id = (String)session.getAttribute("workId");//设置操作者id
		owner.work_id = cOp.dao_List.aDao.findById(Assistance.class, work_id).getAgentid();//设置操作者id
		owner.user_type = (String) session.getAttribute("usertype");
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("获取提交参数失败" + e);
			e.printStackTrace();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		Watch_Object wObject = cOp.Create_Watch_Object(jstr);//设置查看参数
		
		java.util.List list = cOp.Watch(wObject, owner);
		
		Watch_return(list,response,wObject);//返回数据到前台
		
	//	cOp.Close_All_Dao();
	}
	
	/**
	 * Check 审阅付款信息
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Check")
	public void Check(HttpServletRequest request,HttpServletResponse response){
		logger.info("Get Map request");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr  = JSONObject.fromObject(request_s_de);
			int id = jstr.getInt("id");
			String op_result = jstr.getString("op_result");
			System.out.println(id + ":" + op_result); 
			
			PayRecord pRecord = cOp.dao_List.pDao.findById(PayRecord.class, id);
			
			/*如果付款信息已经绑定出纳，则取消绑定*/
			if (pRecord.getIsconnect() == true) {
				int payid = pRecord.getId();
				System.out.println(payid);
				int bankInput_id = pRecord.getBankinputId();
				cOp.auccount.CancelConnecttBWithP(payid, bankInput_id);
			}
			/*如果付款信息已经绑定出纳，则取消绑定*/
			pRecord = cOp.dao_List.pDao.findById(PayRecord.class, id);
			pRecord.setCheckResult(op_result.charAt(0));
			cOp.dao_List.pDao.update(pRecord);
			
			jsonObject.element("flag", 0);
			jsonObject.element("errmsg", "审阅成功");
			OneKeyData_return(response, jsonObject, "checkResult", String.valueOf(op_result.charAt(0)));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Start_CheckA_Work 执行对账操作
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Start_CheckA_Work")
	public void Start_CheckA_Work(HttpServletRequest request,HttpServletResponse response){
		logger.info("Get Start_CheckA_Work request");
		JSONObject jmesg =  new JSONObject();//传递参数中的最外层对象
		HttpSession session = request.getSession(false);
		if (session == null) {
			logger.info("session is null");
			jmesg.element("flag", -1);
			jmesg.element("errmsg", "登录超时，请重新登录");
			
			Common_return_en(response, jmesg);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String agent_id = (String)session.getAttribute("agentid");
		String userType = (String) session.getAttribute("usertype");
		
		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		
		String request_s;
		JSONObject jstr;
		String caid = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String opString = CheckAcManage.START_CHECKWORK;
		jmesg = (JSONObject)cOp.OpSelect(opString, caid, owner);
		
		/*产生报表*/
		List<String> exportlist = new ArrayList<String>();
		exportlist.add("export_totalori");
		exportlist.add("export_OriorderHasBInput");
		exportlist.add("export_OriorderNoBInput");	
		
		exportlist.add("export_TruePayNoBInput");
		exportlist.add("export_FalsePayNoBInput");
		exportlist.add("export_PayHasBinput");
	
		exportlist.add("export_BInputToContract");
		exportlist.add("export_BInputToClient");
		exportlist.add("export_BInputFailConnect");
		
		String dir = request.getServletContext().getRealPath("/报表中心/");
		String filename = caid + "_对账结果.xlsx";

		cOp.formProduce.CreateForm(exportlist, agent_id, userType, dir, filename);
		/*产生报表*/
		
		String checkresult_url = "/check_Accout/报表中心" + "/" + filename;
		if (jmesg.getInt("flag") == 0) {
			logger.info("对账操作成功");
			cOp.auccount.DealAfterCaSucces(owner.work_id,caid,checkresult_url);
			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.START_CHECKWORK, OpLog_Service.result_success);
		}
		else{
			logger.info("对账操作失败");
			cOp.auccount.DealAfterCaSucces(owner.work_id,caid,checkresult_url);
			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.START_CHECKWORK, OpLog_Service.result_failed);
		}
		
		if (!caid.equals(cOp.auccount.CreateCaid(agent_id))) {
			logger.info("本次为历史对账，需恢复对账环境");
		}
		
		Common_return_en(response,jmesg);
	}
	
	/**
	 * Watch_CheckA_Result 查看对账结果
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Watch_CheckA_Result")
	public void Watch_CheckA_Result(HttpServletRequest request,HttpServletResponse response) {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		String workId = (String)session.getAttribute("workId");
		String agentid = (String)session.getAttribute("agentid");
		
		String watch_type = null;
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		
		watch_type = jstr.getString("watch_type");
		Watch_CAResObject wObject = cOp.new Watch_CAResObject(watch_type,agentid);
		Object res_list = cOp.OpSelect(CheckAcManage.WATCH_CARes, wObject,null);
		
		Watch_CARes_return(response,res_list,watch_type);
	}

	/**
	 * Export_CheckA_Result 导出对账结果
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="Export_CheckA_Result")
	public void Export_CheckA_Result(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get_Export_CheckA_Result***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String owner = (String)session.getAttribute("agentid");
		String caid = null;
		String request_s = null;
		JSONObject jstr = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			logger.info("receive en:" + request_s);
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive de:" + request_s_de);
			if (request_s_de == null) {
				logger_error.error("无法获取caid参数");
				logger_error.error("前台传递参数--解密前：" + request_s);
				logger_error.error("前台传递参数--解密后:" + request_s_de);
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "无法获取caid参数");
				Common_return_en(response,jsonObject);	
				return;
			}
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String caid = cOp.auccount.CreateCaid(owner);
		String caurl = cOp.dao_List.cDao.FindBySpeElement("caid", caid, owner).get(0).getUrl();
		
		oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.EXPORT_CARes, OpLog_Service.result_success);
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "导出成功");
		OneKeyData_return(response, jsonObject, "cares_url", caurl);
	}
	
	/**
	 * CancelAndStartAgain_CheckA 取消并重新对账
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="ClAndStAgain_CheckA")
	public void CancelAndStartAgain_CheckA(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}

		Owner owner = cOp.new Owner();
		owner.work_id = (String)session.getAttribute("agentid");
		String who = (String) session.getAttribute("workId");
		
		String request_s;
		JSONObject jstr;
		String caid = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!caid.equals(cOp.auccount.CreateCaid(owner.work_id))) {
			logger.info("历史对账的重新对账");
	/*		String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
			String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
			String filenameA = caid + CheckAcManage.FileName_Orider;
			String filenameB = caid + CheckAcManage.FileName_BankInput;
			
			cOp.auccount.HisCancelAndCaAgain(owner.work_id, caid, savedir_A, savedir_B, filenameA, filenameB);*/
		}
		cOp.OpSelect(CheckAcManage.CANCEL_CaAgain, caid, owner);
		
		oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.CANCEL_CaAgain, OpLog_Service.result_success);
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "取消并重新对账成功");
		Common_return_en(response, jsonObject);
		
	}
	
	/**
	 * HisClAndSAgain_CheckA 从历史对账中重新对账
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="historyca")
	public void HisClAndSAgain_CheckA(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get historyca  request***");
		
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		String who = (String)session.getAttribute("workId");//获取用户名
		String agentid = (String)session.getAttribute("agentid");//设置操作者的所属代理商id
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//解密数据
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		
		String year = Integer.toString(jstr.getInt("year"));
		String month = null;
		if (jstr.getInt("month") < 10) {
			month = "0" + Integer.toString(jstr.getInt("month"));
		}
		else {
			month = Integer.toString(jstr.getInt("month"));
		}
		String caid = year + "-" + month + "-" + agentid;//对账id
		
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "操作成功");
		
		String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
		String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
		String filenameA = caid + CheckAcManage.FileName_Orider;
		String filenameB = caid + CheckAcManage.FileName_BankInput;
		
		int flag = cOp.auccount.HisCancelAndCaAgain(agentid, caid, savedir_A, savedir_B, filenameA, filenameB);
		if (flag == -1) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "操作失败，请确认上次对账是否成功");
			Common_return_en(response,jsonObject);
		}
		else if (flag == -2) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "操作非法，不允许从历史对账中跳转到历史对账");
			Common_return_en(response,jsonObject);
		}
		else {
			OneKeyData_return(response, jsonObject, "caid", caid);
		}
	}

	/**
	 * FreeBack 对上传正确的付款信息的客户返回积分
	 * @param request
	 * @param response
	 * @author zhangxinming
	 */
	@RequestMapping(value="freeback")
	public void FreeBack(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get freeback request***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String agent_id = (String)session.getAttribute("agentid");
		
		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		
		cOp.OpSelect(CheckAcManage.FREEBACK, null, owner);
		
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "返利成功");
		Common_return_en(response, jsonObject);
	}
	
	public void Watch_return(List list,HttpServletResponse response,Watch_Object wobject){
		
		JSONObject ordes_object =  new JSONObject();//传递参数中的最外层对象
		JSONArray orders = new JSONArray();//json类中的数组
		
		if (wobject.table_name.equals(CheckAcManage.RES_CAHISTORY) == true) {
			for(int i = 0;i<list.size();i++){
				CaresultHistory cahistory = (CaresultHistory) list.get(i);
				if (cahistory != null) {
					//System.out.println(object.getPayer());
					String months = cahistory.getCamonth();
					int month = Integer.parseInt(months);
					if (month < 10) {
						months = String.valueOf(month);
					}
					JSONObject caresult = new JSONObject();
					caresult.element("month", months);
					caresult.element("url", cahistory.getUrl());
					orders.add(i,caresult);
				}
				else{
					logger.error("not find the record");
				}
			}
			ordes_object.element("data", orders);//将数组添加到对象中
		}
		else {
			ordes_object.element("data", list);//将数组添加到对象中
		}
		
		ordes_object.element("flag", 0);
		
		/*传递json数据给前台*/
		System.out.println(ordes_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + ordes_object.toString());
			String en_s = AES.aesEncrypt(ordes_object.toString(), AES.key);//加密返回的数据
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
	}

	public void Watch_CARes_return(HttpServletResponse response,Object res_list,String watch_type){

		response.setCharacterEncoding("utf-8");
		JSONArray data = new JSONArray();//json类中的数组
		wRes_ReObject wRes_ReObject;
		if (watch_type.equals("bfailconnect")) {//结果1：无法关联的合同号或者客户名下的出纳记录
			List<Object> failcbinput = (List<Object>) res_list;
			for (int i = 0; i < failcbinput.size(); i++) {
				wRes_ReObject = new wRes_ReObject(failcbinput.get(i), null); 
				JSONObject jObject = JSONObject.fromObject(wRes_ReObject);
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("btocontract")) {//结果2：关联到合同号的出纳记录
			
			List<BankInput> tocontractbinput = (List<BankInput>) res_list;
			for (int i = 0; i < tocontractbinput.size(); i++) {
				BankInput bankInput = (BankInput) tocontractbinput.get(i);
				String many_contract = bankInput.getManyContract();
				JSONArray jmany_c = JSONArray.fromObject(many_contract);
				JSONArray jforder = new JSONArray();
				for (int j = 0; j < jmany_c.size(); j++) {
					JSONObject jObject = (JSONObject) jmany_c.get(j);
					String contract = (String)jObject.get("contract");
					OriOrder fOrder = cOp.dao_List.tDao.findById(OriOrder.class, contract);
					jforder.add(fOrder);
				}
				wRes_ReObject = new wRes_ReObject(bankInput, jforder); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("btoclient")) {//结果3：关联到客户名下的出纳记录
			List<BankInput> toclientbinput = (List<BankInput>) res_list;
			for (int i = 0; i < toclientbinput.size(); i++) {
				BankInput bankInput = (BankInput) toclientbinput.get(i);

				JSONArray jclient = new JSONArray();
				jclient.add(bankInput.getConnectClient());
				wRes_ReObject = new wRes_ReObject(bankInput, jclient); 
				
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("bnopay")) {//结果4：没有关联到付款信息的出纳记录(客户没有上传付款信息)
			
		}
		else if (watch_type.equals("bhaspay")) {//结果5：关联到付款信息的出纳记录

		}
		else if (watch_type.equals("phasbinput")) {//结果6：关联到出纳信息的手机付款记录
			List<PayRecord> connectbp = (List<PayRecord>) res_list;
			for (int i = 0; i < connectbp.size(); i++) {
				PayRecord fPayRecord = (PayRecord) connectbp.get(i);
				
				JSONArray jbanka = new JSONArray();
				BankInput fInput = cOp.dao_List.bDao.findById(BankInput.class, fPayRecord.getBankinputId());
				jbanka.add(fInput);
				
				wRes_ReObject = new wRes_ReObject(fPayRecord, jbanka); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("truepnobinput")) {//结果7：没有关联到出纳信息的真实手机付款记录(催款)
			List<PayRecord> truenobankp = (List<PayRecord>) res_list;
			for (int i = 0; i < truenobankp.size(); i++) {
				PayRecord fPayRecord = (PayRecord) truenobankp.get(i);
				
				wRes_ReObject = new wRes_ReObject(fPayRecord, null); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("falsepnobinput")) {//结果8：没有关联到出纳信息的真实手机付款记录(垃圾信息)
			List<PayRecord> falsenobankp = (List<PayRecord>) res_list;
			for (int i = 0; i < falsenobankp.size(); i++) {
				PayRecord bankInput = (PayRecord) falsenobankp.get(i);
				
				wRes_ReObject = new wRes_ReObject(bankInput, null); 
				data.add(wRes_ReObject);
			}
		}
		else if(watch_type.equals("onobinput")){//结果9：本月没有收到付款的货款记录
			
			List<OriOrder> noborider = (List<OriOrder>) res_list;
			System.out.println("onobinput size is" + noborider.size());
			for (int i = 0; i < noborider.size(); i++) {
				OriOrder bankInput = (OriOrder) noborider.get(i);
				wRes_ReObject = new wRes_ReObject(bankInput, null); 
				JSONObject jObject = JSONObject.fromObject(wRes_ReObject);
				data.add(wRes_ReObject);
				System.out.println("***"+jObject.toString());
			}
		}
		else if(watch_type.equals("ohasbinput")){//结果10：本月有收到付款的货款记录
			List<OriOrder> hasborider = (List<OriOrder>) res_list;

			for (int i = 0; i < hasborider.size(); i++) {
				OriOrder fOrder = (OriOrder) hasborider.get(i);
				
				String connect_contractid =  fOrder.getConnectBank();
				JSONArray jmany_b = JSONArray.fromObject(connect_contractid);
				JSONArray jforder = new JSONArray();
				for (int j = 0; j < jmany_b.size(); j++) {
					int bank_id = (int) jmany_b.get(j);
					
					BankInput fBankInput = cOp.dao_List.bDao.findById(BankInput.class, bank_id);
					jforder.add(fBankInput);
				}
				
				wRes_ReObject = new wRes_ReObject(fOrder, jforder); 
				data.add(wRes_ReObject);
			}
		}
		//添加更多的对账结果
		else{
			logger.error("unknow request of watch result_type");
		}
		
		JSONObject re_object =  new JSONObject();//传递参数中的最外层对象
		re_object.element("data", data);//将数组添加到对象中
		re_object.element("flag", 0);
		
		/*传递json数据给前台*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send" + re_object.toString());
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);//加密数据
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
	}
	
	/**
	 * Goto_Main_return 返回主菜单
	 * @param response
	 * @param newpay_num
	 * @deprecated
	 * @author zhangxinming
	 */
	public 	void Goto_Main_return(HttpServletResponse response,int newpay_num){
		JSONObject re_object =  new JSONObject();//传递参数中的最外层对象
		re_object.element("flag", 0);
		re_object.element("newpay_num", newpay_num);
		if (newpay_num > 0) {
			re_object.element("isnewpay", 1);
		}
		else{
			re_object.element("isnewpay", 0);
		}
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
	
	/**
	 * OneKeyData_return 带一个data信息的返回
	 * @param response
	 * @param jsonObject
	 * @param key 字段名
	 * @param object 具体信息
	 */
	public void OneKeyData_return(HttpServletResponse response,JSONObject jsonObject,String key,Object object){
		response.setCharacterEncoding("utf-8");

		JSONObject ordes_object =  jsonObject;//传递参数中的最外层对象
		ordes_object.element(key, object);
		
		/*传递json数据给前台*/
		System.out.println(ordes_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + ordes_object.toString());
			String en_s = AES.aesEncrypt(ordes_object.toString(), AES.key);//加密返回的数据
			writer.write(en_s);
			logger.info(ordes_object.toString());
		} catch (Exception e) {
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
		
		JSONObject re_object =  jsonObject;//传递参数中的最外层对象
		
		/*传递json数据给前台*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + re_object.toString());
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);//加密返回的数据
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
	}
	
    /**
     * Common_return_en 不带具体信息的加密返回
     * @param response
     * @param re_json 操作结果及信息
     * @author zhangxinming
     */
    public void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
		
    	String en_s = null;
		/*传递json数据给前台*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			logger.info("加密后发送为" + en_s);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
    
	public class wRes_ReObject implements Serializable{
		private Object basicObject;
		private Object connectObject;
		
		public wRes_ReObject(Object basicObject,Object connectObject){
			this.basicObject = basicObject;
			this.connectObject = connectObject;
		}
		
		public Object getBasicObject() {
			return this.basicObject;
		}

		public void setBasicObject(Object connectObject) {
			this.connectObject = connectObject;
		}
		
		public Object getConnectObject() {
			return this.connectObject;
		}

		public void setConnectObject(Object connectObject) {
			this.connectObject = connectObject;
		}
	}
	
}
