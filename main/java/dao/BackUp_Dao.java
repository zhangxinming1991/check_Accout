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
			logger.error("��ӱ���sql��¼" +in_backup.getId()+ "ʧ��" + e);
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
			logger.error("ɾ������sql" + de_backup.getId() + "ʧ��" + e);
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
			logger.error("����id" + "=" + id + "���ұ���sql��¼ʧ��" + e);
			return null;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetTolTb(){
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from Backup";
			java.util.List backups =  (java.util.List) session.createQuery(hql_select_all).list();
			session.close();
			return backups;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��������Backup��ʧ��" + e);
			return null;
		}

	}
}
