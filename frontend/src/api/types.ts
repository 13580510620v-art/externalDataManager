export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  records: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface CurrentUser {
  id: number;
  username: string;
  fullName: string;
  permissions: string[];
}

export interface Supplier {
  id: number;
  supplierCode: string;
  supplierName: string;
  fetcherType: 'SFTP' | 'REST';
  remoteSubDir?: string | null;
  enabled: boolean;
  updateFrequency?: number | null;
  fileNameRule?: string | null;
  s3Bucket?: string | null;
  creator?: string | null;
  createTime?: string | null;
  modifier?: string | null;
  updateTime?: string | null;
}

export type SupplierInput = Omit<Supplier, 'id' | 'enabled' | 'creator' | 'createTime' | 'modifier' | 'updateTime'>;

export type DataTaskStatus =
  | 'PENDING'
  | 'DOWNLOADING'
  | 'DOWNLOAD_FAILED'
  | 'UPLOADED'
  | 'UPLOAD_FAILED'
  | 'INFORMED'
  | 'INFORM_FAILED'
  | 'COMPLETED';

export interface DataTask {
  id: number;
  supplierCode: string;
  sourceUniqueKey: string;
  sourceFileName: string;
  sourceRemotePath?: string | null;
  sourceFileSize?: number | null;
  sourceMtime?: number | null;
  fetcherType: 'SFTP' | 'REST';
  status: DataTaskStatus;
  downloadRetryTimes: number;
  updateRetryTimes: number;
  informRetryTimes: number;
  s3Bucket?: string | null;
  targetS3Key?: string | null;
  informTime?: string | null;
  feedbackFlag: 'Y' | 'N';
  createTime?: string | null;
  updateTime?: string | null;
}

export interface DataTaskDetail extends DataTask {
  localTempPath?: string | null;
  creator?: string | null;
  modifier?: string | null;
}

export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  samlNameId?: string | null;
  enabled: boolean;
  groupIds: number[];
  createTime?: string | null;
  updateTime?: string | null;
}

export interface Group {
  id: number;
  groupCode: string;
  groupName: string;
  description?: string | null;
  enabled: boolean;
  permissionIds: number[];
  permissionCodes: string[];
  createTime?: string | null;
  updateTime?: string | null;
}

export interface Permission {
  id: number;
  permissionCode: string;
  permissionName: string;
  resourceType: string;
  action: string;
  enabled: boolean;
}

export interface DashboardSummary {
  date: string;
  total: number;
  completed: number;
  successRate: number;
  totalChange: number;
  failureChange: number;
  statusCounts: Record<string, number>;
  supplierCounts: Record<string, number>;
  recentTasks: DashboardRecentTask[];
}

export interface DashboardRecentTask {
  id: number;
  supplierName: string;
  sourceFileName: string;
  sourceFileSize?: number | null;
  status: DataTaskStatus;
  createTime?: string | null;
}

export interface AuditLog {
  id: number;
  operator: string;
  action: string;
  targetType: string;
  targetId?: string | null;
  detail?: string | null;
  createTime?: string | null;
}
