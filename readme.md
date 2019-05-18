## Project Overview

[![Play Badge](https://play.google.com/intl/en/badges/images/badge_new.png)](https://play.google.com/store/apps/details?id=com.mainstreetcode.teammate)

Teammate is the culmination of work started in 2017 to boost my expertise of the typical application full stack.
The entire project consists of:

A MERN web application built entirely on Google cloud:

* Database: The MongoDb instances for both Dev and Prod are managed by MLab (Now acquired by MongoDb Atlas) and hosted on Google Cloud and visible only to IP traffic from the Google Cloud project.
* Multimedia storage: Hosted on Google Cloud Storage, the dev environment uses a staging bucket so uploads are automatically deleted after 2 weeks, prod uses a standard bucket.
The app uses Google Cloud Vision to filter any obscene material that is user facing like user photos or team logos.
* Chat: Provided via socket.io which is just an absolute delight to use.

Long story short, in the 2 years I built both the Android and Web Applications for this app, I've really really fallen for Google Cloud as a one stop shop for PaaS solutions.

This repository is for the client Android App. A Video overview for the app can be seen [here](https://youtu.be/h5IX_ACiSL0). Specifics for how the tournament marquee feature works can be seen in this [video](https://youtu.be/WBEA_SmRqmQ).

## Repository Overview

This repo is managed using git flow. The latest code is in the develop branch.

Teammate is a fully featured modern Android team management app that:

* Is offline first
* Is pseudo single activity and heavily fragment dependent (It's got 2. Registration is it's own activity and flow, the rest of the app is the ```MainActivity```)
* Uses the latest iteration of material design including expandable Floating Action Buttons, vertical drawer navigation, bottom sheets and the like.
* Makes animation and transitions a priority and treats them as first class parts of the user experience.
* Is Android Q edge to edge ready.

## Architecture Overview

Teammate is a MVVM Android Application that depends heavily on Functional Reactive Programming
(RxJava) and the Android Architecture Components to function.

On a high level, it makes the following assumptions:

* `Model` objects are effectively final from a UI perspective. Should a `Model` object be changed as a result of an IO call,
the actual reference to the object need not change, rather it's internal transient state should be updated.
* Lists of items are effectively final within the lifecycle of the ```View``` displaying it.
The items within the list may change as a result of IO calls, but the list reference itself must never point to a new List instance.
* All `Model` objects displayed to the user can be represented as a list of smaller, discrete items and these items can be displayed in a simple `RecyclerView` `ViewHolder`.
* Manipulation of the `ViewHolder` by the user of the app, should change the underlying item it is bound to, and therefore the enclosing `Model`.
* Data changes should always be represented as a reactive stream where possible (`Flowable`, `Single`, etc). This makes it easier to reuse logic all through the app.
* Imperative style code for updating UI should be avoided where possible. updates to UI
should be contained within the `bind()` method of a `ViewHolder` near exclusively. This way the state of the UI can only be changed by binding and rebinding a `ViewHolder`.

These assumptions are realized via the following core objects:

### MainActivity

The main `Activity` of this app can be considered a Controller of ViewControllers (```Fragments```) and persistent global views.
It handles The ```UiState``` object which is responsible for the presentation of global persistent UI like Toolbars, Floating Action Buttons, Bottom Sheets and SnackBars.
The navigation of this app makes sure every single full screen destination is added to the back stack. Any destination that is deemed transient, and shouldn't be in the back stack, for example picker or selector screens, will be shown via a sliding up bottom sheet.
This makes shared element transitions, of which there are a lot of in this app, simple to create, and maintain.

### RecyclerView

Besides the sign up flow and a few others, all data is presented in a RecyclerView. Model objects of any kind are broken up into Lists of `Differentiable` implementations
and bound to their respective `ViewHolders`. UI Changes are broadcast solely through `diffResult.dispatchUpdatesTo(RecyclerView)` or `RecyclerView.notifyDataSetChanged()`.
That is, the UI typically has little to no idea the type of the model it binds to, it merely notifies the adapter it hosts of changes.
```InputViewHolder``` is a fine example of this, and is particularly interesting because it's backed by a ```RecycledViewPool``` cached in the ```MainActivity```. It forms the basis for any ```View``` that presents a "form" to the user.

### Differentiable

An `Differentiable` is the basic building block of this app. It represents an item that can change and therefore is "Diff-able", so a ```List``` of it can
be diffed against another ```List``` and the result dispatched to the hosting `RecyclerView`.

### Repos

Stateless (mostly, ```UserRepo``` is the only exception and can be reworked.) singleton objects that perform IO work and publish the result of that work as Reactive wrappers around `Model` objects. All reactive objects exposed by them are published on a background thread, no exceptions.
Should any of these reactive objects need to reach a ```View```, they must go through a ```ViewModel``` which will perform all necessary operations to convert it to a reactive object the ```View``` is concerned with; this is typically a ```DiffUtil.DiffResult```.

### ViewModels

Liaison between `Repositories` and `Views`. Most often converts a reactive `Model` type, to a reactive `DiffResult` type to publish on the UI thread. They also hold most application state, and may communicate with each other as some `Models` (```Teams``` and ```Roles```)are interdependent.

### Gofers

Like their name [suggests](https://youtu.be/zd5M5K5QtrM?t=56s), they communicate to a `ViewModel` changes to an individual `Differentiable` that may be part of a larger list.
This is especially useful if the `Model` is invalidated and has to be deleted from the parent list in the `ViewModel`.

### Notifier

Responsible for creating push notifications of `Models`. Think of them as ```Repositories``` for notifications.

## UI Overview

With the architecture defined above, various UI manipulations can be abstracted out and delegated to specialized objects.
Some of these are:

### ScrollManager

An object which makes its easier to add UI characteristics to Lists such ass endless scrolling, pull to refresh, and notifying of `Model` changes.
It is an extension of the ListManager class, ScrollManager was created first, then eventually abstracted out to ListManager as a downstream dependency.

### FabIconAnimator

An object that keeps track of the current drawable and text displayed in the Floating Action Button and animates it as appropriate.

## Miscellaneous Notes

* Besides a few Room Migration tests, the app is devoid of tests because the iteration pace was so quick, and remains so,
writing Unit tests for the business logic would have discouraged major app refactoring; and this app has seen a lot of them.
Since it's a startup app and I'm the major contributor, Tests we put on the backlog.

* Iconography in the app is represented mostly using vector drawables which help keep the size of the app down.

* The app uses Java generics heavily, because I am a huge fan of the level of abstraction they facilitate, but also abuses them in some ways.
The compiler warnings fought in the writing of `TeamMemberRepository` was a very enlightening experience.

* Working with shared element transitions between fragments was a very fulfilling experience.

* The minimum sdk level is set to 21 only because of the app's heavy dependency on `ViewCompat.setOnApplyWindowInsetsListener`.

Some very opinionated Android specific decisions to note if you're interested, otherwise please skip to the next section:

* Why not Kotlin? I started this in 2017. At the time I was choosing between learning a backend / web stack or building this with Kotlin. I chose the former, and I think it was the right choice.
I've recently started learning and working with Kotlin professionally and should be up to speed pretty soon.

* Why no Dagger? While I love dependency injection, I actively despise Dagger for it's opaqueness, slow build times, and infamously verbose compile time errors.
The latter has been greatly improved recently, but I think for an Android App of this size, Dagger is overkill.
Now this is hardly backed up by much, however the things that would be injected in this app are the ```repositories``` and ```notifiers```, and that job is done by
the ```RepoProviders``` and ```NotifierProviders``` respectively and can very easily be mocked. I don't think I'm missing out on much.

* Why Picasso and not Glide? I love dependencies with small binaries and a focused feature set that doesn't try to do too much.
Glide is amazing, but this app doesn't support gifs, and Picasso works very well. There's no reason to bloat the app for a feature it doesn't need.
The resulting app is not multidex, and the size of the apk off Google play is less than 4 MB. That is a metric I am incredibly proud of for such a fully featured app.

## License

MIT License

Copyright (c) 2019 Adetunji Dahunsi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

