import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useModuleContext } from '../../contexts/ModuleContext';

const moduleDefaultPaths: Record<string, string> = {
  'Asset Management': '/am/dashboard',
  'Checklist Builder': '/checklist/templates',
  'Tasks & Actions': '/tasks/all',
  Analytics: '/analytics/dashboard',
};

export const ModuleNavigationHandler: React.FC = () => {
  const { selectedModule } = useModuleContext();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const currentPath = location.pathname;
    const defaultPath = moduleDefaultPaths[selectedModule];

    if (defaultPath) {
      // Check if current path matches the selected module
      const modulePrefix = defaultPath.split('/').slice(0, 2).join('/');

      // If current path doesn't match the selected module, navigate to default
      if (!currentPath.startsWith(modulePrefix)) {
        navigate(defaultPath);
      }
    }
  }, [selectedModule, navigate, location.pathname]);

  return null;
};

export default ModuleNavigationHandler;
