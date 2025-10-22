import api from '../api/api';

const TOKEN_KEY = 'token';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export const authService = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const response = await api.post('/login', payload);
    const data = response.data as LoginResponse;
    if (data?.token) {
      localStorage.setItem(TOKEN_KEY, data.token);
    }
    return data;
  },

  logout() {
    localStorage.removeItem(TOKEN_KEY);
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  },
};

export default authService;
