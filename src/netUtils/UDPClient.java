package netUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient
{
//	private static final String SERVER_ADDRESS = "182.92.166.53";
	private static final String SERVER_ADDRESS = "online.rushucloud.com";
	private static final int SERVER_PORT = 4888;

	private DatagramSocket socket = null;
	private Thread thread;
	private String message;
	
	public UDPClient()
	{
		super();
		message = HttpUtils.getJWTString();
	}
	
	public void send()
	{
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					socket = new DatagramSocket(SERVER_PORT);
					InetAddress address = InetAddress.getByName(SERVER_ADDRESS);
					socket.connect(address, SERVER_PORT);

					byte[] data = message.getBytes();
					DatagramPacket packet = new DatagramPacket(data, data.length, address, SERVER_PORT);
					socket.send(packet);
				}
				catch (SocketException e)
				{
					e.printStackTrace();
				}
				catch (UnknownHostException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}				
			}
		});
		thread.start();
	}
	
	public void receive(final UDPConnectionCallback callback)
	{		
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					while (true)
					{
//						socket = new DatagramSocket(SERVER_PORT);
						byte[] receivedData = new byte[4096];
						DatagramPacket receivedPacket = new DatagramPacket(receivedData, 0, receivedData.length);
						socket.receive(receivedPacket);
					    String response = new String(receivedPacket.getData());
					    System.out.println("FROM SERVER:" + response);
					    socket.close();
					    
						if (callback != null)
						{
							callback.execute(response);
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}			
			}
		});
		thread.start();
	}
	
	public void close()
	{
		if (socket != null && !socket.isClosed())
		{
			socket.close();
		}
	}

	public boolean isConnected()
	{
		return socket != null && socket.isConnected();
	}
}
