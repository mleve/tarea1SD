package pacman;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

public class Server{
	
	private static ServerInterface stub;

	public static void main(String[] args){
		try{
			stub = new ServerStub();

			Naming.rebind("rmi://localhost:1099/ServerStub", stub);
		} catch (RemoteException e){
			System.out.println("Hubo una excepcion creando la instancia del objeto distribuido");
		} catch (MalformedURLException e){
			System.out.println("URL mal formada al tratar de publicar el objeto");
		}
	}

}