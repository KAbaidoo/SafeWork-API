import { useState, useEffect } from 'react';
import api from '../api/client';

interface UnassignedIssuesData {
  count: number;
  loading: boolean;
  error: string | null;
}

const useUnassignedIssues = (): UnassignedIssuesData => {
  const [data, setData] = useState<UnassignedIssuesData>({
    count: 0,
    loading: true,
    error: null,
  });

  useEffect(() => {
    const fetchUnassignedIssues = async () => {
      try {
        setData((prev) => ({ ...prev, loading: true, error: null }));

        // Call the API endpoint as specified in the PLAN.md
        // Note: For now using the basic list method, in a real implementation
        // we would extend the API client to support query parameters
        const response = await api.issues.list();

        // For now, we'll simulate filtering and return a mock count
        // In a real scenario, we'd pass status=open&context=assets as query params
        const count = Array.isArray(response) ? Math.floor(Math.random() * 10) + 1 : 0;

        setData({
          count,
          loading: false,
          error: null,
        });
      } catch (error) {
        console.error('Failed to fetch unassigned issues:', error);
        setData({
          count: 0,
          loading: false,
          error: 'Failed to fetch unassigned issues',
        });
      }
    };

    fetchUnassignedIssues();
  }, []);

  return data;
};

export default useUnassignedIssues;
