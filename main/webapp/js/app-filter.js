// 将2016-08格式的年月转为“2016年08月”这种对用户更友好的显示
app.filter('friendlyMonth', function () {
    return function (m) {
        var dash = m.indexOf('-');
        return m.substr(0, dash) + '年' + parseInt(m.substr(dash + 1)) + '月';
    }
});

// 映射状态代码为中文
app.filter('ctrlStatusFilter', function () {
    return function (flag) {
        switch (flag) {
            case 0:
                return '正常';
            case -3:
                return '锁定';
            default:
                console.warn('未匹配的管控状态：' + flag);
                return '';
        }
    }
});

app.filter('notifStatusPrinter',function () {
    return function translateNotifyStatus(statusCode) {
        switch (statusCode) {
            case 'Y':
                return '已通过';
            case 'N':
                return '被否决';
            case 'W':
                return '待定中';
            case 'M':
                return '已通过';
            case 'V':
                return '无';
            default:
                return '';
        }
    }
});

app.filter('userRolePrinter', function () {
    return function (rolecode) {
        switch (rolecode){
            case "bu":
                return '代理商财务';
            case 'bm':
                return '管理员';
            default:
                return '其他';
        }
    };
});