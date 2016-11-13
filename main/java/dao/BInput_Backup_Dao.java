package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.BankInputBackup;


public class BInput_Backup_Dao {
	private static Logger logger = LogManager.getLogger(BInput_Backup_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public BInput_Backup_Dao(SessionFactory wFactory) {
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
	
	public void add(BankInputBackup in_bankIn){
		try {
			beginTransaction();
			session.save(in_bankIn);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("添加出纳备份记录失败" + e);
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetBInputBupByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from BankInputBackup where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	/**/
	public void DeleteBInputBupByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInputBackup where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除备份出纳表记录失败" + e);
		}
	}
}
