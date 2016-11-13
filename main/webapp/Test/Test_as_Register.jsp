<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试代理商财务注册</title>
</head>
<body>
<form action="/check_Accout/PMController/as_register">
	用户名:<input type="text" name="username" value="z1234"><br>
	真实姓名:<input type="text" name="name" value="张小明"><br>
	电话:<input type="text" name="phone" value="17355704249"><br>
	邮箱:<input type="text" name="email" value="15587924@qq.com"><br>
	代理商：<select name="agentid">
		<option value="gd0001">广东代理商</option>
		<option value="ah0001">安徽代理商</option>
		<option value="zh0001">中宏</option>
		<option value="zf0001">中发</option>
		<option value="kf0001">康富</option>
	</select><br>
	密码:<input type="password" name="password" value="1234"><br>
	<input type="submit" name="submit" value="提交"><br>
</form>

</body>
</html>