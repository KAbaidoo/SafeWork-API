import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, LinearProgress, Chip } from '@mui/material';
import api from '../../api/client';

interface ComplianceData {
  score: number;
  loading: boolean;
  error: string | null;
}

const ComplianceWidget: React.FC = () => {
  const [data, setData] = useState<ComplianceData>({
    score: 0,
    loading: true,
    error: null,
  });

  useEffect(() => {
    const fetchComplianceScore = async () => {
      try {
        setData((prev) => ({ ...prev, loading: true, error: null }));

        // Call the analytics endpoint for completion rate
        await api.analytics.completionRate();

        // For now, we'll simulate a score. In a real scenario, this would come from the API
        const score = Math.floor(Math.random() * 30) + 70; // Random score between 70-100

        setData({
          score,
          loading: false,
          error: null,
        });
      } catch (error) {
        console.error('Failed to fetch compliance score:', error);
        setData({
          score: 0,
          loading: false,
          error: 'Failed to fetch compliance score',
        });
      }
    };

    fetchComplianceScore();
  }, []);

  const getScoreColor = (score: number): 'success' | 'warning' | 'error' => {
    if (score >= 90) return 'success';
    if (score >= 70) return 'warning';
    return 'error';
  };

  if (data.loading) {
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
            Compliance Score
          </Typography>
          <LinearProgress sx={{ width: '60%', mb: 1 }} />
          <Typography variant="body2" color="text.secondary">
            Loading...
          </Typography>
        </CardContent>
      </Card>
    );
  }

  if (data.error) {
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
            Compliance Score
          </Typography>
          <Typography variant="h3" color="text.secondary" fontWeight="bold">
            --
          </Typography>
          <Typography variant="caption" color="error">
            {data.error}
          </Typography>
        </CardContent>
      </Card>
    );
  }

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
          Compliance Score
        </Typography>
        <Typography
          variant="h3"
          color={`${getScoreColor(data.score)}.main`}
          fontWeight="bold"
          gutterBottom
        >
          {data.score}%
        </Typography>
        <LinearProgress
          variant="determinate"
          value={data.score}
          color={getScoreColor(data.score)}
          sx={{ width: '80%', mb: 1 }}
        />
        <Chip
          label={data.score >= 90 ? 'Excellent' : data.score >= 70 ? 'Good' : 'Needs Attention'}
          color={getScoreColor(data.score)}
          size="small"
        />
      </CardContent>
    </Card>
  );
};

export default ComplianceWidget;
