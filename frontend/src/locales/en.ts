export default {
  app: {
    brandTitle: 'External Data Management Platform',
    name: 'External Data Manager',
    subtitle: '外部数据管理平台',
    workspace: 'Web Console'
  },
  nav: {
    dashboard: 'Console',
    tasks: 'External Data',
    suppliers: 'Suppliers',
    logs: 'Logs',
    system: 'System',
    users: 'Users',
    groups: 'Groups',
    permissions: 'Permissions'
  },
  logs: {
    description: 'Review administrative audit logs'
    ,fields: {
      operator: 'Operator',
      action: 'Action',
      targetType: 'Target type',
      targetId: 'Target ID',
      detail: 'Detail',
      createTime: 'Created at'
    }
  },
  common: {
    search: 'Search',
    reset: 'Reset',
    create: 'Create',
    edit: 'Edit',
    enable: 'Enable',
    disable: 'Disable',
    retry: 'Retry',
    detail: 'Detail',
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    close: 'Close',
    actions: 'Actions',
    loading: 'Loading',
    noData: 'No data',
    total: 'Total',
    success: 'Operation succeeded',
    requestFailed: 'Request failed. Please try again later.',
    required: 'Required field',
    frequencyInvalid: 'Frequency must be at least 1',
    logout: 'Sign out',
    enabled: 'Enabled',
    disabled: 'Disabled'
  },
  login: {
    title: 'External Data Management Platform',
    subtitle: 'External Data Management Platform',
    welcome: 'Welcome back',
    description: 'Sign in to your account to access the External Data Management Platform',
    username: 'Username',
    usernamePlaceholder: 'Enter username',
    password: 'Password',
    passwordPlaceholder: 'Enter password',
    submit: 'Sign in',
    required: 'Enter your username and password',
    or: 'or use',
    sso: 'SSO Sign-in (SAML)',
    samlDisabled: 'SSO is disabled. Ask an administrator to configure SAML.',
    samlError: 'SAML sign-in failed. Contact your administrator.'
  },
  dashboard: {
    title: 'External Data Management Console',
    description: 'Monitor supplier file uploads and track successful and failed records',
    total: 'Total uploaded files',
    completed: 'Successful uploads',
    successRate: 'Success rate',
    failed: 'Failed uploads',
    comparedLastWeek: 'vs. last week',
    recentRecords: 'Recent upload records',
    items: 'items',
    emptyRecords: 'No upload records',
    notificationDisabled: 'Notifications are not enabled'
  },
  tasks: {
    title: 'Data Tasks',
    description: 'Query external data file tasks and retry failed tasks',
    supplier: 'Supplier',
    status: 'Status',
    fileName: 'File name',
    feedback: 'Downstream feedback',
    timeRange: 'Created time',
    retryConfirm: 'Retry this failed task?',
    fields: {
      id: 'ID',
      supplierCode: 'Supplier code',
      sourceFileName: 'Source file name',
      sourceUniqueKey: 'Unique key',
      sourceRemotePath: 'Remote path',
      sourceFileSize: 'File size',
      fetcherType: 'Fetcher type',
      status: 'Status',
      downloadRetryTimes: 'Download retries',
      updateRetryTimes: 'Upload retries',
      informRetryTimes: 'Notify retries',
      s3Bucket: 'S3 bucket',
      targetS3Key: 'S3 key',
      feedbackFlag: 'Feedback',
      createTime: 'Created at',
      updateTime: 'Updated at'
    }
  },
  suppliers: {
    title: 'Supplier Management',
    description: 'Maintain external data supplier integrations',
    keyword: 'Keyword',
    fetcherType: 'Fetcher type',
    enabledStatus: 'Status',
    supplierCode: 'Supplier code',
    supplierName: 'Supplier name',
    remoteSubDir: 'Remote subdirectory',
    updateFrequency: 'Frequency (minutes)',
    fileNameRule: 'File name rule',
    s3Bucket: 'S3 bucket',
    disableConfirm: 'Disable this supplier? The platform will stop fetching its data.'
  },
  system: {
    users: {
      title: 'User Management',
      description: 'Maintain users, account status, and group memberships',
      username: 'Username',
      password: 'Password',
      newPassword: 'New password',
      fullName: 'Full name',
      email: 'Email',
      samlNameId: 'SAML Name ID',
      groups: 'Groups',
      keyword: 'Keyword'
    },
    groups: {
      title: 'Group Management',
      description: 'Maintain groups and permission sets',
      groupCode: 'Group code',
      groupName: 'Group name',
      descriptionLabel: 'Description',
      permissions: 'Permissions'
    },
    permissions: {
      title: 'Permission Management',
      description: 'View system permission codes',
      permissionCode: 'Permission code',
      permissionName: 'Permission name',
      resourceType: 'Resource type',
      action: 'Action'
    }
  },
  status: {
    PENDING: 'Pending',
    DOWNLOADING: 'Downloading',
    DOWNLOAD_FAILED: 'Download failed',
    UPLOADED: 'Uploaded',
    UPLOAD_FAILED: 'Upload failed',
    INFORMED: 'Informed',
    INFORM_FAILED: 'Notify failed',
    COMPLETED: 'Completed'
  }
};
