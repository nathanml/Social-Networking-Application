# Social-Networking-Application
A multithreaded social media application built in Java using socket programming that allows users to become friends 
with one another, post statuses, and message their friends. There were 3 parts to this project within 2 subdirectories:
1. Basic implementation: A client and server that communicate over TCP. The server logs messages from the clients.
2. Broadcast implementation: Communication between client and server over TCP where server broadcasts messages to 
 all other users connected to Server
3. Adding friendship/multicasting: Allow users to connect with other users as friends and share messages exclusively 
 with friends

TestThread.java implements a test use of the Server which is logged in example.pdf.

## Compilation Instructions

To run the project, navigate to the directory of the implementation you would like to use and run the following
commands:

```
javac Server.java
java Server {port}
javac User.java
java User {hostname} {port}
```

To add subsequent users (for non-basic implementation), run
```
java User {hostname} {port}
```

## What I learned
1. Socket programming
2. Multithreading
3. Multicasting
4. TCP using Java