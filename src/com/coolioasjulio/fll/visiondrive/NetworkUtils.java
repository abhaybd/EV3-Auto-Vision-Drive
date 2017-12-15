package com.coolioasjulio.fll.visiondrive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class NetworkUtils {
	public static void main(String[] args) throws IOException{
		Subnet sub = calculateSubnet();
		System.out.println(sub.minIp.toString());
		InetAddress server = pingAll(sub, 4445);
		System.out.println(server.toString());
	}
	
	/**
	 * Calculate subnet of the first network interface. Return as Subnet object.
	 * @return Subnet object representing subnet of first network interface.
	 * @throws IOException
	 */
	public static Subnet calculateSubnet() throws IOException {
		return calculateSubnet(0);
	}
	
	/**
	 * Calculate subnet of the network index at the specified index.
	 * @param index Index of network interface in networkInterface.getInterfaceAddresses().get()
	 * @return Subnet object representing subnet of the specified network interface.
	 * @throws IOException
	 */
	public static Subnet calculateSubnet(int index) throws IOException {
		InetAddress localHost = Inet4Address.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
		InterfaceAddress addr = networkInterface.getInterfaceAddresses().get(0);
		InetAddress minIp = calculateMinIp(addr.getAddress(), addr.getNetworkPrefixLength());
		int[] range = calculateRange(addr.getNetworkPrefixLength());
		return new Subnet(minIp, range);		
	}
	
	/**
	 * Automatically calculate subnet and continuously send udp packets to every machine on the subnet. Stop as soon as one replies, since that's the server.
	 * @return InetAddress of server
	 * @throws IOException
	 */
	public static InetAddress pingAll(int serverPort) throws IOException {
		return pingAll(calculateSubnet(), serverPort);
	}
	
	/**
	 * Send udp packets to every machine on specified subnet. Stop as soon as a machine responds, as that is the server.
	 * @param subnet Subnet object to scan and send messages to
	 * @return InetAddress of server
	 * @throws IOException
	 */
	public static InetAddress pingAll(final Subnet subnet, final int serverPort) throws IOException{
		final DatagramSocket dataSocket = new DatagramSocket();
		Thread send = new Thread(new Runnable(){
			@Override
			public void run(){
				byte[] minIp = subnet.minIp.getAddress();
				int[] range = subnet.range;
				byte[] buf = new byte[256];
				while(!Thread.interrupted()){
					for(int a = minIp[0] & 0xFF; a <= (minIp[0] & 0xFF) + range[0]; a++){
						for(int b = minIp[1] & 0xFF; b <= (minIp[1] & 0xFF) + range[1]; b++){
							for(int c = minIp[2] & 0xFF; c <= (minIp[2] & 0xFF) + range[2]; c++){
								for(int d = minIp[3] & 0xFF; d <= (minIp[3] & 0xFF) + range[3]; d++){
									try {
										InetAddress addr = InetAddress.getByAddress(
												new byte[]{(byte)a,(byte)b,(byte)c,(byte) d});
										DatagramPacket packet = new DatagramPacket(buf,256,addr,serverPort);
										dataSocket.send(packet);
									} catch (UnknownHostException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									if(Thread.interrupted()){
										return;
									}
								}
							}
						}
					}
				}
			}
		});
		send.start();
		DatagramPacket p = new DatagramPacket(new byte[256], 256);
		dataSocket.receive(p);
		send.interrupt();
		dataSocket.close();
		return p.getAddress();
	}
	
	private static InetAddress calculateMinIp(InetAddress ip, int mask) throws UnknownHostException{
		byte[] addr = ip.getAddress();
		for(int i = 32; i > mask; i -= 8){
			addr[i/8 - 1] = 0;
		}
		return InetAddress.getByAddress(addr);
	}
	
	private static int[] calculateRange(int mask) {
		if(mask != 8 && mask != 16 && mask != 24){
			throw new RuntimeException(new Exception("Invalid mask " + mask + "!"));
		}
		int[] range = new int[4];
		for(int i = 32; i > mask; i -= 8){
			range[i/8 - 1] = 0xFF;
		}
		return range;
	}
	
	public static class Subnet{
		public InetAddress minIp;
		public int[] range;
		
		public Subnet(InetAddress minIp, int[] range){
			this.minIp = minIp;
			this.range = range;
		}
	}
}
