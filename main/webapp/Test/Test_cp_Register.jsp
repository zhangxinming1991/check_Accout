<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试对账联系人注册</title>
</head>
<body>
<form action="/check_Accout/PMController/conectp_register">
	用户名:<input type="text" name="username" value="denghang"><br>
	真实姓名:<input type="text" name="real_name" value="邓航"><br>
	电话:<input type="text" name="phone" value="17355742908"><br>
	个人身份证<input type="text" name="cardid" value="41018319731212200">
	邮箱:<input type="text" name="email" value="13487924@qq.com"><br>
	微信:<input type="text" name=weixin value="denghang9012"><br>
	注册形式：<select name="register_way">
		<option value="P">个人</option>
		<option value="C">公司</option>
	</select><br>
	个人/公司名称:<input type="text" name="company" value="邓航"><br>
	身份证号码/组织机构代码证:<input type="text" name="companyid" value="410183197312122000"><br>
	交易合同号:<input type="text" name="contract_mes" value="KFZL2011-654"><br>
	所属代理商：<select name="agent">
		<option value="gd0001">广东代理商</option>
		<option value="ah0001">安徽代理商</option>
	</select><br>
	密码:<input type="password" name="password" value="1234"><br>
	<input type="submit" name="submit" value="提交"><br>
</form>

</body>
</html>