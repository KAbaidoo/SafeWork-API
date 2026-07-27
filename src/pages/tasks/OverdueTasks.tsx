import React from 'react';
import { Typography, Box } from '@mui/material';

const OverdueTasks: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Overdue Tasks
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Critical overdue tasks requiring immediate attention.
      </Typography>
      <Box
        sx={(theme) => ({
          mt: 3,
          p: 2,
          backgroundColor: theme.palette.error.light || '#ffebee',
          borderRadius: 1,
        })}
      >
        <Typography variant="body2" color="error">
          ⚠️ Critical overdue task management interface will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default OverdueTasks;
