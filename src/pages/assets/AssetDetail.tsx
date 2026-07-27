import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Chip,
  Tabs,
  Tab,
  Card,
  CardContent,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import { ArrowBack, Edit } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { Asset } from '../../hooks/useAssets';
import { useAuth } from '../../contexts/AuthContext';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index} style={{ paddingTop: 16 }}>
      {value === index && children}
    </div>
  );
};

const AssetDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { role } = useAuth();
  const [asset, setAsset] = useState<Asset | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    const fetchAsset = async () => {
      if (!id) return;

      try {
        setLoading(true);
        setError(null);

        // For development, simulate fetching asset data
        // In production, this would call: await api.assets.get(id);
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call

        // Generate mock asset data based on ID
        const mockAsset: Asset = {
          id: parseInt(id),
          assetTag: `FL-${id.padStart(3, '0')}`,
          name: `Forklift #${id}`,
          qrCodeId: `QR-FL-${id.padStart(3, '0')}`,
          status: 'ACTIVE',
          organizationId: 1,
          assignedToUserId: 1,
          version: 0,
          currentLocation: 'Warehouse A',
          lastInspectionDate: '2024-10-15',
          nextServiceDate: '2024-12-01',
          warrantyExpiration: '2025-10-20',
          serialNumber: `SN-${Math.random().toString(36).substr(2, 9).toUpperCase()}`,
          customAttributes: {
            manufacturer: 'Toyota',
            model: 'Model-8FBE25',
            year: 2020,
            maxCapacity: '2500 lbs',
            fuelType: 'Electric',
          },
        };

        setAsset(mockAsset);
      } catch (err) {
        console.error('Failed to fetch asset:', err);
        setError('Failed to load asset details');
      } finally {
        setLoading(false);
      }
    };

    fetchAsset();
  }, [id]);

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

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  if (loading) {
    return (
      <Box
        sx={{
          p: 3,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: 200,
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (error || !asset) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error || 'Asset not found'}
        </Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/am/assets')}>
          Back to Asset Inventory
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header with Asset Name and Status (R-AM-07) */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button startIcon={<ArrowBack />} onClick={() => navigate('/am/assets')} size="small">
            Back
          </Button>
          <Typography variant="h4" component="h1">
            {asset.name}
          </Typography>
          <Chip
            label={getStatusLabel(asset.status)}
            color={getStatusColor(asset.status) as 'success' | 'warning' | 'error' | 'default'}
            size="medium"
          />
        </Box>

        {role === 'admin' && (
          <Button
            variant="outlined"
            startIcon={<Edit />}
            onClick={() => {
              // TODO: Open edit asset modal
              console.log('Edit asset:', asset.id);
            }}
          >
            Edit Asset
          </Button>
        )}
      </Box>

      {/* Tabs (R-AM-08) */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Details" />
          <Tab label="History" />
        </Tabs>
      </Box>

      {/* Details Tab (R-AM-09) */}
      <TabPanel value={tabValue} index={0}>
        <Box
          sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3, mt: 2 }}
        >
          {/* Identification Section */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                Identification
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Asset Name
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.name}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    QR Code ID
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.qrCodeId}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Serial Number
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.serialNumber || 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          {/* Status Section */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                Status
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Status
                  </Typography>
                  <Chip
                    label={getStatusLabel(asset.status)}
                    color={
                      getStatusColor(asset.status) as 'success' | 'warning' | 'error' | 'default'
                    }
                    size="small"
                  />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Current Location
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.currentLocation || 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          {/* Maintenance Section */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                Maintenance
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Last Inspection Date
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.lastInspectionDate
                      ? new Date(asset.lastInspectionDate).toLocaleDateString()
                      : 'N/A'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Next Service Date
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.nextServiceDate
                      ? new Date(asset.nextServiceDate).toLocaleDateString()
                      : 'N/A'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Warranty Expiration
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {asset.warrantyExpiration
                      ? new Date(asset.warrantyExpiration).toLocaleDateString()
                      : 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          {/* Custom Attributes Section */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                Custom Attributes
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                {asset.customAttributes &&
                  Object.entries(asset.customAttributes).map(([key, value]) => (
                    <Box key={key}>
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{ textTransform: 'capitalize' }}
                      >
                        {key.replace(/([A-Z])/g, ' $1').trim()}
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {String(value)}
                      </Typography>
                    </Box>
                  ))}
                {(!asset.customAttributes || Object.keys(asset.customAttributes).length === 0) && (
                  <Typography variant="body2" color="text.secondary" fontStyle="italic">
                    No custom attributes defined
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Box>
      </TabPanel>

      {/* History Tab (R-AM-10, R-AM-11) */}
      <TabPanel value={tabValue} index={1}>
        <Card sx={{ mt: 2 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom color="primary">
              Asset History
            </Typography>
            <Typography variant="body2" color="text.secondary" fontStyle="italic">
              📋 Asset history feed will show inspections, maintenance records, and issues
              chronologically (newest first).
            </Typography>
            <Box
              sx={(theme) => ({
                mt: 2,
                p: 2,
                backgroundColor: theme.palette.neutral[100],
                borderRadius: 1,
              })}
            >
              <Typography variant="body2">
                🚧 History implementation coming next - will display inspections and issues with
                dates and descriptions.
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </TabPanel>
    </Box>
  );
};

export default AssetDetail;
