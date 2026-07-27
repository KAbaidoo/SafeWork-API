import React from 'react';
import { Box, Typography } from '@mui/material';
import CounterWidget from '../../components/widgets/CounterWidget';
import ComplianceWidget from '../../components/widgets/ComplianceWidget';
import ActivityFeedWidget from '../../components/widgets/ActivityFeedWidget';
import useUnassignedIssues from '../../hooks/useUnassignedIssues';
import useOverdueTasks from '../../hooks/useOverdueTasks';

const AssetDashboard: React.FC = () => {
  const unassignedIssues = useUnassignedIssues();
  const overdueTasks = useOverdueTasks();

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Asset Management Dashboard
      </Typography>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
          gap: 3,
        }}
      >
        {/* Widget 1: Unassigned Issues Counter */}
        <CounterWidget
          title="Unassigned Issues"
          count={unassignedIssues.loading ? 0 : unassignedIssues.count}
          color="warning"
        />

        {/* Widget 2: Overdue Tasks Counter */}
        <CounterWidget
          title="Overdue Tasks"
          count={overdueTasks.loading ? 0 : overdueTasks.count}
          color="error"
        />

        {/* Widget 3: Compliance Scorecard */}
        <ComplianceWidget />

        {/* Widget 4: Activity Feed */}
        <ActivityFeedWidget />
      </Box>
    </Box>
  );
};

export default AssetDashboard;
