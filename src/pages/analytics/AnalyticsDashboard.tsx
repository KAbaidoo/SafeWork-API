import React from 'react';
import { Typography, Box } from '@mui/material';

const AnalyticsDashboard: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Analytics Dashboard
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Global analytics and key performance indicators across all modules.
      </Typography>
      <Box sx={{ mt: 3, p: 2, backgroundColor: '#f3e5f5', borderRadius: 1 }}>
        <Typography variant="body2">
          📊 Comprehensive analytics dashboard with interactive charts will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default AnalyticsDashboard;
