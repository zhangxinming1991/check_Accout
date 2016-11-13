<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试对账联系人注册审阅</title>
</head>
<body>
	<form action="/check_Accout/PMController/verify_register">
		审阅对账联系人的用户名<select name="username">
			<option value="denghang">denghang</option>
		</select><br>
		审阅<select name="regflag">
			<option value="-2">否定</option>
			<option value="0">通过</option>
		</select><br>
		注册类型<input type="text" name="reg_type" value="cp"><br>
		<input type="submit" name="submit" value="提交"><br>
	</form>
</body>
</html>