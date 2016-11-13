package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.CusSdstoreBackup;

public class CusSdStore_Backup_Dao {
	private static Logger logger = LogManager.getLogger(BInput_Backup_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public CusSdStore_Backup_Dao(SessionFactory wFactory) {
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
	
	public void add(CusSdstoreBackup in_bankIn){
		try {
			beginTransaction();
			session.save(in_bankIn);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("add the CusSdstoreBackup:" + in_bankIn.getClient() + "failed:"+ e);
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetTbByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from CusSdstoreBackup where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	/**/
	public void DeleteTbByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from CusSdstoreBackup where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("delete from CusSdstoreBackup where " + filed + " = :filedname" + "failed" + e);
		}
	}

}
