package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.rmi.Naming;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;

public class Board extends JPanel implements ActionListener{

	Dimension d;
	Font smallfont = new Font("Helvetica", Font.BOLD, 14);

	FontMetrics fmsmall, fmlarge;
	Image ii;
	Color dotcolor = new Color(192, 192, 0);
	Color mazecolor;

	boolean ready = false;
	boolean ingame = false;
	boolean dying = false;
	boolean winner = false;
    boolean dead = false;

	final int blocksize = 24;
	final int nrofblocks = 15;
	final int scrsize = nrofblocks*blocksize;
	final int pacanimdelay = 2;
	final int pacmananimcount = 4;
	final int maxghosts = 12;
	final int pacmanspeed = 6;

	int pacanimcount = pacanimdelay;
	int pacanimdir = 1;
	int pacmananimpos = 0;
	int nrofghosts = 6;
	int pacsleft, score;
	int deathcounter;
	int[] dx, dy;
	int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;

	Image ghost;
	Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
	Image pacman3up, pacman3down, pacman3left, pacman3right;
	Image pacman4up, pacman4down, pacman4left, pacman4right;

	int pacmanx, pacmany, pacmandx, pacmandy;
	int reqdx, reqdy, viewdx, viewdy;

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
	short[] screendata;
	Timer timer;
	// Datos del servidor
	ServerInterface server;
	int pacmandir = 1; // Inicialmente mira hacia la derecha
	int playerId = -1;
	int[][] playersInfo = null;
	public Board(String serverIp){
		// Conectarse al server
		connect(serverIp);
		// Registrarse como player
		try{
			playerId = server.registerPlayer();
		} catch(RemoteException e){
			e.printStackTrace();
			System.exit(128);
		}
		if(playerId == -1){
			// Si no se pudo tomar un cupo, se cierra
			System.exit(1);
		}
		
		GetImages();

		addKeyListener(new TAdapter());

		screendata = new short[nrofblocks*nrofblocks];
		mazecolor = new Color(5, 100, 5);
		setFocusable(true);

		d = new Dimension(400, 400);

		setBackground(Color.black);
		setDoubleBuffered(true);

		ghostx = new int[maxghosts];
		ghostdx = new int[maxghosts];
		ghosty = new int[maxghosts];
		ghostdy = new int[maxghosts];
		ghostspeed = new int[maxghosts];
		dx = new int[4];
		dy = new int[4];
		timer = new Timer(40, this);
		timer.start();
	}

	public void addNotify(){
		super.addNotify();
		GameInit();
	}

	public void DoAnim(){
		pacanimcount--;
		if(pacanimcount<=0){
			pacanimcount = pacanimdelay;
			pacmananimpos = pacmananimpos+pacanimdir;
			if(pacmananimpos==(pacmananimcount-1)||pacmananimpos==0)
				pacanimdir = -pacanimdir;
		}
	}

	public void PlayGame(Graphics2D g2d){
		if(dying){
			Death();
		} else{
			MovePacMan();
			DrawPacMan(g2d);
			moveGhosts(g2d);
			CheckMaze();
		}
	}

	public void ShowIntroScreen(Graphics2D g2d, int i) {

    	String s = "Press s to start.";
    	Font small = new Font("Helvetica", Font.BOLD, 14);
            FontMetrics metr = this.getFontMetrics(small);

            g2d.setColor(Color.white);
            g2d.setFont(small);

    	if(i == 1) {
    		
    		int j = playerId;
    		int aux = playersInfo[playerId][4]; 
    			for(int k = 0; k < playersInfo.length; k++){
    				if(playersInfo[k][4] > aux){ 
    					aux = playersInfo[k][4];
    					j = k;
    				}
    			}
    		
    		s = "Player "+(j+1)+" has won. Press s to play again.";
            	g2d.setColor(new Color(0, 32, 48));
            	g2d.fillRect(10, scrsize / 2 - 30, scrsize - 1, 50);
            	g2d.setColor(Color.white);
            	g2d.drawRect(10, scrsize / 2 - 30, scrsize - 1, 50);
    		g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2 + 10, scrsize / 2);

    	}

    	else if(i == 2) {

    		s = "Game Over. Wait for the others";
            	g2d.setColor(new Color(0, 32, 48));
    	        g2d.fillRect(10, scrsize / 2 - 30, scrsize - 1, 50);
            	g2d.setColor(Color.white);
            	g2d.drawRect(10, scrsize / 2 - 30, scrsize - 1, 50);
    		g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2 + 10, scrsize / 2);

    	}

    	else {
            	g2d.setColor(new Color(0, 32, 48));
    	        g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
            	g2d.setColor(Color.white);
            	g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);
    		g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
    	}
       
    }

	public void CheckMaze(){
		short i = 0;
		boolean finished = true;

		while(i<nrofblocks*nrofblocks&&finished){
			if((screendata[i]&48)!=0)
				finished = false;
			i++;
		}

		if(finished){
			//score += 50;
			ingame = false;
			winner = true;

			if(nrofghosts<maxghosts)
				nrofghosts++;
			if(currentspeed<maxspeed)
				currentspeed++;
			LevelInit();
		}
	}

	public void Death(){
		System.out.println("DEAD");
		pacsleft--;
		if(pacsleft==0){
			ingame = false;
			//ready = true;
			dead = true;
			try{
				server.registerDeath(playerId);
			} catch(Exception e){}
		}
		LevelContinue();
	}

	public void moveGhosts(Graphics2D g2d){
		/*
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
			*/
			//Todos estos calculos de arriba los hace ahora el servidor
		int i;
		//Pedimos la posicion actual de los fantasmas al servidor:
		try{
		ghostx = server.getGhostsX();
		ghosty = server.getGhostsY();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(128);
		}
		for(i = 0; i<nrofghosts; i++){
			drawGhost(g2d, ghostx[i]+1, ghosty[i]+1);

			if(pacmanx>(ghostx[i]-12)&&pacmanx<(ghostx[i]+12)&&pacmany>(ghosty[i]-12)&&pacmany<(ghosty[i]+12)&&ingame){

				dying = true;
				deathcounter = 64;

			}
		}
	}

	public void MovePacMan(){
		int pos;
		short ch;

		if(reqdx==-pacmandx&&reqdy==-pacmandy){
			pacmandx = reqdx;
			pacmandy = reqdy;
			viewdx = pacmandx;
			viewdy = pacmandy;
		}
		if(pacmanx%blocksize==0&&pacmany%blocksize==0){
			pos = pacmanx/blocksize+nrofblocks*(int)(pacmany/blocksize);
			ch = screendata[pos];

			if((ch&16)!=0){
				screendata[pos] = (short)(ch&15);
				score++;
			}

			if(reqdx!=0||reqdy!=0){
				if(!((reqdx==-1&&reqdy==0&&(ch&1)!=0)||(reqdx==1&&reqdy==0&&(ch&4)!=0)||(reqdx==0&&reqdy==-1&&(ch&2)!=0)||(reqdx==0&&reqdy==1&&(ch&8)!=0))){
					pacmandx = reqdx;
					pacmandy = reqdy;
					viewdx = pacmandx;
					viewdy = pacmandy;
				}
			}

			// Check for standstill
			if((pacmandx==-1&&pacmandy==0&&(ch&1)!=0)||(pacmandx==1&&pacmandy==0&&(ch&4)!=0)||(pacmandx==0&&pacmandy==-1&&(ch&2)!=0)||(pacmandx==0&&pacmandy==1&&(ch&8)!=0)){
				pacmandx = 0;
				pacmandy = 0;
			}
		}
		
		/* Si hay mas jugadores participando, se verifica si alguno de ellos (en el orden en que se    
		 * conectaron) se comio alguna ficha del tablero, en cuyo caso se elimina.
		 */

		if(playersInfo != null){
			for(int i = 0; i < playersInfo.length; i++){
				if(i == playerId)
					continue;
				if(playersInfo[i][0]%blocksize==0&&playersInfo[i][1]%blocksize==0){
					pos = playersInfo[i][0]/blocksize+nrofblocks*(int)(playersInfo[i][1]/blocksize);
					ch = screendata[pos];
				
					if((ch&16)!=0){
						screendata[pos] = (short)(ch&15);
					}
				}
			}
		}
		
		pacmanx = pacmanx+pacmanspeed*pacmandx;
		pacmany = pacmany+pacmanspeed*pacmandy;
		//Enviar screendata para actualizar el estado del tablero luego del 
		//movimiento de un jugador:
		try{
			server.sendScreendata(screendata);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(128);
		}
		
	}

	public void DrawPacMan(Graphics2D g2d){
		if(viewdx==-1){
			pacmandir = 3;
			DrawPacManLeft(g2d, pacmanx+1, pacmany+1);
		}
		else if(viewdx==1){
			pacmandir = 1;
			DrawPacManRight(g2d, pacmanx+1, pacmany+1);
		}
		else if(viewdy==-1){
			pacmandir = 0;
			DrawPacManUp(g2d, pacmanx+1, pacmany+1);
		}
		else{
			pacmandir = 2;
			DrawPacManDown(g2d, pacmanx+1, pacmany+1);
		}
		
		if(playersInfo != null){
			int dir;
			for(int i = 0; i < playersInfo.length; i++){
				if(i == playerId)
					continue;				// No dibujar su propio pacman (se dibuja mas arriba)
				if(playersInfo[i][3] == 2){
					dir = playersInfo[i][2];
					switch(dir){
						case 0: // up
							DrawPacManUp(g2d, playersInfo[i][0], playersInfo[i][1]);
							break;
						case 1: // right
							DrawPacManRight(g2d, playersInfo[i][0], playersInfo[i][1]);
							break;
						case 2: // down
							DrawPacManDown(g2d, playersInfo[i][0], playersInfo[i][1]);
							break;
						case 3: // left
							DrawPacManLeft(g2d, playersInfo[i][0], playersInfo[i][1]);
							break;
					}
				}
			}
		}
	}

	public void DrawPacManUp(Graphics2D g2d, int x, int y){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2up, x, y, this);
				break;
			case 2:
				g2d.drawImage(pacman3up, x, y, this);
				break;
			case 3:
				g2d.drawImage(pacman4up, x, y, this);
				break;
			default:
				g2d.drawImage(pacman1, x, y, this);
				break;
		}
	}

	public void DrawPacManDown(Graphics2D g2d, int x, int y){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2down, x, y, this);
				break;
			case 2:
				g2d.drawImage(pacman3down, x, y, this);
				break;
			case 3:
				g2d.drawImage(pacman4down, x, y, this);
				break;
			default:
				g2d.drawImage(pacman1, x, y, this);
				break;
		}
	}

	public void DrawPacManLeft(Graphics2D g2d, int x, int y){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2left, x, y, this);
				break;
			case 2:
				g2d.drawImage(pacman3left, x, y, this);
				break;
			case 3:
				g2d.drawImage(pacman4left, x, y, this);
				break;
			default:
				g2d.drawImage(pacman1, x, y, this);
				break;
		}
	}

	public void DrawPacManRight(Graphics2D g2d, int x, int y){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2right, x, y, this);
				break;
			case 2:
				g2d.drawImage(pacman3right, x, y, this);
				break;
			case 3:
				g2d.drawImage(pacman4right, x, y, this);
				break;
			default:
				g2d.drawImage(pacman1, x, y, this);
				break;
		}
	}
	
	public void drawGhost(Graphics2D g2d, int x, int y){
		g2d.drawImage(ghost, x, y, this);
	}

	public void DrawMaze(Graphics2D g2d){
		short i = 0;
		int x, y;
		//Actualizar screendata con la del server
		screendata =null;
		try{
			screendata = server.requestScreendata();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(128);
		}
		for(y = 0; y<scrsize; y += blocksize){
			for(x = 0; x<scrsize; x += blocksize){
				g2d.setColor(mazecolor);
				g2d.setStroke(new BasicStroke(2));

				if((screendata[i]&1)!=0) // draws left
				{
					g2d.drawLine(x, y, x, y+blocksize-1);
				}
				if((screendata[i]&2)!=0) // draws top
				{
					g2d.drawLine(x, y, x+blocksize-1, y);
				}
				if((screendata[i]&4)!=0) // draws right
				{
					g2d.drawLine(x+blocksize-1, y, x+blocksize-1, y+blocksize-1);
				}
				if((screendata[i]&8)!=0) // draws bottom
				{
					g2d.drawLine(x, y+blocksize-1, x+blocksize-1, y+blocksize-1);
				}
				if((screendata[i]&16)!=0) // draws point
				{
					g2d.setColor(dotcolor);
					g2d.fillRect(x+11, y+11, 2, 2);
				}
				i++;
			}
		}
	}
	
	public void DrawScore(Graphics2D g){
	int i;
	String s;

	g.setFont(smallfont);
	g.setColor(new Color(96, 128, 255));
	s = "Player: "+ (playerId+1) + "                 Score: "+score;
	g.drawString(s, scrsize/2-30, scrsize+16);
	for(i = 0; i<pacsleft; i++){
		g.drawImage(pacman3left, i*28+8, scrsize+1, this);
	}
	}

	public void GameInit(){
		pacsleft = 3;
		score = 0;
		LevelInit();
		nrofghosts = 6;
		currentspeed = 3;
	}

	public void LevelInit(){
		int i;
		for(i = 0; i<nrofblocks*nrofblocks; i++)
			screendata[i] = leveldata[i];

		LevelContinue();
	}

	public void LevelContinue(){
		short i;
		int dx = 1;
		int random;

		for(i = 0; i<nrofghosts; i++){
			ghosty[i] = 4*blocksize;
			ghostx[i] = 4*blocksize;
			ghostdy[i] = 0;
			ghostdx[i] = dx;
			dx = -dx;
			random = (int)(Math.random()*(currentspeed+1));
			if(random>currentspeed)
				random = currentspeed;
			ghostspeed[i] = validspeeds[random];
		}

		pacmanx = 7*blocksize;
		pacmany = 11*blocksize;
		pacmandx = 0;
		pacmandy = 0;
		reqdx = 0;
		reqdy = 0;
		viewdx = -1;
		viewdy = 0;
		dying = false;
	}

	public void GetImages(){

		ghost = new ImageIcon(Board.class.getResource("./ghost.gif")).getImage();
		pacman1 = new ImageIcon(Board.class.getResource("./pacman.gif")).getImage();
		pacman2up = new ImageIcon(Board.class.getResource("./up1.gif")).getImage();
		pacman3up = new ImageIcon(Board.class.getResource("./up2.gif")).getImage();
		pacman4up = new ImageIcon(Board.class.getResource("./up3.gif")).getImage();
		pacman2down = new ImageIcon(Board.class.getResource("./down1.gif")).getImage();
		pacman3down = new ImageIcon(Board.class.getResource("./down2.gif")).getImage();
		pacman4down = new ImageIcon(Board.class.getResource("./down3.gif")).getImage();
		pacman2left = new ImageIcon(Board.class.getResource("./left1.gif")).getImage();
		pacman3left = new ImageIcon(Board.class.getResource("./left2.gif")).getImage();
		pacman4left = new ImageIcon(Board.class.getResource("./left3.gif")).getImage();
		pacman2right = new ImageIcon(Board.class.getResource("./right1.gif")).getImage();
		pacman3right = new ImageIcon(Board.class.getResource("./right2.gif")).getImage();
		pacman4right = new ImageIcon(Board.class.getResource("./right3.gif")).getImage();

	}

	public void paint(Graphics g){
		super.paint(g);

		Graphics2D g2d = (Graphics2D)g;

		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, d.width, d.height);

		DrawMaze(g2d);
		DrawScore(g2d);
		DoAnim();
		if(ingame)
			PlayGame(g2d);
		else{
			if(winner){
				ShowIntroScreen(g2d, 1);
			}
			else if(dead){
				ShowIntroScreen(g2d, 2);
			}
			else{
				ShowIntroScreen(g2d, 0);
			}
		}

		g.drawImage(ii, 5, 5, this);
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	class TAdapter extends KeyAdapter{
		public void keyPressed(KeyEvent e){

			int key = e.getKeyCode();

			if(ingame){
				if(key==KeyEvent.VK_LEFT){
					reqdx = -1;
					reqdy = 0;
				} else if(key==KeyEvent.VK_RIGHT){
					reqdx = 1;
					reqdy = 0;
				} else if(key==KeyEvent.VK_UP){
					reqdx = 0;
					reqdy = -1;
				} else if(key==KeyEvent.VK_DOWN){
					reqdx = 0;
					reqdy = 1;
				} else if(key==KeyEvent.VK_ESCAPE&&timer.isRunning()){
					ingame = false;
					try{
						server.registerQuit(playerId);
					} catch(Exception exception){}
					System.exit(0);
				} else if(key==KeyEvent.VK_PAUSE){
					if(timer.isRunning())
						timer.stop();
					else
						timer.start();
				}
			} else{
				if(!ready && (key=='s'||key=='S')){
					if(!dead){
						/* Al presionar 's' el jugador entra en estado READY, listo para comenzar la
						 * partida. Registra su nuevo estado en el servidor y espera a que todos los
						 * demas jugadores esten listos
						 */
						try{
							server.registerReady(playerId);
							System.out.println("RegisterReady");
						} catch(RemoteException exception){
							exception.printStackTrace();
							System.exit(128);
						}
						ready = true;
					}
				}
			}
		}

		public void keyReleased(KeyEvent e){
			int key = e.getKeyCode();

			if(key==Event.LEFT||key==Event.RIGHT||key==Event.UP||key==Event.DOWN){
				reqdx = 0;
				reqdy = 0;
			}
		}
	}

	public void actionPerformed(ActionEvent e){
		if(ingame){
			try{
				server.registerPosition(playerId, pacmanx, pacmany, pacmandir, score);
				//System.out.println("Registered position: ("+pacmanx+","+pacmany+","+pacmandir+")");
				playersInfo = server.getInfo();
			} catch(RemoteException exception){
				exception.printStackTrace();
				System.exit(128);
			}
		} else{
			/* Si el cliente esta en estado READY, pero no ha comenzado todavia la partida,
			 * cada 40ms el consulta al servidor si la partida comenzo, es decir,
			 * si todos los jugadores registraron su estado como READY.
			 * Si el servidor contesta que si: se desactiva el estado READY local y se activa
			 * el estado local 'ingame'. Ademas se da inicio al juego.
			 */
			boolean started = false;
			try{
				started = server.started(playerId);
			} catch(RemoteException exception){
				exception.printStackTrace();
				System.exit(128);
			}
			if(started && ready){
				// Comienza el juego
				ready = false;
				ingame = true;
				GameInit();
			} else if(!started && dead){
				// Todos murieron, se resetea el juego
				dead = false;
			}
		}
		repaint();
	}
	
	private void connect(String serverIp){
		try{
			server = (ServerInterface)Naming.lookup("rmi://"+serverIp+":1099/ServerStub");
		} catch (NotBoundException e){
			System.out.println("El servicio no esta publicado en el servidor");
			System.exit(128);
		} catch (MalformedURLException e){
			System.out.println("URL invalida");
			System.exit(128);
		} catch (RemoteException e){
			System.out.println("Excepcion remota tratando de conectarse al servidor");
			System.exit(128);
		}
	}
}
