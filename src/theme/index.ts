import { createTheme } from '@mui/material/styles'

declare module '@mui/material/styles' {
  interface Palette {
    background: {
      default: string
      paper: string
    }
  }
}

export const theme = createTheme({
  palette: {
    primary: {
      main: '#007BFF', // Primary action/branding
    },
    success: {
      main: '#28A745', // Safety/compliant status
    },
    warning: {
      main: '#FFC107', // Pending/due soon alerts
    },
    error: {
      main: '#DC3545', // Critical issues/overdue
    },
    background: {
      default: '#F8F9FA', // Main app background
      paper: '#FFFFFF',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
    subtitle1: {
      fontWeight: 600, // For primary data in tables (Asset Name)
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          elevation: 1, // Minimal elevation as per design brief
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          elevation: 1, // Minimal elevation
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none', // Remove uppercase transformation
          fontWeight: 600,
        },
      },
    },
  },
})