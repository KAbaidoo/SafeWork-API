import { createTheme } from '@mui/material/styles';

// Move palette tokens into MUI theme for idiomatic usage and easier theming
declare module '@mui/material/styles' {
  interface Palette {
    neutral: {
      100: string;
      200: string;
      300: string;
    };
  }
  interface PaletteOptions {
    neutral?: {
      100?: string;
      200?: string;
      300?: string;
    };
  }
}

export const theme = createTheme({
  palette: {
    primary: {
      main: '#007BFF', // Primary action/branding
      dark: '#0056b3',
      contrastText: '#FFFFFF',
    },
    success: {
      main: '#28A745', // Safety/compliant status
      light: '#e6f4ea',
    },
    warning: {
      main: '#FFC107', // Pending/due soon alerts
      light: '#fff3e0',
    },
    error: {
      main: '#DC3545', // Critical issues/overdue
      light: '#ffebee',
    },
    info: {
      light: '#e3f2fd',
      main: '#0288d1',
    },
    background: {
      default: '#F8F9FA', // Main app background
      paper: '#FFFFFF',
    },
    neutral: {
      100: '#f5f5f5',
      200: '#E9ECEF',
      300: '#DEE2E6',
    },
  },
  typography: {
    fontFamily: '"Inter", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial',
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
});
