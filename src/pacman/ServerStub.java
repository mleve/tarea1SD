package pacman;

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
	 * 		   -1: empty slot
	 * 			0: not ready
	 * 			1: ready. Listo para empezar
	 * 			2: playing
	 * 			3: dead
	 *		La columna 4 indica el puntaje
	 *		La columna 5 indica si este jugador debe actuar como server(1 si, 0 no)
	 * started indica si la partida comenzo
	 */
	//Variables para la magia de la migracion
	boolean isMigrated = false;
	String newServerIp;
	int hostPlayerId=0;
	long initTime;
	boolean inWorkingTime = true;
	
	
	//Datos que se deben transmitir si se quiere montar el server en otra parte:
	int maxPlayers, playerCount;
	int[][] playersInfo;
	boolean started;
	short[] screendata;
	int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
	int[] dx, dy;
	
	//Datos estaticos necesarios para el juego:
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
	final int validspeeds[] = {1, 2, 3, 4, 6, 8};
	final int maxspeed = 6;
	int currentspeed = 3;
	//Se colocara un timer para ir actualizando los valores de las posiciones de los fantasmas
	Timer timer;
	
	
	public ServerStub(int maxPlayers) throws RemoteException{
		super();
		this.maxPlayers = maxPlayers;
		playerCount = 0;

		playersInfo = new int [maxPlayers][6];
		for(int i = 0; i < playersInfo.length; i++){
			playersInfo[i][3] = -1;
			playersInfo[i][5]=0;
		}
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
	
	//Constructor para iniciar un server migrado
	public ServerStub(ServerBean status)throws RemoteException{
		maxPlayers = status.getMaxPlayers();
		playerCount = status.getPlayerCount();
		playersInfo = status.getPlayersInfo();
		//Limpiar la peticion de cambio de migracion
		for(hostPlayerId=0;hostPlayerId<playersInfo.length;hostPlayerId++){
			if(playersInfo[hostPlayerId][5]==1)
				break;
		}
		playersInfo[hostPlayerId][5]=0;
		started = status.isStarted();
		screendata = status.getScreendata();
		ghostx = status.getGhostx();
		ghosty = status.getGhosty();
		ghostdx = status.getGhostdx();
		ghostdy = status.getGhostdy();
		ghostspeed = status.getGhostspeed();
		dx = status.getDx();
		dy = status.getDy();
		timer = new Timer(40,this);
		timer.start();
		initTime=System.currentTimeMillis();
	}
	
	/* Cada jugador debe registrarse en el servidor llamando a este metodo.
	 * Retorna el id del nuevo jugador registrado.
	 */
	public int registerPlayer() throws RemoteException{
		if(playerCount < maxPlayers){
			for(int i = 0; i < playersInfo.length; i++){
				if(playersInfo[i][3] == -1){
					playersInfo[i][0] = 0;
					playersInfo[i][1] = 0;
					playersInfo[i][2] = 0;
					playersInfo[i][3] = 0;
					playersInfo[i][4] = 0;
					System.out.println("Player "+i+" REGISTERED");
					playerCount++;
					return i;
				}
			}
		}
		return -1;	// No quedan cupos
	}
	/* El jugador debe llamar esta funcion y debe pasar como argumento su id de
	 * jugador para registrar su estado como ready.
	 */
	public void registerReady(int playerId) throws RemoteException{
		playersInfo[playerId][3] = 1;
		System.out.println("Player "+playerId+" is READY");
	}
	/* El jugador debe llamar esta funcion y debe pasar como argumento su id de
	 * jugador para registrar su muerte
	 */
	public void registerDeath(int playerId) throws RemoteException{
		playersInfo[playerId][3] = 3;
		System.out.println("Player "+playerId+" is DEAD");
		for(int i = 0; i < playersInfo.length; i++){
			if(playersInfo[i][3] == 2){
				System.out.println("Waiting for player "+i+" to die");
				return;
			}
		}
		System.out.println("All players are DEAD");
		for(int i = 0; i < playersInfo.length; i++){
			if(playersInfo[i][3] != -1)
				playersInfo[i][3] = 0;
		}
		started = false;
		// Reset ghosts
		currentspeed = 3;
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
	
	public void registerQuit(int playerId) throws RemoteException{
		registerDeath(playerId);
		playersInfo[playerId][3] = -1;
		playerCount--;
			
	}
	
	/* Se consulta este metodo para saber si todos los jugadores estan en estado
	 * ready, listo para comenzar. Si todos estan ready, retorna true, sino false.
	 * Antes de retornar true, se cambia el estado del jugador que hizo la llamada
	 * de 1 (ready) a 2 (playing).
	 */
	public boolean started(int playerId) throws RemoteException{
		if(isMigrated)
			return (Boolean) null;
		if(started){
			if(playersInfo[playerId][3] == 1){
				playersInfo[playerId][3] = 2;
			}
			return started;
		}
		for(int i = 0; i < playersInfo.length; i++){
			if(playersInfo[i][3] != 1){
				//System.out.println("Player "+i+" is NOT READY");
				return false;
			}
		}
		started = true;
		playersInfo[playerId][3] = 2;
		//ripPlayersInfo();
		initTime = System.currentTimeMillis();
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
		//System.out.println("Player "+playerId+" is in ("+x+","+y+")");
	}

	/* El jugador consulta este metodo para conocer el estado actual de los demas jugadores.
	 * Retorna el array de enteros que utiliza el servidor para mantener los datos de los jugadores.
	 */
	public int[][] getInfo() throws RemoteException{
		if(isMigrated)
			return (int[][]) null;
		return playersInfo;
	}
	
	/*Metodos de pruebas para ver si funciona el calcular las posiciones de los fantasmas remotamente
	 *y mandarselas a los jugadores
	 * */
	public int[] getGhostsX() throws RemoteException{
		if(isMigrated)
			return (int[]) null;
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
		//migracion cada 10 seg
		if(inWorkingTime && (System.currentTimeMillis()-initTime)>=10*1000){
			//Se debe realizar migracion, por ahora se pimponean entre jugador 1 y 2
			int newHost = newRandomHost(hostPlayerId);
			playersInfo[hostPlayerId][5]=0;
			playersInfo[newHost][5]=1;
			inWorkingTime=false;
			System.out.println("Se solicito migracion");	
		}
		
		}
	}

	private int newRandomHost(int actualHostId) {
		// Retorna la id de un nuevo host que no sea el actual, y que sea un jugador valido
		int newHostId=-1;
		boolean validCandidate = false;
		while(!validCandidate){
			newHostId = (int) Math.floor(Math.random()*playersInfo.length);
			if(newHostId==actualHostId || playersInfo[newHostId][3]==-1)
				continue;
			else
				break;
		}
		return newHostId;
	}

	@Override
	public void sendScreendata(short[] input) throws RemoteException {
		//System.out.println("servidor recibio screendata");
		screendata = null;
		screendata = input;
		
	}

	@Override
	public short[] requestScreendata() throws RemoteException {
		//System.out.println("servidor envio screendata");
		if(isMigrated)
			return (short[]) null;
		return screendata;
	}

	@Override
	public ServerBean getStatus() throws RemoteException{
		//Devuelve un Bean con todos los datos del server:
		ServerBean bean = new ServerBean();
		bean.setDx(dx);
		bean.setDy(dy);
		bean.setGhostdx(ghostdx);
		bean.setGhostdy(ghostdy);
		bean.setGhostspeed(ghostspeed);
		bean.setGhostx(ghostx);
		bean.setGhosty(ghosty);
		bean.setMaxPlayers(maxPlayers);
		bean.setPlayerCount(playerCount);
		bean.setPlayersInfo(playersInfo);
		bean.setScreendata(screendata);
		bean.setStarted(started);
		return bean;
	}

	@Override
	public void informNewServerIp(String newIp) throws RemoteException {
		/*El cliente que llama a este metodo informa que ha instanciado con exito un nuevo servidor
		 * (con el estado del actual e informa la nueva Ip a la que conectarse
		 * */
		isMigrated = true;
		newServerIp = newIp;
		System.out.println("recibida la IP del nuevo servidor");
	}

	@Override
	public String getNewServerIp() throws RemoteException {
		//Cuando el cliente recibe un null a una peticion, pide la ip de un nuevo servidor para reconectarse
		return newServerIp;
	}
}
