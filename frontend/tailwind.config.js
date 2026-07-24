/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          base: '#0B0F19',
          primary: '#6366F1',
        }
      }
    },
  },
  plugins: [],
}
