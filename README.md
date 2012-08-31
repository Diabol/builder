This is Diabol's Builder Play 2.0 app
=====================================

Build Infrastructure
====================

CI
------
There is a Jenkins-as-a-Service running provided by CloudBees on:
https://diabol.ci.cloudbees.com

To build the project we [installed the sbt plugin](http://wiki.cloudbees.com/bin/view/DEV/Playframework).

As described in
[GitHub Commit Hooks](http://wiki.cloudbees.com/bin/view/DEV/GitHub+Commit+Hooks+HOWTO)
we installed the GitHub plugin so the build can be triggered by a WebHook (a 
kind of Service Hook), i.e. a POST from GitHub to the CI: 
https://diabol.ci.cloudbees.com/github-webhook/. 

Since the repo is private we read
[How to use Private GitHub Repositories with CloudBees](http://wiki.cloudbees.com/bin/view/DEV/How+to+use+Private+GitHub+Repositories+with+CloudBees) 
and added
[CloudBees SSH public key in the GitHub repository as a Deploy Key](https://help.github.com/articles/managing-deploy-keys).

### Accounts
Marcus is the admin for the Diabol CloudBees account. Daniel G has admin role as well.

HEROKU
------
The app can be published on Heroku to be publicly available. The deployment to
Heroku is triggered manually by: `git push heroku master`.

The only addition necessary to the code is the `Procfile` in the root.

Currently it will be deployed to http://limitless-stream-3541.herokuapp.com but
this can be modified.

Marcus followed this:
http://www.playframework.org/documentation/2.0.2/ProductionHeroku
and had a quick look at:
http://www.jamesward.com/2012/02/21/play-framework-2-with-scala-anorm-json-coffeescript-jquery-heroku
and:
https://devcenter.heroku.com/articles/play but note that this is for _Play 1_.

Heroku CLI: https://toolbelt.heroku.com/osx

### Accounts
Marcus created the app on Heroku and invited Andreas and Daniel to it. Anyone
can create a new Heroku app if they want to.
