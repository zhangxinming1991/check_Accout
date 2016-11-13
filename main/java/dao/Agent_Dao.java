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
 * �������ݿ��д����̱�ķ���dao
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
			logger.error("��Ӵ�����" +in_agent.getAgentId() + "ʧ��" + e);
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
			logger.error("ɾ��������" + de_agent.getAgentId() + "ʧ��" + e);
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
			logger.error("����id" + "=" + id + "���Ҵ�����ʧ��" + e);
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
			logger.info("���´�����" + agent.getAgentId() + "ʧ��" + e);
			return false;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetTolTb(){
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "from Agent";
			java.util.List agents =  (java.util.List) session.createQuery(hql_select_all).list();
			session.close();
			return agents;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��������Agent��ʧ��" + e);
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
