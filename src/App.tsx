import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { theme } from './theme';
import Layout from './components/layout/Layout';
import Login from './components/auth/Login';
import RequireAuth from './components/auth/RequireAuth';
import ProtectedRoute from './components/auth/ProtectedRoute';
import ModuleNavigationHandler from './components/navigation/ModuleNavigationHandler';
import Unauthorized from './pages/Unauthorized';

// Asset Management Pages
import AssetDashboard from './pages/assets/AssetDashboard';
import AssetInventory from './pages/assets/AssetInventory';
import AssetDetailPage from './pages/assets/AssetDetail';
import AssetMaintenance from './pages/assets/AssetMaintenance';
import AssetReports from './pages/assets/AssetReports';
import AssetSettings from './pages/assets/AssetSettings';

// Checklist Builder Pages
import ChecklistTemplates from './pages/checklist/ChecklistTemplates';
import ChecklistBuilder from './pages/checklist/ChecklistBuilder';
import ChecklistLibrary from './pages/checklist/ChecklistLibrary';

// Tasks & Actions Pages
import AllTasks from './pages/tasks/AllTasks';
import MyTasks from './pages/tasks/MyTasks';
import OverdueTasks from './pages/tasks/OverdueTasks';

// Analytics Pages
import AnalyticsDashboard from './pages/analytics/AnalyticsDashboard';
import AnalyticsReports from './pages/analytics/AnalyticsReports';
import AnalyticsTrends from './pages/analytics/AnalyticsTrends';

// Legacy components (keeping for backward compatibility)
import AssetList from './components/assets/AssetList';

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Layout>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/unauthorized" element={<Unauthorized />} />

            {/* Protected routes */}
            <Route
              path="/*"
              element={
                <RequireAuth>
                  <>
                    <ModuleNavigationHandler />
                    <Routes>
                      {/* Default redirect to Asset Management Dashboard */}
                      <Route path="/" element={<Navigate to="/am/dashboard" replace />} />

                      {/* Asset Management Routes */}
                      <Route path="/am/dashboard" element={<AssetDashboard />} />
                      <Route path="/am/assets" element={<AssetInventory />} />
                      <Route path="/am/assets/:id" element={<AssetDetailPage />} />
                      <Route path="/am/maintenance" element={<AssetMaintenance />} />
                      <Route path="/am/reports" element={<AssetReports />} />
                      <Route
                        path="/am/settings"
                        element={
                          <ProtectedRoute requiredRole="admin">
                            <AssetSettings />
                          </ProtectedRoute>
                        }
                      />

                      {/* Checklist Builder Routes */}
                      <Route path="/checklist/templates" element={<ChecklistTemplates />} />
                      <Route path="/checklist/builder" element={<ChecklistBuilder />} />
                      <Route path="/checklist/library" element={<ChecklistLibrary />} />

                      {/* Tasks & Actions Routes */}
                      <Route path="/tasks/all" element={<AllTasks />} />
                      <Route path="/tasks/my" element={<MyTasks />} />
                      <Route path="/tasks/overdue" element={<OverdueTasks />} />

                      {/* Analytics Routes */}
                      <Route path="/analytics/dashboard" element={<AnalyticsDashboard />} />
                      <Route path="/analytics/reports" element={<AnalyticsReports />} />
                      <Route path="/analytics/trends" element={<AnalyticsTrends />} />

                      {/* Legacy routes for backward compatibility */}
                      <Route path="/assets" element={<AssetList />} />
                    </Routes>
                  </>
                </RequireAuth>
              }
            />
          </Routes>
        </Layout>
      </Router>
    </ThemeProvider>
  );
}
