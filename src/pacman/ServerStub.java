package pacman;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import javax.swing.Timer;

public class ServerStub extends UnicastRemoteObject implements ServerInterface, ActionListener{
	
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
	 *		La columna 4 indica el puntaje
	 * started indica si la partida comenzo
	 */
	
	int maxPlayers, playerCount;
	int[][] playersInfo;
	boolean started;
	
	//Variables necesarias para manejar los fantasmas:
	int[] dx, dy;
	int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
	int nrofghosts = 6;
	final int maxghosts = 12;
	final int blocksize = 24;
	final int nrofblocks = 15;
	final short leveldata[] = {	19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
								21,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
								21,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
								21,  0,  0,  0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
								17, 18, 18, 18, 16, 16, 20,  0, 17, 16, 16, 16, 16, 16, 20,
								17, 16, 16, 16, 16, 16, 20,  0, 17, 16, 16, 16, 16, 24, 20,
								25, 16, 16, 16, 24, 24, 28,  0, 25, 24, 24, 16, 20,  0, 21,
								1, 17, 16, 20,  0,  0,  0,  0,  0,  0,  0, 17, 20,  0, 21,
								1, 17, 16, 16, 18, 18, 22,  0, 19, 18, 18, 16, 20,  0, 21,
								1, 17, 16, 16, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 21,
								1, 17, 16, 16, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 21,
								1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20,  0, 21,
								1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,  0, 21,
								1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
								9,  8,  8,  8,  8,  8,  8,  8,  8,  8, 25, 24, 24, 24, 28};
	short[] screendata;
	final int validspeeds[] = {1, 2, 3, 4, 6, 8};
	final int maxspeed = 6;
	int currentspeed = 3;
	
	
	//Se colocara un timer para ir actualizando los valores de las posiciones de los fantasmas
	Timer timer;
	
	
	public ServerStub(int maxPlayers) throws RemoteException{
		super();
		this.maxPlayers = maxPlayers;
		playerCount = 0;

		playersInfo = new int [maxPlayers][5];
		
		started = false;
		screendata = new short[nrofblocks*nrofblocks];
		ghostx = new int[maxghosts];
		ghostdx = new int[maxghosts];
		ghosty = new int[maxghosts];
		ghostdy = new int[maxghosts];
		ghostspeed = new int[maxghosts];
		dx = new int[4];
		dy = new int[4];
		levelInit();
		timer = new Timer(40,this);
		timer.start();
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
		for(int i = 0; i < maxPlayers; i++){
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
	public void registerPosition(int playerId, int x, int y, int dir, int score) throws RemoteException{
		playersInfo[playerId][0] = x;
		playersInfo[playerId][1] = y;
		playersInfo[playerId][2] = dir;
		playersInfo[playerId][4] = score;
		System.out.println("Player "+playerId+" is in ("+x+","+y+")");
	}

	/* El jugador consulta este metodo para conocer el estado actual de los demas jugadores.
	 * Retorna el array de enteros que utiliza el servidor para mantener los datos de los jugadores.
	 */
	public int[][] getInfo() throws RemoteException{
		return playersInfo;
	}
	
	/*Metodos de pruebas para ver si funciona el calcular las posiciones de los fantasmas remotamente
	 *y mandarselas a los jugadores
	 * */
	public int[] getGhostsX() throws RemoteException{
		return ghostx;
	}
	public int[] getGhostsY() throws RemoteException{
		return ghosty;
	}
	
	/*refreshGhosts() ocupa el arreglo screendata, en el juego original este 
	 * arreglo es instanciado con leveldata, y su valor solo es actualizado por el
	 * movimiento del pacman.
	 * IMPORTANTE: es posible que este arreglo guarde la informacion de las pelotitas
	 * que hay en el escenario.
	 * */
	public void levelInit(){
		int i;
		for(i = 0; i<nrofblocks*nrofblocks; i++)
			screendata[i] = leveldata[i];
		int random;

		for(i = 0; i<nrofghosts; i++){
			ghosty[i] = 4*blocksize;
			ghostx[i] = 4*blocksize;
			ghostdy[i] = 0;
			ghostdx[i] = 1;
			random = (int)(Math.random()*(currentspeed+1));
			if(random>currentspeed)
				random = currentspeed;
			ghostspeed[i] = validspeeds[random];
		}
	}
	public void refreshGhosts(){
		short i;
		int pos;
		int count;
		
		for(i = 0; i<nrofghosts; i++){
			if(ghostx[i]%blocksize==0&&ghosty[i]%blocksize==0){
				pos = ghostx[i]/blocksize+nrofblocks*(int)(ghosty[i]/blocksize);

				count = 0;
				if((screendata[pos]&1)==0&&ghostdx[i]!=1){
					dx[count] = -1;
					dy[count] = 0;
					count++;
				}
				if((screendata[pos]&2)==0&&ghostdy[i]!=1){
					dx[count] = 0;
					dy[count] = -1;
					count++;
				}
				if((screendata[pos]&4)==0&&ghostdx[i]!=-1){
					dx[count] = 1;
					dy[count] = 0;
					count++;
				}
				if((screendata[pos]&8)==0&&ghostdy[i]!=-1){
					dx[count] = 0;
					dy[count] = 1;
					count++;
				}

				if(count==0){
					if((screendata[pos]&15)==15){
						ghostdx[i] = 0;
						ghostdy[i] = 0;
					} else{
						ghostdx[i] = -ghostdx[i];
						ghostdy[i] = -ghostdy[i];
					}
				} else{
					count = (int)(Math.random()*count);
					if(count>3)
						count = 3;
					ghostdx[i] = dx[count];
					ghostdy[i] = dy[count];
				}

			}
			ghostx[i] = ghostx[i]+(ghostdx[i]*ghostspeed[i]);
			ghosty[i] = ghosty[i]+(ghostdy[i]*ghostspeed[i]);

		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(started){
			refreshGhosts();
		}
	}

	@Override
	public void sendScreendata(short[] input) throws RemoteException {
		// TODO Auto-generated method stub
		//System.out.println("servidor recibio screendata");
		screendata = null;
		screendata = input;
		
	}

	@Override
	public short[] requestScreendata() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("servidor envio screendata");
		return screendata;
	}
}
