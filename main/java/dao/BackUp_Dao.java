package dao;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.Backup;

public class BackUp_Dao {
	private static Logger logger = LogManager.getLogger(BackUp_Dao.class);
	
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public BackUp_Dao(SessionFactory wFactory) {
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
		try {
				session = sessionFactory.openSession();
				transaction = session.beginTransaction();
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("beginTransaction failed:" + e);
		}

	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public boolean add(Backup in_backup){
		try {
			beginTransaction();
			session.save(in_backup);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("添加备份sql记录" +in_backup.getId()+ "失败" + e);
			return false;
		}
	}
	
	public void delete(Backup de_backup){
		try {
			beginTransaction();
			session.delete(de_backup);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除备份sql" + de_backup.getId() + "失败" + e);
		}
	}
	
	public Backup findById(Class<Backup> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			Backup find_backup = (Backup) session.get(cla, id);
			session.close();
			return find_backup;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据id" + "=" + id + "查找备份sql记录失败" + e);
			return null;
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetTolTb(){
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from Backup";
			java.util.List backups =  (java.util.List) session.createQuery(hql_select_all).list();
			session.close();
			return backups;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("查找整张Backup表失败" + e);
			return null;
		}

	}
}
