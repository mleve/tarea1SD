package pacman;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class ServerStub extends UnicastRemoteObject implements ServerInterface{
	
	/* maxPlayers es la cantidad maxima de jugadores online simultaneos que puede haber
	 * 
	 * playerCount es la cantidad de jugadores actual
	 * 
	 * playersInfo es una matriz que tiene la informacion de todos los jugadores.
	 * 		La fila i pertenece al jugador con id i
	 * 		La columna 0 indica su posicion en el eje x
	 * 		La columna 1 indica su posicion en el eje y
	 * 		La columna 2 indica su direccion (util para dibujar el pacman)
	 * 			0: up
	 * 			1: right
	 * 			2: down
	 * 			3: left
	 * 		La columna 3 indica su estado
	 * 			0: not ready
	 * 			1: ready. Listo para empezar
	 * 			2: playing
	 * 			3: dead
	 * 
	 * started indica si la partida comenzo
	 */
	
	int maxPlayers, playerCount;
	int[][] playersInfo;
	boolean started;
	
	public ServerStub() throws RemoteException{
		super();
		maxPlayers = 5;
		playerCount = 0;
		playersInfo = new int [maxPlayers][4];
	}
	
	/* Cada jugador debe registrarse en el servidor llamando a este metodo.
	 * Retorna el id del nuevo jugador registrado.
	 */
	public int registerPlayer() throws RemoteException{
		if(playerCount < maxPlayers){
			playersInfo[playerCount][3] = 0;
			System.out.println("Player "+playerCount+" REGISTERED");
			return playerCount++;
		} else
			return -1;	// No quedan cupos
	}
	/* El jugador debe llamar esta funcion y debe pasar como argumento su id de
	 * jugador para registrar su estado como ready.
	 */
	public void registerReady(int playerId) throws RemoteException{
		playersInfo[playerId][3] = 1;
		System.out.println("Player "+playerId+" is READY");
	}
	/* Se consulta este metodo para saber si todos los jugadores estan en estado
	 * ready, listo para comenzar. Si todos estan ready, retorna true, sino false.
	 * Antes de retornar true, se cambia el estado del jugador que hizo la llamada
	 * de 1 (ready) a 2 (playing).
	 */
	public boolean started(int playerId) throws RemoteException{
		if(started){
			playersInfo[playerId][3] = 2;
			return started;
		}
		for(int i = 0; i < playerCount; i++){
			if(playersInfo[i][3] != 1){
				System.out.println("Player "+i+" is NOT READY");
				return false;
			}
		}
		started = true;
		playersInfo[playerId][3] = 2;
		ripPlayersInfo();
		return started;
	}
	/*
	 * Truca la matriz playersInfo, eliminando las filas de los jugadores vacias
	 */
	private void ripPlayersInfo(){
		int[][] rippedPlayersInfo= new int[playerCount][playersInfo[0].length];
		for(int i = 0; i < rippedPlayersInfo.length; i++)
			for(int j = 0; j < rippedPlayersInfo[0].length; j++)
				rippedPlayersInfo[i][j] = playersInfo[i][j];
		playersInfo = rippedPlayersInfo;
	}
	
	/* El jugador llama a este metodo para actualizar su ubicacion en el servidor
	 * El servidor no conoce el tablero, ni valida sus parametros, confia en que 
	 * el jugador le pasa los datos correctos.
	 */
	public void registerPosition(int playerId, int x, int y, int dir) throws RemoteException{
		playersInfo[playerId][0] = x;
		playersInfo[playerId][1] = y;
		playersInfo[playerId][2] = dir;
		System.out.println("Player "+playerId+" is in ("+x+","+y+")");
	}

	/* El jugador consulta este metodo para conocer el estado actual de los demas jugadores.
	 * Retorna el array de enteros que utiliza el servidor para mantener los datos de los jugadores.
	 */
	public int[][] getInfo() throws RemoteException{
		return playersInfo;
	}
}
