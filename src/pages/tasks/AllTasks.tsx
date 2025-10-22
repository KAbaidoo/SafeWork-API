import React from 'react';
import { Typography, Box } from '@mui/material';

const AllTasks: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        All Tasks
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Complete overview of all tasks and corrective actions across the system.
      </Typography>
      <Box sx={{ mt: 3, p: 2, backgroundColor: '#fff3e0', borderRadius: 1 }}>
        <Typography variant="body2">
          📝 Comprehensive task management interface will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default AllTasks;
