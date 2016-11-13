// console.log('config controllers');

var msgOkTimeout = 800;
var msgErrTimeout = 1200;

var loginForm = $('#loginForm');
var loginModal = $('#loginModal');
var loginSubmitBtn = loginForm.find('button[type="submit"]');
var registerForm = $('#signUpForm');
var registerModal = $('#registerModal');
var regSubmitBtn = registerForm.find('button[type="submit"]');
var regLogModal = $('#loginModal,#registerModal');      // reg -> register, log -> login
var signOutEntry = $('div[ng-show="loggedInUser"]');
var regLogEntry = $('div[ng-hide="loggedInUser"]');
// var regLogModal = $('.modal');

// 首页
app.controller('indexCtrl', ['$scope', '$state', '$rootScope', function ($scope, $state, rootsgop) {
    //console.log('indexCtrl');

    // rebind F5(refresh) to reload current state( refresh data by javascript)
    $scope.disableKeyF5 = function (evt) {
        if ((evt.which || evt.code) === 116 && !evt.ctrlKey) {    // do not disable <Ctrl>+F5 (force refreshing)
            //console.log('F5 pressed');
            evt.preventDefault();
            // console.log('reloading state');
            $state.reload();
        }
    };
    $(document).on('keydown', $scope.disableKeyF5);

    regLogModal.on('hidden.bs.modal', function () {
        // console.info('hidden...', $(this));
        $(this).find('div.with-errors').html('');
    });
    // $('.modal').modal('hide');

    rootsgop.$on('$stateChangeSuccess', function (event, toState) {
        rootsgop.activeState = toState.name;
    });

    rootsgop.$watch('loggedInUser', function (nv) {
        if (nv) {
            // 先隐藏，再显示
            regLogEntry.css('display', 'none');
            signOutEntry.css('display', 'inherit');
        } else {
            signOutEntry.css('display', 'none');
            regLogEntry.css('display', 'inherit');
        }
    });
}]);

// 登录
var signInCtrl = ['$scope', '$state', 'AccountService',
    function (sgop, $state, AccountService) {
        //console.log('sign in ctrl 登陆');

        var ctrl = sgop;

        sgop.submitForm = function () {
            if (loginForm.hasClass('ng-invalid')) {
                return false;
            }

            loginSubmitBtn.attr('disabled', true);
            ctrl.errMsg = '登录中……';
            // // remove stored sign info
            // AccountService.removeStoredSignLocalInfo();
            var formUser = sgop.formUser;
            AccountService.signIn(formUser).then(function (userInfo) {
                    goState(userInfo.role);
                },
                function (err) {
                    ctrl.errMsg = '登陆失败：' + ensureErrMsg(err);
                }).finally(function () {
                loginSubmitBtn.removeAttr('disabled');
            });
        };

        // U<-->财务人员<-->fw;  M<-->财务总监<-->fs; S-->系统管理人员<-->sa
        function goState(role) {
            switch (role) {
                case "bu":
                    $state.go('u.fw');
                    return;
                case "bm":
                    $state.go('u.fs');
                    return;
                case "bs":
                    $state.go('u.sa');
                    return;
            }
        }
    }];
app.controller('SignInCtrl', signInCtrl);

// 注册
var signUpCtrl = ['$scope', '$state', 'AccountService', '$uibModal', '$timeout',
        function (sgop, $state, AccountService, msgbox, timeout) {
            //console.log('sign up ctrl-> 注册用户');

            var ctrl = this;
            // for no 'controller as ctrl' grammar
            // sgop.signUpCtrl=sgop;
            // var ctrl = sgop;

            ctrl.submitForm = function () {

                if (registerForm.hasClass('ng-invalid')) {
                    return false;
                }
                regSubmitBtn.attr('disabled');
                ctrl.errMsg = '申请注册中……';

                var formUser = angular.copy(ctrl.formUser);
                var m = {
                    'U': 'bu',
                    'M': 'bm'
                };
                formUser.role = m[formUser.role] || formUser.role;
                formUser.renameProperty('uid', 'username');
                formUser.renameProperty('upwd', 'password');
                formUser.renameProperty('agent', 'agentid', function (agent) {
                    return agent.code;
                });
                AccountService.signUp(formUser).then(function (resData) {
                    // $state.go('login');
                    registerModal/*$('.modal')*/.modal('hide');
                    var boxInst = showMsg({
                        msgbox: msgbox,
                        title: '注册结果',
                        msgHtml: '注册<strong class="text-success">成功</strong>，管理员将会审阅您的注册申请。'
                    });
                    timeout(function () {
                        boxInst.close();
                        loginModal.modal('show');
                    }, msgOkTimeout);
                }, function (err) {
                    ctrl.errMsg = '注册失败：' + ensureErrMsg(err);
                }).finally(function () {
                });
            };


            registerModal.on('shown.bs.modal', function () {
                if (!sgop.agents || !sgop.agents.length) {
                    (function refreshAgents() {
                        AccountService.sellerAgents().then(function (items) {
                            sgop.agents = items;
                        }, function (errObj) {
                        });
                    })();
                }
            });
        }
    ]
    ;
app.controller('SignUpCtrl', signUpCtrl);

var resetPasswordCtrl = ['$scope', '$state', 'AccountService',
    function ($scope, $state, AccountService) {
        //console.log('reset password 忘记密码');
    }];
// 对于直接通过URL访问需要授权信息页面的情况（#/u/*, #/u/fw,#/fs, #/u/sa），Javascript环境已没有任何用户登陆信息，如何自动登陆？
// 已#/u打头的路径，需要授权信息，需检测是否已登陆，如果没有,或者自动登陆、或者弹窗登陆、或者跳转登陆页面
var uCtrl = ['$scope', '$state', 'AccountService', '$timeout', '$uibModal', 'FncRmindService', 'ChkRsltSvc', function (sgop, state, AccSvc, timeout, msgbox, NotifSvc, ChkSvc) {
    //console.log('uCtrl');

    regLogModal.modal('hide');

    //如果未登录
    if (sgop.loggedInUser === undefined) {
        // todo go login?  auto-login? popup-login?
        // console.log('trying to re-login by: ',sessionStorage, sessionStorage.getItem('formUser'));
        AccSvc.signInBySessionStorage().then(function () {
            //console.info('自动登录（session storage）');
        }, function (fail) {
            state.go('index');
        });
    } else {
        // logged in ?
        // pass through
    }

    var ctrl = sgop.$root;

    ctrl.signOut = function () {
        AccSvc.signOut();
        state.go('index');
    };

    ctrl.userInfo = function () {
        msgbox.open({
            templateUrl: 'fw-info.html'
            , controller: ['$scope', '$uibModalInstance', function (boxsgop, boxInst) {
                // console.log('box ctrl');
                boxsgop.closeMsgbox = function () {
                    boxInst.dismiss();
                };
            }]
        });
    };

}];


//**** 财务人员fw控制器 ----->>>>start *****//

var fwCtrl = ['$scope', '$state', '$timeout', '$uibModal', 'FncRmindService', 'ChkRsltSvc', '$rootScope',
    function (sgop, state, timeout, msgbox, NotifSvc, ChkSvc, rootsgop) {
        //console.log('fw info ctrl');

        sgop.checkingEnv = function () {
            ChkSvc.initCheckingEnv().then(function (caid) {
                state.go('u.fw.p');
            }, function (errMsgObj) {
                var boxInst = showMsg({
                    msgbox: msgbox,
                    msgHtml: '对账环境准备收了，网络或系统错误，请重试。'
                });
                timeout(function () {
                    boxInst.dismiss();
                }, msgErrTimeout);
            });
        };

    }];
//
// // 财务人员导航数据获取
// var fwNavCtrl = ['$scope', '$state', 'OrderService',
//     function ($scope, $state, OrderService) {
//         console.log('fwNavCtrl,', $scope);
//
//     }];

function ensureErrMsg(errMsgObj) {
    return errMsgObj.msg || errMsgObj.errmsg || '无更详细信息';
}
//
// // 财务人员账单
// var fwoCtrl = ['$scope', '$state', '$stateParams', 'OrderService', function (sgop, $state, $stateParams, OrderService) {
//     console.log('fwoGridCtrl, params: ', $stateParams);
//
//     // [caution] 变量与Uploadctrl中有重复！！
//
//     var self = this;
//
//     function tryRefreshGrid() {
//         sgop.isLoading = true;
//         OrderService.orders().then(function (orders) {
//             sgop.orders = orders;
//             // self.orders = orders;
//             // isNonEmptyGrid => 蕴含已获得数据反馈，数据或许空或许不空
//             sgop.isNonEmptyGrid = orders.length > 0;
//         }, function (errMsgObj) {
//             sgop.errMsg = '请求数据失败：' + ensureErrMsg(errMsgObj);
//             sgop.canShowError = true;
//         }).finally(function () {
//             sgop.isLoading = false;
//         });
//     };
//     // init grid
//     tryRefreshGrid();
//
//     sgop.$on('EvtUploadOrderSucc', function (evt, newOrders) {
//         console.log('event: EvtUploadOrderSucc caught', evt, newOrders);
//         // clear status text
//         // evt.stopPropagation();
//
//         clearStatus();
//
//         sgop.isNonEmptyGrid = newOrders.length > 0;
//         sgop.orders = newOrders;
//         // self.orders = newOrders;
//     });
//
//     // 'reset' scope
//     function clearStatus() {
//         // 都是为加载grid而定义的变量
//         sgop.isLoading = undefined;
//         sgop.canShowError = undefined;
//         sgop.isNonEmptyGrid = undefined;
//         sgop.errMsg = '';
//
//         // sgop.orders = undefined;
//     }
// }];

// 文件上传
var uploadCtrl = ['$scope', 'Upload', '$timeout', '$state', '$rootScope',
    function (sgop, Upld, timeout, $state, rootsgop) {
        //console.log('file upload ctrl');

        // for SomeController as ctrl; let var ctrl = this;

        // sgop.uploadFileSelect = function (selectedFile) {
        //     var info = sgop.uploadInfo;
        //     try {
        //         if (selectedFile) {
        //             info.fileName = selectedFile.name;
        //             console.log('uploading file: ', selectedFile, sgop.uploadFileType);
        //             info.isUploading = true;
        //             // 使用timeout让控制器有时间先更新UI显示正准备上传的信息
        //             timeout(function () {
        //                 Upld.upload({
        //                     url: ReqUrl.fwOrderUpload,
        //                     data: {"file": selectedFile, "import_select": sgop.uploadFileType}
        //                 }).then(function (resPkg) {
        //                     console.log('response recved', resPkg);
        //                     var resbody = resPkg.data;
        //                     if (isOkResBody(resbody)) {
        //                         info.uploadResult = true;
        //                         //todo refresh data <- upload
        //                         console.log('trying to refresh grid of orders after successful uploading file');
        //
        //                         if (sgop.uploadFileType == 'A') {
        //                             sgop.$emit('EvtUploadOrderSucc', resbody.data);
        //                         }
        //                     } else {
        //                         info.uploadResult = false;
        //                         info.errMsg = '上传失败，' + (resbody.errmsg || '无更详细信息');
        //                     }
        //                     info.isUploading = false;
        //                 }, function (errPkg) {
        //                     console.error('upload err', errPkg);
        //                     info.errMsg = '网络或系统错误';
        //                     info.uploadResult = false;
        //                 }, function (evt) {
        //                     console.log('evt', evt);
        //                     info.progressMax = parseInt(evt.total);
        //                     info.progress = parseInt(evt.loaded);
        //                 }).catch(function (ex) {
        //                     console.log('catch exception:', ex);
        //                     info.isUploading = false;
        //                     info.uploadResult = false;
        //                     info.errMsg = '网络或系统错误';
        //                 }).finally(function () {
        //                     info.isUploading = false;
        //                 });
        //             }, 50);
        //         } else {
        //             info.errMsg = '请（正确）选择文件';
        //         }
        //     } catch (expt) {
        //         console.error(expt);
        //         info.errMsg = '上传失败，网络或系统错误';
        //     }
        // };

        sgop.formSubmit = function () {
            var info = sgop.uploadInfo;
            try {
                //console.log('上传货款和账单', sgop.uploadCfg);
                info.isUploading = true;
                // 使用timeout让控制器有时间先更新UI显示正准备上传的信息
                timeout(function () {
                    sgop.uploadCfg.caid = Encrypt(rootsgop.caid, true);
                    Upld.upload({
                        url: ReqUrl.fwOrderUpload,
                        data: sgop.uploadCfg
                    }).then(function (resPkg) {
                        console.log('(upload) response recved', resPkg);
                        var resbody = JSON.parse(Decrypt(resPkg.data));
                        console.log('decrypted: ', resbody);
                        if (isOkResBody(resbody)) {
                            info.uploadResult = true;
                        } else {
                            info.uploadResult = false;
                            info.errMsg = '上传失败，' + (resbody.errmsg || '无更详细信息');
                        }
                        info.isUploading = false;
                    }, function (errPkg) {
                        //console.error('upload err', errPkg);
                        info.errMsg = '网络或系统错误';
                        info.uploadResult = false;
                    }, function (evt) {
                        // console.log('evt', evt);
                        info.progressMax = parseInt(evt.total);
                        info.progress = parseInt(evt.loaded);
                    }).catch(function (ex) {
                        //console.error(ex);
                        info.isUploading = false;
                        info.uploadResult = false;
                        info.errMsg = '网络或系统错误';
                    }).finally(function () {
                        info.isUploading = false;
                    });
                }, 50);
            } catch (expt) {
                //console.error(expt);
                info.errMsg = '上传失败，网络或系统错误';
            }
        };
    }];

var fwcCtrl = ['$scope', '$stateParams', '$rootScope', function (sgop, stateParams, rootsgop) {
    // console.log('fwcCtrl->', stateParams);

    sgop.targetDate = stateParams;

    // 禁止浏览器回退到对账流程的上一步
    sgop.$on('$stateChangeStart',
        function (event, toState, toParams, fromState, fromParams) {
            // 可以从最后一步（对账结果）返回到对账流程第一步，即允许“重新对账”
            if (fromState.name.indexOf('u.fw.p') > -1 && toState.name.indexOf('u.fw.p') > -1 && !(fromState.name == 'u.fw.p.m.x' && toState.name == 'u.fw.p') && toState.name.length < fromState.name.length) {
                //console.log('禁止浏览器回退');
                event.preventDefault();
                window.history.forward();
            }
        });

    sgop.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
        // console.log('state changed: ', fromState, toState);
        sgop.checkProgress = {};
        switch (toState.name) {
            case 'u.fw.p':
                sgop.checkProgress.step = 1;
                break;
            case 'u.fw.p.m':
                sgop.checkProgress.step = 2;
                break;

            // 流程3是对账操作，只是一个对后端的请求，没有数据页面，没有单独的状态，只有一个消息框

            case 'u.fw.p.m.x':
                sgop.checkProgress.step = 4;
                break;

            default:
                sgop.checkProgress.step = -1;
        }
        rootsgop.inChecking = sgop.checkProgress.step > 1 && sgop.checkProgress.step < 4;
    });
}];

// register controller
app.controller('UploadCtrl', uploadCtrl);

function showMsg(msgCfg/*msgbox, title, msgHtml*/) {
    if (arguments.length == 3) {
        msgCfg = {
            msgbox: msgCfg,
            title: arguments[1],
            msgHtml: arguments[2]
        }
    }
    return msgCfg.msgbox.open({
        templateUrl: 'msgbox.html',
        controller: msgCfg.ctrl || ['$scope', '$uibModalInstance', function (sgop, msgbox) {
            // sgop.msgboxTitle = msgCfg.title;
            // sgop.msgHtml = msgCfg.msgHtml;
            sgop.msgCfg = msgCfg;
            sgop.closeMsgbox = function () {
                msgbox.dismiss();
            };
            sgop.okBtnClick = function () {
                msgbox.close();
            };
            sgop.cancelBtnClick = function () {
                msgbox.dismiss();
            };
        }],
        backdrop: msgCfg.backdrop === undefined ? !msgCfg.important : msgCfg.backdrop,
        keyboard: msgCfg.keyboard === undefined ? !msgCfg.important : msgCfg.keyboard,   // if undefined, can close by ESC
        size: msgCfg.msgboxSize || 'md',
    })
        ;
}


// 财务人员“付款通知”（未入对账依赖数据）
var fwnCtrl = ['$scope', 'FncRmindService', 'Lightbox', "$uibModal", '$state', 'ChkRsltSvc', '$timeout',
    function (sgop, FncRmindService, picModal, msgBox, state, ChkRsltSvc, timeout) {
        //console.log('fwnCtrl 待审付款通知');

        (function refreshData() {
            sgop.isLoading = true;
            FncRmindService.fncReminds().then(function (reminds) {
                sgop.fncReminds = reminds;
                sgop.isNonEmptyGrid = reminds.length > 0;
            }, function (err) {
                //console.warn(err);
                sgop.canShowError = true;
                sgop.errMsg = '请求数据失败：' + ensureErrMsg(err);
            }).finally(function () {
                sgop.isLoading = false;
            });
        }());

        function opPaymentNotifyItem(paymentNotifyItem, logtxt, svcFnc) {
            //console.log(logtxt);
            //console.log('数据操作，前：', paymentNotifyItem);

            var approveCfg = {
                "id": paymentNotifyItem.id
            };
            var msgcfg = {msgbox: msgBox, title: '审核通过操作', msgHtml: '操作<strong class="">正在进行……</strong>'};
            var boxInst = showMsg(msgcfg);
            svcFnc(approveCfg).then(function (status) {
                paymentNotifyItem.result = status;
                msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                timeout(function () {
                    boxInst.dismiss();
                }, msgOkTimeout)
            }, function (errMsgObj) {
                //console.log(logtxt + '失败');
                msgcfg.msgHtml = '操作<strong class="text-info">失败</strong>';
                timeout(function () {
                    boxInst.dismiss();
                }, msgErrTimeout)
            }).finally(function () {
            });
        }


        //  // "通过"付款通知和出纳
        // sgop.approvePaymentNotification = function (paymentNotifyItem) {
        //     opPaymentNotifyItem(paymentNotifyItem, "“通过”（关联付款通知和出纳）", FncRmindService.approvePaymentNotification)
        // };

        // 否决付款通知（标记为无效的用户上传数据）
        sgop.rejectPaymentNotification = function (paymentNotifyItem) {
            opPaymentNotifyItem(paymentNotifyItem, '“否决”（取消付款通知与出纳的关联）', FncRmindService.rejectPaymentNotification)
        };

        // “待定”
        sgop.tbdPaymentNotification = function (paymentNotifyItem) {
            opPaymentNotifyItem(paymentNotifyItem, '“待定”（）', FncRmindService.tbdPaymentNotification);
        };

        // 关联到出纳
        sgop.attachPaymentNotification = function (paymentNotifyItem) {
            msgBox.open({
                templateUrl: 'attach-to-t.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    var ctrl = this;
                    ctrl.r = paymentNotifyItem;
                    (function getCandidates() {
                        ctrl.isLoading = true;
                        FncRmindService.getBankTransactionCandidates({id: paymentNotifyItem.id})
                            .then(function (candidates) {
                                ctrl.isNonEmptyGrid = candidates.length > 0;
                                ctrl.trans = candidates;
                                if (!paymentNotifyItem.matchedId && candidates.length > 0) {
                                    ctrl.attachCfg.targetId = candidates[0].id;
                                }
                            }, function (errMsgObj) {
                                //console.log(errMsgObj);
                                ctrl.canShowError = true;
                                ctrl.errMsg = ensureErrMsg(errMsgObj);
                            })
                            .catch(function (excpt) {
                                //console.error(excpt);
                            })
                            .finally(function () {
                                ctrl.isLoading = false;
                            });
                    }());

                    sgop.attachSubmit = function () {
                        ctrl.isProcessing = true;
                        ctrl.attachErrMsg = undefined;
                        FncRmindService.attachToBankTransaction(ctrl.attachCfg)
                            .then(function (res) {
                                // 显示为“已通过”
                                paymentNotifyItem.result = 'Y';
                                msgbox.close();
                            }, function (errMsgObj) {
                                ctrl.attachErrMsg = errMsgObj.msg;
                            })
                            .finally(function () {
                                ctrl.isProcessing = false;
                            });
                    };

                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg',
                controllerAs: 'ctrl'

            }).result.then(function (ok) {
                var boxInst = showMsg({
                    msgbox: msgBox,
                    title: '匹配付款通知到出纳',
                    msgHtml: '匹配<strong class="text-success">成功</strong>'
                });
                timeout(function () {
                    boxInst.dismiss();
                }, msgOkTimeout);
            });
        };

        // 显示凭证图片
        sgop.showPicture = function (url) {
            // console.log('pic url: ', url);
            // picModal.open({
            //     templateUrl: 'showPicTpl.html',
            //     controller: ['$scope','$uibModalInstance', function (sgop,picModal) {
            //         sgop.imgUrl = url;
            //         sgop.closePicModal=function () {
            //             picModal.dismiss();
            //         };
            //     }],
            //     // size:'220px × 117',
            //     resovle: {
            //         // "imgUrl": function () {
            //         //     return url;
            //         // }
            //     },
            // });
            picModal.openModal([url], 0);
        };

        // 多合同的付款分配详细信息
        sgop.showCM = function (item) {
            msgBox.open({
                templateUrl: 'cm-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.r = item;

                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }]
            });
        };


        // 对账操作 lol...
        sgop.checkCashing = function () {
            //console.log('对账操作请求发起');
            sgop.checkProgress.step = 3;
            var msgCfg = {
                msgbox: msgBox,
                title: '对账操作',
                msgHtml: '对账<strong class="text-info">进行中</strong>……',
                noCloseBtn: true,
                ctrl: ['$scope', '$uibModalInstance', '$timeout', function (sgop, boxInst, timeout) {
                    sgop.msgCfg = msgCfg;
                    sgop.closeMsgbox = function () {
                        boxInst.dismiss();
                    }
                }],
                backdrop: 'static',
                keyboard: false,
            };

            var boxInst = showMsg(msgCfg);
            ChkRsltSvc.checkWork()
                .then(function () {
                    msgCfg.msgHtml = '对账<strong class="text-success">成功</strong>，正跳转到对账结果展示页面……';
                    timeout(function () {
                        boxInst.dismiss();
                        state.go(".x");
                    }, 800);
                }, function (errMsgObj) {
                    // 窗口已经打开，再改动以下两个参数已无作用
                    // msgCfg.keyboard = true;
                    // msgCfg.backdrop = undefined;

                    msgCfg.noCloseBtn = false;
                    msgCfg.msgHtml = '对账<strong class="text-danger">失败</strong>，错误：' + errMsgObj.msg;
                    // 关闭信息窗口后，将对账流程显示回退到第2步
                    boxInst.result.finally(function () {
                        sgop.checkProgress.step = 2;
                    });
                })
                .finally(function () {
                });
        };


        // /**
        //  * 将状态（付款通知审核结果）的后台存储码映射对用户友好的、给用户展示的文本
        //  * @param statusCode
        //  * @returns {*}
        //  */
        // sgop.translateNotifyStatus = function translateNotifyStatus(statusCode) {
        //     switch (statusCode) {
        //         case 'Y':
        //             return '已通过';
        //         case 'N':
        //             return '被否决';
        //         case 'W':
        //             return '待定中';
        //         case 'M':
        //             return '已通过';
        //         case 'V':
        //             return '无';
        //         default:
        //             return '';
        //     }
        // };

    }];

function getRefreshFnc(sgop, svcfnc, tab) {
    return function refreshGrid() {
        sgop.$watch('initTab', function (nv, ov) {
            if (nv == tab) {
                // console.log('initing tab-' + tab);
                sgop.isLoading = true;
                svcfnc()
                    .then(function (rows) {
                        sgop.isNonEmptyGrid = rows.length > 0;
                        sgop.rows = rows;

                        sgop.tabInited[tab] = true;
                    }, function (errMsgObj) {
                        sgop.canShowError = true;
                        sgop.errMsg = errMsgObj.msg;
                    })
                    .finally(function () {
                        sgop.isLoading = false;
                    });
            }
        });
    };
}

// 预览付款通知数据
var fwvCtrl = ['$scope', 'Lightbox', '$uibModal', 'FncRmindService', function (sgop, picModal, msgBox, NotifSvc) {
    //console.log('fwv ctrl->预览付款通知数据');
    sgop.checkInoperable = true;
    (function refreshData() {
        sgop.isLoading = true;
        NotifSvc.viewNotifications().then(function (items) {
            sgop.fncReminds = items;
            sgop.isNonEmptyGrid = items.length > 0;
        }, function (err) {
            //console.warn(err);
            sgop.canShowError = true;
            sgop.errMsg = '请求数据失败：' + ensureErrMsg(err);
        }).finally(function () {
            sgop.isLoading = false;
        });
    }());


    // 显示凭证图片
    sgop.showPicture = function (url) {
        //console.log('pic url: ', url);
        // picModal.open({
        //     templateUrl: 'showPicTpl.html',
        //     controller: ['$scope','$uibModalInstance', function (sgop,picModal) {
        //         sgop.imgUrl = url;
        //         sgop.closePicModal=function () {
        //             picModal.dismiss();
        //         };
        //     }],
        //     // size:'220px × 117',
        //     resovle: {
        //         // "imgUrl": function () {
        //         //     return url;
        //         // }
        //     },
        // });
        picModal.openModal([url], 0);
    };

    // 多合同的付款分配详细信息
    sgop.showCM = function (item) {
        msgBox.open({
            templateUrl: 'cm-detail-box.html',
            controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                sgop.r = item;

                sgop.closeMsgbox = function () {
                    msgbox.dismiss();
                };
            }]
        });
    };
}];

// 历史对账结果查看
var fwhCtrl = ['$scope', 'ChkRsltSvc', '$uibModal', '$state', function (sgop, ChkRsltSvc, msgbox, state) {
    //console.log('ctrl->历史对账结果');

    var thisYear = new Date().getFullYear();
    sgop.yearInput = sgop.maxYear = thisYear;
    sgop.minYear = 2000;

    sgop.formSubmit = function () {
        //console.log('查询' + sgop.yearInput + '年对账历史');
        if (sgop.yearInput) {
            refreshData();
        }
    };

    // 立即显示本年度已有对账结果
    sgop.formSubmit();

    function refreshData() {
        sgop.isLoading = true;
        sgop.canShowError = undefined;
        sgop.isNonEmptyGrid = undefined;
        sgop.year = sgop.yearInput;
        ChkRsltSvc.historyResults(sgop.year)
            .then(function (yearHistory) {
                sgop.isNonEmptyGrid = true;
                sgop.historyResults = yearHistory;
            }, function (errMsgObj) {
                sgop.canShowError = true;
                sgop.errMsg = errMsgObj.msg;
            })
            .finally(function () {
                sgop.isLoading = false;
            });
    };

    sgop.recheck = function (year, month, noConfirm) {
        if (noConfirm) {
            var opInfo = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                , size: 'sm'
                , important: true
            };
            var boxInst = showMsg(opInfo);
            ChkRsltSvc.recheck2(year, month).then(function (ok) {
                boxInst.close();
                state.go('u.fw.p', {year: year, month: month});
            }, function (errObj) {
                opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
            });
        } else {
            var msgConfirm = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '确定丢弃 <strong>' + year + '年' + month + '月</strong> 的对账结果,并重新核对当月账目吗?'
                , okBtn: true
                , cancelBtn: true
                , important: true
            };
            showMsg(msgConfirm).result.then(function () {
                var opInfo = {
                    msgbox: msgbox
                    , title: '重新对账操作'
                    , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                    , size: 'sm'
                    , important: true
                };
                var boxInst = showMsg(opInfo);
                ChkRsltSvc.recheck2(year, month).then(function (ok) {
                    boxInst.close();
                    state.go('u.fw.p', {year: year, month: month});
                }, function (errObj) {
                    opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
                });
            });
        }
    };

    sgop.thisDate = new Date();

}];

// 对账结果
var fwxCtrl = ['$scope', 'ChkRsltSvc', '$uibModal', '$state', '$stateParams', function (sgop, ChkRsltSvc, msgbox, state, stateParams) {
        //console.log('fwxCtrl 报表标签组');

        var self = this;

        sgop.tabInited = [];
        // sgop.tabCfg={
        //     openTab:0
        // };

        sgop.sel = function (tab) {
            if (!sgop.tabInited[tab]) {
                // console.log('Tab init event of tab-' + tab + ' fired');
                sgop.initTab = tab;
                // ('initTab' + tab);
            }
            // sgop.tabCfg.openTab = tab;
        };

        // 到处对账结果
        ChkRsltSvc.checkResultExport().then(function (fileUrl) {
            sgop.fileExportedUrl = fileUrl;
        }, function (errMsgObj) {
            sgop.exportErr = '报表生成失败：' + errMsgObj.msg;
        });

        // 重新对账
        sgop.startOver = function () {
            // var msgCfg = {
            //     msgbox: msgbox,
            //     title: '请求重新对账',
            //     msgHtml: '重新对账操作请求中……',
            //     noCloseBtn: true
            // };
            // var boxInst = showMsg(msgCfg);
            // // “重新对账”请求成功
            // ChkRsltSvc.reCheck().then(function () {
            //     boxInst.close();
            //     state.go('u.fw.p');
            // }, function (errMsgObj) {
            //     msgCfg.msgHtml = '操作请求失败：' + ensureErrMsg(errMsgObj);
            // });
            var year = stateParams.year;
            var month = stateParams.month;
            var msgConfirm = {
                msgbox: msgbox
                , title: '重新对账操作'
                , msgHtml: '确定丢弃 <strong>' + year + '年' + month + '月</strong> 的对账结果,并重新核对当月账目吗?'
                , okBtn: true
                , cancelBtn: true
                , important: true
            };
            showMsg(msgConfirm).result.then(function () {
                var opInfo = {
                    msgbox: msgbox
                    , title: '重新对账操作'
                    , msgHtml: '重新对账操作<strong class="text-info">请求中</strong>……'
                    , size: 'sm'
                    , important: true
                };
                var boxInst = showMsg(opInfo);
                ChkRsltSvc.reCheck().then(function (ok) {
                    boxInst.close();
                    state.go('u.fw.p');
                }, function (errObj) {
                    opInfo.msgHtml = '重新对账操作请求<strong class="text-danger">失败</strong>,' + ensureErrMsg(errObj);
                });
            });
        };

// “返利”
        sgop.acceptResult = function () {
            var msgCfg = {
                msgbox: msgbox,
                title: '返利操作',
                msgHtml: '操作<strong class="text-info">执行中</strong>……',
                noCloseBtn: true
            };
            var boxInst = showMsg(msgCfg);
            // “重新对账”请求成功
            ChkRsltSvc.acceptCheckingResult().then(function () {
                msgCfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                setTimeout(function () {
                    boxInst.close();
                }, msgOkTimeout);
                boxInst.result.finally(function () {
                    sgop.resultChkd = true;
                    // state.go('u.fw.p');
                });
            }, function (errMsgObj) {
                msgCfg.msgHtml = '操作<strong class="text-danger">失败</strong>：' + ensureErrMsg(errMsgObj);
            }).finally(function () {
                msgCfg.noCloseBtn = false;
            });
        };
    }]
    ;


// 对账结果分类显示 X1->X8
app.controller('X1Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无匹配合同、客户的出纳'
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);


    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToNeitherContractNorClient, 1)();

}]);

app.controller('X2Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '未到账的货款'
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.ordersUnpaid, 2)();

}]);

app.controller('X3Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无匹配出纳的付款通知'
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationNotAttachedToTransaction, 3)();

}]);

app.controller('X4Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '有匹配合同的出纳',
        connectType: 'A',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'contract-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToContract, 4)();
}]);

app.controller('X5Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '有匹配客户的出纳',
        connectType: 'C'
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.transactionAttachedToClient, 5)();
}]);

app.controller('X6Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '有出纳的付款通知',
        connectType: 'H',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'trans-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationAttachedToTransaction, 6)();
}]);

app.controller('X7Ctrl', ['$scope', 'ChkRsltSvc', '$uibModal', function (sgop, ChkRsltSvc, msgbox) {
    sgop.gridCfg = {
        tableCaption: '已到账的货款',
        connectType: 'T',
        attachDetail: function (item) {
            msgbox.open({
                templateUrl: 'trans-detail-box.html',
                controller: ['$scope', '$uibModalInstance', function (sgop, msgbox) {
                    sgop.gridRows = item.connect;
                    sgop.closeMsgbox = function () {
                        msgbox.dismiss();
                    };
                }],
                size: 'lg'
            });
        }
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.ordersPaid, 7)();
}]);

app.controller('X8Ctrl', ['$scope', 'ChkRsltSvc', function (sgop, ChkRsltSvc) {
    sgop.gridCfg = {
        tableCaption: '无效的付款通知'
    };

    //console.log('ctrl ' + sgop.gridCfg.tableCaption);

    getRefreshFnc(sgop, ChkRsltSvc.notificationInvalid, 8)();
}]);


// 管理员
var fsCtrl = ['$scope', function (sgop) {
    //console.log('fsCtrl');
}];

function refreshGridFnc(svcfnc, sgop) {
    return function () {
        sgop.isLoading = true;
        svcfnc().then(function (items) {
            // console.log('recvd:',items)
            sgop.items = items;
        }, function (errobj) {
            //console.error(errobj);
            sgop.errMsg = ensureErrMsg(errobj);
        }).finally(function () {
            sgop.isLoading = false;
        });
    }
}

function approveReg(item, items, svcfnc, msgbox) {
    var msgcfg = {
        msgbox: msgbox
        , title: '审阅操作结果'
        , msgHtml: '操作<strong class="text-info">执行中</strong>'
        , size: 'sm'
    };
    var boxInst = showMsg(msgcfg);
    svcfnc(item.workId || item.username).then(function (ok) {
        msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
        setTimeout(function () {
            boxInst.close();
        }, msgOkTimeout);
        // 如果用户注册审阅操作成功（被通过、被拒绝），则不再在表中显示
        var i = items.indexOf(item);
        if (i > -1) {
            items.splice(i, 1);
        }
    }, function (errobj) {
        msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>，' + ensureErrMsg(errobj);
        setTimeout(function () {
            boxInst.close();
        }, msgErrTimeout);
    }).finally(function () {
    });
};

// 审阅对账联系人注册申请
var fsanCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', function (sgop, timeout, AccSvc, msgbox) {
    console.log('fsan Ctrl->审阅对账联系人注册申请');

    refreshGridFnc(AccSvc.regPendingPaymentNotifiers, sgop)();

    sgop.acceptNotifier = function (item) {
        approveReg(item, sgop.items, AccSvc.acceptNotifierReg, msgbox);
    };
    sgop.rejectNotifier = function (item) {
        approveReg(item, sgop.items, AccSvc.rejectNotifierReg, msgbox);
    };
}];
app.controller('fsanCtrl', fsanCtrl);

// 审阅代理商注册
var fsawCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', function (sgop, timeout, AccSvc, msgbox) {
    //console.log('fsaw Ctrl->审阅代理商注册');

    refreshGridFnc(AccSvc.regPendingFinancialWorker, sgop)();

    // sgop.isLoading = true;
    // AccSvc.regPendingFinancialWorker().then(function (items) {
    //     sgop.items = items;
    // }, function (errobj) {
    //     console.error(errobj);
    //     sgop.errMsg = ensureErrMsg(errobj);
    // }).finally(function () {
    //     sgop.isLoading = false;
    // });
    sgop.accept = function (item) {
        approveReg(item, sgop.items, AccSvc.acceptFinancialWorkerReg, msgbox);
    };
    sgop.reject = function (item) {
        approveReg(item, sgop.items, AccSvc.rejectFinancialWorkerReg, msgbox);
    };
}];
app.controller('fsawCtrl', fsawCtrl);

function ctrlUser(item, svcfnc, msgbox) {
    var msgcfg = {
        msgbox: msgbox
        , title: '管控操作结果'
        , msgHtml: '操作<strong class="text-info">执行中</strong>'
        , size: 'sm'
    };
    var boxInst = showMsg(msgcfg);
    svcfnc(item.workId || item.username).then(function (newCtrlFlag) {
        msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
        setTimeout(function () {
            boxInst.close();
        }, msgOkTimeout);
        // 改变管控状态
        item.ctlflag = newCtrlFlag;
    }, function (errobj) {
        msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>，' + ensureErrMsg(errobj);
        setTimeout(function () {
            boxInst.close();
        }, msgErrTimeout);
    }).finally(function () {
    });
}

var fscnCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', function (sgop, timeout, AccSvc, msgbox) {
    //console.log('fscwCtrl ->管控对账联系人');

    refreshGridFnc(AccSvc.paymentNotifiers, sgop)();

    sgop.lock = function (item) {
        ctrlUser(item, AccSvc.lockNotifier, msgbox);
    };
    sgop.unlock = function (item) {
        ctrlUser(item, AccSvc.unlockNotifier, msgbox);
    };
}];
app.controller('fscnCtrl', fscnCtrl);

var fscwCtrl = ['$scope', '$timeout', 'AccountService', '$uibModal', function (sgop, timeout, AccSvc, msgbox) {
    //console.log('fscnCtrl ->管控代理商财务员');

    refreshGridFnc(AccSvc.financialWorkers, sgop)();

    sgop.lock = function (item) {
        ctrlUser(item, AccSvc.lockFworker, msgbox);
    };
    sgop.unlock = function (item) {
        ctrlUser(item, AccSvc.unlockFworker, msgbox);
    };
}];
app.controller('fscwCtrl', fscwCtrl);

var fsdCtrl = ['$scope', '$timeout', 'MgmtSvc', '$uibModal', function (sgop, timeout, MgmtSvc, msgbox) {
    sgop.backupDB = function () {
        var msgcfg = {
            msgbox: msgbox,
            title: '备份数据库操作'
            , msgHtml: '操作<strong class="text-info">执行中</strong>'
            , size: 'sm'
        };
        var boxInst = showMsg(msgcfg);
        MgmtSvc.backupDB().then(function (ok) {
            msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
            timeout(function () {
                boxInst.close();
            }, msgOkTimeout);
        }, function (fail) {
            msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>';
            timeout(function () {
                boxInst.close();
            }, msgErrTimeout);
        });
    };

    sgop.restorePoints = refreshGridFnc(MgmtSvc.getDBBackups, sgop);

    sgop.restoreDB = function (item) {
        var msgConfirm = {
            msgbox: msgbox
            , title: '恢复数据库操作'
            , msgHtml: '确定将数据库的数据恢复到 <strong class="text-danger">' + item.timeStr + '</strong> 时间点吗?'
            , okBtn: true
            , cancelBtn: true
            , important: true
        };
        showMsg(msgConfirm).result.then(function () {
            var msgcfg = {
                msgbox: msgbox,
                title: '恢复数据库操作'
                , msgHtml: '操作<strong class="text-info">执行中</strong>'
                , size: 'sm'
            };
            var boxInst = showMsg(msgcfg);
            MgmtSvc.restoreDB(item.id).then(function (ok) {
                msgcfg.msgHtml = '操作<strong class="text-success">成功</strong>';
                timeout(function () {
                    boxInst.close();
                }, msgOkTimeout);
            }, function (fail) {
                msgcfg.msgHtml = '操作<strong class="text-danger">失败</strong>';
                timeout(function () {
                    boxInst.close();
                }, msgErrTimeout);
            });
        });
    };
}];

// 用户日志
var fslCtrl = ['$scope', '$timeout', 'MgmtSvc', '$uibModal', function (sgop, timeout, MgmtSvc, msgbox) {
    //console.log('fslCtrl->查看用户操作日志');
    refreshGridFnc(MgmtSvc.fetchLogs, sgop)();
}];