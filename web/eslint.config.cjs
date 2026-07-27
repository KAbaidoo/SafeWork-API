const tsParser = require('@typescript-eslint/parser')
const tsPlugin = require('@typescript-eslint/eslint-plugin')
const reactPlugin = require('eslint-plugin-react')
const reactHooks = require('eslint-plugin-react-hooks')
const prettierConfig = require('eslint-config-prettier')

module.exports = [
  // ignores (replaces .eslintignore)
  {
    ignores: ['node_modules/**', 'dist/**', '.husky/**', '.git/**']
  },
  // Rules for JS / TS files
  {
    files: ['**/*.{ts,tsx,js,jsx}'],
    languageOptions: {
      // must be the parser module which exposes parse/parseForESLint
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: { jsx: true }
      }
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
      react: reactPlugin,
      'react-hooks': reactHooks
    },
    rules: Object.assign(
      {},
      tsPlugin && tsPlugin.configs && tsPlugin.configs.recommended ? tsPlugin.configs.recommended.rules : {},
      reactPlugin && reactPlugin.configs && reactPlugin.configs.recommended ? reactPlugin.configs.recommended.rules : {},
      reactHooks && reactHooks.configs && reactHooks.configs.recommended ? reactHooks.configs.recommended.rules : {},
      prettierConfig && prettierConfig.rules ? prettierConfig.rules : {},
      {
        // project-specific overrides
        'react/react-in-jsx-scope': 'off'
      }
    ),
    settings: { react: { version: 'detect' } }
  }
]
