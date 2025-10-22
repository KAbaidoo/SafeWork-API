import React from 'react'
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Box,
} from '@mui/material'
import {
  Home as HomeIcon,
  Assignment as TasksIcon,
  Inventory as AssetsIcon,
  Checklist as ChecklistIcon,
  Analytics as AnalyticsIcon,
} from '@mui/icons-material'
import { useNavigate, useLocation } from 'react-router-dom'

const DRAWER_WIDTH = 240

const navigationItems = [
  { text: 'Home', icon: HomeIcon, path: '/' },
  { text: 'Tasks', icon: TasksIcon, path: '/tasks' },
  { text: 'Assets', icon: AssetsIcon, path: '/assets' },
  { text: 'Checklists', icon: ChecklistIcon, path: '/checklists' },
  { text: 'Analytics', icon: AnalyticsIcon, path: '/analytics' },
]

export default function Sidebar() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
        },
      }}
    >
      <Toolbar>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h6" noWrap component="div" color="primary">
            SafeWork
          </Typography>
        </Box>
      </Toolbar>
      
      <List>
        {navigationItems.map((item) => {
          const Icon = item.icon
          const isSelected = location.pathname === item.path || 
            (item.path !== '/' && location.pathname.startsWith(item.path))
          
          return (
            <ListItem key={item.text} disablePadding>
              <ListItemButton
                selected={isSelected}
                onClick={() => navigate(item.path)}
                sx={{
                  '&.Mui-selected': {
                    backgroundColor: 'primary.main',
                    color: 'white',
                    '&:hover': {
                      backgroundColor: 'primary.dark',
                    },
                    '& .MuiListItemIcon-root': {
                      color: 'white',
                    },
                  },
                }}
              >
                <ListItemIcon>
                  <Icon />
                </ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItemButton>
            </ListItem>
          )
        })}
      </List>
    </Drawer>
  )
}