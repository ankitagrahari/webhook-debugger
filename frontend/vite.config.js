import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        // Forward cookies so Spring Security sessions work
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            proxyReq.setHeader('Origin', 'http://localhost:8082')
          })
        }
      }
    }
  },
  build: {
    outDir: '../ui-service/src/main/resources/static',
    emptyOutDir: true
  }
})
