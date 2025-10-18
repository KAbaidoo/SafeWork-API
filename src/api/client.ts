// Minimal typed API client for SafeWork
// - Uses Vite env var VITE_API_BASE or falls back to http://localhost:8081/api
// - Attaches Authorization from localStorage key `token` (simple default)
// - Exposes a few typed helpers used by the frontend

export type ErrorResponse = {
  message: string;
  errors?: Record<string, unknown> | Array<unknown>;
};

export class ApiError extends Error {
  status: number;
  body?: unknown;
  constructor(status: number, message: string, body?: unknown) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

export class ConflictError extends ApiError {}

const BASE =
  ((import.meta.env as unknown as Record<string, unknown>).VITE_API_BASE as string | undefined) ??
  'http://localhost:8081/api';

function getToken(): string | null {
  try {
    return localStorage.getItem('token');
  } catch {
    return null;
  }
}

async function parseBodyIfJson(res: Response) {
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return res.text();
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers as HeadersInit);
  headers.set('Accept', 'application/json');
  if (!headers.has('Content-Type') && init.body) headers.set('Content-Type', 'application/json');

  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const res = await fetch(`${BASE}${path}`, { ...init, headers });

  if (!res.ok) {
    const body = await parseBodyIfJson(res).catch(() => undefined);
    const message = (body && body.message) || res.statusText || 'API error';
    if (res.status === 409) throw new ConflictError(res.status, message, body);
    throw new ApiError(res.status, message, body);
  }

  // successful but may be empty
  if (res.status === 204) return undefined as unknown as T;
  return (await parseBodyIfJson(res)) as T;
}

// --- Types (small subset used by the frontend)
export type LoginResponse = {
  token: string;
  id?: number;
  organizationId?: number;
  role?: string;
};

export type AssetDto = {
  id: number;
  assetTag?: string;
  name: string;
  status?: string;
};

// --- API helpers
export const auth = {
  login: (email: string, password: string) =>
    request<LoginResponse>('/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
};

export const assets = {
  list: (page = 0, size = 20) =>
    request<{ content: AssetDto[]; pageable: unknown; totalElements: number }>(
      `/v1/assets?page=${page}&size=${size}`,
    ),
  get: (id: number | string) => request<AssetDto>(`/v1/assets/${id}`),
  create: (payload: Partial<AssetDto> & Record<string, unknown>) =>
    request<AssetDto>('/v1/assets', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number | string, payload: Partial<AssetDto> & { version?: number }) =>
    request<AssetDto>(`/v1/assets/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  delete: (id: number | string) => request<void>(`/v1/assets/${id}`, { method: 'DELETE' }),
};

export const checklists = {
  list: () => request<Array<{ id: number; title: string; version?: number }>>('/v1/checklists'),
  update: (id: number | string, payload: Record<string, unknown>) =>
    request(`/v1/checklists/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
};

export const inspections = {
  create: (payload: Record<string, unknown>) =>
    request('/v1/inspections', { method: 'POST', body: JSON.stringify(payload) }),
  get: (id: number | string) => request<Record<string, unknown>>(`/v1/inspections/${id}`),
};

export const issues = {
  create: (payload: Record<string, unknown>) =>
    request('/v1/issues', { method: 'POST', body: JSON.stringify(payload) }),
  list: () => request<Array<Record<string, unknown>>>('/v1/issues'),
};

export const analytics = {
  completionRate: () => request<Record<string, unknown>>('/analytics/completion-rate'),
  topIssues: () => request<Record<string, unknown>>('/analytics/top-issues'),
};

export default {
  request,
  auth,
  assets,
  checklists,
  inspections,
  issues,
  analytics,
  ApiError,
  ConflictError,
};
