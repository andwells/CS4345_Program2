import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.*;

public class Server implements Subject
{
	private List<Observer> observers;
	private LinkedBlockingDeque<String> messages;
	private int maxConnections = 20;
	private ServerSocket serverSocket;
	
	public Server()
	{
		observers = Collections.synchronizedList(new LinkedList<Observer>());
		messages = new LinkedBlockingDeque<String>();
	}

	public boolean bind()
	{
		boolean bindingSuccessful = true;
		try
		{
			serverSocket = new ServerSocket(10000);
		}
		catch(IOException ioEx)
		{
			System.out.println("Could not bind socket. Will exit...");
			bindingSuccessful = false;
		}
		return bindingSuccessful;
	}
	
	
	@Override
	public void registerObserver(Observer o)//Does this work? Or de we need an index?
	{
		synchronized(observers)
		{
			if(observers.contains(o))
			{
				return;//Client is already in list
			}
			notifyObservers(String.format("%s has joined the conversation.\n", o.getName()));
			observers.add(o);
			
		}
		
	}

	@Override
	public void removeObserver(Observer o)
	{
		synchronized(observers)
		{
			if(!observers.contains(o))//Does this work? Or de we need an index?
			{
				return;//Client isn't registered, so we can't do anything
			}
			else
			{
				observers.remove(o);
				
				notifyObservers(String.format("%s has disconnected.\n", o.getName()));
			}
		}
	}

	@Override
	public void notifyObservers(Object o)
	{
		synchronized(observers)
		{
			for(Observer obs : observers)
			{
				obs.update(o);
			}
		}
		
	}
	
	
	private class ConnectionHandler implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
