package controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysql.fabric.Response;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import com.sun.org.apache.xml.internal.security.utils.SignerOutputStream;
import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import check_Asys.CheckAcManage;
import check_Asys.OpLog_Service;
import check_Asys.Person_Manage;
import check_Asys.Person_Manage.Login_Mange;
import check_Asys.Person_Manage.Register_Manage;
import check_Asys.Person_Manage.DB_Operator;
import dao.Assistance_Dao;
import dao.ConnectPerson_Dao;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.Assistance;
import entity.Backup;
import entity.ConnectPerson;
import entity.OpLog;
import entity.OriOrder;
import file_op.AnyFile_Op;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import entity.Assistance;

/**
 * PMController ��Ա����ӿ��� ���ṩ�Ľӿڰ����û�ע�ᣬ�����û�Ȩ��,������־�鿴�����ݿⱸ��
 * @author zhangxinming
 * @version 1.0.0
 */

@Controller
public class PMController {
	
	private static Logger logger = LogManager.getLogger(PMController.class);
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public  final static Person_Manage pManage = new Person_Manage(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	private static AES ase = new AES();

	/**
	 * Signout ע����¼
	 * @category �����û��ӿ�
	 * @param request
	 * @param response
	 * @author zhangxinming
	 */
	@RequestMapping(value="/signout")
	public void Signout(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Signout request***");
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String usertype = (String) session.getAttribute("usertype");
		String username  = (String) session.getAttribute("workId");
		if (usertype.equals("bu")) {//������ϵ��
			oLog_Service.AddLog(OpLog_Service.utype_as, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "ע���ɹ�");
			
		}
		else if (usertype.equals("bm")) {//������Ա
			oLog_Service.AddLog(OpLog_Service.utype_ma, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "ע���ɹ�");
			
		}
		else{
			logger.error("δ֪�û�����");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "ע��ʧ�ܣ�δ֪�û�����");
		}
		Common_return_en(response,re_jsonobject);
	//	Signout_return(response);

	}
	
	/**
	 * Asssistance_login �����̲����¼
	 * @category �����̲���ӿ�
	 * @param as
	 * @param model
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @author zhangxinming
	 */
    @RequestMapping(value="/login")
    public void Asssistance_login(Assistance as , Model model,HttpServletRequest request,HttpServletResponse response) throws ServletException{
    	logger.info("***Get Asssistance_login request***");   	
    	String work_id = null;
    	String password = null;
    	String lgtype = null;
    	int newpay_num = 0;
        try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			work_id = jstr.getString("uid");//��ȡ��¼id
			password = jstr.getString("upwd");//��ȡ��¼����
			lgtype = jstr.getString("from"); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
        
        Login_Mange login_Mange = pManage.new Login_Mange();
        
        int isillegal = login_Mange.LgEnter_Select(lgtype, work_id, password);
        if (isillegal == 0) {
        	String agentid = (String) pManage.aS_Dao.findById(Assistance.class, work_id).getAgentid();
        	HttpSession session = request.getSession();//����session
    		System.out.println("login success");
			session.setAttribute("usertype", lgtype);
    		session.setAttribute("workId", work_id);
    		session.setAttribute("agentid", agentid);
    		session.setMaxInactiveInterval(0);
    		
    		newpay_num = pManage.pCDao.GetPayRecordsTb(agentid).size();
    		
    		Asssistance_login_return(0,work_id,password,lgtype,newpay_num,response);
    		if (lgtype.equals("bu")) {
    			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		else if (lgtype.equals("bm")) {
    			oLog_Service.AddLog(OpLog_Service.utype_ma, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		else{
    			oLog_Service.AddLog(OpLog_Service.utype_un, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		
		}
        else{
        	Asssistance_login_return(isillegal,work_id,password,lgtype,newpay_num,response);
    		if (lgtype.equals("bu")) {
    			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
    		else if (lgtype.equals("bm")) {
    			oLog_Service.AddLog(OpLog_Service.utype_ma, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
    		else{
    			oLog_Service.AddLog(OpLog_Service.utype_un, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
        }  	
    }
    
    /**
     * Assistance_register ������Աע��
     * @category �����̲�����Աע��ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="/as_register")
    public void Assistance_register(HttpServletRequest request,HttpServletResponse response){
    	logger.info("Get as_register request");
    	JSONObject re_jsonobject = new JSONObject();
    	
		String request_s = null;
		String request_s_en = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
			/*	InputStream inputStream = request.getInputStream();
				byte[] readbuf = new byte[200];
				inputStream.read(readbuf);
				request_s = new String(readbuf, "utf-8");
				request_s = readbuf.toString();*/
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive:" + request_s_en);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_en);
    	String work_id = jstr.getString("username");//�û�
    	String name = jstr.getString("name");//��ʵ����
    	String phone = jstr.getString("phone");//�绰����
    	String email = jstr.getString("email");
    	String password = jstr.getString("password");
    	String usertype = jstr.getString("role");
    	String agentid = jstr.getString("agentid");
    	
    	Assistance assistance = new Assistance();
    	assistance.setWorkId(work_id);
    	assistance.setName(name);
    	assistance.setPhone(phone);
    	assistance.setEmail(email);
    	assistance.setPassword(password);
    	assistance.setUsertype(usertype);
    	assistance.setAgentid(agentid);
    	assistance.setUsertype("bu");//��ͨ������Ա
  
    	Register_Manage regmanager = pManage.new Register_Manage();
    	re_jsonobject = regmanager.RgEnter_Select(assistance, "as");
    	
    	//Assistance_Register_return(response,flag);
    	Common_return_en(response, re_jsonobject);
    }
      
    /**
     * Conectp_register ������ϵ��ע��  �ͻ�ע��ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="/conectp_register")
    public void Conectp_register(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get Conectp_register request***");
    	try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String username = null;
    	String phone = null;
    	String company = null;
    	String agent = null;
    	String real_name = null;
    	char register_way = 0;
    	String companyid = null;
    	String weixin = null;
    	String password = null;
    	String email = null;
    	String contractMes = null;
    	String cardid = null;
    	
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
	    	phone =  AES.aesDecrypt(request.getParameter("phone"),AES.key);//�绰
	    	company =  new String(AES.aesDecrypt(request.getParameter("company"),AES.key).getBytes("GBK"),"GBK");//��˾����
	    	agent = AES.aesDecrypt(request.getParameter("agent"),AES.key);//����������id
	    	real_name = new String(AES.aesDecrypt(request.getParameter("real_name"),AES.key).getBytes("GBK"),"GBK");//��ʵ����
	    	register_way = AES.aesDecrypt(request.getParameter("register_way"),AES.key).charAt(0);//ע�᷽ʽ
	    	weixin = AES.aesDecrypt(request.getParameter("weixin"),AES.key);//΢�ź�
	    	companyid = AES.aesDecrypt(request.getParameter("companyid"),AES.key);//��˾id
	    	password = AES.aesDecrypt(request.getParameter("password"),AES.key);//����
	    	email = AES.aesDecrypt(request.getParameter("email"),AES.key);//����
	    	contractMes = AES.aesDecrypt(request.getParameter("contract_mes"),AES.key);//��Чƾ֤
	    	cardid = AES.aesDecrypt(request.getParameter("cardid"),AES.key);//������ϵ�����֤
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//�û���

    	
    	ConnectPerson inConnectPerson = new ConnectPerson();
    	inConnectPerson.setUsername(username);
    	inConnectPerson.setPassword(password);
    	inConnectPerson.setCompany(company);
    	inConnectPerson.setRealName(real_name);
    	inConnectPerson.setWeixin(weixin);
    	inConnectPerson.setAgent(agent);
    	inConnectPerson.setPhone(phone);
    	inConnectPerson.setCompanyid(companyid);
    	inConnectPerson.setRegisterWay(register_way);
    	inConnectPerson.setFlag(Person_Manage.REG_NEW);//����ע��״̬Ϊ�ȴ�����
    	inConnectPerson.setEmail(email);
    	inConnectPerson.setContractMes(contractMes);
    	inConnectPerson.setCardid(cardid);
    	inConnectPerson.setScore(0);
    	
    	Register_Manage register_Manage = pManage.new Register_Manage();
    	JSONObject flag = register_Manage.RgEnter_Select(inConnectPerson, "cp");
    	
		response.setCharacterEncoding("utf-8");
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	Common_return(response, flag);
    }
    
    /**
     * Verify_Register ϵͳ����Ա��ע���������ȷ��  ϵͳ����Ա�ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="/verify_register")
    public void Verify_Register(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get verify_register request***");
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_en = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("request content:" + request_s_en);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_en);
		
		String reg_type = jstr.getString("reg_type");
    	String username = null;
    	if (reg_type.equals("as")) {
    	//	username = request.getParameter("workId");
    		username = jstr.getString("id");
		}
    	else{//������ϵ��
    		    		//username = request.getParameter("username");
    		username = jstr.getString("id");
    	}

    	int flag = jstr.getInt("regflag");
    	
    	Register_Manage register_Manage = pManage.new Register_Manage();
    	JSONObject jsonObject = register_Manage.Verify_RgRequest(reg_type,username, flag);
    	
    	//Verify_Register_return(response,jsonObject);
    	re_jsonobject = jsonObject;
    	Common_return_en(response, re_jsonobject);
    }

    /**
     * Control_Power ϵͳ����Ա���û�Ȩ�޽��п��� 
     * @author ϵͳ����Ա�ӿ� 
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="control_power")
    public void Control_Power(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get control_power request***");
		
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_en = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("request content:" + request_s_en);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_en);
		String control_type = jstr.getString("control_type");//�������ͣ�as:�����̲���  cp:������ϵ��
		int ctlflag = jstr.getInt("ctlflag"); //���ƵĽ��  0������  -3������
		String id = jstr.getString("id");//���ƶ����id
		
		pManage.Control_Power(control_type,ctlflag,id);//����Ȩ�޿��ƴ���
		
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "����Ȩ�޳ɹ�");
		Common_return_en(response,re_jsonobject);
    }
   
    /**
     * Connectp_login ������ϵ�˵�¼ 
     * @author  �ͻ��ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="/ConnectPLogin")
    public void Connectp_login(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get ConnectPLogin request***");
    	
    	JSONObject re_jsonobject = new JSONObject();

    	String username = null;
    	String password = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			password = AES.aesDecrypt(request.getParameter("password"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Login_Mange login_Mange = pManage.new Login_Mange();
		int isllegal = login_Mange.LgEnter_Select("bc", username, password);
		
		if (isllegal == 0) {
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "��¼�ɹ�");
			OneKeyData_return_enData(response, re_jsonobject, "connectp", pManage.cDao.findById(ConnectPerson.class, username));
		}
		else if (isllegal == -1) {//�û���������
			logger.error("��¼ʧ��");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "�û���������");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -3) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "ע��������");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -4) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "�û��Ѿ�������������ϵ����Ա");
			Common_return(response, re_jsonobject);
		}
		else {//�������
			logger.error("�������");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "�������");
			Common_return(response, re_jsonobject);
		}
    }
  
    /**
     * get_agentcodeAname ��ȡ�����̵�id������ 
     * @category  ������ϵ��ע��ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="get_agentcodeAname")
    public void Get_AgentCAN(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get get_agentcodeAname request***");
    	
    	JSONObject re_jsonobject = new JSONObject();
    	
    	JSONArray re_jarry = pManage.Get_AgentCAN();//�����ȡ��Ϣ����
    	
    	/*�������ݵ�ǰ̨*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "��ȡ�����̼�id�ɹ�");
    	OneKeyData_return_enall(response, re_jsonobject, "data", re_jarry);
    	/*�������ݵ�ǰ̨*/
    }
    
    /**
     * Watch ��̨����ϵͳ�Ĳ鿴����
     * @category ����Ա�ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="watch")
    public void Watch(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get watch request***");

		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//��������
				logger.info("received content:" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		String watch_type = jstr.getString("watch_type");//����鿴������
		
    	List re_list = pManage.Watch(watch_type);//����鿴����
    	
    	/*�������ݵ�ǰ̨*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "�鿴�ɹ�");
    	OneKeyData_return_enall(response, re_jsonobject, "data", re_list);
    	/*�������ݵ�ǰ̨*/
    	
    }
    
    /**
     * BackUpDatabase �������ݿ�
     * @param request
     * @param response
     */
    @RequestMapping(value="backupdb")
    public void BackUpDatabase(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get BackUpDatabase request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
    	
    	DB_Operator dOperator = pManage.new DB_Operator();
    	String savedir = request.getServletContext().getRealPath("/" + DB_Operator.dirname);
    	
    	String filename = dOperator.produce_time() + ".sql";
    	dOperator.BackUp_db(savedir, filename);
    	
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "���ݳɹ�");
    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Verify_Restore �ύ�ָ�sqlȷ��
     * @param request
     * @param response
     */
    @RequestMapping(value="verify_backup")
    public void Verify_Restore(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get Verify_Restore request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		int backupid = -1;
		 try {
				String request_s = IOUtils.toString(request.getInputStream());
				String request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				backupid = jstr.getInt("id");//��ȡ��¼id
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
    	DB_Operator dOperator = pManage.new DB_Operator();
    	int flag = dOperator.Restore(backupid);
    	
    	if (flag == -1) {
	    	re_jsonobject.element("flag", -1);
	    	re_jsonobject.element("errmsg", "�ָ�ʧ��");
		}
    	else {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "�ָ��ɹ�");
		}

    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Choose_Restore ѡ��ָ���sql
     * @param request
     * @param response
     */
    @RequestMapping(value="choose_backup")
    public void Choose_Restore(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get Choose_Restore request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		List<Backup> fBackups = pManage.bUp_Dao.GetTolTb();
		if (fBackups != null) {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "�����ɹ�");
	    	
	    	OneKeyData_return_enall(response,re_jsonobject,"data",fBackups);
		}
		else{
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
    }
    
    /**
     * Asssistance_login_return ������Ա��¼����
     * @param flag
     * @param username �û���
     * @param password ����
     * @param role �û�����
     * @param newpay_num �µĸ�����Ϣ��Ŀ
     * @param response
     * @author zhangxinming
     */
    public void Asssistance_login_return(int flag,String username,String password,String role,int newpay_num,HttpServletResponse response){
		response.setCharacterEncoding("utf-8");
		JSONObject userJ =  new JSONObject();//���ݲ����е���������
		JSONObject resJ=new JSONObject();
		if (flag == -1) {//�û�������
				resJ.put("flag", -1);
				resJ.put("errmsg", "�û���������");
		}
		else if (flag == -2) {
			resJ.put("flag", -1);
			resJ.put("errmsg", "�������");
		}
		else if(flag == -3){
			resJ.put("flag", -3);
			resJ.put("errmsg", "ע�������");
		}
		else if (flag == -4) {
			resJ.put("flag", -4);
			resJ.put("errmsg", "���˺��ѱ�����������ϵϵͳ����Ա");
		}
		else {
				Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
				userJ.element("role", role);//��������ӵ�������
				userJ.element("uid", username);
				userJ.element("upwd", password);
				userJ.element("name", fAssistance.getName());
				userJ.element("phone", fAssistance.getPhone());
				userJ.element("email", fAssistance.getEmail());
				if (role.equals("bu")) {
					String agentid = fAssistance.getAgentid();
					if (agentid.equals("gd0001")) {
						userJ.element("agentname", "�㶫������(gd0001)");
					}
					else if (agentid.equals("ah0001")) {
						userJ.element("agentname", "���մ�����(ah0001)");
					}
					else{
						userJ.element("agentname", "δ֪������");
					}
				}
				
				if (newpay_num > 0) {
					resJ.put("isnewpay", 1);
				}
				else{
					resJ.put("isnewpay", 0);
				}
				
				resJ.put("flag", 0);
				resJ.put("user", userJ);
				resJ.put("token","ok");
				resJ.put("newpay_num", newpay_num);
		}
    	
    	Writer writer;
		try {
			logger.info(resJ.toString());
			writer = response.getWriter();
			String en_s = AES.aesEncrypt(resJ.toString(),AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * OneKeyData_return ��һ���������ݵ�ǰ̨����
     * @param response
     * @param re_json ���������������Ϣ
     * @param key ������Ϣ��key
     * @param data ������Ϣ
     * @category����Ա�ӿ�
     */
    public void OneKeyData_return_enall(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		re_object.element(key, data);
		
		/*����json���ݸ�ǰ̨*/
		try {
			logger.info("send content:" + re_object.toString());
			Writer writer = response.getWriter();
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
    
    /**
     * OneKeyData_return_enData ��һ�����������Ϣ�ķ��أ�ֻ���ܾ�����Ϣ
     * @param response
     * @param re_json ���������������Ϣ
     * @param key ������Ϣ��key
     * @param data ������Ϣ
     * @�ͻ����ؽӿ�
     */
    public void OneKeyData_return_enData(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
    	

		JSONObject aes_object = JSONObject.fromObject(data);
		String aes_object_s = aes_object.toString();
		logger.info("Before ��[aesEncrypt],context is:" + aes_object_s);
		String en_s = null;
		try {
			en_s = AES.aesEncrypt(aes_object_s,AES.key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		re_object.element(key, en_s);
		
		/*����json���ݸ�ǰ̨*/
		try {
			logger.info("send content��" + re_object.toString());
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
    
    /**
     * Common_return ����������Ϣ�ķ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    public void Common_return(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		
		/*����json���ݸ�ǰ̨*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }

    /**
     * Common_return_en ����������Ϣ�ļ��ܷ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    public void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		
    	String en_s = null;
		/*����json���ݸ�ǰ̨*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }

}


