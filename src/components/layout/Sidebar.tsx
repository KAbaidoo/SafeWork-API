import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Box,
  Divider,
  FormControl,
  Select,
  MenuItem,
} from '@mui/material';
import { NavLink, useLocation } from 'react-router-dom';
import { useModuleContext } from '../../contexts/ModuleContext';
import { navigationConfig } from '../../config/navigationConfig';

const DRAWER_WIDTH = 240;

export default function Sidebar() {
  const location = useLocation();
  const { selectedModule, setSelectedModule } = useModuleContext();

  // Get navigation items for the selected module
  const navigationItems = navigationConfig[selectedModule] || [];

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
          backgroundColor: '#F8F9FA',
          borderRight: '1px solid #E0E0E0',
          mt: 8, // Account for AppBar height
        },
      }}
    >
      {/* Module Selector */}
      <Box sx={{ p: 2, pt: 3 }}>
        <FormControl fullWidth size="small">
          <Select
            value={selectedModule}
            onChange={(event) => setSelectedModule(event.target.value as string)}
            displayEmpty
            inputProps={{ 'aria-label': 'Select Module' }}
            sx={{
              backgroundColor: 'white',
              border: '1px solid #E0E0E0',
              borderRadius: 1,
              '& .MuiOutlinedInput-notchedOutline': {
                border: 'none',
              },
              '&:hover': {
                backgroundColor: '#F8F9FA',
                borderColor: '#007BFF',
              },
              '& .MuiSelect-select': {
                fontWeight: 600,
                color: '#007BFF',
                fontSize: '1rem',
              },
            }}
          >
            <MenuItem value="Asset Management">Asset Management</MenuItem>
            <MenuItem value="Checklist Builder">Checklist Builder</MenuItem>
            <MenuItem value="Tasks & Actions">Tasks & Actions</MenuItem>
            <MenuItem value="Analytics">Analytics</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <Divider sx={{ mx: 2, mb: 1 }} />

      {/* Sub-Module Navigation */}
      <List sx={{ px: 1 }}>
        {navigationItems.map((item) => {
          const isSelected =
            location.pathname === item.path ||
            (item.path !== '/' && location.pathname.startsWith(item.path));

          return (
            <ListItem key={item.name} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                component={NavLink}
                to={item.path}
                selected={isSelected}
                sx={{
                  borderRadius: 1,
                  mx: 1,
                  '&.Mui-selected': {
                    backgroundColor: '#007BFF',
                    color: 'white',
                    '&:hover': {
                      backgroundColor: '#0056b3',
                    },
                  },
                  '&:hover': {
                    backgroundColor: 'rgba(0, 123, 255, 0.1)',
                  },
                }}
              >
                <ListItemText
                  primary={item.name}
                  primaryTypographyProps={{
                    fontSize: '0.95rem',
                    fontWeight: isSelected ? 600 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Drawer>
  );
}
