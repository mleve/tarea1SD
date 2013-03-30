import javax.swing.JFrame;
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

public class PacMan extends JFrame implements ActionListener{
	// Variables usadas para el tema grafico
	private Timer timer;
	private JPanel board;
	private Image ghost;
	private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
	private Image pacman3up, pacman3down, pacman3left, pacman3right;
	private Image pacman4up, pacman4down, pacman4left, pacman4right;
	private Dimension d;
	Color dotcolor = new Color(192, 192, 0);
	Color mazecolor;
	short[] screendata;
	Font smallfont = new Font("Helvetica", Font.BOLD, 14);
	boolean ingame = false;
	Image ii;
	final short leveldata[] = 
		{19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
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
		  9,  8,  8,  8,  8,  8,  8,  8,  8,  8, 25, 24, 24, 24, 28 };

	// Copia local de las posiciones de fantasmas y pacmans
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

	int pacmanx, pacmany, pacmandx, pacmandy;
	int reqdx, reqdy, viewdx, viewdy;

	// Controlador del juego:
	GameController gc;

	private void boardInit(){
		board = new JPanel();
		board.addKeyListener(new TAdapter());
		board.setFocusable(true);
		d = new Dimension(400, 400);
		board.setBackground(Color.black);
		board.setDoubleBuffered(true);

		// preparacion del terreno:
		for(int i = 0; i<nrofblocks*nrofblocks; i++){
			screendata[i] = leveldata[i];

		}

		timer = new Timer(40, this);
		timer.start();

	}

	public void GetImages(){

		ghost = new ImageIcon(GameController.class.getResource("./ghost.gif"))
				.getImage();
		pacman1 = new ImageIcon(
				GameController.class.getResource("./pacman.gif")).getImage();
		pacman2up = new ImageIcon(GameController.class.getResource("./up1.gif"))
				.getImage();
		pacman3up = new ImageIcon(GameController.class.getResource("./up2.gif"))
				.getImage();
		pacman4up = new ImageIcon(GameController.class.getResource("./up3.gif"))
				.getImage();
		pacman2down = new ImageIcon(
				GameController.class.getResource("./down1.gif")).getImage();
		pacman3down = new ImageIcon(
				GameController.class.getResource("./down2.gif")).getImage();
		pacman4down = new ImageIcon(
				GameController.class.getResource("./down3.gif")).getImage();
		pacman2left = new ImageIcon(
				GameController.class.getResource("./left1.gif")).getImage();
		pacman3left = new ImageIcon(
				GameController.class.getResource("./left2.gif")).getImage();
		pacman4left = new ImageIcon(
				GameController.class.getResource("./left3.gif")).getImage();
		pacman2right = new ImageIcon(
				GameController.class.getResource("./right1.gif")).getImage();
		pacman3right = new ImageIcon(
				GameController.class.getResource("./right2.gif")).getImage();
		pacman4right = new ImageIcon(
				GameController.class.getResource("./right3.gif")).getImage();

	}

	public PacMan(){
		// Inicializo las variables del juego:
		screendata = new short[nrofblocks*nrofblocks];
		mazecolor = new Color(5, 100, 5);
		ghostx = new int[maxghosts];
		ghostdx = new int[maxghosts];
		ghosty = new int[maxghosts];
		ghostdy = new int[maxghosts];
		ghostspeed = new int[maxghosts];
		dx = new int[4];
		dy = new int[4];

		GetImages();
		// Inicio el tablero:
		boardInit();
		add(board);
		setTitle("Pacman");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(380, 420);
		setLocationRelativeTo(null);
		setVisible(true);

		gc = new GameController();
	}

	void makeRequest(int option){
		/*
		 * Hay 5 solicitudes posibles al controlador del juego: Iniciar (o
		 * conectarse). (opt 1) Moverse en alguna dirección.(2 izq, 3 arriba, 4
		 * der, 5 abajo)
		 * 
		 * La respuesta del servidor deberia ser un arreglo de objetos:
		 * (boolean) Esta jugando o no (int) vidas restantes (int) hacia donde
		 * mira ahora pacman (int)x N las nuevas posiciones del pacman y
		 * fantasmas
		 * 
		 * Este metodo procesa la "respuesta" y actualiza los campos
		 * correspondientes.
		 */

	}

	public void PlayGame(Graphics2D g2d){
		DrawPacMan(g2d);
		drawGhost(g2d, 100, 100);
		/*
		 * if (dying) { Death(); } else { //MovePacMan(); DrawPacMan(g2d);
		 * moveGhosts(g2d); CheckMaze();
		 * 
		 * 
		 * }
		 */
	}

	// Los metodos desque aqui hasta el main tienen que ver con temas graficos:
	public void DoAnim(){
		pacanimcount--;
		if(pacanimcount<=0){
			pacanimcount = pacanimdelay;
			pacmananimpos = pacmananimpos+pacanimdir;
			if(pacmananimpos==(pacmananimcount-1)||pacmananimpos==0)
				pacanimdir = -pacanimdir;
		}
	}

	public void drawGhost(Graphics2D g2d, int x, int y){
		g2d.drawImage(ghost, x, y, this);
	}

	public void DrawPacMan(Graphics2D g2d){
		if(viewdx==-1)
			DrawPacManLeft(g2d);
		else if(viewdx==1)
			DrawPacManRight(g2d);
		else if(viewdy==-1)
			DrawPacManUp(g2d);
		else
			DrawPacManDown(g2d);
	}

	public void DrawPacManUp(Graphics2D g2d){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2up, pacmanx+1, pacmany+1, this);
				break;
			case 2:
				g2d.drawImage(pacman3up, pacmanx+1, pacmany+1, this);
				break;
			case 3:
				g2d.drawImage(pacman4up, pacmanx+1, pacmany+1, this);
				break;
			default:
				g2d.drawImage(pacman1, pacmanx+1, pacmany+1, this);
				break;
		}
	}

	public void DrawPacManDown(Graphics2D g2d){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2down, pacmanx+1, pacmany+1, this);
				break;
			case 2:
				g2d.drawImage(pacman3down, pacmanx+1, pacmany+1, this);
				break;
			case 3:
				g2d.drawImage(pacman4down, pacmanx+1, pacmany+1, this);
				break;
			default:
				g2d.drawImage(pacman1, pacmanx+1, pacmany+1, this);
				break;
		}
	}

	public void DrawPacManLeft(Graphics2D g2d){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2left, pacmanx+1, pacmany+1, this);
				break;
			case 2:
				g2d.drawImage(pacman3left, pacmanx+1, pacmany+1, this);
				break;
			case 3:
				g2d.drawImage(pacman4left, pacmanx+1, pacmany+1, this);
				break;
			default:
				g2d.drawImage(pacman1, pacmanx+1, pacmany+1, this);
				break;
		}
	}

	public void DrawPacManRight(Graphics2D g2d){
		switch(pacmananimpos){
			case 1:
				g2d.drawImage(pacman2right, pacmanx+1, pacmany+1, this);
				break;
			case 2:
				g2d.drawImage(pacman3right, pacmanx+1, pacmany+1, this);
				break;
			case 3:
				g2d.drawImage(pacman4right, pacmanx+1, pacmany+1, this);
				break;
			default:
				g2d.drawImage(pacman1, pacmanx+1, pacmany+1, this);
				break;
		}
	}

	public void paint(Graphics g){
		/*
		 * Este metodo es al parecer el que se encarga de refrescar la pantalla,
		 * su firma al parecer es obligatoria, aproveche dentro de este metodo
		 * para obtener el Graphics de el Jpanel Board (el tablero del juego) y
		 * en este momento dibujar a pacman y los fantasmas segun las posiciones
		 * que hay almacenadas "localmente" (en la instancia de esta clase)
		 */
		Graphics boardCanvas = board.getGraphics();
		Graphics2D g2d = (Graphics2D)boardCanvas;

		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, d.width, d.height);

		DrawMaze(g2d);

		DrawScore(g2d);
		DoAnim();
		if(ingame)
			PlayGame(g2d);
		else
			ShowIntroScreen(g2d);

		g.drawImage(ii, 5, 5, this);
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	public void DrawMaze(Graphics2D g2d){
		short i = 0;
		int x, y;

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

	public void ShowIntroScreen(Graphics2D g2d){

		g2d.setColor(new Color(0, 32, 48));
		g2d.fillRect(50, scrsize/2-30, scrsize-100, 50);
		g2d.setColor(Color.white);
		g2d.drawRect(50, scrsize/2-30, scrsize-100, 50);

		String s = "Press s to start.";
		Font small = new Font("Helvetica", Font.BOLD, 14);
		FontMetrics metr = this.getFontMetrics(small);

		g2d.setColor(Color.white);
		g2d.setFont(small);
		g2d.drawString(s, (scrsize-metr.stringWidth(s))/2, scrsize/2);
	}

	public void DrawScore(Graphics2D g){
		int i;
		String s;

		g.setFont(smallfont);
		g.setColor(new Color(96, 128, 255));
		s = "Score: "+score;
		g.drawString(s, scrsize/2+96, scrsize+16);
		for(i = 0; i<pacsleft; i++){
			g.drawImage(pacman3left, i*28+8, scrsize+1, this);
		}
	}

	public static void main(String[] args){
		new PacMan();
	}

	/*
	 * Esta subClase (no se el nombre tecnico que llevan estas cosas xD) es la
	 * que se encarga de manejar los inputs del teclado:
	 */
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
				} else if(key==KeyEvent.VK_PAUSE){
					if(timer.isRunning())
						timer.stop();
					else
						timer.start();
				}
			} else{
				if(key=='s'||key=='S'){
					ingame = true;
					pacsleft = 3;
					// GameInit();
				}
			}
		}

		public void keyReleased(KeyEvent e){
			int key = e.getKeyCode();

			if(key==Event.LEFT||key==Event.RIGHT||key==Event.UP
					||key==Event.DOWN){
				reqdx = 0;
				reqdy = 0;
			}
		}
	}

	public void actionPerformed(ActionEvent e){
		/*
		 * Segun lo poco que lei, el profe, o quien hizo este juego, uso una
		 * implementacion antigua para la parte grafica, pero creo que funciona
		 * asi: Cada 40 milisegundos (el timer) se llama a este metodo, el cual
		 * solo llama a repaint, este a su vez, llama a paint() de todos los
		 * elementos graficos de la interfaz, partiendo (supongo) por el JFrame
		 * padre. Paint() (para el jFrame) esta redefinido arriba, segun lo que
		 * lei, este metodo no debería ser llamado directamente, pero bueh!, la
		 * cosa funciona.
		 */
		repaint();
	}

}
