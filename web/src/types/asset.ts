export interface Asset {
  id: string
  name: string
  qr_code_id: string
  serial_number: string
  status: AssetStatus
  location: string
  last_inspection_date: string | null
  next_service_date: string | null
  warranty_expiration: string | null
  custom_attributes_json: Record<string, unknown>
  version: number
  created_at: string
}

export enum AssetStatus {
  IN_SERVICE = 'in_service',
  MAINTENANCE = 'maintenance',
  OUT_OF_SERVICE = 'out_of_service'
}

export interface CreateAssetRequest {
  name: string
  qr_code_id: string
  serial_number?: string
  status: AssetStatus
  location?: string
  next_service_date?: string
  warranty_expiration?: string
  custom_attributes_json?: Record<string, unknown>
}

export interface UpdateAssetRequest extends CreateAssetRequest {
  version: number
}

export interface AssetListResponse {
  data: Asset[]
  total: number
  page: number
  size: number
}

export interface AssetSearchParams {
  page?: number
  size?: number
  search?: string
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
}