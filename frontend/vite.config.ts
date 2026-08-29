import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    rolldownOptions: {
      output: {
        /** PDF 생성기와 전역 라이브러리를 화면 청크에서 분리해 캐시 효율을 높인다. */
        manualChunks(id) {
          if (id.includes('@react-pdf/renderer')) return 'pdf-renderer'
          if (id.includes('/node_modules/react/') || id.includes('/node_modules/react-dom/') || id.includes('/node_modules/react-router-dom/') || id.includes('/node_modules/axios/')) {
            return 'react-vendor'
          }
          return undefined
        },
      },
    },
  }
})
