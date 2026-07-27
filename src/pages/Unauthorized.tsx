import React from 'react';
import { Typography, Box, Button, Card, CardContent } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LockIcon from '@mui/icons-material/Lock';

const Unauthorized: React.FC = () => {
  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate(-1); // Go back to previous page
  };

  const handleGoHome = () => {
    navigate('/am/dashboard'); // Navigate to default dashboard
  };

  return (
    <Box
      sx={{
        p: 3,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '60vh',
      }}
    >
      <Card sx={{ maxWidth: 500, textAlign: 'center' }}>
        <CardContent sx={{ p: 4 }}>
          <LockIcon
            sx={{
              fontSize: 64,
              color: 'error.main',
              mb: 2,
            }}
          />
          <Typography variant="h4" gutterBottom color="error">
            Access Denied
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            You don&apos;t have permission to access this page. Please contact your administrator if
            you believe this is an error.
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button variant="outlined" onClick={handleGoBack}>
              Go Back
            </Button>
            <Button variant="contained" onClick={handleGoHome}>
              Go to Dashboard
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Unauthorized;
