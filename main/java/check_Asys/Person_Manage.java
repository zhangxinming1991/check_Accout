package check_Asys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.resolver.apps.resolver;
import org.hibernate.SessionFactory;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import controller.PMController;
import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.BackUp_Dao;
import dao.ConnectPerson_Dao;
import dao.OpLog_Dao;
import dao.PayRecordCache_Dao;
import entity.Agent;
import entity.Assistance;
import entity.Backup;
import entity.ConnectPerson;
import file_op.AnyFile_Op;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sun.util.logging.resources.logging;

/**
 * Person_Manage 人员管理服务，管理包括注册审阅，权限控制，操作日志，数据库备份等
 * @author zhangxinming
 * @version 1.0.0
 */
public class Person_Manage {
	
	private static Logger logger = LogManager.getLogger(Person_Manage.class);
	public SessionFactory mFactory;
	
	/*连接数据库表的dao*/
	public Assistance_Dao aS_Dao; //财务人员dao
	public PayRecordCache_Dao pCDao;//付款信息缓存区dao
	public ConnectPerson_Dao cDao;//对账联系人dao
	public Agent_Dao agent_Dao;//代理商dao
	public OpLog_Dao opLog_Dao;//操作日志dao
	public BackUp_Dao bUp_Dao;//备份sqldao
	/*连接数据库表的dao*/
	
	/*查看资源的定义*/
	public static final String REG_CP = "reg_cp";//等待审核的对账联系人注册请求,cp(connect person)
	public static final String REG_AS = "reg_as";//等待审核的财务人员注册请求,as(assistance)
	public static final String REGED_CP = "reged_cp";//已注册的对账联系人
	public static final String REGED_AS = "reged_as";//已注册的财务人员
	public static final String OP_LOG = "op_log";//所有用户的操作日志
	/*查看资源的定义*/
	
	/*用户权限状态*/
	public static final int REG_SUCCESS = 0;//注册成功
	public static final int REG_NEW = -1;//待定审核
	public static final int REG_REJECT = -2;//拒绝注册
	public static final int LOCKED = -3;//锁定
	/*用户权限状态*/
	
	public Person_Manage(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		mFactory = wFactory;
		aS_Dao = new Assistance_Dao(mFactory);
		pCDao = new PayRecordCache_Dao(mFactory);
		cDao = new ConnectPerson_Dao(mFactory);
		agent_Dao = new Agent_Dao(mFactory);
		opLog_Dao = new OpLog_Dao(mFactory);
		bUp_Dao = new BackUp_Dao(mFactory);
	}
	
	/**
	 * Get_AgentCAN 获取所有代理商的id及名字，用于注册时提供给用户选择
	 * @return
	 * @author zhangxinming
	 */
	public JSONArray Get_AgentCAN(){
		List agents = agent_Dao.GetTolTb();
		
		JSONArray jagents = new JSONArray();
		for (int i = 0; i < agents.size(); i++) {
			JSONObject jsonObject = new JSONObject();
			Agent agent = (Agent) agents.get(i);
			jsonObject.element("code", agent.getAgentId());
			jsonObject.element("name", agent.getAgentName());
			logger.info(agent.getAgentName());
			jagents.add(jsonObject);
		}
		
		return jagents;
	}
	
	/**
	 * Watch 后台管理系统的查看功能,查看包括注册请求，以注册用户，操作日志等资源
	 * @param watch_type
	 * @return
	 * @author zhangxinming
	 */
	public List Watch(String watch_type){
		List re_list = null;
		List re_list_locked = null;
		if (watch_type.equals("reg_cp")) {//查看新注册的对账联系人
			logger.info("查看新注册的对账联系人");
			re_list = cDao.GetTotalTbByElement("flag",REG_NEW);
		}
		else if (watch_type.equals("reg_as")) {//查看新注册的财务人员
			logger.info("查看新注册的财务人员");
			re_list = aS_Dao.GetTotalTbByElement("flag", REG_NEW);
		}
		else if (watch_type.equals("reged_cp")) {//查看已注册的对账联系人
			logger.info("查看已注册的对账联系人");
			re_list = cDao.GetTotalTbByElement("flag",REG_SUCCESS);
			re_list_locked = cDao.GetTotalTbByElement("flag", LOCKED);
			re_list.addAll(re_list_locked);
		}
		else if (watch_type.equals("reged_as")) {//查看已注册的财务人员
			logger.info("查看已注册的财务人员");
			re_list = aS_Dao.GetTotalTbByElement("flag", REG_SUCCESS);
			re_list_locked = aS_Dao.GetTotalTbByElement("flag", LOCKED);
			re_list.addAll(re_list_locked);
		}
		else if (watch_type.equals("op_log")) {//查看操作日志
			logger.info("查看操作日志");
			re_list = opLog_Dao.GetOpLogTb();
		}
		else {
			logger.error("未知的查看类型" + watch_type);
		}
		
		return re_list;
	}
	
	/**
	 * Control_Power 权限控制
	 * @param ctltype 控制用户类型 as:财务人员  cp:对账联系人
	 * @param ctlflag 具体控制信息：0（解锁）/ -3（锁定） 
	 * @param id
	 * @return
	 * @author zhangxinming
	 */
	public boolean Control_Power(String ctltype,int ctlflag,String id){
		if (ctltype.equals("as")) {
			Assistance assistance = aS_Dao.findById(Assistance.class, id);
			assistance.setFlag(ctlflag);
			aS_Dao.update(assistance);
		}
		else if (ctltype.equals("cp")) {
			ConnectPerson cPerson = cDao.findById(ConnectPerson.class, id);
			cPerson.setFlag(ctlflag);
			cDao.update(cPerson);
		}
		return true;
	}
	
	public class DB_Operator{
	
		public static final String restore_cmd = "/bin/sh /var/tomcat/tomcat-7/webapps/check_Accout/backup_database/restore.sh ";
		public static final String backup_cmd = "/bin/sh /var/tomcat/tomcat-7/webapps/check_Accout/backup_database/backup.sh ";
		public static final String restore_cmd_wd = "cmd /c start e:/restore.bat";
		public static final String dirname = "backup_database";
		public static final String db_name = "check_a_db";
		public static final String db_passwd = "1234";
		public static final String db_user = "root";
		/**
		 * BackUp_db 备份数据库
		 */
		public void BackUp_db(String savedir,String filename){
			String save = savedir + "/" + filename;
			AnyFile_Op aOp = new AnyFile_Op();
			aOp.CreateDir(savedir);
			File savefile = new File(save);
			
			String backupsql = produce_time() + ".sql";
            Backup in_backup = new Backup();
            in_backup.setFilename(backupsql);
            bUp_Dao.add(in_backup);
            
			try {
				 String[] execCMD = new String[] {"mysqldump", "-u" + db_user, "-p" + db_passwd, db_name,  
				            "-r" + save, "--skip-lock-tables"}; 
			//	Process process = Runtime.getRuntime().exec(execCMD);//windows backup
				 Process process = Runtime.getRuntime().exec(backup_cmd + backupsql);//linux backup
				
		        int processComplete = process.waitFor();
		        /*NOTE: processComplete=0 if correctly executed, will contain other values if not*/
		        if (processComplete == 0) {
		            logger.info("Backup Complete");
		            
		        } else {
		            logger.error("Backup Failure" + processComplete);
		            bUp_Dao.delete(in_backup);
		        }
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("备份失败" + e);
			}  	
		}	
		
		/**
		 * 提交恢复sql确定
		 * @param backupid
		 * @return
		 */
		public int Restore(int backupid){
			logger.info("restore");
			Backup fBackup = bUp_Dao.findById(Backup.class, backupid);
			if (fBackup == null) {
				logger.error("恢复数据库失败");
				return -1;
			}
		    Process process;
			try {
				process = Runtime.getRuntime().exec(restore_cmd + fBackup.getFilename());//windows restore
				int processComplete = process.waitFor();  
			    if (processComplete == 0) {  
			        logger.info("还原成功.");  
			        return 0;
			    } else {  
			    	logger.error(processComplete);
			     //   throw new RuntimeException("还原数据库失败."); 
			    	return -1;
			    } 
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}  
		}
		
		public String produce_time(){
			Date date = new Date();
			SimpleDateFormat sFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String dates = sFormat.format(date);
			
			return dates;
		}
	}
	

	/**
	 * Login_Mange 登录管理类，管理所有用户的登录
	 * @author zhangxinming
	 *
	 */
	public class Login_Mange{

		public Login_Mange() {
		}
		
		public int LgEnter_Select(String lgway,String username,String password ){
			int isllegal = -1;
			switch (lgway) {
			case "bm"://总监登录
				isllegal = BU_Lg_isllegal(username, password,"bm");//登录合法性判断
				break;
			case "bu"://对账人员登录
				isllegal = BU_Lg_isllegal(username, password,"bu");//登录合法性判断
				break;
			case "bc"://对账联系人浏览器登录		
				isllegal = BC_Lg_isillegal(username, password);//登录合法性判断
				break;
			case "mc"://对账联系人手机登录
				break;
			default:
				
				break;
			}	
			return isllegal;
		}
		
	
		public int BC_Lg_isillegal(String username,String password){
	        ConnectPerson f_cPerson = cDao.findById(ConnectPerson.class, username);
	        if (f_cPerson == null) {
				logger.info("不存在该用户");
				return -1;
			}
	        else{
	        	if (f_cPerson.getFlag() == -1) {
					logger.info("注册审核中");
					return -3;
				}
	        	if (f_cPerson.getFlag() == -3) {
					logger.info("该账户已被锁定，请联系系统管理员");
					return -4;
				}
	        }
	        if (f_cPerson.getPassword().equals(password)) {
	        	System.out.println("[BC_Lg_isillegal]:login success");
				return 0;
	        }
	        else{
	        	return -2;
	        }
		}
		
		public int BU_Lg_isllegal(String username,String password,String usertype) {
			System.out.println(username + ":" + password);
			Assistance f_as = aS_Dao.findById(Assistance.class, username);
		//	aS_Dao.Close_Connect();
			if (f_as == null || f_as.getFlag() == -2) {
				logger.info("不存在该用户");
				return -1;
			}
			else{
				if (f_as.getFlag() == -1) {
					logger.info("注册审核中");
					return -3;
				}
				if (f_as.getFlag() == -3) {
					logger.info("该账户已被锁定，请联系系统管理员");
					return -4;
				}
				if (f_as.getPassword().equals(password) && f_as.getUsertype().equals(usertype)){
					return 0;
				}
				else{
					logger.info("密码错误");
					return -2;
				}				
			}

				
		}
		// TODO Auto-generated constructor stub
	}

	/**
	 * Register_Manage 注册管理类，管理对账联系人和财务人员的注册
	 * @author zhangxinming
	 *
	 */
	public class Register_Manage{
		public static final int reg_username_hasexit = -2;
		public static final int reg_wait_check = -1;
		public static final int reg_agent_hasbind = -4;
		public static final int reg_unknow_error = -5;
		public static final int reg_agent_notfound = -3;
		public static final int reg_success = 0;
		public Register_Manage(){
			
		}
		
		//注册选择
		public JSONObject RgEnter_Select(Object reg_object,String reg_type){
			
			JSONObject re_jobject = null;
			switch (reg_type) {
			case "cp"://对账联系人
				re_jobject = Accept_CpRegisterRequest((ConnectPerson) reg_object);
				break;
			case "as"://代理商财务
				re_jobject = Accept_AsRegisterRequest((Assistance) reg_object);
				break;
			default:
				logger.error("未知注册类型:" + reg_type);
				re_jobject = new JSONObject();
				re_jobject.element("flag", -1);
				re_jobject.element("errmsg", "未知错误");
				break;
			}
			
			return re_jobject;
		}
		
		//接受并处理对账联系人注册请求
		public JSONObject Accept_CpRegisterRequest(ConnectPerson re_cp){
			
			re_cp.setFlag(reg_wait_check);
			boolean result = cDao.add(re_cp);
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			if (result == true) {
				jsonObject.element("flag", 0);
				jsonObject.element("errmsg", "等待处理");
			}
			else
			{
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "用户名已存在");
			}
			
			return jsonObject;
		}
		
		/**
		 * Accept_AsRegisterRequest 接受注册请求
		 * @param re_as 注册信息
		 * @return
		 */
		public JSONObject Accept_AsRegisterRequest(Assistance re_as){
			JSONObject jsonObject = new JSONObject();
			
			Agent fAgent = agent_Dao.findById(Agent.class, re_as.getAgentid());
			if (fAgent != null) {
				if (fAgent.getIsregister() == true) {
					logger.error("代理商已经绑定财务人员");
					jsonObject.element("flag", -1);
					jsonObject.element("errmsg", "代理商已经绑定财务人员");
					return jsonObject;
				}
				
				re_as.setFlag(reg_wait_check);
				int add_result = aS_Dao.add(re_as);
				if (add_result == 0) {//注册成功,等待审核
					jsonObject.element("flag", 0);
					jsonObject.element("errmsg", "等待审核");
				}
				else{
					jsonObject.element("flag", -1);
					jsonObject.element("errmsg", "用户已经存在");
				}
				
				return jsonObject;
			}
			else{
				logger.error("没有查找到该代理商" + re_as.getAgentid() + "的信息");
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "代理商信息没有录入");
				
				return jsonObject;
			}
		}
		
		/**
		 * Verify_RgRequest 对请求进行审阅
		 * @param reg_type 注册者的身份（as:财务员   cp:对账联系人）
		 * @param id 注册信息的id
		 * @param flag flag:-1待定  0:通过  -2:拒绝
		 * @return
		 */
		public JSONObject Verify_RgRequest(String reg_type,String id ,int flag){//flag:-1待定  0:通过  -2:拒绝
			int result = -1;
			JSONObject re_jObject = new JSONObject();
			re_jObject.element("flag", -1);
			
			if (reg_type.equals("cp")) {
				logger.info(id);
				ConnectPerson re_cp = cDao.findById(ConnectPerson.class, id);
				re_cp.setFlag(flag);
				cDao.update(re_cp);
				result = 0;
				re_jObject.element("flag", 0);
				re_jObject.element("errmsg", "操作成功");
			}
			else if (reg_type.equals("as")) {
				Assistance re_as = aS_Dao.findById(Assistance.class, id);
				String agentid = re_as.getAgentid();
				Agent agent = agent_Dao.findById(Agent.class, agentid);
				if (flag == 0) {
					if (agent_Dao.findById(Agent.class, agentid).getIsregister() == true) {
						logger.error("代理商已经绑定财务人员");
						
						re_jObject.element("flag", -1);
						re_jObject.element("errmsg", "代理商已经绑定财务人员");
					}
					else{
						/*绑定代理商*/
						agent.setIsregister(true);
						agent.setAgentConnectperson(re_as.getWorkId());
						agent.setAgentConnectpname(re_as.getName());
						agent.setAgentCpemail(re_as.getEmail());
						agent.setAgentCpphone(re_as.getPhone());
						agent_Dao.update(agent);
						/*绑定代理商*/
						
						re_jObject.element("flag", 0);
						re_jObject.element("errmsg", "操作成功");
						
						re_as.setFlag(flag);
						aS_Dao.update(re_as);
					}
				}
				else if (flag == -2) {
					/*取消绑定代理商*/
				/*	logger.info("取消绑定代理商");
					if ((agent_Dao.findById(Agent.class, agentid).getIsregister() == true)) {
						 if (agent.getAgentConnectperson().equals(re_as.getWorkId())) {						
							agent.setIsregister(false);
							agent.setAgentConnectperson(null);
							agent.setAgentConnectpname(null);
							agent.setAgentCpemail(null);
							agent.setAgentCpphone(null);
							agent_Dao.update(agent);
						}
					}*/
					re_jObject.element("flag", 0);
					re_jObject.element("errmsg", "操作成功");
					/*取消绑定代理商*/
					
					re_as.setFlag(flag);
					aS_Dao.update(re_as);
				}
			}
			return re_jObject;
		}
	}
}
