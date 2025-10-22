import React, { useState } from 'react';
import {
  Typography,
  Box,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Chip,
  Paper,
} from '@mui/material';
import { Add } from '@mui/icons-material';
import { DataGrid, GridColDef, GridRowParams, GridPaginationModel } from '@mui/x-data-grid';
import { useNavigate } from 'react-router-dom';
import useAssets, { AssetFilters } from '../../hooks/useAssets';
import { useAuth } from '../../contexts/AuthContext';

const AssetInventory: React.FC = () => {
  const navigate = useNavigate();
  const { role } = useAuth();
  const [filters, setFilters] = useState<AssetFilters>({
    search: '',
    status: 'all',
    page: 0,
    pageSize: 25,
  });

  const { assets, loading, totalCount, page, pageSize } = useAssets(filters);

  // Define columns as per R-AM-03
  const columns: GridColDef[] = [
    {
      field: 'name',
      headerName: 'Asset Name',
      flex: 1,
      minWidth: 200,
      renderCell: (params) => (
        <Typography variant="body2" fontWeight="bold">
          {params.value}
        </Typography>
      ),
    },
    {
      field: 'qrCodeId',
      headerName: 'QR Code ID',
      flex: 0.8,
      minWidth: 150,
    },
    {
      field: 'status',
      headerName: 'Status',
      flex: 0.6,
      minWidth: 120,
      renderCell: (params) => {
        const getStatusColor = (status: string) => {
          switch (status) {
            case 'ACTIVE':
              return 'success';
            case 'MAINTENANCE':
              return 'warning';
            case 'OUT_OF_SERVICE':
              return 'error';
            case 'INACTIVE':
              return 'default';
            default:
              return 'default';
          }
        };

        const getStatusLabel = (status: string) => {
          switch (status) {
            case 'ACTIVE':
              return 'In Service';
            case 'MAINTENANCE':
              return 'Maintenance';
            case 'OUT_OF_SERVICE':
              return 'Out of Service';
            case 'INACTIVE':
              return 'Inactive';
            default:
              return status;
          }
        };

        return (
          <Chip
            label={getStatusLabel(params.value)}
            color={getStatusColor(params.value) as 'success' | 'warning' | 'error' | 'default'}
            size="small"
          />
        );
      },
    },
    {
      field: 'currentLocation',
      headerName: 'Current Location',
      flex: 0.8,
      minWidth: 150,
    },
    {
      field: 'lastInspectionDate',
      headerName: 'Last Inspection',
      flex: 0.7,
      minWidth: 130,
      renderCell: (params) => {
        if (!params.value) return '-';
        return new Date(params.value).toLocaleDateString();
      },
    },
  ];

  const handleRowClick = (params: GridRowParams) => {
    navigate(`/am/assets/${params.row.id}`);
  };

  const handlePaginationChange = (model: GridPaginationModel) => {
    setFilters((prev) => ({
      ...prev,
      page: model.page,
      pageSize: model.pageSize,
    }));
  };

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFilters((prev) => ({
      ...prev,
      search: event.target.value,
      page: 0, // Reset to first page when searching
    }));
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleStatusFilterChange = (event: any) => {
    setFilters((prev) => ({
      ...prev,
      status: event.target.value as string,
      page: 0, // Reset to first page when filtering
    }));
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Asset Inventory
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage and track all physical assets within the system.
          </Typography>
        </Box>

        {/* Admin-only Create Asset button (R-AM-05) */}
        {role === 'admin' && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => {
              // TODO: Open create asset modal
              console.log('Create new asset');
            }}
          >
            Create New Asset
          </Button>
        )}
      </Box>

      {/* Search and Filter Controls (R-AM-04) */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <TextField
            label="Search Assets"
            placeholder="Search by Asset Name, QR Code, or Serial Number"
            value={filters.search}
            onChange={handleSearchChange}
            sx={{ flex: 1 }}
            size="small"
          />

          <FormControl sx={{ minWidth: 120 }} size="small">
            <InputLabel>Status</InputLabel>
            <Select
              value={filters.status || 'all'}
              onChange={handleStatusFilterChange}
              label="Status"
            >
              <MenuItem value="all">All Status</MenuItem>
              <MenuItem value="ACTIVE">In Service</MenuItem>
              <MenuItem value="MAINTENANCE">Maintenance</MenuItem>
              <MenuItem value="OUT_OF_SERVICE">Out of Service</MenuItem>
              <MenuItem value="INACTIVE">Inactive</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Paper>

      {/* DataGrid (R-AM-01, R-AM-02) */}
      <Paper sx={{ height: 600 }}>
        <DataGrid
          rows={assets}
          columns={columns}
          loading={loading}
          paginationMode="server"
          rowCount={totalCount}
          paginationModel={{ page, pageSize }}
          onPaginationModelChange={handlePaginationChange}
          pageSizeOptions={[10, 25, 50, 100]}
          onRowClick={handleRowClick}
          sx={{
            border: 0,
            '& .MuiDataGrid-row:hover': {
              cursor: 'pointer',
            },
          }}
          disableRowSelectionOnClick
        />
      </Paper>
    </Box>
  );
};

export default AssetInventory;
