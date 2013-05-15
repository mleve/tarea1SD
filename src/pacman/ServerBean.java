package pacman;

import java.io.Serializable;

public class ServerBean implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4970521718443349999L;
	private int maxPlayers, playerCount;
	private int[][] playersInfo;
	private boolean started;
	private short[] screendata;
	private int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
	private int[] dx, dy;	

	public int getMaxPlayers() {
		return maxPlayers;
	}


	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}


	public int getPlayerCount() {
		return playerCount;
	}


	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}


	public int[][] getPlayersInfo() {
		return playersInfo;
	}


	public void setPlayersInfo(int[][] playersInfo) {
		this.playersInfo = playersInfo;
	}


	public boolean isStarted() {
		return started;
	}


	public void setStarted(boolean started) {
		this.started = started;
	}


	public short[] getScreendata() {
		return screendata;
	}


	public void setScreendata(short[] screendata) {
		this.screendata = screendata;
	}


	public int[] getGhostx() {
		return ghostx;
	}


	public void setGhostx(int[] ghostx) {
		this.ghostx = ghostx;
	}


	public int[] getGhosty() {
		return ghosty;
	}


	public void setGhosty(int[] ghosty) {
		this.ghosty = ghosty;
	}


	public int[] getGhostdx() {
		return ghostdx;
	}


	public void setGhostdx(int[] ghostdx) {
		this.ghostdx = ghostdx;
	}


	public int[] getGhostdy() {
		return ghostdy;
	}


	public void setGhostdy(int[] ghostdy) {
		this.ghostdy = ghostdy;
	}


	public int[] getGhostspeed() {
		return ghostspeed;
	}


	public void setGhostspeed(int[] ghostspeed) {
		this.ghostspeed = ghostspeed;
	}


	public int[] getDx() {
		return dx;
	}


	public void setDx(int[] dx) {
		this.dx = dx;
	}


	public int[] getDy() {
		return dy;
	}


	public void setDy(int[] dy) {
		this.dy = dy;
	}



	
	public ServerBean(){}
	

	
	
	
	

}
