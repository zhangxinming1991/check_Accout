<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试查找匹配</title>
</head>
<body>
	Test_Map<br>
	<form name="Test_Map_form" action="/check_Accout/Check_MainController/Map" method ="post">
		pay_id:<input type="text" name="pay_id" value="7"><br>
		<input type="hidden" name="map_op" value="find_map"><br>
		<input type="submit" name"test" value="test_map">
	</form>
	
</body>
</html>