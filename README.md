# NetworkChat
This is a java application that allows multiple clients to enter a private and secure chat room 
hosted by the server application. The client application starts off in a login page, which allows you to
connect to the server application. You will need to know the server's IPaddres and port number tht the application
is listening to. You will also need to enter a unique name. Before entering the chat room, the client application 
sets up a secure connection between the client application and the server application.

# Getting started
You can clone this project directory and import it into your eclipse workspace. There are two packages 
in this project. The client package, which runs the client application, and the server package, which runs
the server application. The server application takes one int parameter, which is the port number that the server
will be listening on. The server Application must be running in order for you to enter the chat room!

# Running the application
You can download the executable .jar files in the "exec" folder and run the application.
The server application must be running in order for the client application to connect to the chat room. 
The server application can only be run in the terminal or console. You need to enter a port number that the server application
will be listening on as a parameter for the server application.Once ther server is running, you can double click on the NetworkChat.jar executable file and you should be good to go. 

You don't have to wait to start the server application in order to start the client application, but you won't be able to enter the private secure chatroom until you have the server application up and running.

# Example
java -jar NetworkChatServer.jar [Port]

Example: 
java -jar NetworkChatServer.jar 3000

If you copy and paste this example into the terminal, the NetworkChat server will be listening on port 3000.
Make sure you are in the same directory as the server application in the terminal when you run this command.
