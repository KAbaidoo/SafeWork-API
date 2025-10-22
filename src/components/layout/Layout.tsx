import React from 'react'
import { Box, CssBaseline, Toolbar } from '@mui/material'
import Sidebar from './Sidebar'

interface LayoutProps {
  children: React.ReactNode
}

const DRAWER_WIDTH = 240

export default function Layout({ children }: LayoutProps) {
  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <Sidebar />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
          backgroundColor: 'background.default',
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  )
}