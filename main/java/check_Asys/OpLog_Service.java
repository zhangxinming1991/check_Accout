package check_Asys;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import dao.OpLog_Dao;
import entity.OpLog;

/**
 * OpLog_Service 操作日志类 提供用户的操作日志服务
 * @author zhangxinming
 *
 */
public class OpLog_Service {
	
	private static Logger logger = LogManager.getLogger(OpLog_Service.class);
	public OpLog_Dao opLog_Dao;
	
	/*日志内容*/
	public static final String Log = "登录";
	public static final String SIGNOUT = "注销";
	public static final String Upload_Pay = "上传付款信息";
	public static final String Upload_Pay_Wexin = "微信上传付款信息";
	public static final String IMPORT = "导入货款表及出纳表";
	public static final String START_CHECKWORK = "执行对账";
	public static final String ENTRER_CaModel = "进入对账流程";
	public static final String CANCEL_CaAgain = "取消并重新对账";
	public static final String EXPORT_CARes = "导出对账结果";
	/*日志内容*/
	
	/*用户类型*/
	public static final String utype_as = "代理商财务";
	public static final String utype_cp = "客户";
	public static final String utype_ma = "管理员";
	public static final String utype_un = "未知用户类型";
	/*用户类型*/
	
	/*操作结果*/
	public static final String result_success = "成功";
	public static final String result_failed = "失败";
	/*操作结果*/
	
	public OpLog_Service(SessionFactory wFactory){
		opLog_Dao = new OpLog_Dao(wFactory);
	}
	
	private String produce_time(){
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
		String dates = sFormat.format(date);
		
		return dates;
	}
	
	/**
	 * AddLog 生成一条日志
	 * @param usertype 日志主体的用户类型
	 * @param who 用户名
	 * @param content 日志内容
	 * @param result 操作结果
	 * @author zhangxinming
	 */
	public void AddLog(String usertype,String who,String content,String result){
		String op_time = produce_time();
		
		OpLog logrd = new OpLog();
		logrd.setTime(op_time);
		logrd.setUsertype(usertype);
		logrd.setUsername(who);
		logrd.setContent(content);
		logrd.setResult(result);
		
		opLog_Dao.add(logrd);
		logger.info("添加一条日志记录" + usertype + ":" + who + ":" + content + ":" + result);
	}
}
