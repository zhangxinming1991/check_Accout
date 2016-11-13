package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import entity.Agent;
import entity.Assistance;
import entity.BankInput;

/**
 * Assistance_Dao 连接数据库财务人员的dao
 * @author zhangxinming
 * @version 1.0.0
 */
public class Assistance_Dao {
	
	private static Logger logger = LogManager.getLogger(Assistance_Dao.class);
	
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Assistance_Dao(SessionFactory wFactory) {
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
	
	public int add(Assistance in_assis){
		try {
			beginTransaction();
			session.save(in_assis);
			endTransaction();	
			return 0;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("添加Assistance：" + in_assis.getWorkId() + "失败" + e);
			return -1;
		}
	}
	
	public boolean delete(Assistance de_assis){
		try {
			beginTransaction();
			session.delete(de_assis);
			endTransaction();		
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除Assistance:" + de_assis.getWorkId() + "失败" + e);
			return false;
		}
	}
	
	public Assistance findById(Class<Assistance> cla,Serializable id){
		logger.info("findById" + "id=" + id);
		try {
			session = sessionFactory.openSession();
			Assistance find_Assis = (Assistance) session.get(cla, id);
			session.close();
			return find_Assis;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + "id= " + id + "查找财务人员失败" + e);
			return null;
		}
	}
	
	public boolean update(Assistance assistance){
		try {
			beginTransaction();
			session.update(assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("更新Assistance:" + assistance.getWorkId() + "失败" + e);
			return false;
		}
	}
	
	public List<Assistance> GetTotalTbByElement(String filed1,Object value1){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "from Assistance where " + filed1 + " = :value1";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("value1", value1)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + filed1 + "=" + value1 + "获取整个Assistance表" + "失败" + e);
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
