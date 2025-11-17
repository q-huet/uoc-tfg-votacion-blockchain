# Building and Testing

This document describes how to set up your development environment to build and test FRF.
It also explains the basic mechanics of using `git`, `node`, and `npm`.

## Prerequisite Software

Before you can build and test, you must install and configure the
following products on your development machine:

- Git - [GitHub's Guide to Installing Git](https://docs.github.com/en/get-started/quickstart/set-up-git) is a good source of information. For internal Ford resources, see the [GitHub Guide](https://github-guide.ford.com/) that the Dev Tools team provides.

- Node.js - this is used to run tooling, tests, and generate distributable files. Install Node.js on your computer; Follow the [Installation instructions for Node.js](https://azureford.sharepoint.com/sites/WebCOE/Pages/Node.js.aspx).

- An IDE - WaME recommends WebStorm or Visual Studio Code (VS Code). Installers can be found on the [JetBrains' website for WebStorm](https://www.jetbrains.com/webstorm/download/) and on [Microsoft's VS Code webpage](https://code.visualstudio.com/). WebStorm or VS Code are not required, although an IDE that is TypeScript aware is highly recommended.

## Acquiring the Source Code

Note that we track two main branches in the frf-starter repo - _main_ and _dev_. _main_ should never be touched directly - treat it as our production code. Releases will be tagged from this branch.

_dev_ is our working branch for development efforts, but again, don't check code directly into this branch. Instead, we use a feature branching strategy, where each bit of work will be done in a branch off of _dev_, and will then be merged into _dev_ with a pull request. Typically, these branches will be named to reflect the GitHub issue and the item being worked on. For instance, if you're working on issue #123, you'd create a branch named _123-feature_.

### Cloning the frf-starter repository

1. [Login to Ford's GitHub Enterprise Cloud (GHEC)](https://github.com) with `CDSID_ford` as the username. If you don't have access, follow the [instructions to gain GHEC access](https://github-guide.ford.com/ch01/04-request-access-dot-com.html)
1. Visit the [devenablement.wame.frf-starter repository](https://github.com/ford-innersource/devenablement.wame.frf-starter)
1. Clone the repository somewhere locally. (note that code snippets here should be performed in GitBash on Windows).
   ```shell
   cd <your local project directory>
   git clone https://github.com/ford-innersource/devenablement.wame.frf-starter.git
   ```

## Setting up your local workspace

1. Change into the project directory and set up local branches

   ```shell
   #change into the project
   cd devenablement.wame.frf-starter

   #checkout the dev branch
   git checkout dev

   #create a new local branch from dev
   git checkout -b <name of branch here>
   ```

### Installing NPM Modules

Next, install the JavaScript modules needed to build and test Angular:

```shell
npm install
```

Note that this runs a npm install locally, which is different from the `npm ci` (clean install) in CI/CD that ensures the installation will match as closely as possible to the packages that we ship in this project.

Additionally, this installation process relies on configuration in the project's `.npmrc` file. This configuration is set up to properly leverage Ford's dependency repo, JFrog Artifactory.

If you see errors during the installation that refer to unavailable hosts, or other seemingly network related issues, ensure your proxy configs are correct using the [system level proxy environment variables guide for macOS](https://github.com/ford-innersource/devenablement.cloudnative.dev-guides/tree/main/spring-boot/proxy-configuration#macos) or the [system level proxy environment variables guide for for Windows](https://github.com/ford-innersource/devenablement.cloudnative.dev-guides/tree/main/spring-boot/proxy-configuration#windows). As JFrog Artifactory is a public externally hosted platform, while on VPN, or onsite, proxies are required to be configured to reach the JFrog Artifactory server.

### Building

To build frf-starter for dev, run:

```shell
npm run build-dev
```

Similarly, to run a prod build, use:

```shell
npm run build-prod
```

Results are put in the `dist` folder.

### Serving content locally

You have two options for serving your built content locally. Which one you choose depends on personal preference. They are:

1. `npm run build-dev` + a standalone WebServer (like WebStorm's), running on port 9500. If using this strategy, a build will need to be manually triggered whenever a set of code changes has been completed. The `npm run build-dev` command will build the application and put the output into the `dist` folder. Contents of this file should then be served by the WebServer of your choice.
1. `npm run start-local`. If using this strategy, a watcher will start up that will build the project whenever a file is saved. Builds will run in memory, and the files will be served by angular-cli's integrated development WebServer, which is configured to run on port 9500.

Because of this, you cannot use both of these options at the same time - your workstation only has a single port 9500.

### Running Tests Locally

To run tests:

```shell
# unit tests
npm run test

# end to end (e2e) tests
npm run e2e-local
```

You should execute both test suites before committing any code and pushing to GitHub.

All the tests are executed on our Continuous Integration (Tekton) infrastructure, and will currently run when code attempts to be integrated with the _dev_ branch. The CI process will fail if your tests fail.

## Coding style and formatting

The frf-starter project is set up with a `.editorconfig` file that defines our desired style and formatting conventions. Editors/IDEs like WebStorm and VS Code will obey this config and provide guidance on how to format your code. Also, editors can perform auto-formatting using a hotkey combination - for instance, to reformat the current file in WebStorm, press `CTRL`+`ALT`+`L` on Windows.

## Linting/verifying your source code

The project is set up with _ESLint_, _Stylelint_ and _Yamllint_ for static code analysis. These tools can run automatically in Webstorm. To enable this, see instructions on how to [configure ESLint automatically](https://www.jetbrains.com/help/webstorm/eslint.html#ws_js_eslint_automatic_configuration) and [configure Stylelint automatically](https://www.jetbrains.com/help/webstorm/using-stylelint-code-quality-tool.html#ws_stylelint_configure). Yamllint can be configured by following our [Yamllint Installation and Configuration guide.](https://github.com/ford-innersource/devenablement.wame.frf-tekton-pipelines/blob/main/.github/docs/Yamllint.md)

In VS Code, full support for ESLint requires that the [ESLint extention](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) from Microsoft be installed. Options and settings can be found on the [VS Code ESLint extension GitHub page readme](https://github.com/Microsoft/vscode-eslint?tab=readme-ov-file#vs-code-eslint-extension).

In VS Code, full support for stylelint requies that the [vscode-stylint extention](https://marketplace.visualstudio.com/items?itemName=stylelint.vscode-stylelint) be installed. Options and settings can be found on the [vscode-stylelint extension GitHub page readme](https://github.com/Microsoft/vscode-eslint?tab=readme-ov-file#vs-code-eslint-extension)

If you prefer to see this output on the command line, you can trigger both by running:

```shell
npm run lint-local
```

## Pushing/Pulling code

When your work is complete, you'll want to push your code to the remote for your feature branch:

```shell
git push
```

If it is the first time you're pushing code, you'll need to set your upstream branch:

```shell
git push --set-upstream origin <my branch>
```

Once your code is in the remote repo, you'll want to create a pull request to pull your changes into _dev_. In the comments, make sure to reference the issue you were working on using the [closing keyword syntax](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue#linking-a-pull-request-to-an-issue-using-a-keyword). An example of this would be:

```shell
This PR closes #123.
```

## Running Lighthouse Tests

![Lighthouse Settings](images/Lighthouse%20Settings.png)
We currently use the following suggested settings for performing [Lighthouse Testing](https://developer.chrome.com/docs/lighthouse/overview/).

- [x] Clear Storage
- [ ] Enable JS sampling
- [x] [Simulated Throttling (default)](https://github.com/GoogleChrome/lighthouse/blob/main/docs/throttling.md#devtools-lighthouse-panel-throttling)

**[Mode](https://github.com/GoogleChrome/lighthouse/blob/HEAD/docs/user-flows.md#the-three-modes-navigation-timespan-snapshot)**

- [x] Navigation(default)
- [ ] Timespan
- [ ] Snapshot

**Device:**

- [x] [Mobile](https://developer.chrome.com/docs/lighthouse/performance/performance-scoring/#desktop)
- [ ] [Desktop](https://developer.chrome.com/docs/lighthouse/performance/performance-scoring/#desktop)

**Categories:**

- [x] [Performance](https://developer.chrome.com/docs/lighthouse/performance/performance-scoring)
- [x] [Accessibility](https://developer.chrome.com/docs/lighthouse/accessibility/scoring)
- [x] [Best Practices](https://developer.chrome.com/docs/lighthouse/best-practices/doctype)
- [x] [SEO](https://developer.chrome.com/docs/lighthouse/seo/meta-description)
- [x] [Progressive Web App](https://developer.chrome.com/docs/lighthouse/pwa/load-fast-enough-for-pwa)

### Baseline Scores

![Lighthouse Scores](images/Lighthouse%20Baseline%20Scores.png)

After running Lighthouse we want to ensure the following baseline scores aren't hit with regressions as we develop our applications. Lighthouse may produce different results depending on the page it is run on.

**Baseline Scores for Page Under Development:**

- Performance: 80
- Accessibility: 80
- Best Practices: 100
- SEO: 80
- Progressive Web App: ✔️

### References

- [Performance Throttling Reference](https://github.com/GoogleChrome/lighthouse/blob/main/docs/throttling.md#devtools-lighthouse-panel-throttling)

- [Lighthouse Scoring Calculator](https://googlechrome.github.io/lighthouse/scorecalc/)
