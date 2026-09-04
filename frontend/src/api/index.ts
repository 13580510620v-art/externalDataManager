import { http } from './http';
import type {
  CurrentUser,
  DashboardSummary,
  DataTask,
  DataTaskDetail,
  Group,
  AuditLog,
  PageResponse,
  Permission,
  Supplier,
  SupplierInput,
  User
} from './types';

type QueryParams = Record<string, string | number | boolean | undefined | null>;

export const authApi = {
  login: (username: string, password: string) =>
    http.post<CurrentUser>('/api/auth/login', { username, password }),
  logout: () => http.post<void>('/api/auth/logout'),
  fetchMe: () => http.get<CurrentUser>('/api/auth/me'),
  samlEnabled: () => http.get<boolean>('/api/auth/saml/enabled')
};

export const dashboardApi = {
  today: () => http.get<DashboardSummary>('/api/dashboard/today')
};

export const taskApi = {
  page: (params: QueryParams) => http.get<PageResponse<DataTask>>('/api/tasks', { params }),
  detail: (id: number) => http.get<DataTaskDetail>(`/api/tasks/${id}`),
  retry: (id: number) => http.post<DataTask>(`/api/tasks/${id}/retry`)
};

export const supplierApi = {
  page: (params: QueryParams) => http.get<PageResponse<Supplier>>('/api/suppliers', { params }),
  create: (data: SupplierInput) => http.post<Supplier>('/api/suppliers', data),
  update: (id: number, data: SupplierInput) => http.post<Supplier>(`/api/suppliers/${id}/update`, data),
  enable: (id: number) => http.post<Supplier>(`/api/suppliers/${id}/enable`),
  disable: (id: number) => http.post<Supplier>(`/api/suppliers/${id}/disable`)
};

export interface UserInput {
  username: string;
  password?: string;
  fullName: string;
  email: string;
  samlNameId?: string | null;
  enabled: boolean;
  groupIds: number[];
}

export const userApi = {
  page: (params: QueryParams) => http.get<PageResponse<User>>('/api/users', { params }),
  create: (data: UserInput & { password: string }) => http.post<User>('/api/users', data),
  update: (id: number, data: UserInput) => http.post<User>(`/api/users/${id}/update`, data),
  enable: (id: number) => http.post<User>(`/api/users/${id}/enable`),
  disable: (id: number) => http.post<User>(`/api/users/${id}/disable`)
};

export interface GroupInput {
  groupCode: string;
  groupName: string;
  description?: string | null;
  enabled: boolean;
}

export const groupApi = {
  page: (params: QueryParams) => http.get<PageResponse<Group>>('/api/groups', { params }),
  create: (data: GroupInput) => http.post<Group>('/api/groups', data),
  update: (id: number, data: GroupInput) => http.post<Group>(`/api/groups/${id}/update`, data),
  assignPermissions: (id: number, permissionIds: number[]) =>
    http.post<Group>(`/api/groups/${id}/permissions`, { permissionIds })
};

export const permissionApi = {
  list: () => http.get<Permission[]>('/api/permissions')
};

export const auditLogApi = {
  page: (params: QueryParams) => http.get<PageResponse<AuditLog>>('/api/audit-logs', { params })
};
