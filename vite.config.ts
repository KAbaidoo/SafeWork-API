import { defineConfig } from 'vite'

export default defineConfig(async () => {
  // Dynamically import the React plugin (ESM-only) to avoid loader issues
  const reactPlugin = (await import('@vitejs/plugin-react')).default
  return {
    plugins: [reactPlugin()],
    server: { port: 5173 }
  }
})
