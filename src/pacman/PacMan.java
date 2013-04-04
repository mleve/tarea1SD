package pacman;

import javax.swing.JFrame;

import pacman.Board;

public class PacMan extends JFrame{

	public PacMan(String server){
		add(new Board(server));
		setTitle("Pacman");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(380, 420);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args){
		String dirServer="localhost";
		if(args.length==1){
			//usage: java.PacMan dirIp
			dirServer=args[0];
		}
		new PacMan(dirServer);
	}
}
