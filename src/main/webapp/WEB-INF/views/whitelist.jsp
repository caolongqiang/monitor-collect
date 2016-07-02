<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
</head>
<head>
    <title>白名单配置</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="/static/css/bootstrap.css" rel="stylesheet" media="screen"/>
    <link href="/static/css/bootstrap-table.css" rel="stylesheet" media="screen"/>
    <script src="/static/js/jquery-2.2.2.min.js"></script>
    <script src="/static/js/bootstrap-table.js"></script>
    <script src="/static/js/bootstrap.min.js"></script>
</head>

<body>
<div class="container bs-docs-container">
    <h2 class="page-header">增加白名单</h2>
    <form name="form1" method="post" autocomplete="on">
        <div class="form-group">
            <label for="app">app</label>
            <input type="text" id="app" class="form-control" name="app" size="60" placeholder="app的名字. 譬如 bbae-web-api"
                   value="${app}"/>
        </div>
        <div class="form-group">
            <label for="env">env</label>
            <input type="text" id="env" class="form-control" name="env" size="60" placeholder="env的名字, 譬如 production"
                   value="${env}"/>
        </div>

        <div class="form-group">
            <button type="button" class="btn btn-primary" id="addFilter">添加配置</button>
        </div>
    </form>

    <h3 class="page-header">现有的白名单</h3>

    <div class="bootstrap-table">
        <table id="all_whitelist">

        </table>
    </div>


    <hr/>

    <dl>
        <dt>
            <button type="button" class="btn btn-danger" id="reloadWhiteList">重新load白名单配置</button>
        </dt>
        <dt><a href="/monitor/showFileGroupList.j" target="_blank">展示现在的文件配置</a></dt>
        <dt><a href="/monitor/reloadFileConfig.j" target="_blank">重新load文件配置</a></dt>
        <dt><a href="/monitor/showEtcdGroupList.j" target="_blank">展示所有的ETCD里的配置</a></dt>
        <dt><a href="/monitor/reloadEtcdConfig.j" target="_blank">重新读取所有etcd里的配置</a></dt>
        <dt><a href="/monitor/showEtcdWorkingConfig.j" target="_blank">展示所有的ETCD里的正在抓取配置</a></dt>
    </dl>

</div>
<br/>
<script language="JavaScript">

    $(document).ready(function () {
        $('#addFilter').click(function () {

            $.post("/monitor/insertFilter.j",
                    {
                        app: $('#app').val(),
                        env: $('#env').val()
                    },
                    function (data, status) {
                        alert(data.msg);
                        $('#all_whitelist').bootstrapTable('refresh');
                    });

        });

        $('#reloadWhiteList').click(function () {

            $.get("/monitor/reloadWhiteList.j",
                    function (data, status) {
                        alert(data.msg);
                    });

        });
    });

    $(function () {

        //1.初始化Table
        var oTable = new TableInit();
        oTable.Init();

        //2.初始化Button的点击事件
        var oButtonInit = new ButtonInit();
        oButtonInit.Init();

        // 加上crsf.
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });

    });


    var TableInit = function () {
        var oTableInit = new Object();
        //初始化Table
        oTableInit.Init = function () {
            $(function () {

                //1.初始化Table
                var oTable = new TableInit();
                oTable.Init();

                //2.初始化Button的点击事件
                var oButtonInit = new ButtonInit();
                oButtonInit.Init();

            });


            var TableInit = function () {
                var oTableInit = new Object();
                //初始化Table
                oTableInit.Init = function () {
                    $('#all_whitelist').bootstrapTable({
                        url: '/monitor/queryWhiteList.j',         //请求后台的URL（*）
                        method: 'get',                      //请求方式（*）
                        //toolbar: '#toolbar',                //工具按钮用哪个容器
                        striped: true,                      //是否显示行间隔色
                        cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                        pagination: true,                   //是否显示分页（*）
                        sortable: false,                     //是否启用排序
                        sortOrder: "asc",                   //排序方式
                        queryParams: oTableInit.queryParams,//传递参数（*）
                        sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                        pageNumber: 1,                       //初始化加载第一页，默认第一页
                        pageSize: 100,                       //每页的记录行数（*）
                        pageList: [100, 300, 500, 1000],        //可供选择的每页的行数（*）
                        columns: [{
                            checkbox: true
                        }, {
                            field: 'id',
                            title: 'id'
                        }, {
                            field: 'app',
                            title: 'app名字'
                        }, {
                            field: 'env',
                            title: '环境'
                        }, {
                            field: 'status',
                            title: '状态',
                            align: 'center',
                            formatter: statusFormatter

                        }, {
                            field: 'operate',
                            title: '操作',
                            align: 'center',
                            events: operateEvents,
                            formatter: operateFormatter
                        }]
                    });
                };

                //得到查询的参数
                oTableInit.queryParams = function (params) {
                    var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                        limit: params.limit,   //页面大小
                        offset: params.offset,  //页码
                    };
                    return temp;
                };
                return oTableInit;
            };


            var ButtonInit = function () {
                var oInit = new Object();
                var postdata = {};

                oInit.Init = function () {
                    //初始化页面上面的按钮事件
                };

                return oInit;
            };
        };

        //得到查询的参数
        oTableInit.queryParams = function (params) {
            var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                limit: params.limit,   //页面大小
                offset: params.offset  //页码
            };
            return temp;
        };
        return oTableInit;
    };


    var ButtonInit = function () {
        var oInit = new Object();
        var postdata = {};

        oInit.Init = function () {
            //初始化页面上面的按钮事件
        };

        return oInit;
    };

    //初始化Table

    function statusFormatter(value, row, index) {
        if (row.status == 1) {
            return "无效";
        } else {
            return "有效";
        }
    }


    function operateFormatter(value, row, index) {
        if (row.status == 0) {
            return "<button class=\"btn btn-danger remove\">废除</button>";
        } else {
            return "<button class=\"btn btn-success reworking\">有效</button>";
        }
    }

    window.operateEvents = {
        'click .remove': function (e, value, row, index) {
            $.post("/monitor/updateFilter.j",
                    {
                        id: row.id,
                        status: 1
                    },
                    function (data, status) {
                        alert(data.msg);
                        $('#all_whitelist').bootstrapTable('refresh');
                    });
        },
        'click .reworking': function (e, value, row, index) {
            $.post("/monitor/updateFilter.j",
                    {
                        id: row.id,
                        status: 0
                    },
                    function (data, status) {
                        alert(data.msg);
                        $('#all_whitelist').bootstrapTable('refresh');
                    });
        }
    };


</script>
</body>
</html>