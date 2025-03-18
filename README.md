# Introduction
This is a KMP repository for a minimal project that showcases a git-subtree setup for sharing KMP code between 3 repositories: [Android](https://github.com/gohlinka2/KMPIntegrationDemoAndroid), [iOS](https://github.com/gohlinka2/KMPIntegrationDemoiOS) and KMP itself. The idea is to share source code between the platforms as opposed to sharing binaries, which makes iteration and debugging much faster and attempts to minimize the inequalities between the Android/iOS developer experiences (to a certain degree). When we share source, we can also look at KMP differently - not as a library/dependency that is developed independently and then just consumed in our project, but as a part of the app's codebase, in the sense that we can edit KMP and native code as a unit (e.g. when building a feature, we can do changes to both KMP and native code together and debug immediatelly). If course eventually we need to push our changes to the KMP code back upstream so that the other platform can grab it, which is what this doc describes. More information about this approach can be found in [this article from TouchLab](https://touchlab.co/kmp-teams-use-source).

For integration of the KMP source into the native repos and sharing changes back up to the KMP repo, we use git-subtree (alternative to git-submodules, etc.).

# Setup
To start:

3 repositories:
- KMP
- Android
- iOS

In KMP, create main-android and main-ios branches from the main branch. For now, they are the same as main, but they will each mirror the main branches of the native repos, but only have commits related to KMP.

In the following lines, there are 2 approaches - either with `--squash` and without it. Both have an advantage and a disadvantage and it is up to you to decide where you want to make compromises. I will describe both.

To integrate KMP into native code (let's say the Android part now):

Add a named kmp remote first:

`git remote add kmp <remote git>`

`git subtree add --prefix=kmp kmp main-android`

This adds the KMP repo and includes it into our native repo's git. There is no fetching or management involved like with submodules, and when you clone the native repo, you now also get the KMP code with it because it is included in git same as any other file.
If you add `--squash`, it squashes the history up to this point into a single commit, so your native repo's history is nice and clean. The drawback is of course that you don't see the history of the KMP repo locally. If you omit `--squash`, you can see the history of the KMP repo in your native git's graph. The graph is not very nice, but it works. Not squashing also has further consequences that will become apparent further on - keep reading.

Let’s do a feature:
Branch out from the main branch of the native repo as usual and do a commit. For demonstration purposes, make one that has changes both to KMP code and native code at once. When you are done, you would then push the changes to the native repo remote and do the CR process as usual. After merging, a CI process runs, that backports our commits back to the KMP repo so that it can be eventually synced to the other platform.

To backport (done in CI after pushing to main):
Assuming we are in android:

`git subtree push -P kmp kmp main-android --annotate="[KMP-And]"`

This scans our branch for all commits that touch the KMP code. If there are any that are not yet upstream, it recreates them (essentially cherry-picks) and only includes changes done kmp. Therefore, the commit you made, that made changes to both native and kmp code, is copied as if it just did changes to KMP. The copied commits are prefixed with [KMP-And] to quickly identify commits that are made to KMP from Android. The reason for the prefix will become apparent once we start pulling changes in (but mostly in case you don't `--squash`).

Once duplicated, it pulls the branch upstream to main-android. Because this just mirrors our main branch in android, the push should never fail. If there no commits to push to KMP, it correctly reports „Everything up-to-date“ and succeeds.

The commits we made to KMP in the native repo are now mirrored in the KMP repo, waiting to be grabbed by someone from the other platform. 

To do that (we are now switching context to the other platform, let’s suppose iOS):

`git subtree pull --prefix=kmp kmp v2/main-android --squash`

This pulls in and merges the changes from the android counterpart.
- If you `--squash`, you will only see a squash & merge commit, and you cannot see the individual commits made in Android. You can check them out in the KMP branch clearly, but not in your iOS repo where you will most probably be looking at the files.
- Conversely, if you don't squash, you will see the commits from Android prefixed with [KMP-And] in the graph. This is where the prefix comes into play - suppose we now made another feature in iOS and pushed it to main-ios. Eventually, we would then like to integrate this feature in the android part. So we pull the main-ios branch, which will pull in the new [KMP-iOS] commits, but it will unfortunately also pull in the [KMP-And] commits that are cloned from our native commits. We will end up with having duplicated commits in our native history - one set is the one that contains the platform changes along with the KMP changes, and the prefixed ones are those that are stripped of the platform changes. This is unfortunately the price we have to pay for seeing the full commit history from both platforms, but at least with the prefix you can quickly see what’s what.

The choice of to squash or not to squash is up to you.

If there are any conflicts, you resolve them. Then go through CR process as usual, and once this integration PR gets merged, the backport CI check runs again to push the changes we just made to main-ios. If you check main-ios in the KMP repo, you can see that it now also contains the commits prefixed with [KMP-And].

Note: the CI check is not included here, but it should not be difficult to build it with GitHub Actions.
￼
Reference: https://medium.com/p/mastering-git-subtrees-943d29a798ec

# How builds work in Android

The KMP is included as a Gradle module as if it always were a part of the project.

# How builds work in iOS

There is a build phase script that builds and embeds the local KMP project. You can check it in the build phases of the iOS project.
This means that unlike when using a library publishing approach, you can make changes to KMP locally and then directly run the app via the XCode run pipeline. However, I still recommend to open the KMP subdirectory in a Kotlin IDE and edit it there so you get code completion, etc.