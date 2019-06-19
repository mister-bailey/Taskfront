# Taskfront
Dynamic-list oriented task and project management app


Taskfront is personal task and project planning app (a fancy to-do list), designed to provide, via a minimalist user interface, a number of features which the author (Michael Bailey) wants, but which are consistently lacking in existing commercial solutions to this day.

Development is ongoing. Try it out at bailey-list.appspot.com

Full user documentation does not exist yet, but what follows is a description of the project.

GENERAL FORMAT:

"Items" (eg., tasks or projects) exist in a database, and are collected into lists, and these lists are displayed and manipulated via a minimalist web app. An Item may be a member of multiple lists, and may itself have a list of sub-Items. Lists are either basic lists, or dynamically generated from other lists via inclusion and sorting criteria.

For example, in the standard setup, a collection of tasks is included by default in a main list, but displayed and accessed via a series of dynamic lists---TODAY, THIS WEEK, THIS MONTH, LONG TERM and UNSCHEDULED---whose membership and sorting is determined by a "due date" field on the tasks. But the variations on this are endless (eg., other complex criteria, dynamic lists derived from dynamic lists, unions/intersections of lists).

In the web interface, new items are created and added to a list by pressing carriage return and then typing the main item text, in a deliberately close analogy with how one makes ad hoc lists in a text editor. The tab key moves the current item onto the sub-List of the item above it (shift-tab going the other way). Finally, dragging and dropping items within or between dynamic lists (or invoking one of the other ways of moving items around) automatically updates the underlying criteria so that the items are included and ordered as indicated by the move.

So one intuitive way to manipulate the Item's data fields is to use the appropriate dynamic lists and just move the Item around (for example, there is also a Calendar view available for day-to-day scheduling, and one could imagine creating list categories based on, eg., scheduling of sub-Items).

TECHNOLOGY:

The database back-end uses Google App Engine with its Google Cloud Datastore. The front end uses Google Web Toolkit, a (now) open source system by which one writes web apps in Java with a desktop-style object-oriented GUI library, and then cross-compiles into JavaScript.

Database items are keyed to an OpenID (eg., a Google account), and thus many users can coexist on the same App Engine instance safely. As an intermediate storage between the client's RAM and the cloud, the app writes to HTML5 Local Storage if available, and then pushes to the cloud when available (so that you can safely work on the subway) [This feature is under development and has some bugs.] The app deals gracefully with synchronizing and merging edits from different client instances the user may have running.

In local RAM, dynamic lists are doubly-linked to their source lists, so that any changes to an Item or a source list can propagate its consequences incrementally through the dynamic lists. Database updates are also incremental for the most part. (Dynamic lists are stored in the database just as their criteria, and their population is left to the client.)

GIT REPOSITORY:

Taskfront is being developed within Eclipse using the plugins for App Engine and Web Toolkit integration and publishing. The GIT repository you see here is just a clone of the Eclipse project directory on the author's Dropbox, and us such it includes a lot of stuff which is unnecessary for a third party trying to rebuild the project. However, in the past I have had success, when migrating between systems, by just copying the project directory and loading into Eclipse (with AppEngine and GWT plugins installed).

The Java source files can be found in src/org/bailey/taskfront, and within the 3 sub-folders "client", "server" and "shared". There are also some style sheets lurking around which integrate with Web Toolkit. 

