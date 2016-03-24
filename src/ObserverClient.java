/** 
 * Author: Max Carter, Robert Walker, Andrew Wells
 * Course Number: CS4345
 * Semester: Spring 2016
 * Assignment: Program2
 * File: ObserverClient.java
 * Date: 3/23/2016
 * 
 * Details: Encapsulates on the server-side a connection between the client and server  
 * */


import java.io.*;
import java.net.*;

public class ObserverClient implements Observer 
{
	private Socket clientSocket;
	private DataOutputStream toClient;
	private DataInputStream fromClient;
	private String name;
	
	public ObserverClient(Socket s)
	{
		clientSocket = s;
		try
		{
			//Gets IOStreams
			toClient = new DataOutputStream(s.getOutputStream());
			fromClient = new DataInputStream(s.getInputStream());
			
			//Username should be first thing that is sent to server
			name = fromClient.readUTF().trim();
			
			
			System.out.printf("Received user name %s.\n", name);
		}
		catch(IOException ioEx)
		{
			System.out.println("(ObserverClient) Error establishing connection: " + ioEx.getMessage());
		}
	}

	@Override
	public void update(Object o)
	{
		try
		{
			toClient.writeUTF((String) o);
		}
		catch(IOException ioEx)
		{
			System.out.printf("IO Error from ObserverClient %s: %s\n", name, ioEx.getMessage());
		}
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	
	public String read() throws IOException
	{
		return fromClient.readUTF();
	}
}
