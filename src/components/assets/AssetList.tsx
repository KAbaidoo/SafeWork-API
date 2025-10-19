import React, { useState, useEffect } from 'react';
import { Box, Button, TextField, Typography, Chip, Card, CardContent } from '@mui/material';
import { Add as AddIcon, Search as SearchIcon } from '@mui/icons-material';
import { DataGrid, GridColDef, GridPaginationModel, GridRowParams } from '@mui/x-data-grid';
import { useNavigate } from 'react-router-dom';
import { assetService } from '../../services/assetService';
import { Asset, AssetStatus } from '../../types/asset';
import AssetModal from './AssetModal';

// Mock user role for now - will be replaced with actual auth
const mockUserRole = 'admin'; // or 'supervisor'

const getStatusColor = (status: AssetStatus) => {
  switch (status) {
    case AssetStatus.IN_SERVICE:
      return 'success';
    case AssetStatus.MAINTENANCE:
      return 'warning';
    case AssetStatus.OUT_OF_SERVICE:
      return 'error';
    default:
      return 'default';
  }
};

const getStatusLabel = (status: AssetStatus) => {
  switch (status) {
    case AssetStatus.IN_SERVICE:
      return 'In Service';
    case AssetStatus.MAINTENANCE:
      return 'Maintenance';
    case AssetStatus.OUT_OF_SERVICE:
      return 'Out of Service';
    default:
      return status;
  }
};

export default function AssetList() {
  const navigate = useNavigate();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalRows, setTotalRows] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 25,
  });
  const [createModalOpen, setCreateModalOpen] = useState(false);

  const columns: GridColDef[] = [
    {
      field: 'name',
      headerName: 'Asset Name',
      flex: 1,
      minWidth: 200,
      renderCell: (params) => (
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          {params.value}
        </Typography>
      ),
    },
    {
      field: 'qr_code_id',
      headerName: 'QR Code ID',
      width: 150,
    },
    {
      field: 'status',
      headerName: 'Status',
      width: 130,
      renderCell: (params) => (
        <Chip
          label={getStatusLabel(params.value)}
          color={getStatusColor(params.value)}
          size="small"
        />
      ),
    },
    {
      field: 'location',
      headerName: 'Current Location',
      width: 180,
    },
    {
      field: 'last_inspection_date',
      headerName: 'Last Inspection',
      width: 140,
      renderCell: (params) => (params.value ? new Date(params.value).toLocaleDateString() : 'N/A'),
    },
  ];

  const fetchAssets = async () => {
    setLoading(true);
    try {
      const response = await assetService.getAssets({
        page: paginationModel.page,
        size: paginationModel.pageSize,
        search: searchQuery || undefined,
      });

      // Debug log to inspect actual API shape if needed
      // console.debug('assets API response:', response)

      // The backend may return several shapes:
      // 1) An array of items
      // 2) { data: [...], total: N }
      // 3) Spring-style Page: { content: [...], totalElements }
      let itemsRaw: unknown[] = [];
      let total = 0;

      if (Array.isArray(response)) {
        itemsRaw = response;
        total = itemsRaw.length;
      } else if (response && typeof response === 'object') {
        const respObj = response as unknown as Record<string, unknown>;
        const maybeData = respObj.data;
        const maybeContent = respObj.content;

        if (Array.isArray(maybeData)) {
          itemsRaw = maybeData as unknown[];
          total = typeof respObj.total === 'number' ? (respObj.total as number) : itemsRaw.length;
        } else if (Array.isArray(maybeContent)) {
          itemsRaw = maybeContent as unknown[];
          total =
            typeof respObj.totalElements === 'number'
              ? (respObj.totalElements as number)
              : itemsRaw.length;
        } else if (maybeContent && !Array.isArray(maybeContent)) {
          // single-item content
          itemsRaw = [maybeContent];
          total = 1;
        }
      }

      // Helper to safely read string fields from unknown objects
      const readStr = <T = string,>(
        obj: Record<string, unknown>,
        keys: string[],
        fallback?: T,
      ): T | string => {
        for (const k of keys) {
          const v = obj[k];
          if (typeof v === 'string' || typeof v === 'number') return String(v) as unknown as T;
        }
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore - allow returning fallback of different type when requested
        return fallback as T;
      };

      // Normalize rows so DataGrid has an `id` field and expected column keys
      const normalized = itemsRaw.map((it, idx) => {
        const o = (it as Record<string, unknown>) || {};
        const id = readStr(
          o,
          ['id', '_id', 'assetTag', 'asset_tag', 'qrCodeId', 'qr_code_id'],
          String(idx),
        );
        const name = readStr(o, ['name', 'assetName', 'asset_name'], '');
        const qr_code_id = readStr(o, ['qr_code_id', 'qrCodeId', 'qr_code', 'qrCode'], '');
        const statusRaw = readStr(o, ['status', 'state'], '');
        // Normalize status: attempt to map backend statuses like 'ACTIVE' to our enum values
        const status =
          statusRaw && typeof statusRaw === 'string'
            ? (statusRaw.toLowerCase().replace(/\s+/g, '_') as AssetStatus)
            : (AssetStatus.IN_SERVICE as AssetStatus);
        const location = readStr(o, ['location', 'currentLocation'], '');
        const last_inspection_date = readStr<string | null>(
          o,
          ['last_inspection_date', 'lastInspectionDate'],
          null,
        ) as string | null;

        // Build minimal asset object for the grid (we don't need every Asset field to display rows)
        const mapped = {
          id,
          name,
          qr_code_id,
          status,
          location,
          last_inspection_date,
        };
        return mapped as unknown as Asset;
      });

      setAssets(normalized as Asset[]);
      setTotalRows(total);
    } catch (error) {
      console.error('Failed to fetch assets:', error);
      // In a real app, you'd show a proper error message
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAssets();
  }, [paginationModel, searchQuery]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleRowClick = (params: GridRowParams) => {
    const id = String(params.id);
    navigate(`/assets/${id}`);
  };

  const handleCreateAsset = () => {
    setCreateModalOpen(true);
  };

  const handleModalClose = () => {
    setCreateModalOpen(false);
  };

  const handleModalSave = () => {
    setCreateModalOpen(false);
    fetchAssets(); // Refresh the list
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" component="h1">
          Asset Management
        </Typography>
        {mockUserRole === 'admin' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateAsset}
            sx={{ fontWeight: 600 }}
          >
            Create New Asset
          </Button>
        )}
      </Box>

      {/* Search Bar */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search by Asset Name, QR Code ID, or Serial Number"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
            }}
          />
        </CardContent>
      </Card>

      {/* DataGrid */}
      <Card>
        <DataGrid
          rows={assets}
          columns={columns}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 25, 50, 100]}
          rowCount={totalRows}
          paginationMode="server"
          loading={loading}
          onRowClick={handleRowClick}
          disableRowSelectionOnClick
          sx={{
            '& .MuiDataGrid-row': {
              cursor: 'pointer',
            },
            '& .MuiDataGrid-cell:focus': {
              outline: 'none',
            },
            '& .MuiDataGrid-row:hover': {
              backgroundColor: 'action.hover',
            },
          }}
          autoHeight
        />
      </Card>

      {/* Create Asset Modal */}
      <AssetModal
        open={createModalOpen}
        onClose={handleModalClose}
        onSave={handleModalSave}
        mode="create"
      />
    </Box>
  );
}
