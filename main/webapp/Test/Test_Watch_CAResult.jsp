<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试查看对账结果</title>
</head>
<body>
	Test_Watch_CheckAresult<br>
	<form name="Test_WatchCAResult_form" action="/check_Accout/Check_MainController/Watch_CheckA_Result" method ="post">
		<input type="radio" name="watch_type" value="bfailconnect">查看无法关联的出纳记录<br>
		<input type="radio" name="watch_type" value="btocontract">查看关联到合同号的出纳记录<br>
		<input type="radio" name="watch_type" value="btoclient">查看关联到客户名下的出纳记录<br>
		<input type="radio" name="watch_type" value="bnopay">没有关联的付款信息的出纳记录<br>
		<input type="radio" name="watch_type" value="bhaspay">关联到付款信息的出纳记录<br>
		<input type="radio" name="watch_type" value="phasbinput">关联到出纳信息的付款记录<br>
		<input type="radio" name="watch_type" value="truepnobinput">没有关联到出纳信息的真实付款记录<br>
		<input type="radio" name="watch_type" value="falsepnobinput">没有关联到出纳信息的无用付款记录<br>
		<input type="radio" name="watch_type" value="onobinput">本月没有收到付款的货款记录<br>
		<input type="radio" name="watch_type" value="ohasbinput">本月有收到付款的货款记录<br>
		<input type="submit" name"test" value="Test_WatchCAResult">
	</form>
</body>
</html>