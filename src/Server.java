/**
 * Author: Max Carter, Robert Walker, Andrew Wells
 * Course Number: CS4345
 * Semester: Spring 2016
 * Assignment: Program2
 * File: Server.java
 * Date: 3/23/2016
 * 
 * Details: The server for relaying chat messages
 * */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Server implements Subject
{
	private Map<String, Observer> observers;			//Maps usernames to connections
	private final int MAX_CONNECTIONS = 20;				//Only allow 20 users at a time
	private ServerSocket serverSocket;					//For accepting
	private boolean acceptConnections = false;			
	private ExecutorService pool;						//For recycling threads
	private StringBuilder sList;						//For updating clients of connected users
	
	public Server()
	{
		observers = Collections.synchronizedMap(new HashMap<String, Observer>());
		pool = Executors.newCachedThreadPool();
	}

	
	//Performs connection logic
	public boolean bind()
	{
		boolean bindingSuccessful = true;
		try
		{
			serverSocket = new ServerSocket(50000);
			acceptConnections = true;
			System.out.println("Server socket bound to port " + serverSocket.getLocalPort());
		}
		catch(IOException ioEx)
		{
			System.out.println("Could not bind socket. Will exit...");
			bindingSuccessful = false;
		}
		return bindingSuccessful;
	}
	
	public void start()
	{
		pool.execute(new ConnectionHandler());
	}
	
	public void shutdown()
	{
		this.acceptConnections = false;
	}
	
	
	//Add user to list
	@Override
	public boolean registerObserver(Observer o)
	{
		synchronized(observers)
		{
			if(observers.containsKey(o.getName()))
			{
				//Update format to include message tags (IE: error, PM, regular message)
				o.update("Name is already in use. Choose a different username");
				return false;//Client is already in list
			}
			observers.put(o.getName(), o);
			
			notifyObservers(String.format("%s has joined the conversation.", o.getName()));
			
			//Get list of registered users to send to client 
			String userList = buildUserList(observers.keySet());
			
			System.out.println("Sending user list after connection.");
			
			//Publish registered users to client
			notifyObservers(userList);
			return true;
		}
	}

	//Remove a connected user 
	@Override
	public void removeObserver(Observer o)
	{
		synchronized(observers)
		{
			
			if(!observers.containsKey(o.getName()))
			{
				return;//Client isn't registered, so we can't do anything
			}
			else
			{
				observers.remove(o.getName());
				System.out.printf("User %s has disconnected from server.\n", o.getName());
				
				notifyObservers(String.format("%s has disconnected.", o.getName()));
				
				String userList = buildUserList(observers.keySet());
				System.out.println("Sending user list after disconnect.");
				
				//Publish registered users to client
				notifyObservers(userList);
			}
		}
	}

	
	//Update all registered users
	@Override
	public void notifyObservers(Object o)
	{
		synchronized(observers)
		{
			for(Observer obs : observers.values())
			{
				obs.update(o);
			}
		}
	}
	
	
	/**
	 *buildUserList
	 *@param users a set containing all usernames
	 *@param calling the name of the user that connected/disconnected
	 *@return a string containing all registered users in the format "/list:<username>;<username>.../list"
	 **/
	private String buildUserList(Set<String> users)
	{
		//Lazy initialization
		sList = new StringBuilder();
		sList.append("/list:");//Append 
		
		//For iterating through set
		Iterator<String> it = users.iterator();
		
		while(it.hasNext())
		{
			String username = it.next();//Consumes the next item

			sList.append(username);
			if(it.hasNext())//If there are more items, add a semicolon to separate between usernames
			{
				sList.append(";");
			}
			
		}
		sList.append("/list");
		
		
		return sList.toString();
	}
	
	
	private class ConnectionHandler implements Runnable
	{
		@Override
		public void run()
		{
			System.out.println("Starting connection listener");
			while(acceptConnections)
			{
				try
				{
					if(observers.size() < MAX_CONNECTIONS)
					{
						
						Socket connection = serverSocket.accept();
						System.out.printf("Client connected from %s\n", connection.getInetAddress().toString());
						
						Observer ob = new ObserverClient(connection);
						boolean registrationSuccess =  registerObserver(ob);
						
						if(registrationSuccess)
						{
							pool.execute(new ClientConnection(ob));
						}
						
					}
				}
				catch (IOException e)
				{
					//Do something better; May need to encapsulate loop in try-catch
					e.printStackTrace();
				}
			}
			System.out.println("Shutting down connection thread.");
		}
	}
	
	private class ClientConnection implements Runnable
	{
		ObserverClient oc;
		
		public ClientConnection(Observer o)
		{
			oc = (ObserverClient)o;
		}

		@Override
		public void run() 
		{
			try
			{
				while(true)
				{
					String output =  oc.read();
					
					if(output.matches("^(\\w)*/(.+)?(\\w)*:(.+)$"))//matches PM format
					{
						String[] parts = output.split(":", 2);
						String name = parts[0].substring(1).trim();
						String message = parts[1];
						
						synchronized(observers)
						{
							if(observers.containsKey(name))
							{
								//Display PM with this format: sender->receiver:message
								String pm = String.format("%s->%s: %s", oc.getName(), name, message);
								observers.get(name).update(pm);
								oc.update(pm);
							}
							else
							{
								oc.update(String.format("(ERROR)Could not find user with name %s.", name));
							}
						}
					}
					else
					{
						notifyObservers(String.format("%s: %s", oc.getName(), output));
					}
				}
				
			}
			catch(IOException ioEx)//If connection handler has IO error, assume they disconnected 
			{
				synchronized(observers)
				{
					removeObserver(oc);
				}
			}
		}
	}
}
