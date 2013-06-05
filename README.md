This is Diabol's Builder Play 2.0 app
=====================================

Development & Testing
=====================

### Fake a post from github ###
Use this curl to fake a post from github to trigger a pipeline. Change path to have the pipename you want to start.

    curl --header "Content-type: application/x-www-form-urlencoded" --request POST --data 'payload=%7B%22pusher%22%3A%7B%22name%22%3A%22Grassman%22%2C%22email%22%3A%22daniel.gronberg%40diabol.se%22%7D%2C%22repository%22%3A%7B%22name%22%3A%22nicetohave%22%2C%22size%22%3A120%2C%22has_wiki%22%3Atrue%2C%22created_at%22%3A%222012-07-06T04%3A24%3A43-07%3A00%22%2C%22private%22%3Atrue%2C%22watchers%22%3A1%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FDiabol%2Fnicetohave%22%2C%22fork%22%3Afalse%2C%22language%22%3A%22Java%22%2C%22id%22%3A4923639%2C%22pushed_at%22%3A%222012-10-23T01%3A56%3A34-07%3A00%22%2C%22has_downloads%22%3Atrue%2C%22open_issues%22%3A0%2C%22has_issues%22%3Atrue%2C%22stargazers%22%3A1%2C%22organization%22%3A%22Diabol%22%2C%22description%22%3A%22Place%20to%20put%20small%20nicetohaves%22%2C%22forks%22%3A0%2C%22owner%22%3A%7B%22name%22%3A%22Diabol%22%2C%22email%22%3Anull%7D%7D%2C%22forced%22%3Afalse%2C%22after%22%3A%22c16e58faf1f5952ac5abcbe2e87dd8beada59284%22%2C%22head_commit%22%3A%7B%22added%22%3A%5B%5D%2C%22modified%22%3A%5B%22selenium-example%2Ftest.txt%22%5D%2C%22timestamp%22%3A%222012-10-23T01%3A56%3A21-07%3A00%22%2C%22removed%22%3A%5B%5D%2C%22author%22%3A%7B%22name%22%3A%22Grassman%22%2C%22username%22%3A%22Grassman%22%2C%22email%22%3A%22daniel.gronberg%40diabol.se%22%7D%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FDiabol%2Fnicetohave%2Fcommit%2Fc16e58faf1f5952ac5abcbe2e87dd8beada59284%22%2C%22id%22%3A%22c16e58faf1f5952ac5abcbe2e87dd8beada59284%22%2C%22distinct%22%3Atrue%2C%22message%22%3A%22This%20is%20a%20commit%22%2C%22committer%22%3A%7B%22name%22%3A%22Grassman%22%2C%22username%22%3A%22Grassman%22%2C%22email%22%3A%22daniel.gronberg%40diabol.se%22%7D%7D%2C%22deleted%22%3Afalse%2C%22ref%22%3A%22refs%2Fheads%2Fmaster%22%2C%22commits%22%3A%5B%7B%22added%22%3A%5B%5D%2C%22modified%22%3A%5B%22selenium-example%2Ftest.txt%22%5D%2C%22timestamp%22%3A%222012-10-23T01%3A56%3A21-07%3A00%22%2C%22removed%22%3A%5B%5D%2C%22author%22%3A%7B%22name%22%3A%22Grassman%22%2C%22username%22%3A%22Grassman%22%2C%22email%22%3A%22daniel.gronberg%40diabol.se%22%7D%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FDiabol%2Fnicetohave%2Fcommit%2Fc16e58faf1fasfaw3Qbcbe2e87dd8beada59284%22%2C%22id%22%3A%22c16e58faf1f5952ac5abcbe2e87dd8beada59284%22%2C%22distinct%22%3Atrue%2C%22message%22%3A%22This%20is%20a%20another%20commit%22%2C%22committer%22%3A%7B%22name%22%3A%22Grassman%22%2C%22username%22%3A%22Grassman%22%2C%22email%22%3A%22daniel.gronberg%40diabol.se%22%7D%7D%5D%2C%22before%22%3A%22b3c99b5ae68a6d1bc57159a7a6944e1214899bdc%22%2C%22compare%22%3A%22https%3A%2F%2Fgithub.com%2FDiabol%2Fnicetohave%2Fcompare%2Fb3c99b5ae68a...c16e58faf1f5%22%2C%22created%22%3Afalse%7D' http://localhost:9000/pipe/Component-B/github

### Integration Test
There is a maven project in integration-test to run to perform tests at a given PipeIt installation. Execute
'mvn clean install -Dpipeit.host=http://host:port' to run. If pipeit.host is not specified, the test defaults to 'http://localhost:9000'

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
Marcus is the admin for the Diabol CloudBees account. Andreas and Peter H.M. has admin role as well.

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

ItelliJ
-------

IntelliJ 12.1.3 - Ultimate Edition (but NOT Community Edition), has built-in support for Play framework projects.

First, ensure that the built-in "Playframework Support" plugin is enabled, then install the plugins called "Scala" and
"Play 2.0 support" from the Jetbrains repository.

Second, from the command line in the root directory, perform the following commands:
> play clean
> play run
(Hit Ctrl-C to quit the application, then...)
> play idea

Now reopen the project, wait a minute for all the indexing to complete, and all the red should be gone.
