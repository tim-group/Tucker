What is it?
===========

.. image:: https://travis-ci.org/tim-group/Tucker.svg?branch=master
    :target: https://travis-ci.org/tim-group/Tucker


Tucker is a small library for gently and politely helping with the communication and management of application status.

Tucker contains two main parts. Firstly, a simple framework for building a 'status page', summarising a variety of information about the application, rendering it as an XML document, and perhaps serving it over HTTP. Secondly, a simple state machine describing the lifecycle of an application process, with ways to manipulate it, and ways to hook actions into transitions.

The second part does not yet exist.

How do i build it?
==================

With Gradle (http://www.gradle.org/). To build, simply do::

    ./gradlew build

This builds a jar file in ``build/libs``. To use this in other projects, you might like to install it in your local Maven repository::

    ./gradlew publishToMavenLocal

How do i know the artifacts are kosher?
=======================================

The artifacts are signed with GPG key 83C61133, fingerprint 03EE E3B3 8FC8 2905 71C7  A7AA 7BB1 B35B 83C6 1133, `owned by`_ Tom Anderson <tom.anderson@timgroup.com>::

    -----BEGIN PGP PUBLIC KEY BLOCK-----
    Version: GnuPG v1.4.11 (GNU/Linux)
    
    mQENBFIOSpYBCACVf65DevXFOCQSzuUoKvMxYvIQAbwXzl7Zmnq2qR8M4E6IpHK+
    fqgD0Dn0vTV9j2uYZZjCCYS3ZqB3n/6lmmUBgZtOYHyD51RUK9W3wzQxS9w+RQ38
    bD1S9zRBDWUdMKf87GyzUDXu2lSPIvacntwdJM8vsgyjxxBtTbwMM+dCG4+aD0x7
    0CQib1/3tyo8tWcrZy+BiPdEZQMTkCr7oYWnKB8Z8K3aPyNPCxu4xBqc8kn9aWHd
    c0j6WLfaYhgdhLY5qNaPWwxgt6F+3exMlcThAG10na4mREHjs/mArCl/Y+64sy1o
    xX4IIeEa96ZtKbNwwHj32RisCxDAc/GTHM45ABEBAAG0KFRvbSBBbmRlcnNvbiA8
    dG9tLmFuZGVyc29uQHRpbWdyb3VwLmNvbT6JATgEEwECACIFAlIOSpYCGwMGCwkI
    BwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEHuxs1uDxhEz+XoH/0Y1zB8Bc3aWmnv9
    Xg25mK4oVc/nZjmhHYuAhIKC+x7XmwAnxQcsOZsIPfoPv4BTngDkAQxjQHTlE3QG
    xVGGhAwPlVETrR2e7VqV0vs+14+4gGGEzvO1l+Y8mlBvb5HewvoZm+x6aTMQJJsq
    ZtGJp1sjWGPuFGqwj7MzTV+DJJRbWbz8JSDd3ETRbJvWCfwhtrHKbS9wZ8P64Zk/
    plfpszdizdIcFIwO6Py9KwZwDIL5r9ilF4eOtDwL7H9uEcsL9rnaOJZWdh/6EHC3
    +qMnpUOV1w+z2v4Bz1nxE8ygPYi/+B0zvEmc3w+BuH3iwzt09EqzCRMd3QQf1QRX
    2KEuXbm5AQ0EUg5KlgEIAL+0CP62xvYRzxSi4f/H3faf/h7VgVFIafxNqwiyEUxs
    vWgxwy8y7s5z8pTUWbGO40ZlGRqkRkine9OP3d2HZwYK8IHAenjB4TWDGeN68qih
    FH7OmjvXIYgUFMgqBfj77QW/swuIPkbPsObIZ3r7/l/+i+4r2bZJkn9gBik7+ecA
    VVsZYoc13+kvHi0Pf++6stS7XzRShFHejzJb1hT67R522lPoUNP5VDPKJ/P0EaeQ
    C/QQAo4UQYbngteXOIvTAyCOzMoZQaQogsytxRJOc98X6pLbS7MHUT3bIZG6Zgii
    EYQhwp3qkGN5XdpDarvVhgQuaG4HgjPNLFqs/Fcks1UAEQEAAYkBHwQYAQIACQUC
    Ug5KlgIbDAAKCRB7sbNbg8YRM3IMCACPA2X6IOzpiEJXErKpXdpJv/D4boZuKWno
    LSwQwSFx5A0HjN58WiimOWkH8lHqZEv89k7Z84Z26yceISnjTDW3mP6IHR6nTZp+
    bwIhfvVbnv7AW4Zm7wO8WILOjnYa57KnKarv4UDlqw2IgNFn0ckpHJ5r1DCW3TXU
    cLEB9y2hKZVRgMBpMHSIT4wbsREECwGRi0X6Vb6hpxDPU1hVjN7bRmNM/375bqST
    RuHUEWTcHaoAz1ydsmDM+OXxcZ7kqLC2eaJO/GqdW9yRlLouAbQnaFQ4GarabRlU
    m5aVshxAVJVttP5uiKDyKtQmP0d8doeyCh3MeyCSvrpzW5AwcDH+
    =VGX1
    -----END PGP PUBLIC KEY BLOCK-----

.. _owned by: http://pool.sks-keyservers.net:11371/pks/lookup?op=vindex&search=tom.anderson%40timgroup.com

How do i use it?
================

You must create and configure an instance of ``com.timgroup.status.StatusPage``. You must then expose this somehow. If you are writing a web application, that probably means using ``com.timgroup.status.servlet.StatusPageServlet``.

For a concrete example of how to do this, see the ``Demo`` directory of the project. This contains a small, self-contained project which sets up and displays a status page. It obtains Tucker as a normal dependency, so you will need to install it into your local Maven repository, as detailed above. Then, run (from the root directory of the Tucker project)::

    gradle -b Demo/build.gradle clean jettyRun

When it starts, it will print out a URL that you should look at.

Does it really need all those dependencies?
===========================================

No. Tucker can interact with a variety of other APIs, and has compile-time dependencies on them. They are only runtime dependencies if you are using that particular interaction - they are, in a word, optional. However, Gradle does not presently let us express that. Consult the ``build.gradle`` file for hints on what might be optional.

What's all this about a runnable jar in the Demo project?
=========================================================

Don't worry about that.

Why is it called that?
======================

Tucker's role is, ostensibly, to present a simple summary of the status of a system to the outside world, but behind the scenes, it also bosses the application around. It is named after the character Malcolm Tucker (http://en.wikipedia.org/wiki/The_Thick_of_It#Cast_and_characters).
