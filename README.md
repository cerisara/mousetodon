
![Mousetodon](./mousetodon.jpg)

MouseApp is an android client for the Mastodon social network.

MouseApp is in development stage for now, but it should already fulfill the most basic interactions with any Mastodon server you may expect from an android app.
As compared to the other available android apps, MouseApp is certainly not as nice and visually attractive (because it's not a priority for me),
but it has other advantages depicted below.
Its most noticeable features, which makes it different from the other apps, are:

- **langage detection** and filtering: you may define one or several languages, and only the toots from these languages will be shown.
For now, only the following languages can be detected: French (fr), English (en), Spanish (sp), German (ge) and Japanese-Chinese-Korean (ja) (I know, sorry, these 3 languages are very different, but I'm no expert in them and I'll do my best to improve them in a later release, or
hopefully before thanks to a merge request).
- **multiple accounts/instances** support, with fast switching between them with one-click.
- Open-source and not dependent on any 3rd-party library, especially Google ones.

Target features not implement yet:

- Search in toots
- Various language advanced features (words clouds, clustering...) (I'm in NLP after all !)

Current limitations:

- Does not show user details
- Does not allow (yet) to reblog (in progress... but you can boost !)
- Does not support content warnings, nor media uploads

-----------

## User guide

At the first connection, the app will pop up a window asking for a Mastodon instance (without the http:// prefix, so just type in something like *mastodon.social*),
your email and password on this instance.
You then see 3 buttons on the top left, respectively for showing your notifications, your home timeline and the federated public timeline.

The 4th button allow you to toot a message, and the 5th button gives you the option to switch to the next known instance.
Initially, you should only have defined a single instance, but you can later on easily add another one via the application menu (3 little dots at the bottom right on
"old" Android devices), item "+Account".
Another item in the application menu allows you to switch on and off language detection+filtering, and define the languages to show:
use the two-letter codes (fr en sp ge ja) separated by whitespace.

Any timeline shows a list of toots, with a picture of the sender on the left, and some technical information on top
(the date, whether you have boosted this toot (with a star), the sender name).
At the bottom of the list, you have the option to download older toots.

If you click on any of these toots, a popup windows appears that shows all the medias and URLs links from the toot:
now, if you click on one of these media/URLs, it is opened with the appropriate Android application.
This popup window also gives you the option to reply to this toot, boost or unboost it.

-----------

## How to build

Contrary to other apps, building MouseApp is very simple, because my objective is to be able
to build it even from within an android device itself !
So you don't need heavy IDE like Virtual Studio, nor even internet connections, as there's no external dependencies.
You just need the Java JDK, an Android SDK and the good old ANT build system.
You will soon not need ANT any more as I plan to commit a shell compilation script.

After cloning, you should fix the file *local.properties* and just type:
```
ant -f makeitlocal.xml debug
```
and it should create the apk straight away.

-----------

## Why yet another android app for mastodon ?

There are already several android apps for mastodon. 
However, a number of them are not written in *native java*, which makes them too slow or even
incompatible on old devices.

The *Tusky* app is native, and is also one of the most advanced android client I've found;
it is also packed with lots of features, so I recommend you to try it out first, and if it
does not work fine for you, then you may come back here.
I actually don't like a few of Tusky features:

- Tusky relies on Google Firebase: this makes the app heavy to build, incompatible with old devices,
dependent on Google play services, and your data is not totally in your control.
From firebase site: "[Firebase] helps you quickly [...] earn more money.", 
"At the heart of Firebase is Firebase Analytics",
"Earn money by displaying engaging ads"... Is it really FOSS ?
- Tusky connects to a special server to allow for two-factor authentification; I consider it is a potential threat
for privacy, and the app will not work whenever this additional server is down or attacked.
So I really prefer to use simple login/password authentification, which also looks to work on all Mastodon instances afaik.

Because of these reasons, and also because Tusky do not implement some features that I really want,
I've decided to implement my own android client for mastodon.
The features that are not available on other apps and that I want to have are:

- language detection: a first version is already up and running;
- multiple logins: when you have several accounts on several instances, you can quickly
switch from one account to another;
- support for older devices; my app is designed to work
on an old 2.3.3 android (and all more recent androids as well, of course);
- very simple compilation process, as few external dependencies as possible. I must be able to compile it on an
android smartphone (without any PC: yes, it's possible !).

------------

### A note on SSL handshake error

It *may* be that connection to a Mastodon server just fails, and if you look at the android logs, you see
an *SSL handshake error*. This is a known bug/incompatibility between some versions of Android and some
configurations of some Mastodon server. This issue also affects all native Java android apps, in particular *Tusky* and *Twidere*,
but apparently it does not affect android clients that are not programmes in native java, such as TootyFruity.

The problem is that secure connection with a Mastodon instance can be configured (at the instance level, so
I don't see what can done in the app to fix that) with various TLS protocols, and that from one version to another,
the core Android libraries implement in very different ways these protocols, and sometimes, it just does not work.

Note that version 0.8 of MouseApp application is also affected by this issue, but the new version 0.9 of
the MouseApp application has been built to solve this issue.
The solution adopted consists in removing the faulty HttpUrlConnection class to
communicate with the Mastodon API server and replacing it with javascript.

-------------

## ChangeLog

### v0.8

- Support multi-instances: Switch with the press of a button
- Language detection and filtering: lightweight keywords-implementation, support fr, en, ge, sp, ja

### v0.9

- Rewrote the connection with Mastodon API: replace HttpUrlConnection with Javascript, which solves
a Android/Mastodon SSL handshake incompatibility for all native apps (Tusky and Twidere have the bug).
- Show list of toots per user
- Better date formatting
- Smoother icon loading in the background, with caching
- Support added for following user 

### v0.10

- reblog
- added local timeline
- change reply button: don't link in the metadata, but rather adds @-mention + the URL of the original toot: hence, the reply is also published in the public timeline.

-------------

### Features to be implemented next:

- quick-toot from outside the app with *share as* thanks to TaskLabAct

