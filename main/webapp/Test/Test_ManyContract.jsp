<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>多合同付款测试</title>
</head>
<body>
	<form name="Test_Map_form" action="/check_Accout/Check_MainController/Test_ManyContractPay" method ="post">
		<input type="hidden" name="many_pay" value="[{\"contract_num\":\"0001\",\"pay_money\":12},{\"contract_num\":\"0002\",\"pay_money\":13}]">
		<input type="submit" name"test" value="test_ManyContractPay">
	</form>
</body>
</html>