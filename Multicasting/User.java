//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 8000;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net

				//Read and store userName
				System.out.println("Welcome! Please enter your username:");
				output_stream.println("#join " + inputLine.readLine());
				

				while (!closed) {
					String userMessage = new String();
					String userInput = inputLine.readLine().trim();

					if(userInput.compareTo("Exit") == 0){
						output_stream.println("#Bye");
					}
					else if(userInput.length() > 5 && userInput.substring(0,5).compareTo("@deny") == 0){
						String otherUsername = userInput.substring(6);
						output_stream.println("#DenyFriendRequest " + otherUsername);
					}
					else if(userInput.length() > 7 && userInput.substring(0,7).compareTo("@friend") == 0){
						String otherUsername = userInput.substring(8);
						output_stream.println("#friends " + otherUsername);
					}
					else if(userInput.length() > 8 && userInput.substring(0,8).compareTo("@connect") == 0){
						String otherUsername = userInput.substring(9);
						output_stream.println("#friendme" + otherUsername);
					}
					else if(userInput.length() > 11 && userInput.substring(0,11).compareTo("@disconnect") == 0){
						String otherUsername = userInput.substring(12);
						output_stream.println("#unfriend" + otherUsername);
					}
					else{
						userMessage = "#newStatus" + userInput;
						output_stream.println(userMessage);
					}
					

					// Read user input and send protocol message to server

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		
		try {
			while ((responseLine = input_stream.readLine()) != null) {


				// Display on console based on what protocol message we get from server.
				
				if(responseLine.compareTo("#Bye") == 0){
							System.out.println("Closing now");
							closed = true;
							output_stream.close();
							input_stream.close();
							userSocket.close();
						}
				else if(responseLine.substring(0,6).compareTo("#Leave") == 0)
				{
					System.out.println("User " + responseLine.substring(7) + " has left");
				}
				else if(responseLine.substring(0,8).compareTo("#newuser") == 0)
				{
					System.out.println("A new user has joined!");
				}
				else if(responseLine.compareTo("#welcome") == 0)
				{
					System.out.println("Connection to server has been made");
				}
				else if(responseLine.substring(0,9).compareTo("#friendme") == 0){
					System.out.println(responseLine.substring(10) + " wants to be friends!");
				}
				else if(responseLine.substring(0,10).compareTo("#Okfriends") == 0){
					System.out.println(responseLine.substring(10) + " are now friends");
				}
				else if(responseLine.substring(0,10).compareTo("#newStatus") == 0)
				{
					System.out.println(responseLine.substring(11));
				}
				else if(responseLine.substring(0,11).compareTo("#Notfriends") == 0)
				{
					System.out.println(responseLine.substring(12) + " are no longer friends");
				}
				else if(responseLine.compareTo("#statusPosted") == 0)
				{
					System.out.println("Your status has posted!");
				}
				else if(responseLine.substring(0,20).compareTo("#FriendRequestDenied") == 0){
					System.out.println(responseLine.substring(20) + " rejected your friend request");
				}
				
				

			}
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}



