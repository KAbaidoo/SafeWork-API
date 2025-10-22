export interface NavigationItem {
  name: string;
  path: string;
  icon?: string;
}

export interface NavigationConfig {
  [moduleName: string]: NavigationItem[];
}

export const navigationConfig: NavigationConfig = {
  'Asset Management': [
    { name: 'Dashboard', path: '/am/dashboard' },
    { name: 'Assets', path: '/am/assets' },
    { name: 'Maintenance', path: '/am/maintenance' },
    { name: 'Reports', path: '/am/reports' },
    { name: 'Settings', path: '/am/settings' },
  ],
  'Checklist Builder': [
    { name: 'Templates', path: '/checklist/templates' },
    { name: 'Builder', path: '/checklist/builder' },
    { name: 'Library', path: '/checklist/library' },
  ],
  'Tasks & Actions': [
    { name: 'All Tasks', path: '/tasks/all' },
    { name: 'My Tasks', path: '/tasks/my' },
    { name: 'Overdue', path: '/tasks/overdue' },
  ],
  Analytics: [
    { name: 'Dashboard', path: '/analytics/dashboard' },
    { name: 'Reports', path: '/analytics/reports' },
    { name: 'Trends', path: '/analytics/trends' },
  ],
};
