<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>
    <div>
        <form name="login_form" action="/check_Accout/LoginController/login" method ="post">
        	工号： <input type="text" name="workId"><br/>
        	密码：<input type="password" name="password"><br/>
			<select name="usertype">
		  		<option value="U">对账人员</option>
		  		<option value="S">管理员</option>
			</select>
			<input type="submit" value="SUBMIT"><br>
    </div>
</body>