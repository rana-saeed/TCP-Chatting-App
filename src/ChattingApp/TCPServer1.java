package ChattingApp;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class TCPServer1 {
	ServerSocket welcomeSocket;
	BufferedReader inFromClient;
	static HashMap<String, Socket> users = new HashMap<String,Socket>();
	static HashMap<String, Socket> globalUsers = new HashMap<String,Socket>();	
	public TCPServer1()
	{
		try {
			welcomeSocket = new ServerSocket(6000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server 1 is open.");
		
		while(true)
		{
				Socket connectionSocket;
				try {
					connectionSocket = welcomeSocket.accept();
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					String incomingSentence = inFromClient.readLine();
					
					//connecting server 2 to server 1
					if(incomingSentence.equals("Server 2"))
					{
						users.put(incomingSentence,connectionSocket);
						globalUsers.put(incomingSentence,connectionSocket);
						System.out.println("Server 2 connected to server 1.");
						new Thread(new Server1Helper(connectionSocket,incomingSentence)).start();
					}
					else
					{
						//connecting server 3 to server 1
						if(incomingSentence.equals("Server 3"))
						{
							users.put(incomingSentence,connectionSocket);
							globalUsers.put(incomingSentence,connectionSocket);
							System.out.println("Server 3 connected to server 1.");
							new Thread(new Server1Helper(connectionSocket,incomingSentence)).start();
						}
						else
						{
							//connecting server 4 to server 1
							if(incomingSentence.equals("Server 4"))
							{
								users.put(incomingSentence,connectionSocket);
								globalUsers.put(incomingSentence,connectionSocket);
								System.out.println("Server 4 connected to server 1.");
								new Thread(new Server1Helper(connectionSocket,incomingSentence)).start();
							}
							//connecting to clients
							else
							{
								System.out.println("accepted client");
								new Thread(new Server1Helper(connectionSocket,incomingSentence)).start();
							}	
						}
					}
				}
				 catch (IOException e) {
					e.printStackTrace();
				}
		}				
	}	
	public static void main(String[] args) throws IOException {
		
		TCPServer1 s = new TCPServer1();	
	}
}
