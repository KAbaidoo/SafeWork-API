import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
}

const TOKEN_KEY = 'token'

export const authService = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const res = await api.post('/login', payload)
    // expect response shape { token: string }
    const data = res.data as LoginResponse
    if (data?.token) {
      localStorage.setItem(TOKEN_KEY, data.token)
    }
    return data
  },

  logout() {
    localStorage.removeItem(TOKEN_KEY)
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem(TOKEN_KEY)
  },
}

export default authService
