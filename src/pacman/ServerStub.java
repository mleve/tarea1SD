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
	 * 		La columna 2 indica su estado
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
	
	
	public ServerStub() throws RemoteException{
		super();
		maxPlayers = 5;
		playerCount = 0;
		playersInfo = new int [maxPlayers][3];
		
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
		playersInfo[playerCount][2] = 0;
		System.out.println("Player "+playerCount+" REGISTERED");
		return playerCount++;
	}
	/* El jugador debe llamar esta funcion y debe pasar como argumento su id de
	 * jugador para registrar su estado como ready.
	 */
	public void registerReady(int playerId) throws RemoteException{
		playersInfo[playerId][2] = 1;
		System.out.println("Player "+playerId+" is READY");
	}
	/* Se consulta este metodo para saber si todos los jugadores estan en estado
	 * ready, listo para comenzar. Si todos estan ready, retorna true, sino false.
	 * Antes de retornar true, se cambia el estado del jugador que hizo la llamada
	 * de 1 (ready) a 2 (playing).
	 */
	public boolean started(int playerId) throws RemoteException{
		if(started){
			playersInfo[playerId][2] = 2;
			return started;
		}
		for(int i = 0; i < playerCount; i++){
			if(playersInfo[i][2] != 1){
				System.out.println("Player "+i+" is NOT READY");
				return false;
			}
		}
		started = true;
		playersInfo[playerId][2] = 2;
		return started;
	}
	/* El jugador llama a este metodo para actualizar su ubicacion en el servidor
	 * El servidor no conoce el tablero, ni valida sus parametros, confia en que 
	 * el jugador le pasa los datos correctos.
	 */
	public void registerPosition(int playerId, int x, int y) throws RemoteException{
		playersInfo[playerId][0] = x;
		playersInfo[playerId][1] = y;
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
		/*Aqui se debería pedir la posicion de los fantasmas al servidor,
		 * luego ejecuta todo el jugo local que se ve aquí,
		 * actualiza sus posiciones*/
		
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
}
