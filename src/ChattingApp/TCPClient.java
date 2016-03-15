package ChattingApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class TCPClient{
	BufferedReader inFromServer;
	BufferedReader inFromUser;
	DataOutputStream outToServer;
	Socket clientSocket;
	

	public TCPClient()throws UnknownHostException,IOException
	{
		
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Choose to connect to server 1 or 2 or 3 or 4:");
		int serverNo = Integer.parseInt(inFromUser.readLine());
			if(serverNo == 1){
				clientSocket = new Socket("localhost", 6000);
				System.out.println("You are now connected to server 1.");
			}
			else
			{
				if(serverNo == 2)
				{
					clientSocket = new Socket("localhost", 6002);
					System.out.println("You are now connected to server 2.");
				}
				else
				{
					if(serverNo == 3)
					{
						clientSocket = new Socket("localhost", 6003);
						System.out.println("You are now connected to server 3.");
					}
					else
					{
						clientSocket = new Socket("localhost", 6004);
						System.out.println("You are now connected to server 4.");
					}
				}
			}	
		outToServer = new DataOutputStream(clientSocket.getOutputStream());	
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		//choosing a username
		System.out.println("Pick a username:");
		String incomingSentence = "";
		String name = "";
		name = inFromUser.readLine();
		outToServer.writeBytes(name + '\n');
		if((incomingSentence = inFromServer.readLine()) != null)
		{
			if(name.equalsIgnoreCase("get member list"))
			{
				if(incomingSentence.equals("There are no members currently."))
				{
					System.out.println(incomingSentence);
					name = inFromUser.readLine();
					outToServer.writeBytes(name + '\n');
				}
				else
				{
					while((incomingSentence = inFromServer.readLine()) != null)
						System.out.println(incomingSentence);
					name = inFromUser.readLine();
					outToServer.writeBytes(name + '\n');
				}
			}
			// sent a username
			else
			{
				System.out.println(incomingSentence);
				while(incomingSentence.equals("Username already exists!"))
				{
					name = inFromUser.readLine();
					outToServer.writeBytes(name + '\n');
				}	
			}		
		}

		//starting to chat
		Thread t = new Thread()
		{
			//receiving
			public void run()
			{
				String line="";
				try {
					while((line = inFromServer.readLine()) != null){
     					System.out.println(line);
					}
				} catch (IOException e) {
					System.err.println("Problem reading input from server!");
				}
			}
		};
		t.start();

		//sending
		String sentence = "";
		while(true){
			sentence = inFromUser.readLine();
			//sentence = fromUser;
			outToServer.writeBytes(sentence +'\n');
		}	
	}


	public static void main(String[] args) throws UnknownHostException, IOException{
		TCPClient c =  new TCPClient();
	}

}
