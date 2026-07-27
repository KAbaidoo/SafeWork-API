import React from 'react';
import { Typography, Box } from '@mui/material';

const ChecklistBuilder: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Checklist Builder
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Create and customize new checklists with drag-and-drop interface.
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
          🛠️ Interactive checklist builder with form components will be implemented here.
        </Typography>
      </Box>
    </Box>
  );
};

export default ChecklistBuilder;
