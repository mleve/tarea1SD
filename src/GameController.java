
public class GameController{

	boolean ingame = false;
	boolean dying = false;

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
	
	final int validspeeds[] = {1, 2, 3, 4, 6, 8};
	final int maxspeed = 6;

	int currentspeed = 3;
	short[] screendata;

	public GameController(){
		// Inicializo las variables del juego:
		ghostx = new int[maxghosts];
		ghostdx = new int[maxghosts];
		ghosty = new int[maxghosts];
		ghostdy = new int[maxghosts];
		ghostspeed = new int[maxghosts];
		dx = new int[4];
		dy = new int[4];
	}

	public void GameInit(){
		pacsleft = 3;
		score = 0;
		LevelInit();
		nrofghosts = 6;
		currentspeed = 3;
	}

	/*
	 * Interface: gameInit: Inicia un nuevo juego moveGhosts: actualiza las
	 * posiciones de los fantasmas (no los dibuja) movePacMan: actualiza la
	 * posicion del pacman checkMaze: Chequea si el jugador termino el nivel
	 */

	public void CheckMaze(){
		short i = 0;
		boolean finished = true;

		while(i<nrofblocks*nrofblocks&&finished){
			if((screendata[i]&48)!=0)
				finished = false;
			i++;
		}

		if(finished){
			score += 50;

			if(nrofghosts<maxghosts)
				nrofghosts++;
			if(currentspeed<maxspeed)
				currentspeed++;
			LevelInit();
		}
	}

	public void Death(){

		pacsleft--;
		if(pacsleft==0)
			ingame = false;
		LevelContinue();
	}

	public void moveGhosts(){
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

			if(pacmanx>(ghostx[i]-12)&&pacmanx<(ghostx[i]+12)&&pacmany>(ghosty[i]-12)&&pacmany<(ghosty[i]+12)&&ingame){

				dying = true;
				deathcounter = 64;

			}
		}
	}

	public void movePacMan(){
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
		pacmanx = pacmanx+pacmanspeed*pacmandx;
		pacmany = pacmany+pacmanspeed*pacmandy;
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

}
