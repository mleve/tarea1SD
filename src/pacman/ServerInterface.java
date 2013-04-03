package pacman;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote{
	public int registerPlayer() throws RemoteException;
	public void registerReady(int playerId) throws RemoteException;
	public boolean started(int playerId) throws RemoteException;
	public void registerPosition(int playerId, int x, int y) throws RemoteException;
	public int[][] getInfo() throws RemoteException;
	public int[] getGhostsX() throws RemoteException;
	public int[] getGhostsY() throws RemoteException;

}
