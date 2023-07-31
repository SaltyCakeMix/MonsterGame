import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel implements Runnable, KeyListener, MouseListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	static int windowWidth = 1600;
	static int windowHeight = 1000;
	private final int FPS = 60;
	private boolean running = false;
	private Graphics2D g = null;
	private BufferedImage image;
	private boolean resized = true;
	Thread thread;
	Random rand = new Random();

	//controls input
	ArrayList<Integer> keysHeld = new ArrayList<Integer>(); 
	ArrayList<Integer> mouseHeld = new ArrayList<Integer>();
	
	final int n = 200;
	ArrayList<ArrayList<Monster>> monsters = new ArrayList<ArrayList<Monster>>(0);
	static final int monsterSize = 32;
	BufferedImage[] sprites = new BufferedImage[3];

	public static void main(String[] args) {
		JFrame window = new JFrame("Rock Paper Scissors");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Main main = new Main();
		
		window.setContentPane(main);
		window.setAlwaysOnTop(false);
		window.setMinimumSize(new Dimension(100, 100));
		window.setResizable(true);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);               
	}
	
	public Main() {
		setPreferredSize(new Dimension(windowWidth, windowHeight));
		setFocusable(true);
		requestFocus(); 
		
		sprites[0] = loadImage("rock.png", monsterSize);
		sprites[1] = loadImage("paper.png", monsterSize);
		sprites[2] = loadImage("scissors.png", monsterSize);
	}

	public void addNotify() {
		super.addNotify();
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		};
		addKeyListener(this);
		addMouseListener(this);
		addComponentListener(this);
	};

	public void run() {
		running = true;

		long startTime;
		long totalTime = 0;
		long takenTime = 0;
		int frameCount = 0;
		long totalProcessTime = 0;
		long targetTime = 1000000000 / FPS;
		long waitDiff = 0;

		spawnMonsters();
		
		while(running) {
			startTime = System.nanoTime();

			gameUpdate();
			gameRender();
			gameDraw();

			// Calculating how long system needs to wait for
        	long processTime = System.nanoTime() - startTime;
        	long waitTime = targetTime - processTime + waitDiff;

			try {
				Thread.sleep(waitTime / 1000000);
			} catch(Exception e) {};

			takenTime = System.nanoTime() - startTime;
        	waitDiff = (long) (waitDiff*0.75 + (targetTime - takenTime)*0.25);
        	
        	frameCount++;
        	totalTime += takenTime;
        	totalProcessTime += processTime;
        	if(totalTime >= 1000000000) {
        		System.out.print(frameCount + " ");
        		System.out.println(1 - totalProcessTime / 1000000000f);
        		frameCount = 0;
        		totalTime = 0;
        		totalProcessTime = 0;
        	}
		};
	};

	public void gameUpdate() {
		int emptyLists = 0;
		for(int i = 0; i < 3; i++) {
			ArrayList<Monster> currentMonsters = monsters.get(i);
			if(currentMonsters.isEmpty()) {
				emptyLists++;
			} else {
				ArrayList<Monster> list = new ArrayList<Monster>(0);
				
				list.addAll(monsters.get(Functions.wrap(i - 1, 0, 3)));
				list.addAll(monsters.get(Functions.wrap(i + 1, 0, 3)));
				
				ArrayList<Monster> nextMonsters = monsters.get(Functions.wrap(i + 1, 0, 3));
				for(int j = 0; j < currentMonsters.size(); j++) {
					Monster monster = currentMonsters.get(j);
					if(monster.move(list)) {
						currentMonsters.remove(monster);
						nextMonsters.add(monster);
						j--;
					};
				};
			};
		};
		
		if(emptyLists >= 2) {
			// Slowly removes every element
			int totalCount = 0;
			for(int i = 0; i < 3; i++) {
				ArrayList<Monster> list = monsters.get(i);
				int size = list.size();
				totalCount += size;
				if(size > 0) { // Removes last element
					list.remove(size - 1);
				};
			};
			
			// Repopulates and starts again
			if(totalCount == 0) {
				spawnMonsters();
			}; 
		};
	};

	public void gameRender() {
		if (resized) {
			if(g != null) {
				g.dispose();
			};
			Dimension d = this.getSize();
			windowWidth = d.width;
			windowHeight = d.height;

			image = new BufferedImage(windowWidth, windowHeight,
					BufferedImage.TYPE_INT_RGB);
			g = image.createGraphics();
			resized = false;
		};
		
		//draw background color
		g.setColor(new Color(255, 255, 255));
		g.fillRect(
			0,
			0,
			windowWidth,
			windowHeight
			);
		
		for(ArrayList<Monster> list : monsters) {
			for(Monster monster : list) {
				monster.draw(g);
			};
		};
	}

	private void gameDraw() {
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	};

	private void spawnMonsters() {
		monsters.clear();
		for(int i = 0; i < 3; i++) {
			ArrayList<Monster> list = new ArrayList<Monster>(0);
			
			for(int j = 0; j < n; j++) {
				Monster monster = new Monster(rand.nextInt(windowWidth - monsterSize), rand.nextInt(windowHeight - monsterSize), i, sprites[i]);
				list.add(monster);
			};
			
			monsters.add(list);
		};
	};
	
	public void keyPressed(KeyEvent e) { //stores which keys are being held when pressed
		int key = e.getKeyCode();

		if(!keysHeld.contains(key)) {
			keysHeld.add(key);
		};
	};

	public void keyReleased(KeyEvent e) { //removes which keys are being held when released
		int key = e.getKeyCode();

		keysHeld.remove(Integer.valueOf(key));
	};

	public void mousePressed(MouseEvent e) { //same thing for mouse buttons
		int key = e.getButton();

		if(!mouseHeld.contains(key)) {
			mouseHeld.add(key);
		};
	}	

	public void mouseReleased(MouseEvent e) {
		int key = e.getButton();

		mouseHeld.remove(Integer.valueOf(key));
	}
	
	public void componentResized(ComponentEvent componentEvent) {
		resized = true;
	};
	
	public void keyTyped(KeyEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}

	BufferedImage loadImage(String input, int size) {
		try {
			BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(ImageIO.read(new File("images/" + input)), 0, 0, size, size, null);
			g.dispose();
			return image;
		} catch (IOException exc) {
			System.out.println("Error opening image file: " + exc.getMessage());
		};
		return new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
	}; 
};