package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.PayRecord;
import sun.util.logging.resources.logging;

/**
 * PayRecord_Dao 连接数据库付款信息表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class PayRecord_Dao {
	private static Logger logger = LogManager.getLogger(PayRecord_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public PayRecord_Dao(SessionFactory wFactory) {
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
	
	public void add(PayRecord in_pR){
		try {
			beginTransaction();
			session.save(in_pR);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	public void addlist(List<PayRecord> in_payrecord){
		for (int i = 0; i < in_payrecord.size(); i++) {
			add(in_payrecord.get(i));
		}
	}
	
	public void delete(PayRecord de_pR){
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
			String de_all_hql = "delete from PayRecord";
			session.createQuery(de_all_hql).executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete all failed" + e);
		}
	}
	
	public void DeletePrecordTbByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from PayRecord where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete PrecordTbByElement failed" + e);
		}
	}
	
	public PayRecord findById(Class<PayRecord> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			PayRecord find_PayR = (PayRecord) session.get(cla, id);
			session.close();
			return find_PayR;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("findById failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecord> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + filed + "=" + value + "查找付款记录失败" + e);
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecord> FindBySpeElement_S_limit(String filed,String value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setFirstResult(0)
					.setMaxResults(1)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("有限制查找付款信息失败" + e);
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecord> FindBySpeElement(String filed,Object value,String owner){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value" + " and " + "owner = :owner_value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setParameter("owner_value", owner)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + filed + "=" + value + "查找付款记录失败" + e);
			//System.out.println("find client failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<PayRecord> FindBySpeElement_S_N(String filed,Object value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " != :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
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
	
	public boolean update(PayRecord pRecord){
		try {
			beginTransaction();
			session.update(pRecord);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("payrecord update failed");
			return false;
		}
	}
	
	/*获取整张数据表*/
	public List<PayRecord> GetPrecordTb(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecord";
		List<PayRecord> payr =   session.createQuery(hql_select_all).list();
		session.close();
		return payr;
	}
	
	/*获取整张数据表*/
	public List<PayRecord> GetPrecordTbByElement(String filed,String filedvalue){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecord where " + filed + " = :filename";
		List<PayRecord> payr =   session.createQuery(hql_select_all)
				.setParameter("filename", filedvalue)
				.list();
		session.close();
		return payr;
	}
	
	/*对指定字段进行修改*/
	public boolean ModifySpeElement(String filed,Object value,int payid){
		session = sessionFactory.openSession();
		String md_hql = "update PayRecord payR set payR.bankinput = :bid where payR.id =:payid";
		Query query = session.createQuery(md_hql);
		query.setParameter("bid", value);
		query.setParameter("payid", payid);
		query.executeUpdate();
		session.close();
		return true;
	}
	
	public List<PayRecord> FindPayToBInput(boolean isConnect_value,String owner_value){
		String pToBInput = "select payrecord from PayRecord payrecord where " +  "isconnect" + " = :isconnect" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecord";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", isConnect_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecord> payRecords = query.list();
			session.close();
			
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find pToBInput failed:" + e);
			return null;
		}	
	}
	
	public List<PayRecord> FindPayNoBInput(Character checkResult_value,String owner_value){
		String pToBInput = "select payrecord from PayRecord payrecord where " +  "isconnect" + " = :isconnect" + " and " + "checkResult" + " = :checkResult" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecord";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", false);
			query.setParameter("checkResult", checkResult_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecord> payRecords = query.list();
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

