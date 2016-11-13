<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="/check_Accout/Test_Page">
		<input type="text" name="input_1" value="hello">
		<input type="submit" value="测试">
		<h5><%=request.getAttribute("input_2")%></h5>
	</form>
</body>
</html>