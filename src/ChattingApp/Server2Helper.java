package ChattingApp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedHashMap;

public class Server2Helper implements Runnable{
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	String sourceName;
	static Socket toServer1  = TCPServer2.serverAsClient;
	static DataOutputStream outToServer1;
	static LinkedHashMap<String, Socket> users = TCPServer2.users;

	public Server2Helper(Socket client, String incoming)
	{
		connectionSocket = client;
		sourceName = incoming;
		try {
            outToServer1 = new DataOutputStream(toServer1.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//client connection
			try {
				outToClient.writeBytes("Welcome "+ sourceName + " :)" + "\n");
				String s = "";
				while(!(s = inFromClient.readLine()).equals(null))
				{
					
					if(s.equalsIgnoreCase("get member list"))
						this.getMemberList();
					else
					{
						if(s.equalsIgnoreCase("quit"))
						{
							outToClient.writeBytes("You are now logged off." + '\n');
							users.remove(sourceName);
							outToServer1.writeBytes("Server:remove:" + sourceName + '\n');
							connectionSocket.close();
						}
						else
						{
							String[] in = s.split(":");
							String destination = in[0];
							String msg = in[1];
							
							//destination found on the same server
							if(users.containsKey(destination))
							{
								Socket destSocket = users.get(destination);
								DataOutputStream outToDestination = new DataOutputStream(destSocket.getOutputStream());
								outToDestination.writeBytes(sourceName.replaceAll("\\s","") + ":" + msg + '\n');
							}
							// destination not connected to server 2
							else
							{
								String out = "Server:chat:" + destination + ":" +msg + ":2:" + sourceName;
								outToServer1.writeBytes(out + '\n');
							}
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	
	
	
	public void getMemberList()
	{
		try {
			outToServer1.writeBytes("Server:get members:" + sourceName + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
