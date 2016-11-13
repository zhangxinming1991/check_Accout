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
 * Person_Manage ��Ա������񣬹������ע�����ģ�Ȩ�޿��ƣ�������־�����ݿⱸ�ݵ�
 * @author zhangxinming
 * @version 1.0.0
 */
public class Person_Manage {
	
	private static Logger logger = LogManager.getLogger(Person_Manage.class);
	public SessionFactory mFactory;
	
	/*�������ݿ���dao*/
	public Assistance_Dao aS_Dao; //������Աdao
	public PayRecordCache_Dao pCDao;//������Ϣ������dao
	public ConnectPerson_Dao cDao;//������ϵ��dao
	public Agent_Dao agent_Dao;//������dao
	public OpLog_Dao opLog_Dao;//������־dao
	public BackUp_Dao bUp_Dao;//����sqldao
	/*�������ݿ���dao*/
	
	/*�鿴��Դ�Ķ���*/
	public static final String REG_CP = "reg_cp";//�ȴ���˵Ķ�����ϵ��ע������,cp(connect person)
	public static final String REG_AS = "reg_as";//�ȴ���˵Ĳ�����Աע������,as(assistance)
	public static final String REGED_CP = "reged_cp";//��ע��Ķ�����ϵ��
	public static final String REGED_AS = "reged_as";//��ע��Ĳ�����Ա
	public static final String OP_LOG = "op_log";//�����û��Ĳ�����־
	/*�鿴��Դ�Ķ���*/
	
	/*�û�Ȩ��״̬*/
	public static final int REG_SUCCESS = 0;//ע��ɹ�
	public static final int REG_NEW = -1;//�������
	public static final int REG_REJECT = -2;//�ܾ�ע��
	public static final int LOCKED = -3;//����
	/*�û�Ȩ��״̬*/
	
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
	 * Get_AgentCAN ��ȡ���д����̵�id�����֣�����ע��ʱ�ṩ���û�ѡ��
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
	 * Watch ��̨����ϵͳ�Ĳ鿴����,�鿴����ע��������ע���û���������־����Դ
	 * @param watch_type
	 * @return
	 * @author zhangxinming
	 */
	public List Watch(String watch_type){
		List re_list = null;
		List re_list_locked = null;
		if (watch_type.equals("reg_cp")) {//�鿴��ע��Ķ�����ϵ��
			logger.info("�鿴��ע��Ķ�����ϵ��");
			re_list = cDao.GetTotalTbByElement("flag",REG_NEW);
		}
		else if (watch_type.equals("reg_as")) {//�鿴��ע��Ĳ�����Ա
			logger.info("�鿴��ע��Ĳ�����Ա");
			re_list = aS_Dao.GetTotalTbByElement("flag", REG_NEW);
		}
		else if (watch_type.equals("reged_cp")) {//�鿴��ע��Ķ�����ϵ��
			logger.info("�鿴��ע��Ķ�����ϵ��");
			re_list = cDao.GetTotalTbByElement("flag",REG_SUCCESS);
			re_list_locked = cDao.GetTotalTbByElement("flag", LOCKED);
			re_list.addAll(re_list_locked);
		}
		else if (watch_type.equals("reged_as")) {//�鿴��ע��Ĳ�����Ա
			logger.info("�鿴��ע��Ĳ�����Ա");
			re_list = aS_Dao.GetTotalTbByElement("flag", REG_SUCCESS);
			re_list_locked = aS_Dao.GetTotalTbByElement("flag", LOCKED);
			re_list.addAll(re_list_locked);
		}
		else if (watch_type.equals("op_log")) {//�鿴������־
			logger.info("�鿴������־");
			re_list = opLog_Dao.GetOpLogTb();
		}
		else {
			logger.error("δ֪�Ĳ鿴����" + watch_type);
		}
		
		return re_list;
	}
	
	/**
	 * Control_Power Ȩ�޿���
	 * @param ctltype �����û����� as:������Ա  cp:������ϵ��
	 * @param ctlflag ���������Ϣ��0��������/ -3�������� 
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
		 * BackUp_db �������ݿ�
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
				logger.error("����ʧ��" + e);
			}  	
		}	
		
		/**
		 * �ύ�ָ�sqlȷ��
		 * @param backupid
		 * @return
		 */
		public int Restore(int backupid){
			logger.info("restore");
			Backup fBackup = bUp_Dao.findById(Backup.class, backupid);
			if (fBackup == null) {
				logger.error("�ָ����ݿ�ʧ��");
				return -1;
			}
		    Process process;
			try {
				process = Runtime.getRuntime().exec(restore_cmd + fBackup.getFilename());//windows restore
				int processComplete = process.waitFor();  
			    if (processComplete == 0) {  
			        logger.info("��ԭ�ɹ�.");  
			        return 0;
			    } else {  
			    	logger.error(processComplete);
			     //   throw new RuntimeException("��ԭ���ݿ�ʧ��."); 
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
	 * Login_Mange ��¼�����࣬���������û��ĵ�¼
	 * @author zhangxinming
	 *
	 */
	public class Login_Mange{

		public Login_Mange() {
		}
		
		public int LgEnter_Select(String lgway,String username,String password ){
			int isllegal = -1;
			switch (lgway) {
			case "bm"://�ܼ��¼
				isllegal = BU_Lg_isllegal(username, password,"bm");//��¼�Ϸ����ж�
				break;
			case "bu"://������Ա��¼
				isllegal = BU_Lg_isllegal(username, password,"bu");//��¼�Ϸ����ж�
				break;
			case "bc"://������ϵ���������¼		
				isllegal = BC_Lg_isillegal(username, password);//��¼�Ϸ����ж�
				break;
			case "mc"://������ϵ���ֻ���¼
				break;
			default:
				
				break;
			}	
			return isllegal;
		}
		
	
		public int BC_Lg_isillegal(String username,String password){
	        ConnectPerson f_cPerson = cDao.findById(ConnectPerson.class, username);
	        if (f_cPerson == null) {
				logger.info("�����ڸ��û�");
				return -1;
			}
	        else{
	        	if (f_cPerson.getFlag() == -1) {
					logger.info("ע�������");
					return -3;
				}
	        	if (f_cPerson.getFlag() == -3) {
					logger.info("���˻��ѱ�����������ϵϵͳ����Ա");
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
				logger.info("�����ڸ��û�");
				return -1;
			}
			else{
				if (f_as.getFlag() == -1) {
					logger.info("ע�������");
					return -3;
				}
				if (f_as.getFlag() == -3) {
					logger.info("���˻��ѱ�����������ϵϵͳ����Ա");
					return -4;
				}
				if (f_as.getPassword().equals(password) && f_as.getUsertype().equals(usertype)){
					return 0;
				}
				else{
					logger.info("�������");
					return -2;
				}				
			}

				
		}
		// TODO Auto-generated constructor stub
	}

	/**
	 * Register_Manage ע������࣬���������ϵ�˺Ͳ�����Ա��ע��
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
		
		//ע��ѡ��
		public JSONObject RgEnter_Select(Object reg_object,String reg_type){
			
			JSONObject re_jobject = null;
			switch (reg_type) {
			case "cp"://������ϵ��
				re_jobject = Accept_CpRegisterRequest((ConnectPerson) reg_object);
				break;
			case "as"://�����̲���
				re_jobject = Accept_AsRegisterRequest((Assistance) reg_object);
				break;
			default:
				logger.error("δ֪ע������:" + reg_type);
				re_jobject = new JSONObject();
				re_jobject.element("flag", -1);
				re_jobject.element("errmsg", "δ֪����");
				break;
			}
			
			return re_jobject;
		}
		
		//���ܲ����������ϵ��ע������
		public JSONObject Accept_CpRegisterRequest(ConnectPerson re_cp){
			
			re_cp.setFlag(reg_wait_check);
			boolean result = cDao.add(re_cp);
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			if (result == true) {
				jsonObject.element("flag", 0);
				jsonObject.element("errmsg", "�ȴ�����");
			}
			else
			{
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "�û����Ѵ���");
			}
			
			return jsonObject;
		}
		
		/**
		 * Accept_AsRegisterRequest ����ע������
		 * @param re_as ע����Ϣ
		 * @return
		 */
		public JSONObject Accept_AsRegisterRequest(Assistance re_as){
			JSONObject jsonObject = new JSONObject();
			
			Agent fAgent = agent_Dao.findById(Agent.class, re_as.getAgentid());
			if (fAgent != null) {
				if (fAgent.getIsregister() == true) {
					logger.error("�������Ѿ��󶨲�����Ա");
					jsonObject.element("flag", -1);
					jsonObject.element("errmsg", "�������Ѿ��󶨲�����Ա");
					return jsonObject;
				}
				
				re_as.setFlag(reg_wait_check);
				int add_result = aS_Dao.add(re_as);
				if (add_result == 0) {//ע��ɹ�,�ȴ����
					jsonObject.element("flag", 0);
					jsonObject.element("errmsg", "�ȴ����");
				}
				else{
					jsonObject.element("flag", -1);
					jsonObject.element("errmsg", "�û��Ѿ�����");
				}
				
				return jsonObject;
			}
			else{
				logger.error("û�в��ҵ��ô�����" + re_as.getAgentid() + "����Ϣ");
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "��������Ϣû��¼��");
				
				return jsonObject;
			}
		}
		
		/**
		 * Verify_RgRequest �������������
		 * @param reg_type ע���ߵ���ݣ�as:����Ա   cp:������ϵ�ˣ�
		 * @param id ע����Ϣ��id
		 * @param flag flag:-1����  0:ͨ��  -2:�ܾ�
		 * @return
		 */
		public JSONObject Verify_RgRequest(String reg_type,String id ,int flag){//flag:-1����  0:ͨ��  -2:�ܾ�
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
				re_jObject.element("errmsg", "�����ɹ�");
			}
			else if (reg_type.equals("as")) {
				Assistance re_as = aS_Dao.findById(Assistance.class, id);
				String agentid = re_as.getAgentid();
				Agent agent = agent_Dao.findById(Agent.class, agentid);
				if (flag == 0) {
					if (agent_Dao.findById(Agent.class, agentid).getIsregister() == true) {
						logger.error("�������Ѿ��󶨲�����Ա");
						
						re_jObject.element("flag", -1);
						re_jObject.element("errmsg", "�������Ѿ��󶨲�����Ա");
					}
					else{
						/*�󶨴�����*/
						agent.setIsregister(true);
						agent.setAgentConnectperson(re_as.getWorkId());
						agent.setAgentConnectpname(re_as.getName());
						agent.setAgentCpemail(re_as.getEmail());
						agent.setAgentCpphone(re_as.getPhone());
						agent_Dao.update(agent);
						/*�󶨴�����*/
						
						re_jObject.element("flag", 0);
						re_jObject.element("errmsg", "�����ɹ�");
						
						re_as.setFlag(flag);
						aS_Dao.update(re_as);
					}
				}
				else if (flag == -2) {
					/*ȡ���󶨴�����*/
				/*	logger.info("ȡ���󶨴�����");
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
					re_jObject.element("errmsg", "�����ɹ�");
					/*ȡ���󶨴�����*/
					
					re_as.setFlag(flag);
					aS_Dao.update(re_as);
				}
			}
			return re_jObject;
		}
	}
}
