	package pacman;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote{
	public int registerPlayer() throws RemoteException;
	public void registerReady(int playerId) throws RemoteException;
	public void registerDeath(int playerId) throws RemoteException;
	public void registerQuit(int playerId) throws RemoteException;
	public boolean started(int playerId) throws RemoteException;
	public void registerPosition(int playerId, int x, int y, int dir, int score) throws RemoteException;
	public void registerLoad(int playerId, int load) throws RemoteException;
	public int[][] getInfo() throws RemoteException;
	public int[] getGhostsX() throws RemoteException;
	public int[] getGhostsY() throws RemoteException;
	public void sendScreendata(short[] input) throws RemoteException;
	public short[] requestScreendata() throws RemoteException;
	public ServerBean getStatus() throws RemoteException;
	public void informNewServerIp(String newIp) throws RemoteException;
	public String getNewServerIp() throws RemoteException;

}