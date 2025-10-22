# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Development Server
- `npm run dev` - Start Vite development server on port 5173
- `npm run build` - Build production bundle
- `npm run preview` - Preview production build on port 4173

### Code Quality
- `npm run lint` - Run ESLint on TypeScript/JavaScript files
- `npm run lint:fix` - Run ESLint with auto-fix
- `npm run typecheck` - Run TypeScript type checking without emitting files

### Setup
- `npm install` - Install dependencies
- `npm run prepare` - Set up Husky git hooks (runs automatically after install)

## Project Architecture

SafeWork Frontend is a **mobile-first operations platform** for safety, compliance, and asset maintenance. This is the **web dashboard** component that complements a mobile app.

### Tech Stack
- **Frontend**: Vite + React 18 + TypeScript
- **Styling**: CSS (no framework currently configured)
- **Linting**: ESLint with TypeScript, React, and Prettier integration
- **Git Hooks**: Husky with lint-staged for pre-commit formatting/linting

### Key Directories
- `src/` - Main application source code (currently minimal starter)
- `docs/` - Product documentation including PRDs and API structure
- `docs/prds/` - Product Requirements Documents for different modules
- `docs/prds/manifests/` - Machine-readable YAML versions of PRDs

### Application Context
The platform serves three user types:
1. **Inspectors** - On-site workers performing inspections via mobile app
2. **Supervisors** - Managers reviewing reports and assigning tasks via web dashboard
3. **Administrators** - Leaders using analytics for business decisions via web dashboard

Core features planned:
- User and asset management
- Task assignment and tracking  
- Real-time analytics dashboards
- Integration with mobile app for issue reporting

### Development Notes
- Uses ESLint flat config (eslint.config.cjs)
- Pre-commit hooks enforce code formatting with Prettier and linting
- React import not required in JSX files (configured in ESLint)
- Currently in early development stage with minimal implementation