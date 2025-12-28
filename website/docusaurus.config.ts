import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'sbt-config',
  tagline: 'Configure sbt projects via HOCON files',
  favicon: 'img/favicon.svg',

  future: {
    v4: true,
  },

  url: 'https://matejcerny.github.io',
  baseUrl: '/sbt-config/',

  organizationName: 'matejcerny',
  projectName: 'sbt-config',

  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'throw',
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          path: '../sbt-config-docs/target/mdoc',
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          editUrl: 'https://github.com/matejcerny/sbt-config/tree/main/sbt-config-docs/docs/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'sbt-config',
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docs',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/matejcerny/sbt-config',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: '/',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/matejcerny/sbt-config',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Matej Cerny. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'scala', 'bash'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
