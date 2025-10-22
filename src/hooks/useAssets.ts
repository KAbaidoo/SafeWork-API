import { useState, useEffect } from 'react';

// Asset interface based on the PRD data model
export interface Asset {
  id: number;
  assetTag: string;
  name: string;
  qrCodeId: string;
  status: 'ACTIVE' | 'MAINTENANCE' | 'OUT_OF_SERVICE' | 'INACTIVE';
  organizationId?: number;
  assignedToUserId?: number;
  version: number;
  currentLocation?: string;
  lastInspectionDate?: string;
  nextServiceDate?: string;
  warrantyExpiration?: string;
  serialNumber?: string;
  customAttributes?: Record<string, unknown>;
}

export interface AssetFilters {
  search?: string;
  status?: string;
  page?: number;
  pageSize?: number;
}

interface UseAssetsData {
  assets: Asset[];
  loading: boolean;
  error: string | null;
  totalCount: number;
  page: number;
  pageSize: number;
}

interface UseAssetsResult extends UseAssetsData {
  refetch: () => void;
}

const useAssets = (filters: AssetFilters = {}): UseAssetsResult => {
  const [data, setData] = useState<UseAssetsData>({
    assets: [],
    loading: true,
    error: null,
    totalCount: 0,
    page: filters.page || 0,
    pageSize: filters.pageSize || 25,
  });

  // Use a flag to trigger refetch
  const [refetchFlag, setRefetchFlag] = useState(0);

  useEffect(() => {
    const fetchAssets = async () => {
      try {
        setData((prev) => ({ ...prev, loading: true, error: null }));

        // For development, use mock data directly since the API might not be ready
        const mockAssets: Asset[] = generateMockAssets();

        // Mock server-side filtering for development
        let filteredAssets = [...mockAssets];

        // Apply search filter (mock implementation)
        if (filters.search) {
          const searchLower = filters.search.toLowerCase();
          filteredAssets = filteredAssets.filter(
            (asset) =>
              asset.name?.toLowerCase().includes(searchLower) ||
              asset.qrCodeId?.toLowerCase().includes(searchLower) ||
              asset.assetTag?.toLowerCase().includes(searchLower),
          );
        }

        // Apply status filter (mock implementation)
        if (filters.status && filters.status !== 'all') {
          filteredAssets = filteredAssets.filter((asset) => asset.status === filters.status);
        }

        // Mock pagination
        const totalCount = filteredAssets.length;
        const startIndex = (filters.page || 0) * (filters.pageSize || 25);
        const endIndex = startIndex + (filters.pageSize || 25);
        const paginatedAssets = filteredAssets.slice(startIndex, endIndex);

        setData({
          assets: paginatedAssets,
          loading: false,
          error: null,
          totalCount: totalCount,
          page: filters.page || 0,
          pageSize: filters.pageSize || 25,
        });
      } catch (error) {
        console.error('Failed to fetch assets:', error);

        // Fallback to mock data on error
        const mockAssets = generateMockAssets();
        setData({
          assets: mockAssets,
          loading: false,
          error: null, // Don't show error, just use mock data
          totalCount: mockAssets.length,
          page: filters.page || 0,
          pageSize: filters.pageSize || 25,
        });
      }
    };

    fetchAssets();
  }, [filters.search, filters.status, filters.page, filters.pageSize, refetchFlag]);

  const refetch = () => {
    setRefetchFlag((prev) => prev + 1);
  };

  return { ...data, refetch };
};

// Generate mock data for development
const generateMockAssets = (): Asset[] => {
  const statuses: Asset['status'][] = ['ACTIVE', 'MAINTENANCE', 'OUT_OF_SERVICE', 'INACTIVE'];
  const locations = [
    'Warehouse A',
    'Production Floor',
    'Maintenance Bay',
    'Storage Room',
    'Loading Dock',
  ];
  const assetTypes = ['Forklift', 'Conveyor Belt', 'Crane', 'Generator', 'Compressor', 'Pump'];

  return Array.from({ length: 25 }, (_, index) => {
    const assetType = assetTypes[index % assetTypes.length];
    const assetNumber = String(index + 1).padStart(3, '0');

    return {
      id: index + 1,
      assetTag: `${assetType.toUpperCase().slice(0, 2)}-${assetNumber}`,
      name: `${assetType} #${assetNumber}`,
      qrCodeId: `QR-${assetType.toUpperCase().slice(0, 2)}-${assetNumber}`,
      status: statuses[index % statuses.length],
      organizationId: 1,
      assignedToUserId: Math.floor(Math.random() * 10) + 1,
      version: 0,
      currentLocation: locations[index % locations.length],
      lastInspectionDate: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0],
      nextServiceDate: new Date(Date.now() + Math.random() * 90 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0],
      warrantyExpiration: new Date(Date.now() + Math.random() * 365 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0],
      serialNumber: `SN-${Math.random().toString(36).substr(2, 9).toUpperCase()}`,
      customAttributes: {
        manufacturer: ['Caterpillar', 'Toyota', 'Komatsu', 'Hyster'][index % 4],
        model: `Model-${Math.random().toString(36).substr(2, 4).toUpperCase()}`,
        year: 2015 + (index % 8),
      },
    };
  });
};

export default useAssets;
