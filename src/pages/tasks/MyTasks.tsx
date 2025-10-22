import React from 'react';
import { Typography, Box } from '@mui/material';

const MyTasks: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        My Tasks
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Tasks assigned to you and your personal task dashboard.
      </Typography>
      <Box sx={{ mt: 3, p: 2, backgroundColor: '#fff3e0', borderRadius: 1 }}>
        <Typography variant="body2">
          👤 Personal task dashboard with priority and status filtering will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default MyTasks;
