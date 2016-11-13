app.config(['$stateProvider', '$urlRouterProvider', 'stateHelperProvider', function ($stateProvider, $urlRouterProvider, stateHelperProvider) {
    // console.log('config states');

    // $urlRouterProvider.when('', '/login');
    // $urlRouterProvider.otherwise('/login');
    $urlRouterProvider.when('', '/index');
    $urlRouterProvider.when('/u/fw', '/u/fw/v');
    $urlRouterProvider.when('/u/fs', '/u/fs/a');
    $urlRouterProvider.when('/u/fw/p--', ['$state', function (state) {
        var thisDate = new Date();
       // state.go('u.fw.p', {year: thisDate.getFullYear(), month: thisDate.getMonth()});
        state.go('u.fw.p', {year: thisDate.getFullYear(), month: thisDate.getMonth()+1});
    }]);
    $urlRouterProvider.otherwise('/index');

    // $stateProvider
    stateHelperProvider

        .state({
            name: 'index',
            url: '/index',
            template: '<img class="row" src="welcome.jpg">',
            controller: function () {
                $('#loginModal').modal('show');
            }
        })

        // login
        //     .state({
        //         name: 'login',
        //         url: '/login',
        //         // templateUrl: 'login.html',
        //         controller: signInCtrl
        //     })
        // register
        // .state({
        //     name: 'register',
        //     url: '/register',
        //     // templateUrl: 'register.html',
        //     controller: signUpCtrl,
        //     controllerAs: 'signUpCtrl'
        // })
        // reset password, for hyper link 'forget password?'
        // .state({
        //     name: 'resetPassword',
        //     url: '/reset-password',
        //     templateUrl: 'reset-password.html',
        //     controller: resetPasswordCtrl
        // })

        // logged in user, (fw, fs, sa)
        .state({
            name: 'u',
            abstract: true,
            url: '/u',
            template: '<div data-ui-view=""></div>',
            controller: uCtrl,
            children: [
                // 财务人员
                {
                    name: 'fw',
                    // abstract: true,
                    url: '/fw',    // will be appended to url of parent state
                    views: {
                        "": {
                            templateUrl: 'fw.html',
                            controller: fwCtrl
                        },
                        // "": {
                        //     template: '<div ui-view=""></div> ',
                        //     // templateUrl: '',
                        // }
                    },
                    children: [
                        // // 财务人员“订单”
                        // {
                        //     name: 'o',
                        //     url: '/o',
                        //     views: {
                        //         //ui-view=content content对于不同的一级导航(对应state u.fw.o, u.fw.n, u.fw.t）右侧内容视图部分用不同的template文件填充。
                        //         "content@u": {
                        //             templateUrl: 'fw-c-o.html',
                        //             controller: fwoCtrl,
                        //             controllerAs:'fwoCtrl'
                        //         },
                        //         // //因为ui-view=grid在fw-c-o.html中，而该文件的加载是在state: u.fw.o中完成的，因此必须用@指定绝对路径的state为u.w.o，否则相当于在其上级状态（state:u.fw）中替换ui-view=grid的视图占位符（ui-view），而实际上state:u.fw中没有名为grid的视图占位符。
                        //         // "grid@u.fw.o": {
                        //         //     templateUrl: 'fw-c-o-grid.html',
                        //         //     controller: fwoGridCtrl
                        //         // }
                        //     }
                        // },
                        // // 财务人员“待办”
                        // {
                        //     name: 'n',
                        //     url: '/n',
                        //     views: {
                        //         "content@u": {
                        //             templateUrl: 'fw-c-n-grid.html',
                        //             controller: fwnCtrl
                        //         }
                        //     }
                        // },
                        // // 财务人员->待关联出纳
                        // {
                        //     name: 't',
                        //     url: '/t',
                        //     views: {
                        //         "content@u": {
                        //             templateUrl: 'fw-c-t-grid.html',
                        //             controller: fwtCtrl,
                        //             // controllerAs:'fwtCtrl'
                        //             controllerAs:'ctrl'
                        //             // controller:'FwtCtrl as fwtCtrl'
                        //         }
                        //     }
                        // }

                        // 上传
                        {
                            name: "p",
                            url: '/p-:year-:month',
                            views: {
                                "": {
                                    templateUrl: 'fw-c-c.html',
                                    controller: fwcCtrl
                                },
                                "@u.fw.p": {
                                    templateUrl: 'fw-c-upload.html',
                                }
                            },
                            children: [
                                {
                                    // 处理用户上传的数据，然后执行“对账”操作
                                    name: 'm',
                                    url: '/m',
                                    // views: {
                                    //     "@u.fw.p": {
                                    templateUrl: 'fw-c-n-grid.html',
                                    controller: fwnCtrl,
                                    //     }
                                    // },
                                    children: [
                                        {
                                            // 展示对账结果
                                            name: "x",
                                            url: '/x',
                                            views: {
                                                "@u.fw.p": {
                                                    templateUrl: 'fw-c-x.html',
                                                    controller: fwxCtrl,
                                                    controllerAs: 'ctrl',
                                                }
                                            }
                                        }
                                    ]
                                }

                            ]
                        }
                        , {
                            name: 'h',
                            url: '/h',
                            views: {
                                "": {
                                    templateUrl: 'fw-c-x-history.html',
                                    controller: fwhCtrl
                                }
                            }
                        },
                        {
                            name: 'v',
                            url: '/v',
                            views: {
                                "": {
                                    templateUrl: 'fw-c-n-grid.html',
                                    controller: fwvCtrl
                                }
                            }
                        }
                    ]
                },
                {
                    name: 'fs',     // system admin / maintaince
                    url: '/fs',
                    templateUrl: 'fs.html'
                    , controller: fsCtrl
                    , children: [
                    {
                        name: 'a',
                        url: '/a',
                        templateUrl: 'fs-reg-approval.html'
                    },
                    {
                        name: 'c',
                        url: '/c',
                        templateUrl: 'fs-user-ctrl.html'
                    }
                    , {
                        name: 'l',
                        url: '/l',
                        templateUrl: 'fs-user-log.html',
                        controller: fslCtrl
                    }
                    , {
                        name: 'd',
                        url: '/d',
                        template: '<div><p class="text-center h3">数据库操作</p><br><a class="btn btn-primary text-center " ng-click="backupDB()">备份数据库</a></div>',
                        controller: fsdCtrl
                    }
                ]
                }
            ]
        })
    ;
}]);