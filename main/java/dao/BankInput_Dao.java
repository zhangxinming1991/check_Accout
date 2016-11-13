package dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import entity.BankInput;
import entity.OriOrder;
import entity.PayRecord;
import sun.util.logging.resources.logging;

/**
 * BankInput_Dao �������ݿ���ɱ�ķ���dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class BankInput_Dao {
	
	private static Logger logger = LogManager.getLogger(BankInput_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public BankInput_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public boolean add(BankInput in_bankIn){
		try {
			beginTransaction();
			session.save(in_bankIn);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("���BankInput��" + in_bankIn.getId() + "ʧ��" + e);
			return false;
		}
	}
	
	public boolean update_payrecord(int pay_id,int bank_id,String contract_num,String many_contract,boolean isConnect){
		BankInput uRecord = findById(BankInput.class,bank_id);
		if (uRecord != null) {
			
			/*�󶨻���ȡ������Ҫ��ɵ�ҵ����*/
			uRecord.setPayid(pay_id);
			uRecord.setIsConnect(isConnect);
			uRecord.setContractNum(contract_num);
			uRecord.setManyContract(many_contract);
			/*�󶨻���ȡ������Ҫ��ɵ�ҵ����*/
			try {
				beginTransaction();
				session.update(uRecord);
				endTransaction();	
				return true;
			} catch (RuntimeException e) {
				// TODO: handle exception
				logger.error("����BankInput:" + uRecord.getId() + "ʧ��" + e);
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean update(BankInput upBankInput){
		try {
			beginTransaction();
			session.update(upBankInput);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("���³���ʧ��");
			return false;
		}
	}
	
	public boolean delete(BankInput de_bankIn){
		try {
			beginTransaction();
			session.delete(de_bankIn);
			endTransaction();		
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("ɾ��BankInput��" + de_bankIn.getId() + "ʧ��" + e);
			return false;
		}
	}
	
	public boolean DeleteBinputTb(){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInput";
			session.createQuery(de_all_hql).executeUpdate();
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("delete BinputTb failed" + e);
			return false;
		}
	}
	
	public void DeleteBinputTbByElement(String filed,String filedvaule){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInput where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvaule)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("delete BinputTbByElement failed" + e);
		}
	}
	
	public BankInput findById(Class<BankInput> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			BankInput find_bankIn = (BankInput) session.get(cla, id);
			session.close();
			return find_bankIn;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("findById failed");
			logger.info("���ҳ��ɼ�¼����");
			return null;
		}
	}
	
	public void addlist(ArrayList<BankInput> in_bankIns){
		for (int i = 0; i < in_bankIns.size(); i++) {
			add(in_bankIns.get(i));
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetTolBankIns(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from BankInput";
		java.util.List bankins =  (java.util.List) session.createQuery(hql_select_all).list();
		session.close();
		return bankins;
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetTolBankInsByElement(String filed,String value){
		
		session = sessionFactory.openSession();
		String se_all_hql = "from BankInput where " + filed + " = :filedname";
		java.util.List bankins =  (java.util.List) session.createQuery(se_all_hql)
				.setParameter("filedname", value)
				.list();
		session.close();
		return bankins;
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<BankInput> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select binput from BankInput binput where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			for (int i = 0; i < bankInputs.size(); i++) {
				System.out.println(bankInputs.get(i).getPayer());
			}
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed + "=" + value + "����BankInputʧ��" + e);
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public List<BankInput> FindBySpeElement(String filed,Object value,String owner){
		String fdclient_hql = "select binput from BankInput binput where " +  filed + " = :value" + " and " + "owner = :owner_value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql)
					.setParameter("owner_value", owner)
					.setParameter("value", value)
					.list();
			session.close();
	
			for (int i = 0; i < bankInputs.size(); i++) {
				System.out.println(bankInputs.get(i).getPayer());
			}
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("���� " + filed + "=" + value + "|" + "owner" + owner + "����BankInputʧ��" + e);
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<BankInput> FindBySpeElement_Big(String filed,Object value,String owner){
		String fdclient_hql = "select binput from BankInput binput where " +  filed + " > :value" + " and " + "owner = :owner_value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql)
					.setParameter("owner_value", owner)
					.setParameter("value", value)
					.list();
			session.close();
	
			for (int i = 0; i < bankInputs.size(); i++) {
				System.out.println(bankInputs.get(i).getPayer());
			}
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed + "=" + value + "|" + "owner" + "=" + owner + "����BankInputʧ��" + e);
			return null;
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<BankInput> FindBySpeElement_OR(String filed1,String filed2,Object value1,Object value2){
		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select binput from BankInput binput where " +  filed1 + " = :value1" + " or " + filed2 + " = :value2";//����һ��
	//	String fdclient_hql = "select binput from BankInput binput where " +  filed1 + " = :value1" + " or " + filed2 + " = :value2";
		/*�޸�ƥ�����*/

		try {
			
			session = sessionFactory.openSession();
		//	java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql).setParameter("value", value).list();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("value2", value2);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
	
		/*	for (int i = 0; i < bankInputs.size(); i++) {
				System.out.println(bankInputs.get(i).getPayer());
			}*/
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "|" + filed2 + "=" + value2 + "����BankInputʧ��" + e);
			return null;
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<BankInput> GetMapBinputs(String filed1,String filed2,Object value1,Object value2,String owner){
		
		/*�޸�ƥ�����*/
	//	String fdclient_hql = "select binput from BankInput binput where " +  filed1 + " = :value1" + " or " + filed2 + " = :value2" + " and " + " isConnect = :isConnect";//����һ��
		String fdclient_hql = "select binput from BankInput binput where " + "(" + filed1 + " = :value1" + " or " + filed1 + " = :value2" + ")" + " and " + " isConnect = :isConnect" + " and " + "owner = :owner";//����һ��
		/*�޸�ƥ�����*/

		try {
			
			session = sessionFactory.openSession();
		//	java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql).setParameter("value", value).list();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("value2", value2);
			query.setParameter("isConnect", false);
			query.setParameter("owner", owner);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
	
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "|" + filed2 + "=" + value2 + "����BankInputʧ��" + e);
			return null;
		}
	}
	
	public List<BankInput> FindFailConnectBinputs(String owner_value){
		String failbinputconnect_hql = "select binput from BankInput binput where " +  "connectNum" + " = :connectNum" + " and " + "connectClient" + " = :connectClient" + " and " + "owner = :owner";//����һ��
//		String failbinputconnect_hql = "select binput from BankInput binput where " +  "connectClient" + " = :connectClient";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(failbinputconnect_hql);
			query.setParameter("connectNum", 0);
			query.setParameter("connectClient", "");
			query.setParameter("owner", owner_value);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
			
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + "connectNum = 0" + "|" + "owner=" + owner_value + "����BankInput" + "ʧ��" + e);
			return null;
		}
	}
	
	public List<BankInput> FindBtoContract(String owner_value){
		String bToContract = "select binput from BankInput binput where " +  "connectNum" + " > :connectNum" + " and " + "owner = :owner";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(bToContract);
			query.setParameter("connectNum", 0);
			query.setParameter("owner", owner_value);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
			
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����connectNum > 0" + "|" + "owner="+ owner_value + "����BankInputʧ��" + e);
			return null;
		}	
	}
	
	public List<BankInput> FindBtoClient(String owner_value){
		String bToClient = "select binput from BankInput binput where " +  "connectClient" + " != :connectClient" + " and " + "owner = :owner";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(bToClient);
			query.setParameter("connectClient", "");
			query.setParameter("owner", owner_value);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
			
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����connectClient" + "owner=" + owner_value + "����BankInputʧ��" + e);
			return null;
		}	
	}
	
	public List<BankInput> FindBToPay(boolean isConnect_value,String owner_value) {
		String bNoPay = "select binput from BankInput binput where " +  "isConnect" + " = :isConnect" + " and " + "owner = :owner";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(bNoPay);
			query.setParameter("isConnect", isConnect_value);
			query.setParameter("owner", owner_value);
			java.util.List<BankInput> bankInputs = query.list();
			session.close();
			
			return bankInputs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + "isConnect" + "=" + isConnect_value + "|" + "owner" + "=" + owner_value + "����BankInputʧ��" + e);
			return null;
		}	
	}
	
	public void Close_Connect(){
		
	/*	try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}
