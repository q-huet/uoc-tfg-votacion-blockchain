# Contributing to FRF

This document will serve as a guide for those who might want to contribute to the FRF effort. This writeup is based on Angular's contribution guide, which we find to be a great way to manage community contributions.

## Have a Question or Problem?

Please don't open issues for general support questions. We want to keep our GitHub issues for bug reports and feature requests. Instead, refer to [the "Getting Help" section in the README.md](README.md#getting-help).

If you do post an issue that we deem to be a support request, we will mark it as such and close the issue. We'll also remind you to post your question in our forum.

## Found a Bug?

If you find a bug in the source code, you can help us by [submitting an issue](#submitting-an-issue) to our [GitHub Repository][github]. Even better, you can [submit a Pull Request](#submitting-a-pull-request-pr) with a fix.

## Missing a Feature?

You can _request_ a new feature by [submitting an issue](#submitting-an-issue) to our GitHub Repository. If you would like to _implement_ a new feature, please submit an issue with a proposal for your work first, to be sure that we can use it.

Please consider what kind of change it is:

- For a **Major Feature**, first open an issue and outline your proposal so that it can be discussed. This will also allow us to better coordinate our efforts, prevent duplication of work, and help you to craft the change so that it is successfully accepted into the project.
- **Small Features** can be crafted and directly [submitted as a Pull Request](#submitting-a-pull-request-pr).

## Submission Guidelines

### Submitting an Issue

Before you submit an issue, please search the issue tracker. It's very possible an issue for your problem already exists, and the discussion might inform you of workarounds readily available.

We want to fix all the issues as soon as possible, but before fixing a bug we need to reproduce and confirm it. Having a live, reproducible scenario gives us a wealth of important information without going back & forth to you with additional questions like:

- version of Angular used
- 3rd-party libraries and their versions
- and most importantly - a use-case that fails

A minimal reproduce scenario allows us to quickly confirm a bug (or point out coding problem), as well as confirm that we are fixing the right problem. We understand that sometimes it might be hard to extract essentials bits of code from a larger code-base but we really need to isolate the problem before we can fix it. Interestingly, from our experience, users often find coding problems themselves while preparing a minimal scenario.

### Submitting a Pull Request (PR)

Before you submit your Pull Request (PR) consider the following guidelines:

1. Search [FRF-starter pull requests](https://github.com/ford-innersource/devenablement.wame.frf-starter/pulls) for an open or closed PR that relates to your submission. You will not want to duplicate effort.
1. Clone our ford-innersource/devenablement.wame.frf-starter repo.
1. Make your changes in a new git branch:

   ```shell
   git checkout -b my-fix-branch dev
   ```

1. Create your patch, **including appropriate test cases**.
1. Run the full test suite, as described in the [developer documentation][dev-doc], and ensure that all tests pass.
1. Commit your changes using a descriptive commit message

   ```shell
   git commit -a
   ```

   Note: the optional commit `-a` command line option will automatically "add" and "rm" edited files.

1. Push your branch to GitHub:

   ```shell
   git push origin my-fix-branch
   ```

1. In GitHub, send a pull request to `frf-starter:dev`.

- If we suggest changes then:
  - Make the required updates.
  - Re-run the Angular test suites to ensure tests are still passing.
  - Re-push your code to GitHub (this will update your PR)

  ```shell
  git push
  ```

That's it! Thank you for your contribution!

#### After your pull request is merged

After your pull request is merged, you can pull the changes from the dev (origin) repository:

- Check out the dev branch:

  ```shell
  git checkout dev -f
  ```

- Delete the local branch:

  ```shell
  git branch -D my-fix-branch
  ```

- Update your dev with the latest origin version:

  ```shell
  git pull --ff origin dev
  ```

[dev-doc]: DEVELOPER.md
[github]: https://github.com/ford-innersource/devenablement.wame.frf-starter
