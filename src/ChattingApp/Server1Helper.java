package ChattingApp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class Server1Helper implements Runnable{
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	String incomingSentence;
	String sourceName = "";
	static HashMap<String, Socket> users =  ChattingApp.TCPServer1.users;
	static HashMap<String,Socket> globalUsers = ChattingApp.TCPServer1.globalUsers;

	public Server1Helper(Socket client, String incoming)
	{
		connectionSocket = client;
		incomingSentence = incoming;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{	
		if(!incomingSentence.contains("Server"))
		{
			try {
				this.join(incomingSentence);
				outToClient.writeBytes("Welcome "+ sourceName + " :)"+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {	
			
			String s;
			
			while((s = inFromClient.readLine()) != null)
			{
				String[] in = s.split(":");
				// server connections
				if(in[0].equals("Server"))
				{
					
					   //client at server  asks for members
						if(in[1].equals("get members"))
						{
							String source = in[2];
							if(globalUsers.isEmpty())
								outToClient.writeBytes("members:" + source+ ":There are no members currently." + '\n');
							else
							{
								String result = "members:" + source;
								for(String name: globalUsers.keySet())
								{
									result += ":" + name;
								}
								outToClient.writeBytes(result + '\n');
							}
						}
						else
						{
							// server 2 asks to check uniqueness of username
							if(in[1].equals("username"))
							{
							   String un = in[2];
							   if(globalUsers.containsKey(un)){
								   outToClient.writeBytes("taken: " + un + '\n');
							   }
							   else
							   {
								   outToClient.writeBytes("unique: " + un + '\n');
								   globalUsers.put(un, null);
							   }
							 }
							else
							{
								// server 2 looking for client
								if(in[1].equals("chat"))
								{
									String destination = in[2];
									String msg = in[3];
									int TTL = Integer.parseInt(in[4]);
									String source = in[5];
									TTL--;
									
									// destination client found at server 1
									if(users.containsKey(destination))
									{
										Socket destSock = users.get(destination);
										DataOutputStream outToDestination = new DataOutputStream(destSock.getOutputStream());
										outToDestination.writeBytes(source+ ":" + msg + '\n');
									}
									        
									// destination not at server 1: broadcast to all other servers
									else
									{
										Socket server2 = globalUsers.get("Server 2");
										DataOutputStream outToServer2 = new DataOutputStream(server2.getOutputStream());
									    outToServer2.writeBytes("find: " + destination + ":" + msg + ":" + TTL + ":" + source + '\n');
									    
									    Socket server3 = globalUsers.get("Server 3");
										DataOutputStream outToServer3 = new DataOutputStream(server3.getOutputStream());
									    outToServer3.writeBytes("find: " + destination + ":" + msg + ":" + TTL + ":" + source + '\n');
									    
									    Socket server4 = globalUsers.get("Server 4");
										DataOutputStream outToServer4 = new DataOutputStream(server4.getOutputStream());
									    outToServer4.writeBytes("find: " + destination + ":" + msg + ":" + TTL + ":" + source + '\n');
									}
								}
								else
								{
									// client at server logged off
									if(in[1].equals("remove"))
									{
										globalUsers.remove(in[2].replaceAll("\\s",""));
										for(String name: globalUsers.keySet())
										{
											System.out.println(name);
										}
									}
								}
														
							}
						}
					}
				//client connections
				else
				{
					String destination = in[0];
					if(destination.equals("get member list"))
					{
						this.getMemberList();
					}
					else
					{
						if(destination.equals("quit"))
						{
							users.remove(sourceName);
							globalUsers.remove(sourceName);
							outToClient.writeBytes("You are now logged off." + '\n');
							connectionSocket.close();
						}
						else
						{
							String msg = in[1];	
							// destination client also connected on server 1
							if(users.containsKey(destination))	
							{
								Socket destSocket = users.get(destination);
								DataOutputStream outToDestination = new DataOutputStream(destSocket.getOutputStream());
								outToDestination.writeBytes(sourceName.replaceAll("\\s","") + ":" + msg + '\n');				
							}
							// destination client not connected on server 1 so broadcast to all other servers
							else
							{
								Socket server2 = globalUsers.get("Server 2");
								DataOutputStream outToServer2 = new DataOutputStream(server2.getOutputStream());
								outToServer2.writeBytes("find: " + destination + ":" + msg + ":2:" + sourceName + '\n');
								
								Socket server3 = globalUsers.get("Server 3");
							    DataOutputStream outToServer3 = new DataOutputStream(server3.getOutputStream());
								outToServer3.writeBytes("find: " + destination + ":" + msg + ":2:" + ":" + sourceName + '\n');
								    
								Socket server4 = globalUsers.get("Server 4");
							    DataOutputStream outToServer4 = new DataOutputStream(server4.getOutputStream());
								outToServer4.writeBytes("find: " + destination + ":" + msg + ":2:" + ":" + sourceName + '\n');
							}
						}
						
						
					}
			    }
			}
					
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getMemberList()
	{
		if(globalUsers.isEmpty())
			try {
				outToClient.writeBytes("There are no members currently." + '\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
		{
			for(String name: globalUsers.keySet())
			{
				try {
					outToClient.writeBytes(name + '\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void join(String name) throws IOException
	{
		//user asks for member list before inputting name
		if(name.equalsIgnoreCase("get member list"))
		{
			this.getMemberList();
			name = inFromClient.readLine();
		}
		while(globalUsers.containsKey(name)){
			outToClient.writeBytes("Username "  + name + " already exists!" + '\n');
			name = inFromClient.readLine();
		}
		
		//user asks for member list after inputting wrong name
		if(name.equalsIgnoreCase("get member list"))
		{
			this.getMemberList();
			name = inFromClient.readLine();
		}						
		sourceName = name;
		users.put(name,connectionSocket);
		globalUsers.put(name, connectionSocket);
		outToClient.writeBytes("Username " + name + " accepted." + '\n');		
	}

}
