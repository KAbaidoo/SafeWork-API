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
// use theme.palette in sx callbacks instead of direct COLORS import

const DRAWER_WIDTH = 240;

export default function Sidebar() {
  const location = useLocation();
  const { selectedModule, setSelectedModule } = useModuleContext();

  // Get navigation items for the selected module
  const navigationItems = navigationConfig[selectedModule] || [];

  return (
    <Drawer
      variant="permanent"
      sx={(theme) => ({
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
          backgroundColor: theme.palette.background.default,
          borderRight: `1px solid ${theme.palette.neutral[300]}`,
          mt: 8, // Account for AppBar height
        },
      })}
    >
      {/* Module Selector */}
      <Box sx={{ p: 2, pt: 3 }}>
        <FormControl fullWidth size="small">
          <Select
            value={selectedModule}
            onChange={(event) => setSelectedModule(event.target.value as string)}
            displayEmpty
            inputProps={{ 'aria-label': 'Select Module' }}
            sx={(theme) => ({
              backgroundColor: 'white',
              border: `1px solid ${theme.palette.neutral[300]}`,
              borderRadius: 1,
              '& .MuiOutlinedInput-notchedOutline': {
                border: 'none',
              },
              '&:hover': {
                backgroundColor: theme.palette.background.default,
                borderColor: theme.palette.primary.main,
              },
              '& .MuiSelect-select': {
                fontWeight: 600,
                color: theme.palette.primary.main,
                fontSize: '1rem',
              },
            })}
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
                sx={(theme) => ({
                  borderRadius: 1,
                  mx: 1,
                  '&.Mui-selected': {
                    backgroundColor: theme.palette.primary.main,
                    color: 'white',
                    '&:hover': {
                      backgroundColor: theme.palette.primary.dark,
                    },
                  },
                  '&:hover': {
                    backgroundColor: theme.palette.action.hover || 'transparent',
                  },
                })}
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
