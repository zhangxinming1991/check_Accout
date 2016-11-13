package check_Asys;

import java.util.List;

import com.sun.org.apache.regexp.internal.recompile;

import check_Asys.CheckAcManage.Dao_List;
import dao.BankInput_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import entity.BankInput;
import entity.OriOrder;
import entity.PayRecord;

/**
 * CheckARseult 产生不同角度的对账结果
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class CheckARseult {
	
	public Dao_List dao_List;
		
	public CheckARseult(Dao_List dao_List) {
		this.dao_List = dao_List;
	}
	
	//关联到合同的出纳记录
	public List<BankInput> Produce_BInputToContract(String owner){
		List<BankInput> BtoContractlist = dao_List.bDao.FindBtoContract(owner);
		return BtoContractlist;
	}
	
	//关联到客户的出纳记录
	public List<BankInput> Produce_BInputToClient(String owner){
		List<BankInput> BtoClientlist = dao_List.bDao.FindBtoClient(owner);
		return BtoClientlist;
	}
	
	//无法关联的出纳记录
	public List<BankInput> Produce_BInputFailConnect(String owner){
		List<BankInput> FailBlist = dao_List.bDao.FindFailConnectBinputs(owner);
		return FailBlist;
	}
	
	//没有关联到手机付款信息的出纳记录
	public List<BankInput> Produce_BInputNoPayRecord(String owner){
		List<BankInput> BnoPlist = dao_List.bDao.FindBToPay(false,owner);
		return BnoPlist;
	}
	
	//关联到手机付款信息的出纳记录
	public List<BankInput> Produce_BInputHasPayRecord(String owner){
		List<BankInput> BHasPlist = dao_List.bDao.FindBToPay(true,owner);
		return BHasPlist;
	}
	
	//关联到出纳的付款记录
	public List<PayRecord> Produce_PayHasBInput(String owner) {
		List<PayRecord> payHasBlist = dao_List.pDao.FindPayToBInput(true,owner);
		return payHasBlist;
	}
	
	//无法关联的真实付款记录
	public List<PayRecord> Produce_TruePayNoBInput(String owner) {
		List<PayRecord> payHasBlist = dao_List.pDao.FindPayNoBInput('w',owner);
		return payHasBlist;
	}
	
	//无法关联的无用付款记录
	public List<PayRecord> Produce_FalsePayNoBInput(String owner) {
		List<PayRecord> payHasBlist = dao_List.pDao.FindPayNoBInput('n',owner);
		return payHasBlist;
	}
	
	//没有收到付款的货款记录
	public List<OriOrder> Produce_OriorderNoBInput(String owner){
		List<OriOrder> orinoBlist = dao_List.tDao.FindOriNoBInput(owner);
		return orinoBlist;
	}
	
	//收到付款的货款记录
	public List<OriOrder> Produce_OriorderHasBInput(String owner){
		List<OriOrder> orinoBlist = dao_List.tDao.FindOriHasBInput(owner);
		return orinoBlist;
	}
	
	public void Test_ResultB(String result_type,List<BankInput> test_list){
		System.out.println("/*test" + result_type + "start*/");
		if (test_list == null) {
			System.out.println(result_type + " is null");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		
		if (test_list.size() == 0) {
			System.out.println(result_type + " size is 0");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		for(int i = 0;i<test_list.size();i++){
			System.out.println(test_list.get(i).getPayer() + ":" + test_list.get(i).getMoney());
		}
		System.out.println("/*test" + result_type + "end*/");
	}
	
	public void Test_ResultP(String result_type,List<PayRecord> test_list){
		System.out.println("/*test" + result_type + "start*/");
		if (test_list == null) {
			System.out.println(result_type + " is null");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		
		if (test_list.size() == 0) {
			System.out.println(result_type + " size is 0");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		for(int i = 0;i<test_list.size();i++){
			System.out.println(test_list.get(i).getPayer() + ":" + test_list.get(i).getPayMoney() + ":" + test_list.get(i).getReceiver());
		}
		System.out.println("/*test" + result_type + "end*/");
	}
	
	public void Test_ResultO(String result_type,List<OriOrder> test_list){
		System.out.println("/*test" + result_type + "start*/");
		if (test_list == null) {
			System.out.println(result_type + " is null");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		
		if (test_list.size() == 0) {
			System.out.println(result_type + " size is 0");
			System.out.println("/*test" + result_type + "end*/");
			return;
		}
		for(int i = 0;i<test_list.size();i++){
			System.out.println(test_list.get(i).getClient() + ":" + test_list.get(i).getOrderNum() + ":" + test_list.get(i).getDebt());
		}
		System.out.println("/*test" + result_type + "end*/");
	}

}
