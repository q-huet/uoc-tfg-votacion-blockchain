// prettier.config.js, .prettierrc.js, prettier.config.cjs, or .prettierrc.cjs

/**
 * @see https://prettier.io/docs/configuration
 * @type {import("prettier").Config}
 */

const config = {
  plugins: [
    'prettier-plugin-multiline-arrays',
  ],
  printWidth: 100,
  embeddedLanguageFormatting: 'off',
  singleQuote: true,
  semi: true,
  quoteProps: 'preserve',
  bracketSpacing: false,
  trailingComma: 'all',
  endOfLine: 'auto',
  overrides: [
    {
      files: ['**/*.html'],
      options: {
        parser: 'angular',
      },
    },
  ],
};

module.exports = config;
