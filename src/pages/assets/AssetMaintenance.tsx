import React from 'react';
import { Typography, Box } from '@mui/material';

const AssetMaintenance: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Asset Maintenance
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Task management and corrective actions with Kanban board interface.
      </Typography>
      <Box sx={{ mt: 3, p: 2, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
        <Typography variant="body2">
          🚧 Kanban board for maintenance tasks will be implemented here with drag-and-drop
          functionality.
        </Typography>
      </Box>
    </Box>
  );
};

export default AssetMaintenance;
