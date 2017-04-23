[](mousetodon.jpg)

This is an application for reading Mastodon toots.
It is in early development stage for now.

Its current features are:

- Email/password connection to a Mastodon instance
- Download and show timelines: Home, Notifications, Public
- Optional langage detection and filtering: you may define one or several languages, and only the toots from these languages will be shown
Note that this requires *a lot of RAM*: if the app crashes when you activate this function, you don't have enough RAM and you cannot
use it. But I plan in the near future to implement a lightweight version of this function.

Target features I would like to implement:

- Search in toots
- Show public timelines of any instance without account
- Write toots
- Switch between several accounts easily
- Various language advanced features (words clouds, clustering...) (I'm an NLP researcher after all !)

Current limitations: (which make it really beta for now; gonna implement them asap)

- Does not show images / videos
- Does not allow to reply, reblog nor favorize (not yet, in progress...)

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

- language detection: a first version is already up and running, but it consumes too much RAM, so I want
to implement another lightweight version of it;
- multiple logins: when you have several accounts on several instances, it is convenient to be able to quickly
switch from one account to another;
- support for older devices (well, apart from the language detection feature); I would like to make an app that works
on an old 2.3.3 android;
- very simple compilation process, as few external dependencies as possible. I must be able to compile it on an
android smartphone (without any PC).

