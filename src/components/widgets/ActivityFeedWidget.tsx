import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  CircularProgress,
} from '@mui/material';
import { CheckCircle, Error, Build, Assessment } from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';

interface ActivityItem {
  id: string;
  type: 'inspection' | 'issue' | 'maintenance' | 'report';
  title: string;
  description: string;
  timestamp: Date;
}

interface ActivityFeedData {
  items: ActivityItem[];
  loading: boolean;
  error: string | null;
}

const ActivityFeedWidget: React.FC = () => {
  const [data, setData] = useState<ActivityFeedData>({
    items: [],
    loading: true,
    error: null,
  });

  useEffect(() => {
    const fetchRecentActivity = async () => {
      try {
        setData((prev) => ({ ...prev, loading: true, error: null }));

        // In a real scenario, we would have a dedicated endpoint for recent activity
        // For now, we'll simulate with some mock data
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call

        const mockItems: ActivityItem[] = [
          {
            id: '1',
            type: 'inspection',
            title: 'Forklift FL-001 Inspection',
            description: 'Monthly safety inspection completed',
            timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
          },
          {
            id: '2',
            type: 'issue',
            title: 'Conveyor Belt Issue',
            description: 'Belt alignment needs adjustment',
            timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000), // 4 hours ago
          },
          {
            id: '3',
            type: 'maintenance',
            title: 'Crane CRN-003 Service',
            description: 'Scheduled maintenance completed',
            timestamp: new Date(Date.now() - 8 * 60 * 60 * 1000), // 8 hours ago
          },
          {
            id: '4',
            type: 'report',
            title: 'Safety Report Generated',
            description: 'Weekly safety compliance report',
            timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
          },
        ];

        setData({
          items: mockItems.slice(0, 3), // Show only top 3 items
          loading: false,
          error: null,
        });
      } catch (error) {
        console.error('Failed to fetch activity feed:', error);
        setData({
          items: [],
          loading: false,
          error: 'Failed to load activity',
        });
      }
    };

    fetchRecentActivity();
  }, []);

  const getActivityIcon = (type: ActivityItem['type']) => {
    switch (type) {
      case 'inspection':
        return <CheckCircle color="success" />;
      case 'issue':
        return <Error color="error" />;
      case 'maintenance':
        return <Build color="primary" />;
      case 'report':
        return <Assessment color="info" />;
      default:
        return <CheckCircle />;
    }
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
          }}
        >
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Recent Activity
          </Typography>
          <CircularProgress size={24} />
        </CardContent>
      </Card>
    );
  }

  if (data.error || data.items.length === 0) {
    return (
      <Card>
        <CardContent
          sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'flex-start',
            minHeight: 120,
          }}
        >
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Recent Activity
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
            {data.error || 'No recent activity'}
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent sx={{ p: 0 }}>
        <Typography variant="h6" color="text.secondary" sx={{ p: 2, pb: 1 }}>
          Recent Activity
        </Typography>
        <List dense sx={{ pt: 0 }}>
          {data.items.map((item, index) => (
            <React.Fragment key={item.id}>
              <ListItem sx={{ py: 1 }}>
                <ListItemIcon sx={{ minWidth: 36 }}>{getActivityIcon(item.type)}</ListItemIcon>
                <ListItemText
                  primary={
                    <Typography variant="body2" fontWeight="medium">
                      {item.title}
                    </Typography>
                  }
                  secondary={
                    <>
                      <Typography variant="caption" display="block">
                        {item.description}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatDistanceToNow(item.timestamp, { addSuffix: true })}
                      </Typography>
                    </>
                  }
                />
              </ListItem>
              {index < data.items.length - 1 && <Divider variant="inset" />}
            </React.Fragment>
          ))}
        </List>
      </CardContent>
    </Card>
  );
};

export default ActivityFeedWidget;
