import api from '../api/api';
import type {
  Asset,
  AssetListResponse,
  AssetSearchParams,
  CreateAssetRequest,
  UpdateAssetRequest,
} from '../types/asset';

export const assetService = {
  // Get all assets with pagination, sorting, and filtering
  async getAssets(params: AssetSearchParams = {}): Promise<AssetListResponse> {
    const response = await api.get('/assets', { params });
    return response.data;
  },

  // Get asset by ID
  async getAssetById(id: string): Promise<Asset> {
    const response = await api.get(`/assets/${id}`);
    return response.data;
  },

  // Create new asset
  async createAsset(asset: CreateAssetRequest): Promise<Asset> {
    const response = await api.post('/assets', asset);
    return response.data;
  },

  // Update existing asset
  async updateAsset(id: string, asset: UpdateAssetRequest): Promise<Asset> {
    const response = await api.put(`/assets/${id}`, asset);
    return response.data;
  },

  // Delete asset
  async deleteAsset(id: string): Promise<void> {
    await api.delete(`/assets/${id}`);
  },

  // Search assets by name, QR code, or serial number
  async searchAssets(query: string, params: AssetSearchParams = {}): Promise<AssetListResponse> {
    return this.getAssets({ ...params, search: query });
  },
};
