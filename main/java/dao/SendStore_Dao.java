package dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import entity.CusSecondstore;

/**
 * SendStore_Dao 连接数据库客户信息表的服务dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class SendStore_Dao {
	private static Logger logger = LogManager.getLogger(SendStore_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public SendStore_Dao(SessionFactory wFactory) {
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
	
	public void add(CusSecondstore in_cusSt){
		try {
			beginTransaction();
			session.save(in_cusSt);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("add CusSecondstore " + in_cusSt.getClient() + "failed:" +e);
		}
	}
	
	public void addlist(ArrayList<CusSecondstore> in_cusSts){
		for (int i = 0; i < in_cusSts.size(); i++) {
			add(in_cusSts.get(i));
		}
	}
	
	public void delete(CusSecondstore de_cusSt){
		try {
			beginTransaction();
			session.delete(de_cusSt);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete failed");
		}
	}
	
	/*获取整张数据表*/
	public List<CusSecondstore> GetCustomTb(String owner){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from CusSecondstore where owner = :owner_value";
		List<CusSecondstore> cusSecondstores=   (List<CusSecondstore>) session.createQuery(hql_select_all)
				.setParameter("owner_value", owner)
				.list();
		session.close();
		return cusSecondstores;
	}
	
	/**/
	public void DeleteTbByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from CusSecondstore where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("delete from CusSecondstore where " + filed + " = :filedname" + "failed" + e);
		}
	}
	
	public CusSecondstore findById(Class<CusSecondstore> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			CusSecondstore find_cusSt = (CusSecondstore) session.get(cla, id);
			session.close();
			return find_cusSt;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("findcustomById failed:" + e);
			return null;
		}
	}
	
/*	public boolean update(String client,double input,String update_time){
		CusSecondstore cusSt = findById(CusSecondstore.class, client);
		if (cusSt != null) {
			double new_input = cusSt.getInput() + input;
			cusSt.setInput(new_input);
			cusSt.setUpdateTime(update_time);
			
			try {
				beginTransaction();
				session.update(cusSt);
				endTransaction();	
				return true;
			} catch (RuntimeException e) {
				// TODO: handle exception
				System.out.println("update failed");
				return false;
			}
		}
		else{
			System.out.println("update:不存在该客户名称");
			return false;
		}
	}*/
	
	public boolean update(CusSecondstore custom){	
		try {
			beginTransaction();
			session.update(custom);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("update failed" + e);
			return false;
		}
	}
	
	/*寻找匹配的出纳记录*/
	public List<CusSecondstore> FindBySpeElement_Big(String filed1,Object value1,String owner){		
		/*修改匹配策略*/
		String fdclient_hql = "select custom from CusSecondstore custom where " +  filed1 + " > :value1" + " and " + "owner = :owner_value";//策略一：
		/*修改匹配策略*/

		try {			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("owner_value", owner);
			java.util.List<CusSecondstore> customs = query.list();
			session.close();
			
			return customs;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("查找客户失败" + e);
			return null;
		}
	}
	
	public void Close_Connect(){
		
/*		try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}

}
