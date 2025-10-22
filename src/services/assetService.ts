import axios from 'axios'
import type { 
  Asset, 
  AssetListResponse, 
  AssetSearchParams, 
  CreateAssetRequest, 
  UpdateAssetRequest 
} from '../types/asset'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api/v1'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const assetService = {
  // Get all assets with pagination, sorting, and filtering
  async getAssets(params: AssetSearchParams = {}): Promise<AssetListResponse> {
    const response = await api.get('/assets', { params })
    return response.data
  },

  // Get asset by ID
  async getAssetById(id: string): Promise<Asset> {
    const response = await api.get(`/assets/${id}`)
    return response.data
  },

  // Create new asset
  async createAsset(asset: CreateAssetRequest): Promise<Asset> {
    const response = await api.post('/assets', asset)
    return response.data
  },

  // Update existing asset
  async updateAsset(id: string, asset: UpdateAssetRequest): Promise<Asset> {
    const response = await api.put(`/assets/${id}`, asset)
    return response.data
  },

  // Delete asset
  async deleteAsset(id: string): Promise<void> {
    await api.delete(`/assets/${id}`)
  },

  // Search assets by name, QR code, or serial number
  async searchAssets(query: string, params: AssetSearchParams = {}): Promise<AssetListResponse> {
    return this.getAssets({ ...params, search: query })
  },
}