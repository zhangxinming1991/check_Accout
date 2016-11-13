<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<title>数据导入</title>
    <style type="text/css">
    </style>
 
 
<div align="right"> 
    <form action="/check_Accout/Check_MainController/upload" method="get" enctype="multipart/form-data">  
    	<h5>hello,${assis.getName()}</h5>
    	<h5>Workid:${assis.getWorkId()}</h5>
    	<h5>登录时间:${time}</h5>
        <table class="table" id="queryCondition">
            <tbody class="tbd">
            <tr>
                <td align="right" style="padding-right: 2px">
                    <input type="file" name="myfiles" id="myfiles" style="display: none;" onchange="document.getElementById('filePath').value=this.value">
                    <div class="input-group">
                        <input type="'text'" name="'filePath'" id="'filePath'" class="'form-control'/"> 
                        <span class="input-group-btn">
                            <button type="button" class="btn btn-sm btn-info blue" id="btn_check">
                                <i class="icon-edit">请选择文件</i>
                            </button>
                        </span>
                     
                </div></td>
                <td align="left" style="padding-left: 2px">
                    <button type="submit" class="btn btn-sm btn-info" id="upload">
                        <i class="upload-icon icon-cloud-upload bigger-110">导入</i>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
        <h5>${upload_state}</h5>
    </form>
  
<script type="text/javascript">
    $(function() {
        $("#btn_check").click(function() {
            $("#myfiles").trigger('click');
        });
        $("#filePath").click(function() {
            $("#myfiles").trigger('click');
        });
    });
</script>
 
</div>
</html>