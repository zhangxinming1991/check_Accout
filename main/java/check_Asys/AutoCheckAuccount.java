package check_Asys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.naming.NamingContextBindingsEnumeration;

import com.mysql.fabric.xmlrpc.base.Data;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import dao.Agent_Dao;
import dao.BInput_Backup_Dao;
import dao.BankInput_Dao;
import dao.CaresultHistory_Dao;
import dao.ConnectPerson_Dao;
import dao.CusSdStore_Backup_Dao;
import dao.Ori_BackUp_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import entity.Agent;
import entity.BankInput;
import entity.BankInputBackup;
import entity.CaresultHistory;
import entity.ConnectPerson;
import entity.CusSdstoreBackup;
import entity.CusSecondstore;
import entity.OriOrder;
import entity.OriOrderBackup;
import entity.PayRecord;
import entity.PayRecordCache;
import entity.PayRecordHistory;
import file_op.Excel_RW;
import file_op.Excel_RW.Excel_Row;
import jdk.nashorn.api.scripting.JSObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * AutoCheckAuccount 对账业务中每一个操作的具体实现都在这个类里面，通过调用这个类里面的函数来实现对账操作
 * @author zhangxinming
 * @version 1.0.0
 */
public class AutoCheckAuccount {
	private static Logger logger = LogManager.getLogger(AutoCheckAuccount.class);
	private static Logger logger_error = LogManager.getLogger("error");
	BankInput_Dao bDao = null;
	PayRecord_Dao pDao = null;
	Total_Account_Dao tDao = null;
	SendStore_Dao sDao = null;
	PayRecordHistory_Dao pHDao = null;
	CaresultHistory_Dao cDao = null;
	PayRecordCache_Dao pCDao = null;
	ConnectPerson_Dao cPerson_Dao = null;
	Ori_BackUp_Dao oUp_Dao = null;
	BInput_Backup_Dao bInput_Backup_Dao = null;
	Agent_Dao agent_Dao = null;
	CusSdStore_Backup_Dao cBackup_Dao = null;
	
	public boolean isFreeBack(PayRecord payRecord,String owner){
		if (payRecord.getFreeback() == true) {
			logger.info(payRecord.getId() + "付款记录已经返利，不能重复返利！");
			return false;
		}
		else{
			char checkresult = payRecord.getCheckResult();
			if (checkresult == 'Y' || checkresult == 'W') {
				String connp_username = payRecord.getConnPerson();
				ConnectPerson fcPerson = cPerson_Dao.findById(ConnectPerson.class, connp_username);
	
				fcPerson.setScore(fcPerson.getScore() + 10);
				cPerson_Dao.update(fcPerson);
				
				payRecord.setFreeback(true);
				pDao.update(payRecord);
			}
			return true;

		}
	}
	
	public AutoCheckAuccount(BankInput_Dao bDao,PayRecord_Dao pDao,	Total_Account_Dao tDao,SendStore_Dao sDao,PayRecordHistory_Dao pHDao,CaresultHistory_Dao cDao,PayRecordCache_Dao pCDao,ConnectPerson_Dao cPerson_Dao,Ori_BackUp_Dao oUp_Dao,BInput_Backup_Dao bInput_Backup_Dao,Agent_Dao agent_Dao,CusSdStore_Backup_Dao cBackup_Dao) {
		this.bDao = bDao;
		this.pDao = pDao;
		this.tDao = tDao;
		this.sDao = sDao;
		this.pHDao = pHDao;
		this.cDao = cDao;
		this.pCDao = pCDao;
		this.cPerson_Dao = cPerson_Dao;
		this.oUp_Dao = oUp_Dao;
		this.bInput_Backup_Dao = bInput_Backup_Dao;
		this.agent_Dao = agent_Dao;
		this.cBackup_Dao = cBackup_Dao;
	}

	/**
	 * MappPayToBank 利用手机付款信息中客户名称去匹配银行:匹配策略，使用付款信息中的付款人和款项接受人作为匹配依据，去寻找相关的出纳记录
	 * @param payid 付款信息的id
	 * @return
	 */
	public List<BankInput> MappPayToBank(int payid,String owner){
		logger.info("sayhi");
		List<BankInput> mappedBankInput = null;
		
		PayRecord fRecord = pDao.findById(PayRecord.class, payid);
		String payer = fRecord.getPayer();
		String payee = fRecord.getReceiver();
		
		mappedBankInput = bDao.GetMapBinputs("payer", "payer", payer, payee,owner);//根据付款人和收款人两个信息去查找出纳记录
		if (mappedBankInput == null) {
			System.out.println("not found the mapped bankrecord");
		}
		
		Test_MappPayToBank(mappedBankInput);
		
		return mappedBankInput;
	}
	
	/**
	 * ConnectBankWithPay 将满足要求的唯一银行记录关联到付款信息
	 * @param payid 付款信息id
	 * @param bankInput_id 出纳id
	 * @param contract_num 合同个数
	 * @param many_contract 合同具体信息
	 * @return
	 */
	public boolean ConnectBankWithPay(int payid,int bankInput_id,String contract_num,String many_contract){

		/*绑定需要完成的业务操作*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(bankInput_id);
		pRecord.setIsconnect(true);
		pRecord.setCheckResult('Y');
		/*绑定需要完成的业务操作*/
		
		/*绑定需要完成的业务功能*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		bRecord.setPayid(payid);
		bRecord.setIsConnect(true);
		bRecord.setContractNum(contract_num);
		bRecord.setManyContract(many_contract);
		/*绑定需要完成的业务功能*/
		
		//添加更多确定匹配的业务操作
		
		pDao.update(pRecord);
		bDao.update(bRecord);
		return true;
	}
	
	/**
	 * CancelConnecttBWithP  取消唯一银行记录关联到付款信息
	 * @param payid 付款信息id
	 * @param bankInput_id 出纳id
	 * @return
	 */
	public boolean CancelConnecttBWithP(int payid,int bankInput_id){

		/*取消绑定付款信息需要完成的业务操作*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(null);
		pRecord.setIsconnect(false);
		pRecord.setCheckResult(null);
		pDao.update(pRecord);
		/*取消绑定付款信息需要完成的业务操作*/
		
		/*取消绑定需要完成的业务功能*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		if (bRecord != null) {
			bRecord.setPayid(null);
			bRecord.setIsConnect(false);
			bRecord.setContractNum(null);
			bRecord.setManyContract(null);
			bDao.update(bRecord);
		}
		/*取消绑定需要完成的业务功能*/
		
		//添加更多确定匹配的业务操作
		
		return true;
	}
	
	/**
	 * ConnectBankWithCustom  将出纳记录关联到客户名下
	 * @param cInput 出纳信息
	 * @param truepayer 真实付款人
	 * @param truemoney 真实金额 
	 * @return
	 */
	public boolean ConnectBankWithCustom(BankInput cInput,String truepayer,Double truemoney){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, truepayer);
		if (fcustom == null) {
			logger.error("出纳记录关联到客户名下");
			return false;
		}
		
		if (fcustom.getOwner().equals(cInput.getOwner()) == false) {
			logger.info("代理商信息不一样,无法将出纳记录和客户关联");
			return false;
		}
		else{
			/*绑定需要完成的业务操作*/
			fcustom.setInput(truemoney + fcustom.getInput());
			fcustom.setUpdateTime(cInput.getInputTime());
			boolean connectResult = sDao.update(fcustom);
			
			if (connectResult == true) {//如果关联成功,更新出纳记录的关联客户字段
				cInput.setConnectClient(fcustom.getClient());
				bDao.update(cInput);
			}
			return true;
			/*绑定需要完成的业务操作*/
		}

	}
	
	/**
	 * CancelConnectBkAndPay 取消付款记录和出纳记录的绑定
	 * @param payid 付款信息id
	 * @param bankInput_id 出纳信息id
	 * @return
	 */
	public boolean CancelConnectBkAndPay(int payid,int bankInput_id){
		
		/*绑定或者取消付款信息需要完成的业务操作*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(0);//设置为0
		pRecord.setIsconnect(false);
		/*绑定或者取消付款信息需要完成的业务操作*/
		
		/*绑定或者取消绑定需要完成的业务功能*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		bRecord.setPayid(0);
		bRecord.setIsConnect(false);
		bRecord.setContractNum("");
		bRecord.setManyContract("");
		/*绑定或者取消绑定需要完成的业务功能*/
		
		pDao.update(pRecord);
		bDao.update(bRecord);
		return true;
	}
	
	/**
	 * ConnectBankWithAccount 出纳记录关联到货款表：使用合同号关联
	 * @param cInput 出纳记录信息
	 * @return
	 */
	public JSONObject ConnectBankWithAccount(BankInput cInput,String owner){
		OriOrder fOrder = null;
		JSONObject re_object = new JSONObject();
		re_object.element("flag", -1);
		
		JSONArray many_contract = JSONArray.fromObject(cInput.getManyContract());
		if (many_contract == null) {
			logger.error("many_contract is null");
			re_object.element("errmsg", "many_contract is null");
			return re_object;
		}
		if (many_contract.size() == 0) {
			logger.warn("没有任何合同信息");
			re_object.element("errmsg", "没有任何合同信息");
			return re_object;
		}
		
		for (int j = 0; j < many_contract.size(); j++) {
			JSONObject cotract_money = many_contract.getJSONObject(j);
			
			fOrder = tDao.findById(OriOrder.class, cotract_money.getString("contract"));
			if (fOrder == null || fOrder.getOwner().equals(owner) == false) {
		//		logger.info(fOrder.getOwner() + ":" + owner);
				String errmsg = "填写的" + cotract_money.getString("contract") + "合同号有误,货款表中不存在该合同号";
				logger.info(errmsg);
				re_object.element("errmsg", errmsg);
				String truepayer = pDao.findById(PayRecord.class, cInput.getPayid()).getPayer();
				Double money = cotract_money.getDouble("money");

				ConnectBankWithCustom(cInput,truepayer,money);//使用客户名称去关联合同号
				continue;
			}
			/*绑定需要完成的业务功能*/
			fOrder.setInput(cotract_money.getDouble("money") + fOrder.getInput());
			fOrder.setDebt(fOrder.getDebt() - fOrder.getInput());
			fOrder.setUpdateTime(cInput.getInputTime());
			
			JSONArray jconnectbArray;
			if (fOrder.getConnectBank() == null) {
				jconnectbArray = new JSONArray();
			}
			else {
				String connect_bank = fOrder.getConnectBank();
				jconnectbArray = JSONArray.fromObject(connect_bank);
			}
			jconnectbArray.add(cInput.getId());
			fOrder.setConnectBank(jconnectbArray.toString());
			/*绑定需要完成的业务功能*/
			
			if (tDao.update(fOrder) == true) {//如果关联成功，则更新出纳记录的关联合同数目字段
				cInput.setConnectNum(cInput.getConnectNum() + 1);
				bDao.update(cInput);
			}			
		}
		re_object.element("flag", 0);
		re_object.element("errmsg", "使用合同号关联货款表成功");
		return re_object;
	}
	
	/**
	 * ConnectBankWithAccount_Only 出纳记录关联到货款表：客户只有一条合同记录
	 * @param cInput
	 * @return
	 */
	public boolean ConnectBankWithAccount_Only(BankInput cInput){
		OriOrder fOrder = null;
		boolean connectResult = false;

		logger.info(cInput.getPayer());
		fOrder = tDao.FindByClient(cInput.getPayer()).get(0);
		/*绑定需要完成的业务功能*/
		logger.info(cInput.getMoney() + ":" + fOrder.getInput());
		fOrder.setInput(cInput.getMoney() + fOrder.getInput());
		fOrder.setDebt(fOrder.getDebt() - fOrder.getInput());
		fOrder.setUpdateTime(cInput.getInputTime());
		
		JSONArray jconnectbArray;
		if (fOrder.getConnectBank() == null) {
			jconnectbArray = new JSONArray();
		}
		else {
			String connect_bank = fOrder.getConnectBank();
			jconnectbArray = JSONArray.fromObject(connect_bank);
		}
		jconnectbArray.add(cInput.getId());
		fOrder.setConnectBank(jconnectbArray.toString());
		/*绑定需要完成的业务功能*/
		 connectResult = tDao.update(fOrder);
		if (connectResult == true) {
			cInput.setConnectNum(cInput.getConnectNum() + 1);
			JSONObject jObject = new JSONObject();
			jObject.put("contract", fOrder.getOrderNum());
			jObject.put("money", cInput.getMoney());
			JSONArray jArray = new JSONArray();
			jArray.add(jObject);
			cInput.setManyContract(jArray.toString());
			//cInput.setIsConnect(true);
			bDao.update(cInput);
		}
		return true;
	}
	
	/**
	 * IsStartCheckWork 是否满足开始对账的条件
	 * @return
	 */
	public JSONObject IsStartCheckWork(String owner){
		//判断是否所有的付款记录都已经审阅
		JSONObject jmesg = new JSONObject();
		jmesg.element("flag", 0);
		String meString = "";
		List pList = pDao.GetPrecordTbByElement("owner", owner);
		
		for(int i = 0;i<pList.size();i++){
			PayRecord pRecord = (PayRecord)pList.get(i);
			if (pRecord.getCheckResult() == null) {
				logger.info("id为 " + pRecord.getId() + "付款记录没有审阅");
				jmesg.element("flag", -1);
				meString = meString + "[id为 " + pRecord.getId() + "付款记录没有审阅] \r\n";
			}
		}
		
		jmesg.element("errmsg", meString);
		return jmesg;
	}
	
	/**
	 * ConnectAccountWithCustom 添加或者刷新客户合同信息
	 * @param order 货款信息
	 * @return
	 */
	public boolean ConnectAccountWithCustom(OriOrder order){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, order.getClient());
		JSONObject contract_mes = new JSONObject();
		JSONArray acontract_mes = new JSONArray();
		if (fcustom == null) {//客户表中不存在客户信息，则新建用户
			fcustom = new CusSecondstore();
			
			contract_mes.put("contract", order.getOrderNum());
			contract_mes.put("debt", order.getDebt());
			acontract_mes.add(contract_mes);
			
			/*插入需要完成的业务操作*/
			fcustom.setInput(0d);
			fcustom.setClient(order.getClient());
			fcustom.setContractMes(acontract_mes.toString());
			fcustom.setContractNum(1);
			fcustom.setOwner(order.getOwner());
			/*插入需要完成的业务操作*/
			sDao.add(fcustom);
		}
		else {//更新客户信息
			if (fcustom.getContractMes() == null) {
				
			}
			else{
				acontract_mes = JSONArray.fromObject(fcustom.getContractMes());
			}
			
			contract_mes.put("contract", order.getOrderNum());
			contract_mes.put("debt", order.getDebt());
			acontract_mes.add(contract_mes);//插入新的合同
			
			/*更新需要完成的业务操作*/
			fcustom.setContractMes(acontract_mes.toString());
			fcustom.setContractNum(fcustom.getContractNum() + 1);
			sDao.update(fcustom);
			/*更新需要完成的业务操作*/
		}
		return true;
	}
	
	/**
	 * ConnectBankinputWithCustom 添加或者刷新客户的账号信息
	 * @param bankInput 出纳信息
	 * @return
	 */
	public boolean ConnectBankinputWithCustom(BankInput bankInput){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, bankInput.getPayer());
		JSONArray acontract_mes = new JSONArray();
		if (fcustom == null) {

		}
		else {//更新客户信息
			if (bankInput.getPayerAccount() == null) {
				
			}
			else{
				if (fcustom.getAccoutMes() == null) {
					
				}
				else{
					acontract_mes = JSONArray.fromObject(fcustom.getAccoutMes());
				}
				acontract_mes.add(bankInput.getPayerAccount());//插入新的合同
				/*更新需要完成的业务操作*/
				fcustom.setAccoutMes(acontract_mes.toString());
				sDao.update(fcustom);
				/*更新需要完成的业务操作*/
			}			
		}
		return true;
	}

	/**
	 * CreateCaid 产生对账id
	 * @param owner 代理商id
	 * @return
	 */
	public String CreateCaid(String owner){
		//生成新的对账id
		Date datey = new Date();
		Date datem = new Date();
		SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
		SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		String dateys = sdfy.format(datey);//本年
		String datems = sdfm.format(datem);//本月
		String caid = dateys + "-" + datems + "-" + owner;//对账id
		
		return caid;
	}
	

	/**
	 * Enter_CaModel 进入对账模式
	 * @param owner
	 * @return
	 */
	public String Enter_CaModel(String owner){
		logger.info("sayhello");
		logger.info("进入对账模式");
		
		//生成新的对账id
		Date datey = new Date();
		Date datem = new Date();
		SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
		SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		String dateys = sdfy.format(datey);//本年
		String datems = sdfm.format(datem);//本月
		String caid = dateys + "-" + datems + "-" + owner;//对账id
		
		
		//获取当年月份本月的对账结果记录
		List<CaresultHistory> fCrHistories = cDao.FindBySpeElementLimit("caid", caid, owner);
		if (fCrHistories.isEmpty() == true) {//如果记录为空，则插入新的对账记录(本月对账还没开始)	
			logger.info("开始新月份的对账");
			CaresultHistory in_crhistory = new CaresultHistory();//记录的状态为进行中
			in_crhistory.setCaid(caid);
			in_crhistory.setCayear(dateys);
			in_crhistory.setCamonth(datems);
			in_crhistory.setOwner(owner);
			in_crhistory.setCaresult('D');
			in_crhistory.setLastcaid(true);
			cDao.add(in_crhistory);
			
			//将付款工作区的记录移到付款历史区
			TransferPrecord_WAreaToHArea(owner);
			//将付款缓冲区的旧的记录移到付款历史区
			TransferPrecord_CAreaToHArea(owner,caid);
			//从付款缓冲区取付款记录
			TransferPrecord_CAreaToWArea(owner,caid);
		}
		else{
			//List<PayRecord> fpList = pDao.FindBySpeElement_S_limit("caid", caid);
			List<OriOrderBackup> orderBackups = oUp_Dao.GetOriBackupByElment("owner", owner);
			if (orderBackups.isEmpty() != true) {//从历史对账中回到本月对账,（不一定成立，还要判断backup区是否为空，不为空成立）
				logger.info("从历史对账中回到本月对账");
				
				TransferPrecord_WAreaToHArea(owner);//将付款区记录转移到付款历史区
				
				TransferPrecord_HAreaToWArea(owner, caid);//将付款历史区的caid的记录转移到付款区
				
				//清空货款表和出纳表信息
				tDao.DeleteOoriderByElement("owner", owner);
				bDao.DeleteBinputTbByElement("owner", owner);
				//从backup区重新载入货款记录和出纳记录
				TransferOri_BackupToWarea(owner);
				TransferBinput_BackupToWarea(owner);
				
				//将付款缓冲区的记录转于到付款区
				TransferPrecord_CAreaToWArea(owner,caid);
				
				//客户信息的重载
				sDao.DeleteTbByElement("owner", owner);
				TransferCus_BackupToWarea(owner);//将客户信息转移到工作区
				
				CaresultHistory fHLast = cDao.FindBySpeElement("lastcaid", true, owner).get(0);//查找瓶颈
				if (fCrHistories == null) {
					logger_error.error("上次对账caid不存在");
				}
				else {
					fHLast.setLastcaid(false);//将上次对账的lastcaid设为false
					cDao.update(fHLast);
					
					CaresultHistory fHCur = cDao.FindBySpeElement("caid", caid, owner).get(0);//查找瓶颈
					fHCur.setLastcaid(true);//将本次对账的lastcaid设为true
					cDao.update(fHCur);
				}
			}
			else{//本月重复对账
				logger.info("本月重复对账");
				//将付款缓冲区的记录转于到付款区
				TransferPrecord_CAreaToWArea(owner,caid);
			}
			
			/*放在这里合适吗？是否应该放在启动对账操作前一刻*/
			ResetOrider(owner);
			ResetBinputs(owner);
			ResetCustom(owner);
			/*放在这里合适吗？是否应该放在启动对账操作前一刻*/
		}
		//往对账结果历史表中插入一条新的记录
		
		return caid;
	}
	
	/**
	 * ResetCustomToCaDuring 重置客户表为上传前状态
	 * @param owner
	 */
	public void ResetCustomToCaDuring(String owner){
		//重置客户表的input,contract_num,contract_mes字段
		List<CusSecondstore> lcustoms = sDao.GetCustomTb(owner);
		for (int i = 0; i < lcustoms.size(); i++) {
			CusSecondstore resetcustoms = lcustoms.get(i);
			resetcustoms.setContractNum(0);
			resetcustoms.setContractMes(null);
			resetcustoms.setInput(0d);
			resetcustoms.setUpdateTime(null);
			sDao.update(resetcustoms);
		}
	}
	
	/**
	 * ResetCustomAccoutMsg 重置客户表中的账号信息为上传前状态
	 * @param owner
	 */
	public void ResetCustomAccoutMsg(String owner){
		//重置客户表的input,contract_num,contract_mes字段
		List<CusSecondstore> lcustoms = sDao.GetCustomTb(owner);
		for (int i = 0; i < lcustoms.size(); i++) {
			CusSecondstore resetcustoms = lcustoms.get(i);
			resetcustoms.setAccoutMes(null);
			sDao.update(resetcustoms);
		}
	}
	
	/**
	 * ResetPayRecord 重置付款表为初始状态
	 * @param owner
	 */
	public void ResetPayRecord(String owner){
		List<PayRecord> pList = pDao.FindBySpeElement("isconnect", true,owner);
		
		for (int i = 0; i < pList.size(); i++) {
			PayRecord pRecord = pList.get(i);
			pRecord.setCheckResult(null);
			pRecord.setIsconnect(false);
			pRecord.setBankinputId(null);
			pDao.update(pRecord);
		}
	}

	/**
	 * ResetOrider 重置货款表为初始状态
	 * @param owner
	 */
	public void ResetOrider(String owner){
		List<OriOrder> fOrders = tDao.FindOriHasBInput(owner);
		
		for (int i = 0; i < fOrders.size(); i++) {
			OriOrder fOrder = fOrders.get(i);
			double input = fOrder.getInput();
			double debet = fOrder.getDebt() + input;
			input = 0d;
			fOrder.setInput(input);
			fOrder.setDebt(debet);
			fOrder.setUpdateTime(null);
			fOrder.setConnectBank(null);
			tDao.update(fOrder);
		}
	}
	
	/**
	 * ResetBinputs 重置出纳表为对账状态
	 * @param owner
	 */
	public void ResetBinputs(String owner){
		List<BankInput> bankInputs = bDao.FindBySpeElement_Big("connectNum", 0, owner);
		
		for (int i = 0; i < bankInputs.size(); i++) {
			BankInput bInput = bankInputs.get(i);
			bInput.setConnectNum(0);
			bDao.update(bInput);
		}
	}
	
	/**
	 * ResetCustom 重置客户表为对账状态
	 * @param owner
	 */
	public void ResetCustom(String owner){
		List<CusSecondstore> cList = sDao.FindBySpeElement_Big("input", 0d, owner);
		
		for (int i = 0; i < cList.size(); i++) {
			CusSecondstore custom = cList.get(0);
			custom.setInput(0d);
			custom.setUpdateTime(null);
			sDao.update(custom);
		}
	}
	
	/**
	 * TransferPrecord_WAreaToHArea 转存付款记录工作区的记录到付款历史工作区
	 * @param owner
	 */
	public void TransferPrecord_WAreaToHArea(String owner){
		//先转存付款记录到付款记录历史
		List<PayRecord> payTb = pDao.GetPrecordTbByElement("owner", owner);
		
		if (payTb.isEmpty() == true) {
			logger.warn("付款记录工作区中属于" + owner + "代理商记录为空");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecordHistory pHistory = (PayRecordHistory)JSONObject.toBean(jObject, PayRecordHistory.class);
			pHistory.setCheckResult(payTb.get(i).getCheckResult());
			pHistory.setId(payTb.get(i).getId());
			pHDao.add(pHistory);
		}
		
		pDao.DeletePrecordTbByElement("owner", owner);//清空付款记录
	}
	
	/**
	 * TransferPrecord_CAreaToWArea 从付款缓冲区取记录到付款工作区
	 * @param owner
	 * @param caid
	 */
	public void TransferPrecord_CAreaToWArea(String owner,String caid){
	//	List<PayRecordCache> payTb = pCDao.GetPayRecordsTb(owner);//取记录
		List<PayRecordCache> payTb = pCDao.GetTbByElement_Owner("caid",caid,owner);
		if (payTb.isEmpty() == true) {
			logger.warn("付款记录缓冲区中属于" + owner + "代理商记录为空");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecord pRecord = (PayRecord)JSONObject.toBean(jObject, PayRecord.class);
//			pRecord.setCaid(caid);
			pRecord.setCheckResult(payTb.get(i).getCheckResult());
			pRecord.setId(payTb.get(i).getId());
			pDao.add(pRecord);
		}
		
		pCDao.DeleteRecordsByElement("caid", caid, owner);//立马删除记录
	}
	
	/**
	 * TransferPrecord_CAreaToHArea 将付款缓冲区的不是本月的付款记录转移到付款历史区
	 */
	public void TransferPrecord_CAreaToHArea(String owner,String caid){
		List<PayRecordCache> payTb = pCDao.GetTbByElementNot_Owner("caid",caid,owner);
		if (payTb.isEmpty() == true) {
			logger.warn("付款记录缓冲区中属于" + owner + "代理商记录为空");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecordHistory pHistory = (PayRecordHistory) JSONObject.toBean(jObject, PayRecordHistory.class);
			pHistory.setCheckResult(payTb.get(i).getCheckResult());
			pHistory.setId(payTb.get(i).getId());
			pHDao.add(pHistory);
		}
		
		pCDao.DeleteTbByEleNot_OWner("caid", caid, owner);
	}
	
	/**
	 * CallBackPrecord_HAreaToWArea 从付款记录历史区取回本月的付款记录
	 * @param owner
	 * @param caid
	 */
	public void TransferPrecord_HAreaToWArea(String owner,String caid){
		//先转存付款记录到付款记录历史
		List<PayRecordHistory> payHTb = pHDao.FindBySpeElement_AND("caid", "owner", caid, owner);

		if (payHTb.isEmpty() == true) {
			logger.warn("付款历史区找不到 属于" + owner + "|" + caid + "代理商付款记录");
			return;
		}
		
		for (int i = 0; i < payHTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payHTb.get(i));
			PayRecord pRecord = (PayRecord)JSONObject.toBean(jObject, PayRecord.class);
			pRecord.setCheckResult(payHTb.get(i).getCheckResult());
			pRecord.setId(payHTb.get(i).getId());
			pDao.add(pRecord);
		}
		
		pHDao.DeletePrecordTbByElement("caid", caid, owner);
	}
	
	/**
	 * TransferOri_BackupToWarea 将备份区的货款记录转移到工作区
	 * @param owner
	 */
	public void TransferOri_BackupToWarea(String owner){
		List<OriOrderBackup> orderBackups = oUp_Dao.GetOriBackupByElment("owner", owner);
		if (orderBackups.isEmpty() == true) {
			logger.warn("货款备份区属于" + owner + "代理商的记录为空");
			return;
		}
		
		for (int i = 0; i < orderBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(orderBackups.get(i));
			OriOrder order  = (OriOrder) JSONObject.toBean(jsonObject,OriOrder.class);
			order.setConnectBank(orderBackups.get(i).getConnectBank());
			order.setCustomid(orderBackups.get(i).getCustomid());
			tDao.add(order);
		}
		
		oUp_Dao.DeleteOBackupByElement("owner", owner);//删除相应的备份货款记录，实现转移
	}
	
	/**
	 * TransferOri_WareaToBackup 转移工作区的货款记录到备份区
	 * @param owner
	 */
	public void TransferOri_WareaToBackup(String owner){
		List<OriOrder> oriOrders = tDao.GetTolAccountByElment("owner", owner);
		if (oriOrders.isEmpty() == true) {
			logger.warn("货款区属于" + owner + "代理商的记录为空");
			return;
		}
		
		for (int i = 0; i < oriOrders.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(oriOrders.get(i));
			 OriOrderBackup oBackup = (OriOrderBackup) JSONObject.toBean(jsonObject,OriOrderBackup.class);
			 oBackup.setConnectBank(oriOrders.get(i).getConnectBank());
			 oBackup.setCustomid(oriOrders.get(i).getCustomid());
			oUp_Dao.add(oBackup);
		}
		
		tDao.DeleteOoriderByElement("owner", owner);//删除货款工作区记录，实现转移
	}
	
	/**
	 * TransferBinput_BackupToWarea 转移出纳备份区的记录到工作区
	 * @param owner
	 */
	public void TransferBinput_BackupToWarea(String owner){
		List<BankInputBackup> bInputBackups = bInput_Backup_Dao.GetBInputBupByElment("owner", owner);
		if (bInputBackups.isEmpty() == true) {
			logger.warn("出纳备份区的记录为空");
			return;
		}
		
		for (int i = 0; i < bInputBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputBackups.get(i));
			BankInput bInput = (BankInput) JSONObject.toBean(jsonObject,BankInput.class);
			bInput.setPayid(bInputBackups.get(i).getPayid());
			bInput.setConnectNum(bInputBackups.get(i).getConnectNum());
			bInput.setManyContract(bInputBackups.get(i).getManyContract());
			bInput.setConnectClient(bInputBackups.get(i).getConnectClient());
			bInput.setId(bInputBackups.get(i).getId());
			bDao.add(bInput);
		}
		
		bInput_Backup_Dao.DeleteBInputBupByElement("owner", owner);//删除出纳备份区的记录，实现转移
	}
	
	/**
	 * TransferBinput_WareaToBackup 转移工作区的出纳记录到备份区
	 * @param owner
	 */
	public void TransferBinput_WareaToBackup(String owner){
		List<BankInput> bInputs = bDao.GetTolBankInsByElement("owner",owner);
		if (bInputs.isEmpty() == true) {
			logger.info("出纳备份区为空");
			return;
		}
		
		for (int i = 0; i < bInputs.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputs.get(i));
			BankInputBackup bankInputBackup = (BankInputBackup) JSONObject.toBean(jsonObject,BankInputBackup.class);
			
			bankInputBackup.setPayid(bInputs.get(i).getPayid());
			bankInputBackup.setConnectNum(bInputs.get(i).getConnectNum());
			bankInputBackup.setManyContract(bInputs.get(i).getManyContract());
			bankInputBackup.setConnectClient(bInputs.get(i).getConnectClient());
			bankInputBackup.setId(bInputs.get(i).getId());
			bInput_Backup_Dao.add(bankInputBackup);
		}
		
		bDao.DeleteBinputTbByElement("owner", owner);
	}
	
	/**
	 * TransferCus_WareaToBackup 将cus_secondstore表内容转移到backup区
	 * @param owner
	 */
	public void TransferCus_WareaToBackup(String owner){
		List<CusSecondstore> cList = sDao.GetCustomTb(owner);
		
		for (int i = 0; i < cList.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(cList.get(i));
			CusSdstoreBackup cBackup = (CusSdstoreBackup) JSONObject.toBean(jsonObject, CusSdstoreBackup.class);
			
			cBackup.setAccoutMes(cList.get(i).getAccoutMes());
			cBackup.setContractMes(cList.get(i).getContractMes());
			cBackup_Dao.add(cBackup);
		}
		
		sDao.DeleteTbByElement("owner", owner);
	}
	
	/**
	 * TransferCus_BackupToWarea 将CusSdstoreBackup内容转移到工作区CusSecondstore
	 * @param owner
	 */
	public void TransferCus_BackupToWarea(String owner){
		List<CusSdstoreBackup> cBackups = cBackup_Dao.GetTbByElment("owner", owner);
		
		for (int i = 0; i < cBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(cBackups.get(i));
			CusSecondstore secondstore = (CusSecondstore) JSONObject.toBean(jsonObject, CusSecondstore.class);
			
			secondstore.setAccoutMes(cBackups.get(i).getAccoutMes());
			secondstore.setContractMes(cBackups.get(i).getContractMes());
			sDao.add(secondstore);
		}
		
		cBackup_Dao.DeleteTbByElement("owner", owner);
	}

	/*进入上传页对账重复性检测*/
	public void isCaCover(char chooser,String caid,String owner){
		if (chooser == 'Y') {
			TransferPrecord_HAreaToWArea(owner,caid);//从付款记录历史区取回付款记录
			CaresultHistory cHistory = cDao.FindBySpeElement("caid", caid, owner).get(0);//将对账id为caid的对账结果记录的caresult字段修改为D
			
			//前端进入上传页面
		}
		else{
			//前端页面保持不动
		}
	}
	
	/**
	 * DealAfterCaSucces 对账成功后的处理工作
	 * @param owner
	 * @param caid
	 * @param caresult_url
	 */
	public void DealAfterCaSucces(String owner,String caid,String caresult_url){
			
	//	TransferPrecord_WAreaToHArea(owner);//转存付款记录到付款记录历史
		
		CaresultHistory cHistory = (CaresultHistory) cDao.FindBySpeElement("caid", caid, owner).get(0);//根据对账id找到相应的对账结果记录
		cHistory.setCaresult('F');//修改对账结果记录的结果字段为F
		cHistory.setUrl(caresult_url);//对账结果excel表url
		cDao.update(cHistory);
		//前台进入查看对账结果
	}

	/**
	 * CancelAndCaAgain 取消并重新对账 
	 * @param owner 代理商id
	 * @param caid 对账id
	 */
	public void CancelAndCaAgain(String owner,String caid){
		logger.info("进入取消并重新对账处理");
		
//		TransferPrecord_CAreaToWArea(owner, caid);//根据对账id从付款缓冲中取记录
		
		CaresultHistory cHistory = (CaresultHistory) cDao.FindBySpeElement("caid", caid, owner).get(0);//根据对账id找到相应的对账结果记录
		cHistory.setCaresult('D');//修改对账结果记录的结果字段为F
		cDao.update(cHistory);
		
		ResetOrider(owner);//重设货款表信息
		ResetBinputs(owner);//重设出纳信息
		ResetCustom(owner);//重设客户信息
		
		//前端进入上传页面
		logger.info("前端进入上传页面");
	}
	
	/*从历史对账结果中重新对账*/
	public int HisCancelAndCaAgain(String owner,String caid,String savedirA,String savedirB,String filenameA,String filenameB){
		
		List<CaresultHistory> fHLasts = cDao.FindBySpeElement("lastcaid", true, owner);
		if (fHLasts == null || fHLasts.size() == 0) {
			logger_error.error("无法查找上次对账");
			return -1;
		}
		CaresultHistory	fHLast = fHLasts.get(0);
		
		String curmonthcaid = CreateCaid(owner);
		if (fHLast.getCaid().equals(curmonthcaid) == false) {//从历史对账中再次跳转到历史对账
			logger_error.error("操作非法，不允许从历史对账中跳转到历史对账");
			return -2;
		}
		
		if (caid.equals(curmonthcaid) == true) {//选择的历史月份为本月
			logger.info("选择的历史月份为本月");
			return 1;
		}
		
		cBackup_Dao.DeleteTbByElement("owner", owner);
		TransferCus_WareaToBackup(owner);//将客户信息转移到备份区
		
		TransferOri_WareaToBackup(owner);//转移货款工作表到备份区
		
		TransferBinput_WareaToBackup(owner);//转移出纳工作表到备份区
		
		//读服务器目录中的货款和出纳excel，重新载入信息
		File fileA = new File(savedirA + "/" + filenameA);
		if (!fileA.exists()) {
			logger.warn("货款表" + filenameA + "不存在，请重新导入");
		}
		else{
			//ResetCustomToCaDuring(owner);//重置客户表
			
			Excel_RW excel_RW = new Excel_RW();//解析excel内容
			ArrayList<Excel_Row> totalA_table = excel_RW.ReadExcel_Table(savedirA + "/" + filenameA);
	
			Agent fagent = agent_Dao.findById(Agent.class, owner);
			ArrayList<OriOrder> in_orders = excel_RW.Table_To_Ob_OriOrders(totalA_table,fagent);//将excel表转换成对象
			/*解析excel表内容*/
			
			/*写入数据库*/
			for (int i = 0; i < in_orders.size(); i++) {
				OriOrder in_order = in_orders.get(i);
				
				/*填补对账联系人信息*/
				List<ConnectPerson> fPersons = cPerson_Dao.FindBySpeElement("companyid", in_order.getCuscompanyid(), owner);
				if (fPersons.isEmpty() !=  true) {
					in_order.setCustomname(fPersons.get(0).getRealName());
					in_order.setCustomphone(fPersons.get(0).getPhone());
					in_order.setCustomweixin(fPersons.get(0).getWeixin());
				}
				/*填补对账联系人信息*/
				
				/*填补代理商财务信息*/
				Agent agent = agent_Dao.findById(Agent.class, owner);
				in_order.setAsname(agent.getAgentConnectpname());
				in_order.setAsphone(agent.getAgentCpphone());
				in_order.setAsemail(agent.getAgentCpemail());
				/*填补代理商财务信息*/

				tDao.add(in_order);
				ConnectAccountWithCustom(in_order);
			}
			/*写入数据库*/
		}
		File fileB = new File(savedirB + "/" + filenameB);
		if (!fileB.exists()) {
			logger.warn("出纳表" + filenameB + "不存在，请重新导入");
		}
		else{
			ResetCustomAccoutMsg(owner);
			/*解析excel文件*/
			Excel_RW excel_RW = new Excel_RW();//解析excel内容
			ArrayList<Excel_Row> totalA_table = excel_RW.ReadExcel_Table(savedirB + "/" + filenameB);
			ArrayList<BankInput> bankInputs = excel_RW.Table_To_Ob_BankIn(totalA_table,owner);
			/*解析excel文件*/
			
			/*写入数据库*/
			for (int i = 0; i < bankInputs.size(); i++) {
				BankInput bankInput = bankInputs.get(i);
				bDao.add(bankInput);
				ConnectBankinputWithCustom(bankInput);//获取客户的合同信息
			}
			/*写入数据库*/
		}
		
		TransferPrecord_WAreaToHArea(owner);//将付款工作区的记录转移到历史区
		
		TransferPrecord_HAreaToWArea(owner, caid);//将历史区的caid记录转移到付款工作区
		
		CancelAndCaAgain(owner,caid);
		
		fHLast.setLastcaid(false);
		cDao.update(fHLast);
		
		CaresultHistory fHCur = cDao.FindBySpeElement("caid", caid, owner).get(0);
		fHCur.setLastcaid(true);
		cDao.update(fHCur);
		return 0;
	}
	
	public void Test_MappPayToBank(List<BankInput> mappedBankInput){
		System.out.println("result [MappPayToBank] is :");
		for(BankInput iBankInputs:mappedBankInput){
			System.out.println(iBankInputs.getPayer() + ":" + iBankInputs.getMoney() + ":" + iBankInputs.getPayee());
		}
	}

}
