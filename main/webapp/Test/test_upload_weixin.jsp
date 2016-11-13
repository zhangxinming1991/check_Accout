<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>测试微信支付</title>
</head>
<body>
	测试微信支付<br>
	<form action="/check_Accout/ConnectP_Pay_Controller/upload_pay_weixin" method="post">
		付款凭证来源<select name="imageUrl">
		  <option value="https://api.weixin.qq.com/cgi-bin/media/get?access_token=7ZRZJfWZUARgB5qCLEHNOizvkteUXNUYHlGijbaT0PoIVRPxbb_mqQ9zGpE_3KkoEwuMCBNYv_4bcZuedBc0mkZwoHVU7HnVcEuhL0-4p3f7vXnJmbD-foPe-x7wpD5JFILaADAILO&media_id=6EUhkKyJ7nh5fTa1Ut-of6mVew6H5BOpo46qRFLvRC4G4W1qEO0O5E4t85Q7UpbA">微信接口</option>
		  <option value="http://119.29.235.201:8080/check_Accout/ConnectP_Pay_Controller/test_readfile">公网服务器接口</option>
		</select><br>
		<input type="hidden" name="">
		 用户名:<input type="text" name="username" value="denghang"><br>
		 付款金额:<input type="text" name="pay_money" value="1000"><br>
		付款方式:<input type="text" name="pay_way" value="现金"><br>
		付款账号:<input type="text" name="pay_account" value="22222"><br> 
		收款人:<input type="text" name="receiver" value="张三"><br>
		所属代理商:<input type="text" name="owner" value="gd0001"><br>
		<input type="submit" name="提交">
	</form>
</body>
</html>