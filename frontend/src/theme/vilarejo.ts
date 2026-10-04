import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'

export const Vilarejo = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#fdf8ec',
      100: '#faefd0',
      200: '#f6e0a3',
      300: '#f1cf7d',
      400: '#ecc068',
      500: '#e8b55a',
      600: '#c9953f',
      700: '#a67734',
      800: '#84602e',
      900: '#6b4f29',
      950: '#3d2c14',
    },
    colorScheme: {
      dark: {
        surface: {
          0: '#ffffff',
          50: '#f2eee8',
          100: '#d9d3ca',
          200: '#b0a89e',
          300: '#958d83',
          400: '#7a7268',
          500: '#5c554d',
          600: '#3d3731',
          700: '#332e29',
          800: '#27231f',
          900: '#211d19',
          950: '#1a1714',
        },
        primary: {
          color: '#e8b55a',
          contrastColor: '#1d1a17',
          hoverColor: '#ecc068',
          activeColor: '#f1cf7d',
        },
        highlight: {
          background: 'rgba(232, 181, 90, 0.16)',
          focusBackground: 'rgba(232, 181, 90, 0.24)',
          color: '#e8b55a',
          focusColor: '#f1cf7d',
        },
      },
    },
  },
})

export default Vilarejo
