export default {
  app: {
    brandTitle: 'External Data Management Platform',
    name: '外部數據管理平台',
    subtitle: '外部數據管理平台',
    workspace: 'Web 管理端'
  },
  nav: {
    dashboard: '控制台',
    tasks: '外部數據管理',
    suppliers: '供應商管理',
    logs: '日誌管理',
    system: '系統管理',
    users: '用戶管理',
    groups: '群組管理',
    permissions: '權限管理'
  },
  logs: {
    description: '查看管理端操作審計日誌'
    ,fields: {
      operator: '操作人',
      action: '操作',
      targetType: '目標類型',
      targetId: '目標 ID',
      detail: '詳情',
      createTime: '創建時間'
    }
  },
  common: {
    search: '查詢',
    reset: '重置',
    create: '新建',
    edit: '編輯',
    enable: '啟用',
    disable: '停用',
    retry: '重試',
    detail: '詳情',
    confirm: '確認',
    cancel: '取消',
    save: '保存',
    close: '關閉',
    actions: '操作',
    loading: '載入中',
    noData: '暫無數據',
    total: '共',
    success: '操作成功',
    requestFailed: '請求失敗，請稍後重試',
    required: '必填項',
    frequencyInvalid: '上傳頻率必須大於等於 1',
    logout: '退出登入',
    enabled: '啟用',
    disabled: '停用'
  },
  login: {
    title: 'External Data Management Platform',
    subtitle: '外部數據管理平台',
    welcome: '歡迎回來',
    description: '登入您的帳戶以存取外部數據管理平台',
    username: '用戶名',
    usernamePlaceholder: '輸入用戶名',
    password: '密碼',
    passwordPlaceholder: '輸入密碼',
    submit: '登入',
    required: '請輸入用戶名和密碼',
    or: '或使用',
    sso: 'SSO 單點登入 (SAML)',
    samlDisabled: 'SSO 未啟用，請聯繫管理員配置 SAML',
    samlError: 'SAML 登入失敗，請聯繫管理員'
  },
  dashboard: {
    title: '外部數據管理控制台',
    description: '監控各供應商文件上傳情況，追蹤成功與失敗記錄',
    total: '上傳總文件數',
    completed: '上傳成功',
    successRate: '成功率',
    failed: '上傳失敗',
    comparedLastWeek: '較上週',
    recentRecords: '最近上傳記錄',
    items: '個',
    emptyRecords: '暫無上傳記錄',
    notificationDisabled: '通知功能未啟用'
  },
  tasks: {
    title: '數據任務',
    description: '查詢外部數據文件任務，並對失敗任務執行重試',
    supplier: '供應商',
    status: '狀態',
    fileName: '文件名',
    feedback: '下游反饋',
    timeRange: '創建時間',
    retryConfirm: '確認重試該失敗任務？',
    fields: {
      id: 'ID',
      supplierCode: '供應商編碼',
      sourceFileName: '源文件名',
      sourceUniqueKey: '唯一鍵',
      sourceRemotePath: '遠端路徑',
      sourceFileSize: '文件大小',
      fetcherType: '獲取類型',
      status: '狀態',
      downloadRetryTimes: '下載重試',
      updateRetryTimes: '上傳重試',
      informRetryTimes: '通知重試',
      s3Bucket: 'S3 Bucket',
      targetS3Key: 'S3 Key',
      feedbackFlag: '反饋',
      createTime: '創建時間',
      updateTime: '更新時間'
    }
  },
  suppliers: {
    title: '供應商管理',
    description: '維護外部數據供應商接入配置',
    keyword: '關鍵字',
    fetcherType: '獲取類型',
    enabledStatus: '狀態',
    supplierCode: '供應商編碼',
    supplierName: '供應商名稱',
    remoteSubDir: '遠端子目錄',
    updateFrequency: '上傳頻率（分鐘）',
    fileNameRule: '文件命名規則',
    s3Bucket: 'S3 Bucket',
    disableConfirm: '確認停用該供應商？停用後平台將停止拉取其數據。'
  },
  system: {
    users: {
      title: '用戶管理',
      description: '維護系統用戶、賬號狀態和群組關係',
      username: '用戶名',
      password: '密碼',
      newPassword: '新密碼',
      fullName: '姓名',
      email: '郵箱',
      samlNameId: 'SAML Name ID',
      groups: '群組',
      keyword: '關鍵字'
    },
    groups: {
      title: '群組管理',
      description: '維護群組基礎信息和權限集合',
      groupCode: '群組編碼',
      groupName: '群組名稱',
      descriptionLabel: '描述',
      permissions: '權限'
    },
    permissions: {
      title: '權限管理',
      description: '查看系統權限編碼',
      permissionCode: '權限編碼',
      permissionName: '權限名稱',
      resourceType: '資源類型',
      action: '操作'
    }
  },
  status: {
    PENDING: '待處理',
    DOWNLOADING: '下載中',
    DOWNLOAD_FAILED: '下載失敗',
    UPLOADED: '已上傳',
    UPLOAD_FAILED: '上傳失敗',
    INFORMED: '已通知',
    INFORM_FAILED: '通知失敗',
    COMPLETED: '已完成'
  }
};
