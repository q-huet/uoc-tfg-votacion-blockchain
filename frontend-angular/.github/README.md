# Ford Responsive Framework (FRF) Starter Application

Congratulations on generating your FRF Starter based application. Please see the following information on how to utilize this framework. To keep up-to-date with the latest versions the FRF, you can follow the [frf-starter repository](https://github.com/ford-innersouce/devenablement.wame.frf-starter/) and our [migration guides](https://github.com/ford-innersource/devenablement.wame.web-devguides/tree/main/migration).

## Getting Started with the Ford Responsive Framework

To get started with your app, you will need to do the following:

1. Pull down a copy of this release using your preferred method (.zip download, HTTPS/SSH clone). Place it somewhere convenient.
1. Newer versions of Node.js requires that Ford keep track of all users due to licensing requirements. Visit the [Node.js usage tracking page](https://azureford.sharepoint.com/sites/WebCOE/Pages/Node.js.aspx) to complete the form
   1. Install Node.js v22.x on your computer via Volta, the JavaScript tool manager; Follow the [npm Installation and Configuration Guide](https://github.com/ford-innersource/devenablement.wame.web-devguides/tree/main/node-and-npm/npm%20installation%20and%20configuration) to also setup and configure support for JFrog Artifactory.
1. Configure command-line proxy settings appropriately - read more in [the Proxy Developer Guide](https://github.com/ford-innersource/devenablement.cloudnative.dev-guides/tree/main/spring-boot/proxy-configuration).
   1. Locations where environmental variables are often set can include the following:
      1. macOS:
         - Bash`.bashrc`,`.bash_profile`, and /or `.profile`
         - Zsh: `.zshrc` and /or `.profile`
      1. Windows
         - PowerShell: `Profile.ps1`
         - Git Bash: `.bashrc`, `.bash_profile`, and /or `.profile`
1. Open a terminal/command prompt inside of the project.
1. Run `npm install`<sup>1</sup>.
1. Run `npm update @wame/blue-oval-theme @wame/ngx-frf-utilities` to get the latest compatible version of the [blue-oval-theme](https://github.com/ford-innersource/devenablement.wame.primeng-designer/blob/main/src/sass/README.md) and [ngx-frf-utilities](https://github.com/ford-innersource/devenablement.wame.frf-packages/blob/main/projects/ngx-frf-utilities/README.md)
1. Start your local development server with `npm run start-local`
1. Finally, visit <http://localhost:9500> to see your rendered app in the browser

<sup>1</sup> For more information on `npm install` and how to keep up to date with the latest package versions, see the [NPM Version Management guide](https://github.com/ford-innersource/devenablement.wame.web-devguides/blob/main/node-and-npm/npm-version-management.md).

## Authentication

This project includes the packages `@azure/msal-browser` and `@azure/msal-angular` to support the integration of [Entra ID Authentication](https://learn.microsoft.com/en-us/entra/identity/authentication/overview-authentication). For more information and instructions on Entra ID Authentication implementation please refer to the [Entra ID Authentication Guide](https://github.com/ford-innersource/devenablement.wame.web-devguides/blob/main/entra-id/README.md).

### E2E Tests and Authentication

If your application includes Entra ID authentication, end-to-end (e2e) tests may fail without additional configuration. This is because Playwright tests run against a built production version of your application, which requires valid authentication credentials to function properly once Entra ID is enabled. Future releases will include help on how to navigate this authentication flow in your e2e tests.

## Theming your Application

This project utilizes [PrimeNG Sass Theme](https://github.com/primefaces/primeng-sass-theme), and a theme created by the Web Enablement team called the `blue-oval-theme`. These theme files are located within the `@wame/blue-oval-theme` npm package.

For more information and instructions on theme setup please refer to the [Blue Oval Theme Setup Guide](https://github.com/ford-innersource/devenablement.wame.primeng-designer/tree/v18.0.0).

Note that this 18.x release of `frf-starter` only works with the [18.x version of our PrimeNG Designer Showcase theme](https://github.com/ford-innersource/devenablement.wame.primeng-designer/tree/v18.0.0).
This is due to changes in structure and variables additions/removals that occur between PrimeNG Designer releases. Newer releases of PrimeNG Designer may work with older `frf-starter` releases, but are not recommended nor officially supported.

### A Note on Accessibility

Accessibility is of the utmost importance to us. Developing an accessible application means making it usable to everyone, including those with visual, hearing, motor, and cognitive impairments. We follow Ford Motor Company's minimum accessibility standards, which are based on the Web Content Accessibility Guidelines (WCAG) 2.1 Level AA. For more general information on accessibility, see the [Accessibility for Developers guide](https://github.com/ford-innersource/devenablement.wame.web-devguides/tree/main/web-accessibility).

## Continuous Integration (CI)

### Tekton

Version 18.x of `frf-starter` includes support for Tekton pipelines-as-code (PaC) for CI and CD to GCP. You can find the pipeline files in the `.tekton/` directory, and instructions on how to configure what is included in [our frf-tekton-pipelines repo](https://github.com/ford-innersource/devenablement.wame.frf-tekton-pipelines/tree/v18.0.1).

## Other Libraries and Features

This starter project is designed to be an empty scaffold to help kickstart your development efforts. To that end, it deliberately only includes the minimum set of dependencies required to be effective. There are other libraries, such as Chart.js (dynamic charting), NGXS (reactive-style state management), and some others that are approved for use but are not included here. For further information, contact <wamcoe@ford.com>.

## Notable Changes on Each Release

We document the changelog for each release of this starter project in [our GitHub repository's Release Notes](https://github.com/ford-innersource/devenablement.wame.frf-starter/releases). There, you'll find not only the list of feature commits that are included, but also a brief writeup and description of what changes with the release.

## Migrating from a Previous Release

Migrating to Angular 18.x is relatively simple if you are migrating from 17.x. For more information on version migrations between FRF releases, see the [MIGRATION.md docs](https://github.com/ford-innersource/devenablement.wame.web-devguides/tree/main/migration) in our Web DevGuides. Like Google, we only support migrating from one major version to the next, never skipping major versions.

## Upcoming Releases and Version Support

This version of the `frf-starter` should be considered a snapshot in time view of the larger Angular ecosystem. We strive to keep this template up to date with the latest releases from the Angular team, but keep in mind that we also have cascading dependencies and implications to consider when it comes to our release cadence. For more information, review [our release documentation in RELEASE.md](https://github.com/ford-innersource/devenablement.wame.frf-starter/blob/main/.github/RELEASE.md).

When a new version of our frf-starter template is released, we strongly recommend that teams use the new frf-starter template version and the [MIGRATION.md docs](https://github.com/ford-innersource/devenablement.wame.web-devguides/tree/main/migration) to keep their application up to date as soon as possible. Upgrading provides new feature enhancements and potential performance improvements, but perhaps more importantly, also ensures crucial security updates are applied and helps to eliminate technical debt.

In terms of version support, we track [the support policy and schedule that is published by the Angular team](https://angular.dev/reference/releases#support-policy-and-schedule). We do our best to keep up with the current Active releases mentioned there, but keep in mind that various external factors may cause us to stay on an earlier release beyond Active support and into Long-Term Support (LTS). We will always plan to provide updated frf-starter templates and migration guides before a specific version's LTS support by the Angular team ends. At a minimum, we recommend that teams ensure their application does not fall further behind than the provided LTS dates.

## PWA Screenshots

Screenshots are used to visually represent the app within the Progressive Web App (PWA) installation prompt. These screenshots enhance the user experience by giving a preview of the app's functionality and interface before installation.

FRF Starter includes a pre-defined set of images that are based on the template alone. Images in `/src/pwa-images` should be updated to give a depiction on what a user can expect when they install your application as a PWA. For each screenshot that you include you need to update the `manifest.webmanifest` with matching metadata. You should include images for both `wide` and `narrow` `form_factor`. You can see the example metadata from [the web manifest file provided](/src/manifest.webmanifest#L67-L84)

```json
"screenshots": [
    {
      "src": "assets/pwa-screenshots/pwa-screenshot-1.png",
      "sizes": "1920x1030",
      "type": "image/png",
      "form_factor": "wide",
      "label": "Homepage",
      "description": "You are seeing an FRF-Starter project installation."
    },
    {
      "src": "assets/pwa-screenshots/pwa-screenshot-mobile-1.png",
      "sizes": "390x844",
      "type": "image/png",
      "form_factor": "narrow",
      "label": "Mobile Homepage",
      "description": "You are seeing an FRF-Starter project installation."
    }
  ]
```

For more information on what metadata to include for each screenshot you can see the [MDN documentation on including screenshots with the web app manifest](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Manifest/Reference/screenshots).

## Deprecated and Replaced Assets

The following references in `manifest.webmanifest` file are deprecated in v18.x release.

- `android-chrome-192x192.png`
- `android-chrome-512x512.png`

While the old references listed above can still be used in the current version, in our v19.x release, they will be removed and teams will have to make changes in `manifest.webmanifest` to point to the following new references if they have not done that already by then.

- `pwa-192x192.png`
- `pwa-512x512.png`
- `pwa-384x384.png`
- `pwa-1024x1024.png`

`Ford Antenna typeface` is also deprecated in v18.x and associated files will be replaced with `Ford F-1` in our 19.x release.

## Notes

- For more details on the Angular 18.x release, take a look at the [Angular 18.0 release blog post](https://blog.angular.dev/angular-v18-is-now-available-e79d5ac0affe).
- For more details on other breaking changes that could come up in your application, take a look at the [Angular Update Guide](https://angular.dev/update-guide?v=17.0-18.0&l=2).

## Getting Help

Projects built using this starter template are supported by the Web Enablement team. If you need help solving a problem or answering a question, the following materials and channels are available to you:

1. The [Dev Enablement GitHub discussion forum](https://github.com/ford-innersource/devenablement-user-community/discussions) is set up to discuss both backend and frontend questions and topics on Dev Enablement offerings. The Web Enablement team asks that you utilize this forum as the point-of-contact for questions and discussions on the Web Enablement offerings, including frf-starter.
1. [The Web DevGuides repo](https://github.com/ford-innersource/devenablement.wame.web-devguides) contains a collection of internally-written materials to help answer common questions that come up when using this template and its tooling. Try starting with these materials to see if a guide has already been written about the problem you're facing.
1. For general setup and environment questions, [the Dev Central tool](https://dc.ford.com/) provides information on various tools and services offered by the Enablement community.
1. If the above is not sufficient for solving your problem on your own, you can chat with the broader frontend community via the [Front-end Developers Channel in the Dev Enablement User Community Team](https://teams.microsoft.com/l/channel/19%3Ad86d1d4a92e24510a07a648a477d80fd%40thread.tacv2/Front-end%20Developers?groupId=3344a579-900b-495b-8c37-218de488fd87&tenantId=c990bb7a-51f4-439b-bd36-9c07fb1041c0).
