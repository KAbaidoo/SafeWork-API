import React from 'react';
import { Typography, Box } from '@mui/material';

const AssetSettings: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Asset Settings
      </Typography>
      <Typography variant="body1" color="text.secondary">
        User roles, integrations, and asset configuration settings.
      </Typography>
      <Box
        sx={(theme) => ({
          mt: 3,
          p: 2,
          backgroundColor: theme.palette.neutral[100],
          borderRadius: 1,
        })}
      >
        <Typography variant="body2">
          ⚙️ Settings interface with tabs for Roles, Integrations, and Asset Types will be
          implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default AssetSettings;
