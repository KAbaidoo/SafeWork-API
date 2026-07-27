import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactElement;
  requiredRole?: string;
  redirectTo?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  redirectTo = '/login',
}) => {
  const { token, role } = useAuth();

  // If user is not authenticated (no token), redirect to login
  if (!token) {
    return <Navigate to={redirectTo} replace />;
  }

  // If a specific role is required and user doesn't have it, redirect to unauthorized page
  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  // If user is authenticated and has required role (or no role required), render children
  return children;
};

export default ProtectedRoute;
