


import java.util.Scanner;
public class Driver
{
	public static void main(String[] args)
	{
		Server s = new Server();
		Scanner keyboard = new Scanner(System.in);
		
		boolean canBind = s.bind();
		
		if(!canBind)
		{
			System.exit(-1);
		}
		else
		{
			s.start();
		}
	}
}

