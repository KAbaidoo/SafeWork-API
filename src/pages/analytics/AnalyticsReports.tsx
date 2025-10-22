import React from 'react';
import { Typography, Box } from '@mui/material';

const AnalyticsReports: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Analytics Reports
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Detailed reports and exportable analytics across all system modules.
      </Typography>
      <Box
        sx={(theme) => ({
          mt: 3,
          p: 2,
          backgroundColor: theme.palette.info.light,
          borderRadius: 1,
        })}
      >
        <Typography variant="body2">
          📋 Detailed reporting interface with export capabilities will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};
export default AnalyticsReports;
