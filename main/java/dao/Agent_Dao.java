package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.Agent;
import entity.ConnectPerson;

/**
 * 连接数据库中代理商表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 */
public class Agent_Dao {
	private static Logger logger = LogManager.getLogger(Agent_Dao.class);
	
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Agent_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
	/*	session = sessionFactory.openSession();
		transaction = session.beginTransaction();*/
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
	
	public boolean add(Agent in_agent){
		try {
			beginTransaction();
			session.save(in_agent);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("save failed");
			logger.error("添加代理商" +in_agent.getAgentId() + "失败" + e);
			return false;
		}
	}
	
	public void delete(Agent de_agent){
		try {
			beginTransaction();
			session.delete(de_agent);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete failed");
			logger.error("删除代理商" + de_agent.getAgentId() + "失败" + e);
		}
	}
	
	public Agent findById(Class<Agent> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			Agent find_agent = (Agent) session.get(cla, id);
			session.close();
			return find_agent;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据id" + "=" + id + "查找代理商失败" + e);
			return null;
		}
	}
	
	public boolean update(Agent agent){
		try {
			beginTransaction();
			session.update(agent);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("更新代理商" + agent.getAgentId() + "失败" + e);
			return false;
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetTolTb(){
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from Agent";
			java.util.List agents =  (java.util.List) session.createQuery(hql_select_all).list();
			session.close();
			return agents;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("查找整张Agent表失败" + e);
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
