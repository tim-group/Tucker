Tucker is a small library for gently and politely helping with the communication and management of application status.

Tucker contains two main parts. Firstly, a simple framework for building a 'status page', summarising a variety of information about the application, rendering it as an XML document, and perhaps serving it over HTTP. Secondly, a simple state machine describing the lifecycle of an application process, with ways to manipulate it, and ways to hook actions into transitions.

The second part does not yet exist.

Tucker's role is, ostensibly, to present a simple summary of the status of a system to the outside world, but behind the scenes, it also bosses the application around. It is named after the character Malcolm Tucker (http://en.wikipedia.org/wiki/The_Thick_of_It#Cast_and_characters).
