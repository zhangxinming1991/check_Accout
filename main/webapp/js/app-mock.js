// var sc = document.getElementsByTagName('script');
// console.log(sc.item(sc.length - 1))

console.log('mock backend...');

document.write('<script src="js/angular-mocks.js"></script>');

// add requirement
app.requires.push('ngMockE2E');

// var delay = 2000;
var delay = 0;
app.config(function ($provide) {
    $provide.decorator('$httpBackend', function ($delegate) {
        var proxy = function (method, url, data, callback, headers) {
            var encrypedReq = method === 'POST';
            if (encrypedReq) {
                // console.log('request data(before decrypted):', data);
                data = Decrypt(data);
                console.log('request data(decrypted):', data);
            }
            var interceptor = function () {
                var _this = this,
                    _arguments = arguments;
                // arguments: status, data, ?, ?
                if (encrypedReq) {
                    console.log('response data(before encrypted)', arguments[1]);
                    arguments[1] = Encrypt(/*JSON.stringify*/(arguments[1]));
                    // console.log('response data(encrypted)', arguments[1]);
                }

                setTimeout(function () {
                    callback.apply(_this, _arguments);
                }, delay);
            };
            return $delegate.call(this, method, url, data, interceptor, headers);
        };
        for (var key in $delegate) {
            proxy[key] = $delegate[key];
        }
        return proxy;
    });
});

app.run(['$httpBackend', '$timeout', '$q', function (bkd, timeout, Q) {
    function uuid(len, radix) {
        var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
        var uuid = [], i;
        radix = radix || chars.length;

        if (len) {
            // Compact form
            for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
        } else {
            // rfc4122, version 4 form
            var r;

            // rfc4122 requires these characters
            uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
            uuid[14] = '4';

            // Fill in random data. At i==19 set the high bits of clock sequence as
            // per rfc4122, sec. 4.1.5
            for (i = 0; i < 36; i++) {
                if (!uuid[i]) {
                    r = 0 | Math.random() * 16;
                    uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                }
            }
        }
        return uuid.join('');
    }

    // pass through any html file requests
    bkd.whenGET(/.(html)$/).passThrough();

    function failOrNot(failProb) {
        if (!(failProb >= 0 && failProb <= 1)) { // 包括failProb为undefined情况
            failProb = 0.1;
        }
        // 以0.1的概率失败
        return Math.random() < failProb ? -1 : 0;
    }

    var session = {};

    /**
     * 返回a到b的随机数，包含二者
     * @param a
     * @param b
     * @returns {*}
     */
    function randInRange(a, b) {
        if (arguments.length == 1) {
            b = a;
            a = 0;
        }
        return Math.round(Math.random() * (b - a)) + a;
    }

    function randIn(items, undefinedAllowed) {
        return items[randInRange(0, undefinedAllowed ? items.length : ( items.length - 1))];
    }

    // sign in
    bkd.whenPOST(ReqUrl.signIn).respond(function (method, url, reqBody) {
        // data is a string of request
        reqBody = JSON.parse(reqBody);
        console.log('mock backend->login: ', reqBody);

        var resUser = {
            "uid": reqBody.uid,
            "role": reqBody.from,
            "name": '三毛' + reqBody.uid
            // more info
        };
        var uid = reqBody.uid;
        // if (uid.indexOf('u') != -1) {
        //     resUser.role = 'bu';
        //     resUser.name='财务员'
        // } else
        // if (uid.indexOf('m') != -1) {
        //     resUser.role = 'bm';
        //     resUser.name = '总监';
        // }
        // else if (uid.indexOf('s') != -1) {
        //     resUser.role = 'bs';
        //     resUser.name = '管理员';
        // }
        // else {
        //     resUser.role = 'bu';
        //     resUser.name = '财务员'
        // }

        if (uid == '403')
            return [403, {}, {}];
        if (uid == 'fail')
            return [200, {flag: -1, errmsg: '用户名或密码错误'}];

        var resdata = {
            flag: failOrNot(),
            token: 'token-abc',
            user: resUser,
            // isnewpay: randInRange(0, 9) ? 1 : 0,
            // newpay_num: randInRange(1, 15)
        };

        return [200, resdata, {}];
    });

    // sign up
    bkd.whenPOST(ReqUrl.signUp).respond(function (method, url, reqBody) {
        reqBody = JSON.parse((reqBody));
        console.log('mock backend-> register: ', reqBody);

        return [200, {flag: failOrNot(), user: {uid: 'gg'}}];
    });

    // sign out
    bkd.whenPOST(ReqUrl.signOut).respond(function () {
        return [200, {}, {}];
    });


    // // get months
    // bkd.whenGET(ReqUrl.fwOrderMonths).respond(function () {
    //     console.log('mock backend-> get months');
    //     return [200, {flag: 0, data: ['2016-08', '2016-09', '2016-10']}, {}];
    // });


    function orders(itemsLimit) {

        var resdata = [];
        var t =
        {
            "orderNum": "合同号好长好长好长……",
            "input": 1000,
            "debt": 8000,
            "total": 10000,
            "client": "中国铁路",
            "state": "对账中",
            "updateTime": "2016-8-20",
            "remark": "无"
        };
        if (!itemsLimit)itemsLimit = 20;
        for (var i = 0; i < itemsLimit; i++) {
            var tc = angular.copy(t);
            tc.orderNum += '' + i;
            resdata.push(tc);
        }
        return resdata;
    }

    function notifications(itemsLimit) {
        var resdata = [];
        var t =
        {
            orderNum: '用户上传-',
            payer: '中国铁路',
            payTime: '2016-08-09',
            // payMoney: 10000,
            receiver: '三一公司',
            payAccount: '621xxxxxx',
            linkCer: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg',
            // creditPicture: 'http://imgsrc.baidu.com/baike/pic/item/96dda144ad345982df652f2f0ef431adcaef84a4.jpg',
            checkResult: '',
            comment: '图片不是很清晰',
            connPerson: '对账联系人X'
        };
        if (itemsLimit === undefined)itemsLimit = 20;
        for (var i = 0; i < itemsLimit; i++) {
            var tc = angular.copy(t);
            tc.orderNum += i;
            tc.manyPay = [];
            tc.payWay = randPayWay();
            var payAmount = 0;
            for (var j = 0; j < randInRange(1, 3); j++) {
                tc.manyPay[j] = {
                    contract: '合同号好长好长好长好长………………………',
                    money: 10000
                };
                payAmount += tc.manyPay[j].money;
            }
            tc.payMoney = payAmount;
            resdata.push(tc);
        }
        return resdata;
    }

    function transcash(itemLimit) {
        var resdata = [];
        var t = {
            id: '出纳',
            orderNo: '未关联出纳-',
            payee: '收款账户～～',
            payer: '中国铁路15局',
            money: 90000,
            payWay: randPayWay(),
            acountNum: '621xxxxx',
            inputTime: '2016-09',
            status: '状态X',
            comment: '此处省略一万字'
        };
        itemLimit = itemLimit || 20;
        for (var i = 0; i < Math.round(Math.random() * itemLimit); i++) {
            var tc = angular.copy(t);
            tc.id += i;
            tc.orderNo += i;
            resdata.push(tc);
        }
        return resdata;
    }

    // 订单、出纳、付款通知
    // bkd.whenPOST(ReqUrl.fwOrders, function (reqbody) {
    //     reqbody = JSON.parse(reqbody);
    //     return reqbody.table_name == 'ori_account';
    // }).respond(function (method, url, reqBody) {
    //     reqBody = JSON.parse(reqBody);
    //     console.log('mock backend->订单', reqBody);
    //     return [200, {flag: failOrNot(), data: orders()}, {}];
    // });

    bkd.whenPOST(ReqUrl.fwFncReminds, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.table_name == 'payrecord';
    }).respond(function () {
        console.log('mock backend-> 待审付款通知');
        return [200, {flag: failOrNot(), data: notifications()}, {}];
    });
    //
    // bkd.whenPOST(ReqUrl.fwFncBankTrans, function (reqbody) {
    //     reqbody = JSON.parse(reqbody);
    //     return reqbody.table_name == 'bankTs';
    // }).respond(function () {
    //     console.log('mock backend-> 未关联出纳', reqBody);
    //     return [200, {flag: failOrNot, data: transcash()}, {}];
    // });

    // // 付款通知
    // bkd.whenPOST(ReqUrl.fwFncReminds).respond(function () {
    //     console.log('mock backend-> 待审付款通知');
    //     var resbody = {
    //         flag: failOrNot(),
    //         data: []
    //     };
    //     var t =
    //     {
    //         orderNum: '用户上传->待办',
    //         payer: '中国铁路',
    //         payTime: '2016-08-09',
    //         payMoney: 10000,
    //         payWay: '银行转账',
    //         receiver: '三一公司',
    //         payAccount: '621xxxxxx',
    //         linkCer: 'https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1255741462,4261538489&fm=111&gp=0.jpg',
    //         // creditPicture: 'http://imgsrc.baidu.com/baike/pic/item/96dda144ad345982df652f2f0ef431adcaef84a4.jpg',
    //         result: '???',
    //         comment: '图片不是很清晰'
    //     };
    //     for (var i = 0; i < 30; i++) {
    //         var tc = angular.copy(t);
    //         tc.orderNum += i;
    //         resbody.data.push(tc);
    //     }
    //     return [200, resbody, {}];
    // });

    // 付款通知审核通过操作
    bkd.whenPOST(ReqUrl.fwPaymentNotifApprov).respond(function (method, url, reqBody) {
        console.log('mock backend->待审付款通知“通过”');
        reqBody = JSON.parse(reqBody);
        var resbody = {flag: failOrNot()};
        // switch (reqBody.op_result) {
        //     case "Yes":
        //         resbody.result = 'Y';
        //         break;
        //     case 'No':
        //         resbody.result = 'N';
        //         break;
        //     case 'Wait':
        //         resbody.result = 'W';
        //         break;
        //     default:
        //         console.error("INVALID operation on payment notification");
        // }
        return [200, resbody, {}];
    });


    // // “有误”
    // bkd.whenGET(ReqUrl.fwFncBadReminds).respond(function () {
    //     console.log('mock backend-> bad reminds')
    //
    //     var resbody = {
    //         flag: 0,
    //         data: []
    //     };
    //     var t =
    //     {
    //         payer: '中铁-用户上传->有误',
    //         payTime: '2016-08-29',
    //         payAmount: 10000,
    //         payWay: '银行转账',
    //         payee: '三一公司',
    //         payeeAccount: '621xxxxxxx',
    //         creditPicture: '/img/credit/a.png',
    //         assocResult: '失败',
    //         reason: '凭证图片模糊不清'
    //     };
    //     for (var i = 0; i < 10; i++) {
    //         var tc = angular.copy(t)
    //         tc.payer += i;
    //         resbody.data.push(tc)
    //     }
    //     return [200, resbody, {}];
    // });

    // 上传订单或出纳
    bkd.whenPOST(ReqUrl.fwOrderUpload).respond(function (method, url, reqBody) {
        console.log('mock backend-> upload file: ', reqBody);
        function sleep(milliseconds) {
            var e = new Date().getTime() + (milliseconds);
            while (new Date().getTime() <= e) {
            }
        }

        var resbody = {
            flag: failOrNot(),
            data: []
        };
        var t =
        {
            "orderNum": "上传后订单-",
            "input": 1000,
            "debt": 8000,
            "total": 10000,
            "client": "中国铁路",
            "state": "对账中",
            "updateTime": "2016-8-20",
            "remark": "无"
        };
        for (var i = 0; i < 10; i++) {
            var tc = angular.copy(t);
            tc.orderNum += '' + i;
            resbody.data.push(tc);
        }
        sleep(1000);
        console.log('repond client now');
        return [200, resbody, {}];
    });

    // //出纳
    // bkd.whenPOST(ReqUrl.fwFncBankTrans).respond(function (method, url, reqBody) {
    //     console.log('mock backend-> 未关联出纳');
    //     var resbody = {
    //         flag: failOrNot(),
    //         data: []
    //     };
    //     var t = {
    //         id: '出纳',
    //         orderNo: '未关联出纳-',
    //         payer: '中国铁路15局',
    //         money: 90000,
    //         payWay: '银行转账',
    //         acountNum: '621xxxxx',
    //         inputTime: '2016-09',
    //         status: '状态X',
    //         comment: '此处省略一万字'
    //     };
    //     for (var i = 0; i < 1; i++) {
    //         var tc = angular.copy(t);
    //         tc.id += i;
    //         tc.orderNo += i;
    //         resbody.data.push(tc);
    //     }
    //     return [200, resbody, {}];
    // });

    bkd.whenPOST(ReqUrl.fwAssocTrans).respond(function (method, url, reqBody) {
        console.log('mock backend -> associate 关联出纳到订单或客户');
        reqBody = JSON.parse(reqBody);
        var resbody = {};
        if (reqBody.srcId.indexOf('fail') >= 0) {
            resbody.flag = -1;
        } else {
            resbody.flag = failOrNot();
        }

        return [200, resbody, {}];
    });

    bkd.whenPOST(ReqUrl.attachToTransCandidates, /.*"map_op":"find_map".*/).respond(function (method, url, reqbody) {
        reqbody = JSON.parse(reqbody);
        console.log("mock backend->待关联付款通知的出纳候选集", reqbody);
        return [200, {flag: failOrNot(), data: transcash(5)}, {}];
    });

    bkd.whenPOST(ReqUrl.attachToTrans, /.*"map_op":"cer_map".*/).respond(function (method, url, reqBody) {
        reqBody = JSON.parse(reqBody);
        console.log('mock backend -> 关联付款通知到出纳', reqBody);
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.checkWork, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.caid;
    }).respond(function (method, url, reqbody) {
        console.log('mock -> 对账操作', reqbody);
        return [200, {flag: failOrNot()}, {}];
    });

    function randPayWay() {
        return ['银行转账', '现金', '电汇'][randInRange(0, 2)];
    }

    bkd.whenPOST(ReqUrl.checkResult).respond(function (method, url, reqbody) {
        reqbody = JSON.parse(reqbody);
        console.log('mock backend -> 对账结果', reqbody);

        var resdata;
        switch (reqbody["watch_type"]) {
            case "bfailconnect":
                resdata = transcash(10);
                for (var i = 0; i < resdata.length; i++) {
                    var item = {
                        basicObject: resdata[i]
                    };
                    resdata[i] = item;
                }
                break;
            case "btocontract":
                resdata = transcash(10);
                for (var x = 0; x < resdata.length; x++) {
                    var cs = [];
                    var t = {
                        orderNum: '合同号好长好长好长好长好长好长啊～～～',
                        client: '客户',
                        total: 10000,
                        input: 500,
                        debt: 6000,
                    };
                    for (var j = 0; j < 5; j++) {
                        var tc = angular.copy(t);
                        tc.orderNum += j;
                        tc.client += j;
                        cs.push(tc);
                    }
                    var item = {
                        basicObject: resdata[x],
                        connectObject: cs
                    };
                    resdata[x] = item;
                }
                break;
            case "btoclient":
                resdata = transcash(10);
                for (var y = 0; y < resdata.length; y++) {
                    resdata[y] = {
                        basicObject: resdata[y],
                        connectObject: ['客户' + y]
                    };
                }
                break;
            case "phasbinput":
                resdata = notifications(10);
                for (var x = 0; x < resdata.length; x++) {
                    var cs = [];
                    var t = {
                        payee: '付款账户-',
                        payer: '付款人X～',
                        money: 10000,
                        inputTime: '2016-09',
                    };
                    for (var j = 0; j < 5; j++) {
                        var tc = angular.copy(t);
                        tc.payee += j;
                        tc.payer += j;
                        tc.payWay = randPayWay();
                        cs.push(tc);
                    }
                    var item = {
                        basicObject: resdata[x],
                        connectObject: cs
                    };
                    resdata[x] = item;
                }
                break;
            case "truepnobinput":
                resdata = notifications(10);
                for (var i = 0; i < resdata.length; i++) {
                    resdata[i] = {
                        basicObject: resdata[i]
                    }
                }
                break;
            case "falsepnobinput":
                resdata = notifications(20);
                for (var i = 0; i < resdata.length; i++) {
                    resdata[i] = {
                        basicObject: resdata[i]
                    }
                }
                break;
            case "ohasbinput":
                resdata = orders(10);
                for (var x = 0; x < resdata.length; x++) {
                    var cs = [];
                    var t = {
                        payee: '付款账户-',
                        payer: '付款人X～',
                        money: 10000,
                        inputTime: '2016-09',
                    };
                    for (var j = 0; j < 5; j++) {
                        var tc = angular.copy(t);
                        tc.payee += j;
                        tc.payer += j;
                        tc.payWay = randPayWay();
                        cs.push(tc);
                    }
                    var item = {
                        basicObject: resdata[x],
                        connectObject: cs
                    };
                    resdata[x] = item;
                }
                break;
            case "onobinput":
                resdata = orders(10);
                for (var i = 0; i < resdata.length; i++) {
                    resdata[i] = {
                        basicObject: resdata[i]
                    }
                }
                break;
            default:
                console.error('mock 错误，无法响应请求：', reqbody);
        }

        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.checkResultExport).respond(function () {
        console.log('mock backend-> 对账结果文件URL')
        return [200, {
            flag: failOrNot(),
            cares_url: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg'
        }, {}];
    });

    // 对账历史
    bkd.whenPOST(ReqUrl.historyResults, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == "T" && reqbody.table_name == "caresult_history" && reqbody.year
    }).respond(function (method, url, reqbody) {
        console.log('mock backend-> 历史对账结果查询', reqbody);
        var resdata = [];
        var s = randInRange(1, 12);
        for (var i = s; i <= randInRange(s, 12); i++) {
            resdata.push({
                month: "2016-" + i,
                url: 'http://thumb.webps.cn/to/img/3/TB1K3oZLXXXXXXrXFXXXXXXXXXX_!!0-item_pic.jpg'
            });
        }
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.reCheck, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        expect(reqbody.caid).toEqual(session.caid);
        return reqbody.caid;
    }).respond(function (method, url, reqbody) {
        console.log('mock backend-> 重新对账环境准备', reqbody)
        return [200, {flag: failOrNot(), caid: newCaid()}, {}];
    });

    bkd.whenPOST(ReqUrl.accChkRlt).respond(function () {
        console.log('mock backend->“返利”');
        return [200, {flag: failOrNot()}];
    });

    function newCaid() {
        session.caid = uuid(8);
        return session.caid;
    }

    bkd.whenPOST(ReqUrl.recheck2).respond(function () {
        console.log('mock backend->重新对账（2）')
        return [200, {flag: failOrNot(), caid: newCaid()}];
    });

    //
    // bkd.whenPOST(ReqUrl.newNotifNum).respond(function () {
    //     var resdata = {falg: failOrNot(), newpay_num: randInRange(0, 30)};
    //     console.log('mock backend->新付款通知（用户上传）数目查询，返回' + resdata.newpay_num + '条');
    //     resdata.isnewpay = resdata.newpay_num > 0;
    //     return [200, resdata, {}];
    // });

    bkd.whenPOST(ReqUrl.notifView, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'T' && reqbody.table_name == 'pay_cache';
    }).respond(function (method, url, reqbody) {
        console.log('mock backend->预览付款通知数据（用户上传）');
        var resdata = {
            flag: failOrNot(),
            data: notifications()
        };
        return [200, resdata, {}];
    });

    bkd.whenPOST(ReqUrl.prepareChkEnv).respond(function () {
        var resdata = {flag: failOrNot(), caid: newCaid()};
        console.log('mock backend->准备对账环境， 返回caid：' + resdata.caid);
        return [200, resdata];
    });


    function regNotifiers(limit) {
        var itemsNum = randInRange(0, limit || 15);
        var items = [];
        for (var i = 0; i < itemsNum; i++) {
            var item = {
                username: '联系人 ' + i
                , realName: '椿 ' + i
                , phone: '10086 ' + i
                , email: 'k' + i + '@sany.com'
                , weixin: 'wechat-' + i
                , registerWay: randIn(['个人', '公司'])
                , company: '恒大地产-' + i
                , companyid: 'cid-' + i
                , contractMes: '合同号啊～～～～～'
                , agent: '代理商 ' + i
            };
            items.push(item);
        }
        return items;
    }

    function regFworkers(limit) {
        var itemsNum = randInRange(0, limit || 15);
        var items = [];
        for (var i = 0; i < itemsNum; i++) {
            var item = {
                workId: '财务员 ' + i
                , name: '湫 ' + i
                , phone: '10086 ' + i
                , email: 'q' + i + '@sany.com'
                // , company: '恒大地产-' + i
                , companyid: 'cid-' + i
                , agentid: '代理商 ' + i
            };
            items.push(item);
        }
        return items;
    }

    bkd.whenPOST(ReqUrl.regPendingNotifiers, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'reg_cp';
    }).respond(function () {
        var resdata = regNotifiers(20);
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.regPendingFworkers, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'reg_as';
    }).respond(function () {
        var resdata = regFworkers(20);
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.approveNotifier, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.reg_type == 'cp' && reqbody.regflag == 0
    }).respond(function () {
        console.log('mock backend->审阅对账联系人注册（通过）');
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.approveNotifier, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.reg_type == 'cp' && reqbody.regflag == -2
    }).respond(function () {
        console.log('mock backend->审阅对账联系人注册（拒绝）');
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.approveFw, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.reg_type == 'as' && reqbody.regflag == 0;
    }).respond(function () {
        console.log('mock backend->审阅代理商注册（通过）');
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.approveFw, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.reg_type == 'as' && reqbody.regflag == -2
    }).respond(function () {
        console.log('mock backend->审阅代理商注册（拒绝）');
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.notifiers, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'reged_cp';
    }).respond(function () {
        console.log('mock backend->已注册对账联系人获取');
        var resdata = regNotifiers();
        resdata.forEach(function (ele) {
            ele.flag = randIn([0, -3]);
        });
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.fworkers, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'reged_as';
    }).respond(function () {
        console.log('mock backend->已注册代理商财务员获取');
        var resdata = regFworkers();
        resdata.forEach(function (ele) {
            ele.flag = randIn([0, -3]);
        });
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.ctrlNotifier, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.control_type == 'cp' && reqbody.ctlflag !== undefined;
    }).respond(function () {
        console.log('mock backend->管控对账联系人');
        return [200, {flag: failOrNot()}, {}];
    });
    bkd.whenPOST(ReqUrl.ctrlNotifier, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.id && reqbody.control_type == 'as' && reqbody.ctlflag !== undefined;
    }).respond(function () {
        console.log('mock backend->管控代理商财务员');
        return [200, {flag: failOrNot()}, {}];
    });

    bkd.whenPOST(ReqUrl.backupdb).respond(function () {
        return [200, {flag: failOrNot()}];
    });
    function opLogs(limit) {
        var items = [];
        for (var i = 0; i < randInRange(0, limit || 15); i++) {
            var t = {
                time: randIn([2015, 2016]) + '-' + randInRange(1, 12) + '-' + randInRange(1, 30),
                username: randIn(['路飞', '鸣人']),
                usertype: randIn(['客户', '代理商财务', '管理员']),
                content: randIn(['放炸弹', '种树', '卖烧烤']),
                result: randIn(['成功', '失败'])
            };
            items.push(t);
        }
        return items;
    }

    bkd.whenPOST(ReqUrl.viewLog, function (reqbody) {
        reqbody = JSON.parse(reqbody);
        return reqbody.watch_type == 'op_log';
    }).respond(function () {
        console.log('mock backend ->操作日志');
        var resdata = opLogs();
        return [200, {flag: failOrNot(), data: resdata}, {}];
    });

    bkd.whenPOST(ReqUrl.fetchAgents).respond(function () {
        var items = [{code: 'gd0001', name: '广东代理商'}, {code: 'ah0001', name: '安徽代理商'}];
        var extra = 2;
        for (var i = 0; i < randInRange(0, extra); i++) {
            var t = {
                code: 'xxx-' + i,
                name: '吼吼' + i
            };
            items.push(t);
        }
        return [200, {flag: failOrNot(0), data: items}];
    });
}]);