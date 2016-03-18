import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Server implements Subject
{
	private Map<String, Observer> observers;
	private LinkedBlockingDeque<String> messages;
	private final int MAX_CONNECTIONS = 20;
	private ServerSocket serverSocket;
	private boolean acceptConnections = false;
	private ExecutorService pool;
	
	public Server()
	{
		observers = Collections.synchronizedMap(new HashMap<String, Observer>());
		messages = new LinkedBlockingDeque<String>();//Do we need this guy?
		pool = Executors.newCachedThreadPool();
	}

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
	
	@Override
	public void registerObserver(Observer o)//Does this work? Or do we need an index?
	{
		synchronized(observers)
		{
			if(observers.containsKey(o.getName()))
			{
				//Update format to include message tags (IE: error, PM, regular message)
				o.update("Name is already in use. Choose a different username");
				return;//Client is already in list
			}
			observers.put(o.getName(), o);
			
			notifyObservers(String.format("%s has joined the conversation.\n", o.getName()));
		}
		
	}

	@Override
	public void removeObserver(Observer o)
	{
		synchronized(observers)
		{
			
			if(!observers.containsKey(o.getName()))//Does this work? Or do we need an index?
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
			for(Observer obs : observers.values())
			{
				obs.update(o);
			}
		}
		
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
						registerObserver(ob);
						
						
						pool.execute(new ClientConnection(ob));
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
				String output =  oc.read();
				
				if(output.matches("^/(.+)?:(.+)"))//matches PM format
					//We need a format to specify which 
				{
					String[] parts = output.split(":", 2);
					String name = parts[0].substring(1);
					String message = parts[1];
					
					synchronized(observers)
					{
						if(observers.containsKey(name))
						{
							String pm = String.format("%s(Private): %s\\n", oc.getName(), message);
							observers.get(name).update(pm);
							oc.update(pm);
						}
						else
						{
							oc.update(String.format("(ERROR)Could not find user with name %s\\n", name));
						}
					}
				}
				else
				{
					notifyObservers(String.format("%s: %s\\n", oc.getName(), output));
				}
				
			}
			catch(IOException ioEx)
			{
				synchronized(observers)
				{
					removeObserver(oc);
					
					notifyObservers(String.format("%s has diconnected.\\n", oc.getName()));
				}
			}
		}
		
	}
}
