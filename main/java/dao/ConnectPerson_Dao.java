package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import entity.Assistance;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.OriOrder;

/**
 * ConnectPerson_Dao 连接数据库对账联系人表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class ConnectPerson_Dao {
	
	private static Logger logger = LogManager.getLogger(ConnectPerson_Dao.class);
	
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public ConnectPerson_Dao(SessionFactory wFactory) {
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
	
	public boolean add(ConnectPerson in_conp){
		try {
			beginTransaction();
			session.save(in_conp);
			endTransaction();			
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("插入对账联系人失败" + e);
			return false;
		}
	}
	
	public boolean delete(ConnectPerson de_conp){
		try {
			beginTransaction();
			session.delete(de_conp);
			endTransaction();
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("delete ConnectPerson:" + de_conp.getUsername() + "失败" + e);
			return false;
		}
	}
	
	public boolean update(ConnectPerson cPerson){
		try {
			beginTransaction();
			session.update(cPerson);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("更新对账联系人失败");
			return false;
		}
	}
	
	public ConnectPerson findById(Class<ConnectPerson> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			ConnectPerson find_ConP = (ConnectPerson) session.get(cla, id);
			session.close();
			return find_ConP;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + "id=" + id + "查找对账联系人失败" + e);
			return null;
		}
	}
	
	public java.util.List<String> findClientById(String id){
		String fdclient_hql = "select cp.company from ConnectPerson cp where cp.username = :id";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<String> companys = session.createQuery(fdclient_hql).setParameter("id", id).list();
			session.close();
	
			for (int i = 0; i < companys.size(); i++) {
				System.out.println(companys.get(i));
			}
			return companys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("根据" + "id" + "=" + id + "查找ConnectPerson.company失败" + e);
			return null;
		}
	}
	
	/*寻找匹配的出纳记录*/
	public List<ConnectPerson> FindBySpeElement(String filed1,Object value1,String owner){		
		/*修改匹配策略*/
		String fdclient_hql = "select cp from ConnectPerson cp where " +  filed1 + " = :value1" + " and " + "agent = :owner_value";//策略一：
		/*修改匹配策略*/

		try {			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("owner_value", owner);
			java.util.List<ConnectPerson> cps = query.list();
			session.close();
			
			return cps;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("查找对账联系人失败" + e);
			return null;
		}
	}
	
	public List<ConnectPerson> GetTotalTbByElement(String filed1,Object value1){
		session = sessionFactory.openSession();
		String hql_select_all = "from ConnectPerson where " + filed1 + " = :value1";
		List<ConnectPerson> cPersons =   (List<ConnectPerson>) session.createQuery(hql_select_all)
				.setParameter("value1", value1)
				.list();
		session.close();
		return cPersons;
	}
	
	public void Close_Connect(){
		
	/*	try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}
