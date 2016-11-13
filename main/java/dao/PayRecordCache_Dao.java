package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.sun.org.apache.bcel.internal.generic.NEW;

import entity.PayRecordCache;

/**
 * PayRecordCache_Dao 连接数据库付款缓存区表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class PayRecordCache_Dao {
	private static Logger logger = LogManager.getLogger(PayRecordCache_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public PayRecordCache_Dao(SessionFactory wFactory) {
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
	
	public void add(PayRecordCache in_pR){
		try {
			beginTransaction();
			session.save(in_pR);
			endTransaction();	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("插入失败" + e);
			//System.out.println("save failed");
		}
	}
	
	/*获取整张数据表*/
	public List<PayRecordCache> GetPayRecordsTb(String owner){
		logger.info("GetPayRecordsTb by owner="  + owner);
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from PayRecordCache where owner = :owner_value";
			java.util.List paycs =  (java.util.List) session.createQuery(hql_select_all)
					.setParameter("owner_value", owner)
					.list();
			session.close();
			return paycs;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据owner=" + owner + "获取整张PayRecordCache失败" + e);
			return null;
		}
	}
	
	/*获取整张数据表*/
	public List<PayRecordCache> GetTbByElement_Owner(String filed1,Object value1,String owner){
		logger.info("GetPayRecordsTb by owner="  + owner);
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from PayRecordCache where " +  filed1 + " = :value1" + " and " + "owner = :owner_value";
			java.util.List paycs =  (java.util.List) session.createQuery(hql_select_all)
					.setParameter("owner_value", owner)
					.setParameter("value1", value1)
					.list();
			session.close();
			return paycs;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据owner=" + owner +  filed1 + "=" + value1 + "获取整张PayRecordCache失败" + e);
			return null;
		}
	}
	
	/*获取整张数据表*/
	public List<PayRecordCache> GetTbByElementNot_Owner(String filed1,Object value1,String owner){
		logger.info("GetPayRecordsTb by owner="  + owner);
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from PayRecordCache where " +  filed1 + " != :value1" + " and " + "owner = :owner_value";
			java.util.List paycs =  (java.util.List) session.createQuery(hql_select_all)
					.setParameter("owner_value", owner)
					.setParameter("value1", value1)
					.list();
			session.close();
			return paycs;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据owner=" + owner + "|" + filed1 + "!=" + value1 + "获取整张PayRecordCache失败" + e);
			return null;
		}
	}
	
	/*根据条件获取整张数据表*/
	public List<PayRecordCache> GetPayRecordsTbByElment(String filed,Object filed_value){
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecordCache where " + filed + " = :filed_value";
		java.util.List paycs =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("filed_value", filed_value)
				.list();
		session.close();
		return paycs;
	}
	
	/*获取整张数据表*/
	public void DeleteRecordsTb(String owner){
		try{
			beginTransaction();
			String hql_select_all = "delete from PayRecordCache where owner = :owner_value";
			session.createQuery(hql_select_all)
			.setParameter("owner_value", owner)
			.executeUpdate();
			endTransaction();	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + owner + "删除记录失败" + e);
		}
	}
	
	/*根据条件删除部分记录*/
	public void DeleteRecordsByElement(String filed1,Object value1,String owner){
		try{
			beginTransaction();
			String hql_select_all = "delete from PayRecordCache where " +  filed1 + " = :value1" + " and " + "owner = :owner_value";
			session.createQuery(hql_select_all)
					.setParameter("value1", value1)
					.setParameter("owner_value", owner)
					.executeUpdate();
			endTransaction();
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据owner=" + owner + "|" + filed1 + "=" + value1 + "删除整张PayRecordCache失败" + e);
		}

	}
	
	/*根据条件删除部分记录*/
	public void DeleteTbByEleNot_OWner(String filed1,Object value1,String owner){
		try{
			beginTransaction();
			String hql_select_all = "delete from PayRecordCache where " +  filed1 + " != :value1" + " and " + "owner = :owner_value";
			session.createQuery(hql_select_all)
					.setParameter("value1", value1)
					.setParameter("owner_value", owner)
					.executeUpdate();
			endTransaction();
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据owner=" + owner + "|" + filed1 + "!=" + value1 + "删除整张PayRecordCache失败" + e);
		}

	}
	

}
