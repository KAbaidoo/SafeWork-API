import React from 'react';
import { Card, CardContent, Typography } from '@mui/material';

interface CounterWidgetProps {
  title: string;
  count: number;
  color?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
}

const CounterWidget: React.FC<CounterWidgetProps> = ({ title, count, color = 'primary' }) => {
  return (
    <Card>
      <CardContent
        sx={{
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: 120,
          textAlign: 'center',
        }}
      >
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {title}
        </Typography>
        <Typography variant="h3" color={`${color}.main`} fontWeight="bold">
          {count}
        </Typography>
      </CardContent>
    </Card>
  );
};

export default CounterWidget;
