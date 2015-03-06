package netUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient
{
	private DatagramSocket socket = null;
	private Thread thread;
	private String message;
	
	public UDPClient()
	{
		super();
		message = HttpUtils.getJWTString();
	}
	
	public void send(final UDPConnectionCallback callback)
	{
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					socket = new DatagramSocket(URLDef.UDP_SERVER_PORT);
					InetAddress address = InetAddress.getByName(URLDef.UDP_SERVER_ADDRESS);
					socket.connect(address, URLDef.UDP_SERVER_PORT);

					byte[] data = message.getBytes();
					DatagramPacket packet = new DatagramPacket(data, data.length, address, URLDef.UDP_SERVER_PORT);
					socket.send(packet);

					while (true)
					{
						byte[] buffer = new byte[4096];
						DatagramPacket receivedPacket = new DatagramPacket(buffer, 0, buffer.length);
						socket.receive(receivedPacket);
					    String response = new String(buffer, 0, receivedPacket.getLength());
					    socket.close();
					    
						if (callback != null)
						{
							callback.execute(response);
						}
					}
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
	
	public void close()
	{
		if (socket != null && !socket.isClosed())
		{
			socket.close();
		}
	}
}
