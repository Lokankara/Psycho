import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    lib: {
      entry: 'src/index.ts',
      name: 'KanbanBoard',
      fileName: 'kanban',
    },
    rollupOptions: {
      external: ['react', 'react-dom', 'react-router-dom', '@dnd-kit/core'],
      output: {
        globals: {
          react: 'React',
          'react-dom': 'ReactDOM',
          'react-router-dom': 'ReactRouterDOM',
          '@dnd-kit/core': 'DndKitCore',
        },
      },
    },
  },
});
