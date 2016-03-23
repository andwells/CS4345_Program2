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
		catch(IOException ioEX)
		{
			//do something
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
