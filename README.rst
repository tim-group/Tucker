What is it?
===========

Tucker is a small library for gently and politely helping with the communication and management of application status.

Tucker contains two main parts. Firstly, a simple framework for building a 'status page', summarising a variety of information about the application, rendering it as an XML document, and perhaps serving it over HTTP. Secondly, a simple state machine describing the lifecycle of an application process, with ways to manipulate it, and ways to hook actions into transitions.

The second part does not yet exist.

How do i build it?
==================

With Gradle (http://www.gradle.org/). We are using milestone 9, and we suggest you use that. To build, simply do::

    gradle clean build

This builds a jar file in ``build/libs``. To use this in other projects, you might like to install it in your local Maven repository::

    gradle install

The build scripts are written to work in the authors' development environment. If you are not in the authors' development environment, you might need to tweak them slightly. In particular, the ``mavenRepo`` lines should be replaced with ``mavenCentral()``. The authors are working on making this better.

How do i use it?
================

You must create and configure an instance of ``com.timgroup.status.StatusPage``. You must then expose this somehow. If you are writing a web application, that probably means using ``com.timgroup.status.servlet.StatusPageServlet``.

For a concrete example of how to do this, see the ``Demo`` directory of the project. This contains a small, self-contained project which sets up and displays a status page. It obtains Tucker as a normal dependency, so you will need to install it into your local Maven repository, as detailed above. Then, run (from the root directory of the Tucker project):

    gradle -b Demo/build.gradle clean jettyRun

When it starts, it will print out a URL that you should look at.

What's the bit about the runnable jar in the Demo project?
==========================================================

Don't worry about that.

Why is it called that?
======================

Tucker's role is, ostensibly, to present a simple summary of the status of a system to the outside world, but behind the scenes, it also bosses the application around. It is named after the character Malcolm Tucker (http://en.wikipedia.org/wiki/The_Thick_of_It#Cast_and_characters).
