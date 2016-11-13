package dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import entity.OriOrder;
import entity.PayRecord;
import sun.util.logging.resources.logging;

/**
 * Total_Account_Dao 连接数据库货款表的服务dao 
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class Total_Account_Dao {
	private static Logger logger = LogManager.getLogger(Total_Account_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Total_Account_Dao(SessionFactory wFactory) {
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
	
	public void add(OriOrder in_order){
		try {
			beginTransaction();
			session.save(in_order);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info(e+ "add failed:" + in_order.getCustomname() + ":" + in_order.getOrderNum());
		}
	}
	
	public void addlist(ArrayList<OriOrder> in_orders){
		for (int i = 0; i < in_orders.size(); i++) {
			add(in_orders.get(i));
		}
	}
	
	public void delete(OriOrder de_order){
		try {
			beginTransaction();
			session.delete(de_order);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete failed");
		}
	}
	
	public OriOrder findById(Class<OriOrder> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			OriOrder find_order = (OriOrder) session.get(cla, id);
			session.close();
			return find_order;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("findById failed");
			return null;
		}
	}
	
/*	public boolean update(String order_id,double input,String update_time){
		OriOrder order = findById(OriOrder.class, order_id);
		if (order != null) {
			
			double new_debt = order.getDebt() - input;
			order.setInput(input);
			order.setDebt(new_debt);
			order.setUpdateTime(update_time);
			
			try {
				beginTransaction();
				session.update(order);
				endTransaction();	
				return true;
			} catch (RuntimeException e) {
				// TODO: handle exception
				System.out.println("update failed");
				return false;
			}
		}
		else{
			return false;
		}
	}*/
	
	public boolean update(OriOrder order){
		try {
			beginTransaction();
			session.update(order);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("update failed:" + e);
			return false;
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetTolAccount(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from OriOrder";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all).list();
		session.close();
		return orders;
	}
	
	/*获取整张数据表*/
	public java.util.List GetTolAccountByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from OriOrder where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	public void DeleteOoriderTb(){
		try {
			beginTransaction();
			String de_all_hql = "delete from OriOrder";
			session.createQuery(de_all_hql).executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete OoriderTb failed" + e);
		}
	}
	
	public void DeleteOoriderByElement(String filed, Object filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from OriOrder where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();
			logger.info("根据" + filed + "删除货款表成功");
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("根据" + filed + "删除货款表失败" + e);
			//System.out.println("delete OoriderByElement failed" + e);
		}
	}
	
	public java.util.List<OriOrder> FindByClient(String client){
		String fdclient_hql = "select order from OriOrder order where client = :forder";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<OriOrder> orders = session.createQuery(fdclient_hql).setParameter("forder", client).list();
			session.close();
	
			for (int i = 0; i < orders.size(); i++) {
				System.out.println(orders.get(i).getOrderNum());
			}
			return orders;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<OriOrder> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select order from OriOrder order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<OriOrder> orders = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			for (int i = 0; i < orders.size(); i++) {
				System.out.println(orders.get(i).getOrderNum());
			}
			return orders;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	/*根据指定字段进行查找，字段类型为double类型*/
	public java.util.List<OriOrder> FindBySpeElement_N(String filed,Double value){
		String fdclient_hql = "select order from OriOrder order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<OriOrder> orders = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			for (int i = 0; i < orders.size(); i++) {
				System.out.println(orders.get(i).getOrderNum());
			}
			return orders;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	public List<OriOrder> FindOriHasBInput(String owner_value){
		String orinoB = "select ori from OriOrder ori where " +  "input" + " > :input" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecord";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(orinoB);
			query.setParameter("input", 0d);
			query.setParameter("owner", owner_value);
			java.util.List<OriOrder> orders = query.list();
			session.close();
			
			return orders;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find orinoB failed:" + e);
			return null;
		}	
	}
	
	public List<OriOrder> FindOriNoBInput(String owner_value){
		String orinoB = "select ori from OriOrder ori where " +  "input" + " = :input" + " and " + "owner = :owner";//策略一：
	//	String pToBInput = "from PayRecord";//策略一：
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(orinoB);
			query.setParameter("input", 0d);
			query.setParameter("owner", owner_value);
			java.util.List<OriOrder> orders = query.list();
			session.close();
			
			return orders;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find orinoB failed:" + e);
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
