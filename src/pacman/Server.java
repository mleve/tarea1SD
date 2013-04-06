package pacman;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

public class Server{
	
	private static ServerInterface stub;

	public static void main(String[] args){
		String dirServer="localhost";
		if(args.length>=2){
			//Argumentos -Dalgodermi dirIp
			dirServer=args[1];
		}
		//Si no, la conexion es en localhost
		try{
			if(args.length != 0)
				stub = new ServerStub(Integer.parseInt(args[0]));
			else
				stub = new ServerStub(2);

			Naming.rebind("rmi://"+dirServer+":1099/ServerStub", stub);
		} catch (RemoteException e){
			System.out.println("Hubo una excepcion creando la instancia del objeto distribuido");
			e.printStackTrace();
		} catch (MalformedURLException e){
			System.out.println("URL mal formada al tratar de publicar el objeto");
		}
	}

}
