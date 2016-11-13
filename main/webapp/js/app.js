"use strict";

var app = angular.module('sany', ['ui.router', 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'smart-table', 'ngCookies', 'ui.router.stateHelper', 'ngFileUpload', 'bootstrapLightbox']);

// 修改后端请求URL
var ReqUrl = {
    // 登陆
    signIn: '/check_Accout/PMController/login',
    // 注册
    signUp: '/check_Accout/PMController/as_register',
    // 注销（登出）
    signOut: '/check_Accout/PMController/signout',
    // 财务人员->订单
    //   fwOrders:'/check_Accout/Check_MainController/upload_success',
    fwOrders: '/check_Accout/Check_MainController/Watch',
    // fwOrderMonths: '/fw/ordermonths',
    // 财务人员 上传
    fwOrderUpload: '/check_Accout/Check_MainController/upload',
    //财务人员 -> 系统无法关联的付款通知（用户上传的数数据）
    fwFncReminds: '/check_Accout/Check_MainController/Watch',
    // fwFncBadReminds:'fw/fncBadReminds',
    // 财务人员->未关联订单的出纳记录
    fwFncBankTrans: '/check_Accout/Check_MainController/Watch',
    //关联出纳到订单或客户
    fwAssocTrans: '/check_Accout/Check_MainController/Modify',
    // 审核通过付款通知（用户上传数据）
    fwPaymentNotifApprov: '/check_Accout/Check_MainController/Check',
    //关联付款通知到出纳信息
    attachToTrans: '/check_Accout/Check_MainController/Map',
    //待关联付款通知的出纳猴候选集
    attachToTransCandidates: '/check_Accout/Check_MainController/Map',
    // 对账操作
    checkWork: '/check_Accout/Check_MainController/Start_CheckA_Work',
    // 查看对账结果
    checkResult: '/check_Accout/Check_MainController/Watch_CheckA_Result',
    // 导出对账结果报表
    checkResultExport: '/check_Accout/Check_MainController/Export_CheckA_Result',
    // 历史对账结果查询
    historyResults: '/check_Accout/Check_MainController/Watch',
    // 重新对账
    reCheck: '/check_Accout/Check_MainController/ClAndStAgain_CheckA'
    // 历史对账结果中的重新对账操作
    ,recheck2:'/check_Accout/Check_MainController/historyca'
    // 认可对账结果操作
    , accChkRlt: '/check_Accout/Check_MainController/freeback'
    // // 新付款通知信息（用户上传）数目
    // newNotifNum: '/check_Accout/Check_MainController/goto_main',
    // “预览”付款通知数据（用户上传）
    , notifView: '/check_Accout/Check_MainController/Watch',
    // 准备对账的数据环境（进入对账模式申请）
    prepareChkEnv: '/check_Accout/Check_MainController/enter_camodel'

    // 注册申请中的对账联系人列表获取
    , regPendingNotifiers: '/check_Accout/PMController/watch'
    // 注册申请中的代理商列表获取
    , regPendingFworkers: '/check_Accout/PMController/watch'
    // 审阅对账联系人注册
    , approveNotifier: '/check_Accout/PMController/verify_register'
    // 审阅代理商注册
    , approveFw: '/check_Accout/PMController/verify_register'
    // 已注册的对账联系人
    , notifiers: '/check_Accout/PMController/watch'
    // 已注册代理商财务员
    , fworkers: '/check_Accout/PMController/watch'
    // 锁定、解锁联系人
    , ctrlNotifier: '/check_Accout/PMController/control_power'
    // 锁定、解锁代理商财务员
    , ctrlFworker: '/check_Accout/PMController/control_power'

    // 备份数据库操作
    , backupdb: '/check_Accout/PMController/backupdb'
    // 日志获取
    , viewLog: '/check_Accout/PMController/watch'
    // 获取代理商列表
    , fetchAgents: '/check_Accout/PMController/get_agentcodeAname'
};

