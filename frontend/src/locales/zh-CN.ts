export default {
  app: {
    brandTitle: 'External Data Management Platform',
    name: '外部数据管理平台',
    subtitle: '外部数据管理平台',
    workspace: 'Web 管理端'
  },
  nav: {
    dashboard: '控制台',
    tasks: '外部数据管理',
    suppliers: '供应商管理',
    logs: '日志管理',
    system: '系统管理',
    users: '用户管理',
    groups: '群组管理',
    permissions: '权限管理'
  },
  logs: {
    description: '查看管理端操作审计日志'
    ,fields: {
      operator: '操作人',
      action: '操作',
      targetType: '目标类型',
      targetId: '目标 ID',
      detail: '详情',
      createTime: '创建时间'
    }
  },
  common: {
    search: '查询',
    reset: '重置',
    create: '新建',
    edit: '编辑',
    enable: '启用',
    disable: '禁用',
    retry: '重试',
    detail: '详情',
    confirm: '确认',
    cancel: '取消',
    save: '保存',
    close: '关闭',
    actions: '操作',
    loading: '加载中',
    noData: '暂无数据',
    total: '共',
    success: '操作成功',
    requestFailed: '请求失败，请稍后重试',
    required: '必填项',
    frequencyInvalid: '上传频率必须大于等于 1',
    logout: '退出登录',
    enabled: '启用',
    disabled: '禁用'
  },
  login: {
    title: 'External Data Management Platform',
    subtitle: '外部数据管理平台',
    welcome: '欢迎回来',
    description: '登录您的账户以访问外部数据管理平台',
    username: '用户名',
    usernamePlaceholder: '输入用户名',
    password: '密码',
    passwordPlaceholder: '输入密码',
    submit: '登录',
    required: '请输入用户名和密码',
    or: '或使用',
    sso: 'SSO 单点登录 (SAML)',
    samlDisabled: 'SSO 未启用，请联系管理员配置 SAML',
    samlError: 'SAML 登录失败，请联系管理员'
  },
  dashboard: {
    title: '外部数据管理控制台',
    description: '监控各供应商文件上传情况，追踪成功与失败记录',
    total: '上传总文件数',
    completed: '上传成功',
    successRate: '成功率',
    failed: '上传失败',
    comparedLastWeek: '较上周',
    recentRecords: '最近上传记录',
    items: '个',
    emptyRecords: '暂无上传记录',
    notificationDisabled: '通知功能未启用'
  },
  tasks: {
    title: '数据任务',
    description: '查询外部数据文件任务，并对失败任务执行重试',
    supplier: '供应商',
    status: '状态',
    fileName: '文件名',
    feedback: '下游反馈',
    timeRange: '创建时间',
    retryConfirm: '确认重试该失败任务？',
    fields: {
      id: 'ID',
      supplierCode: '供应商编码',
      sourceFileName: '源文件名',
      sourceUniqueKey: '唯一键',
      sourceRemotePath: '远端路径',
      sourceFileSize: '文件大小',
      fetcherType: '获取类型',
      status: '状态',
      downloadRetryTimes: '下载重试',
      updateRetryTimes: '上传重试',
      informRetryTimes: '通知重试',
      s3Bucket: 'S3 Bucket',
      targetS3Key: 'S3 Key',
      feedbackFlag: '反馈',
      createTime: '创建时间',
      updateTime: '更新时间'
    }
  },
  suppliers: {
    title: '供应商管理',
    description: '维护外部数据供应商接入配置',
    keyword: '关键字',
    fetcherType: '获取类型',
    enabledStatus: '状态',
    supplierCode: '供应商编码',
    supplierName: '供应商名称',
    remoteSubDir: '远端子目录',
    updateFrequency: '上传频率（分钟）',
    fileNameRule: '文件命名规则',
    s3Bucket: 'S3 Bucket',
    disableConfirm: '确认禁用该供应商？禁用后平台将停止拉取其数据。'
  },
  system: {
    users: {
      title: '用户管理',
      description: '维护系统用户、账号状态和群组关系',
      username: '用户名',
      password: '密码',
      newPassword: '新密码',
      fullName: '姓名',
      email: '邮箱',
      samlNameId: 'SAML Name ID',
      groups: '群组',
      keyword: '关键字'
    },
    groups: {
      title: '群组管理',
      description: '维护群组基础信息和权限集合',
      groupCode: '群组编码',
      groupName: '群组名称',
      descriptionLabel: '描述',
      permissions: '权限'
    },
    permissions: {
      title: '权限管理',
      description: '查看系统权限编码',
      permissionCode: '权限编码',
      permissionName: '权限名称',
      resourceType: '资源类型',
      action: '操作'
    }
  },
  status: {
    PENDING: '待处理',
    DOWNLOADING: '下载中',
    DOWNLOAD_FAILED: '下载失败',
    UPLOADED: '已上传',
    UPLOAD_FAILED: '上传失败',
    INFORMED: '已通知',
    INFORM_FAILED: '通知失败',
    COMPLETED: '已完成'
  }
};
