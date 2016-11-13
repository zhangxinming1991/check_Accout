<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
    <div>
        <form name="connectp_login_form" action="/check_Accout/ConnectP_LoginController" method ="post">
        	用户名： <input type="text" name="username"><br/>
        	密码：<input type="password" name="password"><br/>
        	<input type="hidden" name="loginway" value="bc">
			<input type="submit" value="SUBMIT"><br>
    </div>
</body>
</html>