package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.OriOrderBackup;


public class Ori_BackUp_Dao {
	private static Logger logger = LogManager.getLogger(Ori_BackUp_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Ori_BackUp_Dao(SessionFactory wFactory) {

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
	
	public void add(OriOrderBackup in_order){
		try {
			beginTransaction();
			session.save(in_order);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("添加ori_backup 记录失败" + e);
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetOriBackupByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from OriOrderBackup where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	/**/
	public void DeleteOBackupByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from OriOrderBackup where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除备份货款表记录失败" + e);
		}
	}
	
}
