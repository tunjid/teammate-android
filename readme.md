## Repository Overview

This repo is managed using git flow. The latest code is in the develop branch.

## Architecture Overview

Teammate is a MVVM Android Application that depends heavily on Functional Reactive Programming
(RxJava) and the Android Architecture Components to function.

On a high level, it makes the following assumptions:

* `Model` objects are effectively final from a UI perspective. Should a `Model` object be changed as a result of an IO call,
the actual reference to the object need not change, rather it's internal transient state should be updated.
* Lists of items are effectively final within the lifecycle of the View displaying it.
The items within the list may change as a result of IO calls, but the list reference itself must never point to a new List instance.
* All `Model` objects displayed to the user can be represented as a list of smaller, discrete items and these items can be displayed in a simple `RecyclerView` `ViewHolder`.
* Manipulation of the `ViewHolder` by the user of the app, should change the underlying item it is bound to, and therefore the enclosing `Model`.
* Data changes should always be represented as a reactive stream where possible (`Flowable`, `Single`, etc). This makes it easier to reuse logic all through the app.
* Imperative style code for updating UI should be avoided where possible. updates to UI
should be contained withn the `bind()` method of a `ViewHolder` near exclusively. This way the state of the UI can only be changed by binding and rebinding a `ViewHolder`.

These assumptions are realized via the following core objects.

### RecyclerView

Besides the sign up flow and a few others, all data is presented in a RecyclerView. Model objects of any kind are broken up into Lists of `Identifiables`
and bound to their respective `ViewHolders`. UI Changes are broadcast soley through `diffResult.dispatchUpdatesTo(RecyclerView)` or `RecyclerView.notifyDataSetChanged()`;

### Identifiable

An `Identifiable` is the basic building block of this app. It represents an item that can change and therefore is "Diff-able", so a List of it can
be diffed against another List and the result dispatched to the hosting `RecyclerView`.

### Repository

Stateless singleton objects that perform IO work and publish the result of that work as Reactive wrappers around `Model` objects.

### ViewModels

Liaison between `Repositories` and `Views`. Most often converts a reactive `Model` type, to a reactive `DiffResult` type. They also hold most application state, and may coommunicate with each other as some `Models` are interdependent.

### Gofers

Like their name [suggests](https://youtu.be/zd5M5K5QtrM?t=56s), they communicate to a `ViewModel` changes to an individual `Identifiable` that may be part of a larger list.
This is especially useful if the `Model` is invalidated and has to be deleted from the parent list in the `ViewModel`.

### Notifier

Responsible for creating push notifications of `Models`.

## UI Overview

With the architecture defined above, various UI manipulations can be abstracted out and delgated to specialized objects.
Some of these are:

### ScrollManager

An object which makes its easier to add UI characteristics to Lists such ass endless scrolling, pull to refresh, and notifying of `Model` changes.

### FabIconAnimator

An object that keeps track of the current drawable displayed in the Floating Action Button and animates it as appropriate.

## Miscellaneous Notes

* Besides a single Room Migration test, the app is devoid of tests because the iteration pace was so quick, and remains so,
writing Unit tests for the business logic would have discouraged major app refactors; and this app has seen a lot of them.
Since it's a startup app and I'm the major contributor, Tests we put on the backlog.

* Iconography in the app is represented mostly using vector drawables which help keep the size of the app down.

* The app uses Java generics heavily, because I am a huge fan of the level of abstraction they facilitate.
The compiler warnings fought in the writing of `TeamMemberRepository` was a very enlightening experience.

* Working with shared element transitions between fragments was a very fulfilling experience.

* The minimum sdk level is set to 21 only because of the app's heavy dependency on `ViewCompat.setOnApplyWindowInsetsListener`.

