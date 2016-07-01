<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>白名单配置</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="/static/css/bootstrap.css" rel="stylesheet" media="screen"/>
    <script src="//cdn.jsdelivr.net/webjars/jquery/2.2.1/jquery.min.js"></script>
</head>

<body>
<div class="container bs-docs-container">
    <h2 class="page-header">增加白名单</h2>
    <form name="form1" method="post" autocomplete="on">
        <div class="form-group">
            <label for="app">app</label>
            <input type="text" id="app" class="form-control" name="app" size="60" placeholder="app的名字. 譬如 bbae-web-api" value="${app}" />
        </div>
        <div class="form-group">
            <label for="env">env</label>
            <input type="text" id="env" class="form-control" name="env" size="60" placeholder="env的名字, 譬如 production" value="${env}" />
        </div>

        <div class="form-group">
            <button type="button" class="btn btn-primary" onclick="add()">添加 </button>
        </div>
    </form>

    <h3 class="page-header">现有的白名单</h3>
    <table class="table table-hover table-responsive table-bordered">
        <tr>
            <th>id</th>
            <th>app</th>
            <th>env</th>
            <th>status</th>
            <th>操作</th>
        </tr>
        <c:forEach var="filter" items="${filterList}">
            <tr>
                <td>${filter.id}</td>
                <td>${filter.app}</td>
                <td>${filter.env}</td>
                <td><c:if test="${filter.status == 0}">有效</c:if><c:if test="${filter.status == 1}">无效</c:if></td>
                <td><c:if test="${filter.status == 0}"><button>无效</button></c:if><c:if test="${filter.status == 1}"><button>有效</button></c:if></td>
            </tr>

        </c:forEach>

    </table>
</div>
<br/>
<script language="JavaScript">
    function invoke() {
        var ip = document.getElementById("ip");
        var port = document.getElementById("port");
        var service = document.getElementById("service");
        var method = document.getElementById("method");
        var params = document.getElementById("params");
        if (ip.value == '' || port.value == '' || service.value == '' || method.value == '' || params.value == '') {
            alert('提交内容不能为空！');
        } else {
            document.form1.action = "invoke.do";
            document.form1.submit();
        }
    }
    function saveRequest() {
        var ip = document.getElementById("ip");
        var port = document.getElementById("port");
        var service = document.getElementById("service");
        var method = document.getElementById("method");
        var params = document.getElementById("params");
        if (ip.value == '' || port.value == '' || service.value == '' || method.value == '' || params.value == '') {
            alert('提交内容不能为空！');
        } else {
            document.form1.action = "saveRequest.do";
            document.form1.submit();
        }
    }
    function getRequest(i, p, s, m, ps) {
        var ip = document.getElementById("ip");
        ip.value = i;
        var port = document.getElementById("port");
        port.value = p;
        var service = document.getElementById("service");
        service.value = s;
        var method = document.getElementById("method");
        method.value = m;
        var params = document.getElementById("params");
        params.value = ps;
    }
    function resetForm() {
        ip.value = "";
        port.value = "";
        service.value = "";
        method.value = "";
        params.value = "";
    }
</script>
</body>
</html>