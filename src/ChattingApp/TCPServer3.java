package ChattingApp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

public class TCPServer3 {
	ServerSocket welcomeSocket;
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream  outToClient;
	String sourceName;
	boolean accepted;
	static Socket serverAsClient;
	static BufferedReader inFromServer1;
	static DataOutputStream outToServer1;
	static LinkedHashMap<String, Socket> users = new LinkedHashMap<String,Socket>();

	public TCPServer3()
	{
		try {
			welcomeSocket = new ServerSocket(6003);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server 3 is open.");
		Thread t = new Thread()
		{
			public void run()
			{
				connectToServer1();
				String s;
				try {
					while((s= inFromServer1.readLine()) != null)
					{
						String[] in = s.split(":");
						String first = in[0];
						
						//joining: server 1 either responds with unique or taken
						if(first.equals("unique"))
						{
							accepted = true;
							sourceName = in[1];
							users.put(sourceName, connectionSocket);
							outToClient.writeBytes("Username " + sourceName + " accepted." + '\n');
						}
						else
						{
							if(first.equals("taken"))
							{
								String username = in[1];
								outToClient.writeBytes("Username "  + username + " already exists!" + '\n');
							}
							else
							{
								//getting the members: server 1 sends the source name and list of all members
								if(first.equals("members"))
								{
									String source = in[1];
									Socket sourceSock = users.get(source);
									DataOutputStream outToSource = new DataOutputStream(sourceSock.getOutputStream());
									
									int len = in.length;
									int i = 2;
									while(i < len)
									{
										outToSource.writeBytes(in[i] + '\n');
										i++;
									}
								}
								else
								{
									// looking for a client 
									if(first.equals("find"))
									{
										String destination = in[1];
										String msg = in[2];
										int TTL = Integer.parseInt(in[3]);
										String source = in[4];
										TTL--;
										
										// destination client at server 3
										if(users.containsKey(destination))
										{
											Socket destSock = users.get(destination);
											DataOutputStream outToDestination = new DataOutputStream(destSock.getOutputStream());
											outToDestination.writeBytes(source+ ":" + msg + '\n');
										}
									}
								}
							}
						}
					}
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();

		while(true)
		{
			try {
				
				//server2 receiving from clients
				connectionSocket = welcomeSocket.accept();
				System.out.println("accepted client");
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				accepted = false;
				while (!accepted)
				{
					if(!accepted)
					{
						if(inFromClient.ready())
						{
							String incomingSentence = inFromClient.readLine();
							this.join(incomingSentence);
						}
					}
					else
						break;
				}
				users.put(sourceName,connectionSocket);
				new Thread(new Server3Helper(connectionSocket,sourceName)).start();


			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void join(String name) throws IOException
	{
		if(!accepted)
			outToServer1.writeBytes("Server:username:" + name + '\n');
	}


	public void connectToServer1()
	{
		try {
			serverAsClient = new Socket("localhost", 6000);
			inFromServer1 = new BufferedReader(new InputStreamReader(serverAsClient.getInputStream()));
			outToServer1 = new DataOutputStream(serverAsClient.getOutputStream());
			outToServer1.writeBytes("Server 3" + '\n');
		} catch (IOException e) {
			connectToServer1();
		}
	}



	public static void main(String[] args) throws IOException {

		TCPServer3 s3 = new TCPServer3();	
	}
}
