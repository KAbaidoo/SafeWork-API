import React, { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Chip,
  Card,
  CardContent,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material'
import { Edit as EditIcon, ArrowBack as ArrowBackIcon } from '@mui/icons-material'
import { useParams, useNavigate } from 'react-router-dom'
import { assetService } from '../../services/assetService'
import { Asset, AssetStatus } from '../../types/asset'
import AssetModal from './AssetModal'

// Mock user role for now
const mockUserRole = 'admin'

interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

function TabPanel({ children, value, index }: TabPanelProps) {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  )
}

const getStatusColor = (status: AssetStatus) => {
  switch (status) {
    case AssetStatus.IN_SERVICE:
      return 'success'
    case AssetStatus.MAINTENANCE:
      return 'warning'
    case AssetStatus.OUT_OF_SERVICE:
      return 'error'
    default:
      return 'default'
  }
}

const getStatusLabel = (status: AssetStatus) => {
  switch (status) {
    case AssetStatus.IN_SERVICE:
      return 'In Service'
    case AssetStatus.MAINTENANCE:
      return 'Maintenance'
    case AssetStatus.OUT_OF_SERVICE:
      return 'Out of Service'
    default:
      return status
  }
}

export default function AssetDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [asset, setAsset] = useState<Asset | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tabValue, setTabValue] = useState(0)
  const [editModalOpen, setEditModalOpen] = useState(false)

  useEffect(() => {
    const fetchAsset = async () => {
      if (!id) return
      
      try {
        setLoading(true)
        const data = await assetService.getAssetById(id)
        setAsset(data)
      } catch (err) {
        setError('Failed to load asset details')
        console.error('Error fetching asset:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchAsset()
  }, [id])

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue)
  }

  const handleEdit = () => {
    setEditModalOpen(true)
  }

  const handleModalClose = () => {
    setEditModalOpen(false)
  }

  const handleModalSave = async () => {
    setEditModalOpen(false)
    // Refresh asset data
    if (id) {
      try {
        const data = await assetService.getAssetById(id)
        setAsset(data)
      } catch (err) {
        console.error('Error refreshing asset:', err)
      }
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (error || !asset) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error || 'Asset not found'}</Alert>
      </Box>
    )
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/assets')}
          variant="outlined"
        >
          Back to Assets
        </Button>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ mb: 1 }}>
            {asset.name}
          </Typography>
          <Chip
            label={getStatusLabel(asset.status)}
            color={getStatusColor(asset.status)}
            size="medium"
          />
        </Box>
        {mockUserRole === 'admin' && (
          <Button
            variant="contained"
            startIcon={<EditIcon />}
            onClick={handleEdit}
          >
            Edit Asset
          </Button>
        )}
      </Box>

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab label="Details" />
            <Tab label="History" />
          </Tabs>
        </Box>

        {/* Details Tab */}
        <TabPanel value={tabValue} index={0}>
          <CardContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              {/* Top Row - Identification and Status */}
              <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
                {/* Identification Section */}
                <Box sx={{ flex: 1, minWidth: 300 }}>
                  <Typography variant="h6" gutterBottom color="primary">
                    Identification
                  </Typography>
                  <Card variant="outlined" sx={{ p: 2 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
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
                          {asset.qr_code_id}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Serial Number
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {asset.serial_number || 'N/A'}
                        </Typography>
                      </Box>
                    </Box>
                  </Card>
                </Box>

                {/* Status Section */}
                <Box sx={{ flex: 1, minWidth: 300 }}>
                  <Typography variant="h6" gutterBottom color="primary">
                    Status
                  </Typography>
                  <Card variant="outlined" sx={{ p: 2 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Status
                        </Typography>
                        <Chip
                          label={getStatusLabel(asset.status)}
                          color={getStatusColor(asset.status)}
                          size="small"
                        />
                      </Box>
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Current Location
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {asset.location || 'N/A'}
                        </Typography>
                      </Box>
                    </Box>
                  </Card>
                </Box>
              </Box>

              {/* Maintenance Section */}
              <Box>
                <Typography variant="h6" gutterBottom color="primary">
                  Maintenance
                </Typography>
                <Card variant="outlined" sx={{ p: 2 }}>
                  <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
                    <Box sx={{ flex: 1, minWidth: 200 }}>
                      <Typography variant="body2" color="text.secondary">
                        Last Inspection Date
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {asset.last_inspection_date 
                          ? new Date(asset.last_inspection_date).toLocaleDateString()
                          : 'N/A'
                        }
                      </Typography>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 200 }}>
                      <Typography variant="body2" color="text.secondary">
                        Next Service Date
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {asset.next_service_date
                          ? new Date(asset.next_service_date).toLocaleDateString()
                          : 'N/A'
                        }
                      </Typography>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 200 }}>
                      <Typography variant="body2" color="text.secondary">
                        Warranty Expiration
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {asset.warranty_expiration
                          ? new Date(asset.warranty_expiration).toLocaleDateString()
                          : 'N/A'
                        }
                      </Typography>
                    </Box>
                  </Box>
                </Card>
              </Box>

              {/* Custom Attributes Section */}
              {Object.keys(asset.custom_attributes_json).length > 0 && (
                <Box>
                  <Typography variant="h6" gutterBottom color="primary">
                    Custom Attributes
                  </Typography>
                  <Card variant="outlined" sx={{ p: 2 }}>
                    {Object.entries(asset.custom_attributes_json).map(([key, value]) => (
                      <Box key={key} sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          {key}
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {String(value)}
                        </Typography>
                      </Box>
                    ))}
                  </Card>
                </Box>
              )}
            </Box>
          </CardContent>
        </TabPanel>

        {/* History Tab */}
        <TabPanel value={tabValue} index={1}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Inspection & Issue History
            </Typography>
            <Alert severity="info" sx={{ mb: 2 }}>
              History integration will be implemented when Inspection and Issue modules are ready.
            </Alert>
            <List>
              <ListItem>
                <ListItemText
                  primary="Sample Inspection Entry"
                  secondary="This is where inspection and issue history will be displayed"
                />
              </ListItem>
            </List>
          </CardContent>
        </TabPanel>
      </Card>

      {/* Edit Asset Modal */}
      <AssetModal
        open={editModalOpen}
        onClose={handleModalClose}
        onSave={handleModalSave}
        asset={asset}
        mode="edit"
      />
    </Box>
  )
}