//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;



/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket userSocket = null;
	// Maximum number of users 
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 8000;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);
		
		
		userThread[] threads = new userThread[maxUsersCount];


		/*
		 * Open a server socket on the portNumber (default 8000). 
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;
	private List<String> accepted = new ArrayList<String>();	//Stores accepted requests for each userThread(aka friends)
	
	public userThread(Socket userSocket, userThread[] threads) throws IOException {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
		this.input_stream = new BufferedReader(new InputStreamReader(this.userSocket.getInputStream()));
		this.output_stream = new PrintStream(this.userSocket.getOutputStream());
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;
	

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			
			String userInput = this.input_stream.readLine();
			String userName = null;
			if(userInput.substring(0,5).compareTo("#join") == 0){	//When new user wants to join
				this.userName = userInput.substring(6);
				this.output_stream.println("#welcome");
				synchronized (userThread.class) {
					for(int i = 0; i<threads.length; i++){
						if(threads[i] != null && threads[i].userSocket != this.userSocket)
							threads[i].output_stream.println("#newuser " + userName);
					}
					}
				}

			


			/* Welcome the new user. */


			/* Start the conversation. */
			while (true) {
				userInput = this.input_stream.readLine();
				if(userInput.substring(0,4).compareTo("#Bye") == 0){	//When user wants to exit
					synchronized (userThread.class) {
						for(int i = 0; i < threads.length; i++){
							if(threads[i].userSocket == this.userSocket)
							{
								this.output_stream.println("#Bye");
								threads[i] = null;
								this.userSocket.close();	//close userSocket if user wants to leave
							}
							else
								threads[i].output_stream.println("#Leave " + this.userName);
						}

					}
				}

				/*
					Handles #friendme request from user. Passes request to thread that user wants to friend
				*/
				else if(userInput.substring(0,9).compareTo("#friendme") == 0){
					String otherUsername = userInput.substring(9);
					
					synchronized(userThread.class){
						for(int i = 0; i < threads.length; i++){
							if(threads[i] != null && otherUsername.compareTo(threads[i].userName) == 0)
							{
								//loops through each thread to see if username matches
								threads[i].output_stream.println("#friendme " + this.userName);
								
							}
						}
					}
				}

				/*
					Accepting a friend request
				*/

				else if(userInput.substring(0,8).compareTo("#friends") == 0){
					String otherUsername = userInput.substring(9);
					this.accepted.add(otherUsername);
					this.output_stream.println("#Okfriends " + this.userName + " and " + otherUsername);
					synchronized(userThread.class){
						for(int i = 0; i < threads.length; i++){
							if(threads[i] != null && otherUsername.compareTo(threads[i].userName) == 0){
								threads[i].output_stream.println("#Okfriends " + this.userName + " and " + otherUsername);
								threads[i].accepted.add(this.userName);	//Adds username to list of friends
							}
						}
					}
				}
				/*
					Unfriending a friend
				*/

				else if(userInput.substring(0,9).compareTo("#unfriend") == 0){
					String otherUsername = userInput.substring(9);
					this.accepted.remove(otherUsername);
					
					this.output_stream.println("#Notfriends " + this.userName + " and " + otherUsername);
					synchronized(userThread.class){
						for(int i = 0; i < threads.length; i++){
							if(threads[i] != null && otherUsername.compareTo(threads[i].userName) == 0){
								threads[i].output_stream.println("#Notfriends " + this.userName + " " + otherUsername);
								
								threads[i].accepted.remove(this.userName);	//removes username from friend list
							}

						}
					}
				}

				/*
					Posting a status that is only visible to friend list
				*/

				else if(userInput.substring(0,10).compareTo("#newStatus") == 0){
					String status = userInput.substring(10);
					System.out.println(userInput);
					this.output_stream.println("#statusPosted");
					synchronized (userThread.class) {
						for(int i = 0; i<threads.length; i++){
								if(threads[i] != null && this.accepted.contains(threads[i].userName)) //checks to send status only to threads in accepted (friend list)
									threads[i].output_stream.println("#newStatus " + this.userName + ": " + status);
						}
					}
				}

				/*
					Denying a friend request
				*/

				else if(userInput.substring(0,18).compareTo("#DenyFriendRequest") == 0){
					String otherUsername = userInput.substring(19);
					
					synchronized(userThread.class){
						for(int i = 0; i < threads.length; i++){
							if(threads[i] != null && otherUsername.compareTo(threads[i].userName) == 0){
								threads[i].output_stream.println("#FriendRequestDenied" + this.userName);
								
							}
						}
					}
				}

				
				
				
			}

			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 
			input_stream.close();
			output_stream.close();
			userSocket.close();
			*/
		}
			catch (IOException e) {
				System.out.println(e);
			}

		}
	}



