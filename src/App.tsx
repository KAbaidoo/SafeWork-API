import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider } from '@mui/material/styles'
import { CssBaseline, Typography, Box } from '@mui/material'
import { theme } from './theme'
import Layout from './components/layout/Layout'
import AssetList from './components/assets/AssetList'
import AssetDetail from './components/assets/AssetDetail'
import Login from './components/auth/Login'
import RequireAuth from './components/auth/RequireAuth'

// Placeholder components for other modules
const PlaceholderModule = ({ title }: { title: string }) => (
  <Box sx={{ p: 3, textAlign: 'center' }}>
    <Typography variant="h4" gutterBottom>
      {title}
    </Typography>
    <Typography variant="body1" color="text.secondary">
      This module will be implemented in future iterations.
    </Typography>
  </Box>
)

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Layout>
          <Routes>
            <Route path="/login" element={<Login />} />

            {/* Protected routes */}
            <Route
              path="/*"
              element={
                <RequireAuth>
                  <Routes>
                    <Route path="/" element={<Navigate to="/assets" replace />} />
                    <Route path="/assets" element={<AssetList />} />
                    <Route path="/assets/:id" element={<AssetDetail />} />
                    <Route path="/tasks" element={<PlaceholderModule title="Tasks & Corrective Actions" />} />
                    <Route path="/checklists" element={<PlaceholderModule title="Checklists & Templates" />} />
                    <Route path="/analytics" element={<PlaceholderModule title="Analytics & Reporting" />} />
                  </Routes>
                </RequireAuth>
              }
            />
          </Routes>
        </Layout>
      </Router>
    </ThemeProvider>
  )
}
