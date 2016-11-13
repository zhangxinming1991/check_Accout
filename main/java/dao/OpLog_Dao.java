package dao;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.OpLog;

/**
 * OpLog_Dao �������ݿ������ʷ��ķ���dao
 * @author zhangxinming 
 * @version 1.0.0
 *
 */
public class OpLog_Dao {
	private static Logger logger = LogManager.getLogger(OpLog_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public OpLog_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
	/*	session = sessionFactory.openSession();
		transaction = session.beginTransaction();*/
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public void add(OpLog in_oplog){
		try {
			beginTransaction();
			session.save(in_oplog);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("save failed");
			logger.error("�����־��¼ʧ��");
		}
	}
	
	public void delete(OpLog de_oplog){
		try {
			beginTransaction();
			session.delete(de_oplog);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete failed");
			logger.error("ɾ����־ʧ��");
		}
	}
	
	public OpLog findById(Class<OpLog> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			OpLog find_oplog = (OpLog) session.get(cla, id);
			session.close();
			return find_oplog;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����id" + "=" + id + "������־ʧ��");
			return null;
		}
	}
	
	public boolean update(OpLog oplog){
		try {
			beginTransaction();
			session.update(oplog);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("������־ʧ��");
			return false;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetOpLogTb(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from OpLog";
		java.util.List oplogs =  (java.util.List) session.createQuery(hql_select_all).list();
		session.close();
		return oplogs;
	}
	
	
	
	public void Close_Connect(){
		
	/*	try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}
