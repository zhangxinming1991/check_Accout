package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.BankInput;
import entity.PayRecordHistory;

/**
 * PayRecordHistory_Dao 连接数据库付款历史区表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class PayRecordHistory_Dao {
	
	private static Logger logger = LogManager.getLogger(PayRecordHistory_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public PayRecordHistory_Dao(SessionFactory wFactory) {
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
	
	public void add(PayRecordHistory in_pR){
		try {
			beginTransaction();
			session.save(in_pR);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	public void addlist(List<PayRecordHistory> in_payrecord){
		for (int i = 0; i < in_payrecord.size(); i++) {
			add(in_payrecord.get(i));
		}
	}
	
	public void delete(PayRecordHistory de_pR){
		try {
			beginTransaction();
			session.delete(de_pR);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete failed");
		}
	}
	
	public void DeletePrecordTb(){
		try {
			beginTransaction();
			String de_all_hql = "delete from PayRecordHistory";
			session.createQuery(de_all_hql).executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete all failed" + e);
		}
	}
	
	public void DeletePrecordTbByElement(String filed,String filedvalue,String owner){
		try {
			beginTransaction();
			String de_all_hql = "delete from PayRecordHistory where " + filed + " = :filedname" + " and " + "owner = :owner_value";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.setParameter("owner_value", owner)
			.executeUpdate();
			endTransaction();
			logger.info("根据" + filed + "=" + filedvalue + "删除记录成功");
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("根据" + filed + "=" + filedvalue + "删除记录失败" + e);
		}
	}
	
	public PayRecordHistory findById(Class<PayRecordHistory> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			PayRecordHistory find_PayR = (PayRecordHistory) session.get(cla, id);
			session.close();
			return find_PayR;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("findById failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecordHistory> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select order from PayRecordHistory order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecordHistory> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecordHistory> FindBySpeElement_S_N(String filed,Object value){
		String fdclient_hql = "select order from PayRecordHistory order where " +  filed + " != :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecordHistory> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			for (int i = 0; i < payRecords.size(); i++) {
				System.out.println(payRecords.get(i).getContractNum());
			}
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	/*寻找匹配的出纳记录*/
	public List<PayRecordHistory> FindBySpeElement_AND(String filed1,String filed2,Object value1,Object value2){
		
		/*修改匹配策略*/
		String fdclient_hql = "select payh from PayRecordHistory payh where " +  filed1 + " = :value1" + " and " + filed2 + " = :value2";//策略一：
	//	String fdclient_hql = "select binput from BankInput binput where " +  filed1 + " = :value1" + " or " + filed2 + " = :value2";
		/*修改匹配策略*/

		try {
			
			session = sessionFactory.openSession();
		//	java.util.List<BankInput> bankInputs = session.createQuery(fdclient_hql).setParameter("value", value).list();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("value2", value2);
			java.util.List<PayRecordHistory> payHistorys = query.list();
			session.close();
	
		/*	for (int i = 0; i < bankInputs.size(); i++) {
				System.out.println(bankInputs.get(i).getPayer());
			}*/
			return payHistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find payHistorys failed");
			return null;
		}
	}
	
	public boolean update(PayRecordHistory pRecord){
		try {
			beginTransaction();
			session.update(pRecord);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("PayRecordHistory update failed");
			return false;
		}
	}
	
	/*获取整张数据表*/
	public List<PayRecordHistory> GetPrecordTb(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecordHistory";
		List<PayRecordHistory> payr =   session.createQuery(hql_select_all).list();
		session.close();
		return payr;
	}
	
	/*获取整张数据表*/
	public List<PayRecordHistory> GetPrecordTbByElement(String filed,String filedvalue){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecordHistory where " + filed + " = :filename";
		List<PayRecordHistory> payr =   session.createQuery(hql_select_all)
				.setParameter("filename", filedvalue)
				.list();
		session.close();
		return payr;
	}
	
	/*对指定字段进行修改*/
	public boolean ModifySpeElement(String filed,Object value,int payid){
		session = sessionFactory.openSession();
		String md_hql = "update PayRecordHistory payR set payR.bankinput = :bid where payR.id =:payid";
		Query query = session.createQuery(md_hql);
		query.setParameter("bid", value);
		query.setParameter("payid", payid);
		query.executeUpdate();
		session.close();
		return true;
	}
	
	public List<PayRecordHistory> FindPayToBInput(boolean isConnect_value,String owner_value){
		String pToBInput = "select PayRecordHistory from PayRecordHistory PayRecordHistory where " +  "isconnect" + " = :isconnect" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecordHistory";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", isConnect_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecordHistory> payRecords = query.list();
			session.close();
			
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find pToBInput failed:" + e);
			return null;
		}	
	}
	
	public List<PayRecordHistory> FindPayNoBInput(Character checkResult_value,String owner_value){
		String pToBInput = "select PayRecordHistory from PayRecordHistory PayRecordHistory where " +  "isconnect" + " = :isconnect" + " and " + "checkResult" + " = :checkResult" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecordHistory";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", false);
			query.setParameter("checkResult", checkResult_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecordHistory> payRecords = query.list();
			session.close();
			
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find pToBInput failed:" + e);
			return null;
		}	
	}
	
	public void Close_Connect(){
		
/*		try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}

}


