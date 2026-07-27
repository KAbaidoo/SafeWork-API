import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { assetService } from '../../services/assetService'
import { Asset, AssetStatus, CreateAssetRequest, UpdateAssetRequest } from '../../types/asset'

interface AssetModalProps {
  open: boolean
  onClose: () => void
  onSave: () => void
  asset?: Asset | null
  mode: 'create' | 'edit'
}

export default function AssetModal({ open, onClose, onSave, asset, mode }: AssetModalProps) {
  const [formData, setFormData] = useState<CreateAssetRequest>({
    name: '',
    qr_code_id: '',
    serial_number: '',
    status: AssetStatus.IN_SERVICE,
    location: '',
    next_service_date: '',
    warranty_expiration: '',
    custom_attributes_json: {},
  })
  const [customAttributes, setCustomAttributes] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (mode === 'edit' && asset) {
      setFormData({
        name: asset.name,
        qr_code_id: asset.qr_code_id,
        serial_number: asset.serial_number || '',
        status: asset.status,
        location: asset.location || '',
        next_service_date: asset.next_service_date || '',
        warranty_expiration: asset.warranty_expiration || '',
        custom_attributes_json: asset.custom_attributes_json,
      })
      setCustomAttributes(JSON.stringify(asset.custom_attributes_json, null, 2))
    } else {
      // Reset form for create mode
      setFormData({
        name: '',
        qr_code_id: '',
        serial_number: '',
        status: AssetStatus.IN_SERVICE,
        location: '',
        next_service_date: '',
        warranty_expiration: '',
        custom_attributes_json: {},
      })
      setCustomAttributes('{}')
    }
    setError(null)
  }, [mode, asset, open])

  const handleInputChange = (field: keyof CreateAssetRequest) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setFormData({ ...formData, [field]: event.target.value })
  }

  const handleSelectChange = (field: keyof CreateAssetRequest) => (
    event: { target: { value: unknown } }
  ) => {
    setFormData({ ...formData, [field]: event.target.value })
  }

  const handleDateChange = (field: keyof CreateAssetRequest) => (date: Date | null) => {
    setFormData({ 
      ...formData, 
      [field]: date ? date.toISOString().split('T')[0] : '' 
    })
  }

  const handleCustomAttributesChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    setCustomAttributes(event.target.value)
  }

  const handleSave = async () => {
    setLoading(true)
    setError(null)

    try {
      // Parse custom attributes
      let parsedAttributes = {}
      if (customAttributes.trim()) {
        try {
          parsedAttributes = JSON.parse(customAttributes)
        } catch {
          throw new Error('Invalid JSON in custom attributes')
        }
      }

      const payload = {
        ...formData,
        custom_attributes_json: parsedAttributes,
      }

      if (mode === 'create') {
        await assetService.createAsset(payload)
      } else if (mode === 'edit' && asset) {
        const updatePayload: UpdateAssetRequest = {
          ...payload,
          version: asset.version,
        }
        await assetService.updateAsset(asset.id, updatePayload)
      }

      onSave()
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save asset')
    } finally {
      setLoading(false)
    }
  }

  const isFormValid = formData.name.trim() && formData.qr_code_id.trim()

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>
          {mode === 'create' ? 'Create New Asset' : 'Edit Asset'}
        </DialogTitle>
        
        <DialogContent>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
            {/* Required Fields Row */}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <TextField
                  fullWidth
                  label="Asset Name"
                  value={formData.name}
                  onChange={handleInputChange('name')}
                  required
                  error={!formData.name.trim()}
                  helperText={!formData.name.trim() ? 'Asset name is required' : ''}
                />
              </Box>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <TextField
                  fullWidth
                  label="QR Code ID"
                  value={formData.qr_code_id}
                  onChange={handleInputChange('qr_code_id')}
                  required
                  error={!formData.qr_code_id.trim()}
                  helperText={!formData.qr_code_id.trim() ? 'QR Code ID is required' : ''}
                />
              </Box>
            </Box>

            {/* Second Row */}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <TextField
                  fullWidth
                  label="Serial Number"
                  value={formData.serial_number}
                  onChange={handleInputChange('serial_number')}
                />
              </Box>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <FormControl fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={formData.status}
                    label="Status"
                    onChange={handleSelectChange('status')}
                  >
                    <MenuItem value={AssetStatus.IN_SERVICE}>In Service</MenuItem>
                    <MenuItem value={AssetStatus.MAINTENANCE}>Maintenance</MenuItem>
                    <MenuItem value={AssetStatus.OUT_OF_SERVICE}>Out of Service</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            </Box>

            {/* Location */}
            <TextField
              fullWidth
              label="Current Location"
              value={formData.location}
              onChange={handleInputChange('location')}
            />

            {/* Date Fields */}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <DatePicker
                  label="Next Service Date"
                  value={formData.next_service_date ? new Date(formData.next_service_date) : null}
                  onChange={handleDateChange('next_service_date')}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Box>
              <Box sx={{ flex: 1, minWidth: 250 }}>
                <DatePicker
                  label="Warranty Expiration"
                  value={formData.warranty_expiration ? new Date(formData.warranty_expiration) : null}
                  onChange={handleDateChange('warranty_expiration')}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Box>
            </Box>

            {/* Custom Attributes */}
            <TextField
              fullWidth
              label="Custom Attributes (JSON)"
              multiline
              rows={4}
              value={customAttributes}
              onChange={handleCustomAttributesChange}
              helperText='Enter custom attributes as valid JSON (e.g., {"manufacturer": "Toyota", "model": "Forklift-X1"})'
            />
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={!isFormValid || loading}
          >
            {loading ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CircularProgress size={20} />
                Saving...
              </Box>
            ) : (
              mode === 'create' ? 'Create Asset' : 'Update Asset'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </LocalizationProvider>
  )
}