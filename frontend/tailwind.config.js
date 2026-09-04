/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        background: 'var(--color-background)',
        foreground: 'var(--color-foreground)',
        card: 'var(--color-card)',
        primary: {
          DEFAULT: 'var(--color-primary)',
          foreground: 'var(--color-primary-foreground)'
        },
        secondary: 'var(--color-secondary)',
        muted: {
          DEFAULT: 'var(--color-muted)',
          foreground: 'var(--color-muted-foreground)'
        },
        accent: {
          DEFAULT: 'var(--color-accent)',
          foreground: 'var(--color-accent-foreground)'
        },
        destructive: {
          DEFAULT: 'var(--color-destructive)',
          foreground: 'var(--color-destructive-foreground)'
        },
        border: 'var(--color-border)'
      },
      borderRadius: {
        sm: '4px',
        md: '6px',
        DEFAULT: '8px',
        lg: '8px',
        xl: '12px'
      },
      fontFamily: {
        sans: 'var(--font-family)'
      }
    }
  },
  plugins: []
};
