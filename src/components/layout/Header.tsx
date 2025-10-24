import React from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { Chip, Select, MenuItem, FormControl } from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import logo from '../../assets/safe-work-logo-1.png';

const Header: React.FC = () => {
  const { role, setRole } = useAuth();

  return (
    <AppBar
      position="fixed"
      sx={{
        height: 60,
        backgroundColor: (theme) => theme.palette.primary.main,
        boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.1)',
        zIndex: (theme) => theme.zIndex.drawer + 1,
      }}
    >
      <Toolbar sx={{ minHeight: 60 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', flex: 1 }}>
          <Box
            component="img"
            src={logo}
            alt="SafeWork logo"
            sx={{ height: 30, mr: 1, display: { xs: 'none', sm: 'block' } }}
          />
          <Typography
            variant="h6"
            component="div"
            sx={{
              fontWeight: 500,
              color: 'white',
            }}
          >
            | Organization name
          </Typography>
        </Box>

        {/* Development Role Switcher */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Chip
            label={`Role: ${role || 'None'}`}
            size="small"
            sx={{
              backgroundColor: 'rgba(255, 255, 255, 0.2)',
              color: 'white',
              fontWeight: 500,
            }}
          />
          <FormControl size="small" sx={{ minWidth: 100 }}>
            <Select
              value={role || ''}
              onChange={(event) => setRole(event.target.value as string)}
              displayEmpty
              sx={{
                color: 'white',
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: 1,
                '& .MuiOutlinedInput-notchedOutline': {
                  border: 'none',
                },
                '& .MuiSvgIcon-root': {
                  color: 'white',
                },
              }}
            >
              <MenuItem value="admin">Admin</MenuItem>
              <MenuItem value="user">User</MenuItem>
              <MenuItem value="">None</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
