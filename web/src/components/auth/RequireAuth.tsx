import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { authService } from '../../services/authService'

interface Props {
  children: JSX.Element
}

export default function RequireAuth({ children }: Props) {
  const location = useLocation()

  if (!authService.isAuthenticated()) {
    // Redirect to login page, preserve the original destination
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}
