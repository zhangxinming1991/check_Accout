<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试后台管理查看</title>
</head>
<body>
	测试后台管理查看<br>
	<form name="Test_WatchCAResult_form" action="/check_Accout/PMController/watch" method ="post">
		<input type="radio" name="watch_type" value="reg_cp">查看新注册的对账联系人<br>
		<input type="radio" name="watch_type" value="reg_as">查看新注册的财务人员<br>
		<input type="radio" name="watch_type" value="reged_cp">查看已注册的对账联系人<br>
		<input type="radio" name="watch_type" value="reged_as">查看已注册的财务人员<br>
		<input type="radio" name="watch_type" value="op_log">查看日志信息<br>
		<input type="submit" name="test" value="test">
	</form>
</body>
</html>