package pacman;

import javax.swing.JFrame;

import pacman.Board;
import pacman.ServerStub;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Enumeration;


public class PacMan extends JFrame{

	private static ServerInterface stub;
	
	public PacMan(String server){
		add(new Board(server));
		setTitle("Pacman");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(380, 420);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args){
		if(args.length<1){
			System.out.println("Error, debe especificar ip servidor o localhost nPlayers para ser host");
			System.exit(-1);
		}
		else{
			String serverIp = args[0];
			if(serverIp.equals("localhost")){
				//Jugador es host de la partida
				if(args.length<2){
					System.out.println("Host debe indicar players: Pacman localhost n");
					System.exit(-1);
				}
					
				serverIp = getLocalIp();
				try{
					stub = new ServerStub(Integer.parseInt(args[1]));
					Naming.rebind("rmi://"+serverIp+":1099/ServerStub", stub);
				}
				catch (RemoteException e){
					System.out.println("Hubo una excepcion creando la instancia del objeto distribuido");
					e.printStackTrace();
					System.exit(-1);
				} catch (MalformedURLException e){
					System.out.println("URL mal formada al tratar de publicar el objeto");
					System.exit(-1);
				}
				new PacMan(serverIp);
			}
			else //Conexion normal a un servidor
				new PacMan(serverIp);
		}
			
	}
	
	private static String getLocalIp(){
		/*Metodo trucho para determinar la Ip de este computador, fue el primero que encontre que me funciono bien xD
		 * */
		String ip=null;
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
			    NetworkInterface current = interfaces.nextElement();
			    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			    Enumeration<InetAddress> addresses = current.getInetAddresses();
			    while (addresses.hasMoreElements()){
			        InetAddress current_addr = addresses.nextElement();
			        if (current_addr.isLoopbackAddress()) continue;
			        if (current_addr instanceof Inet4Address) 
			        	ip = current_addr.getHostAddress();
			    }
			}
		}
		catch(Exception e){
			System.out.println("Error tratando de determinar la ip");
			ip=null;
		}
		return ip;
		
	}
}
