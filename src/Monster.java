import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Monster {
	public float x, y;
	int type;
	BufferedImage sprite;
	Random rand = new Random();
	
	final int moveTickTotal = 5;
	final int vel = 3;
	final int range = 200;
	
	float angle = 0;
	int moveTick = 0;
	float xVel = 0;
	float yVel = 0;
	
	public Monster(int x, int y, int type, BufferedImage sprite) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.sprite = sprite;
	};
	
	public boolean move(ArrayList<Monster> others) {
		// Creates a shallow copy and sorts monsters based on distance
		if(others.isEmpty()) {
			randomMove();
			return false;
		} else {
			ArrayList<Monster> reference = new ArrayList<Monster>(0);
			for(Monster m : others) {
				if(
					m.x > x - range &&
					m.x < x + range &&
					m.y > y - range &&
					m.y < y + range
				) {
					reference.add(m);
				};
			};
			
			if(reference.isEmpty()) {
				randomMove();
				return false;
			};
			
			reference.sort(Comparator.comparing(monster -> Functions.distance(x, y, monster.x, monster.y)));
			if(moveTick == 0) {
				Monster closest = reference.get(0);
				angle = (float) Functions.pointPointAngle(x, y, closest.x, closest.y) + rand.nextFloat(1.5f) - 0.75f;
				if(closest.type == Functions.wrap(type + 1, 0, 3)) {
					angle += Math.PI;
				};
				
				xVel = (float) (Math.cos(angle) * vel);
				yVel = (float) (Math.sin(angle) * vel);
				moveTick = moveTickTotal + rand.nextInt(3);
			} else {
				moveTick--;
			};
			
			x = Functions.clamp(x + xVel, 0, Main.windowWidth - Main.monsterSize);
			y = Functions.clamp(y + yVel, 0, Main.windowHeight - Main.monsterSize);
			return collide(reference);
		}
	};
	
	private void randomMove() {
		if(moveTick == 0) {
			angle += rand.nextFloat(1.5f) - 0.75f;
			xVel = (float) (Math.cos(angle) * vel);
			yVel = (float) (Math.sin(angle) * vel);
			moveTick = moveTickTotal + rand.nextInt(3);
		} else {
			moveTick--;
		};
		
		x = Functions.clamp(x + xVel, 0, Main.windowWidth - Main.monsterSize);
		y = Functions.clamp(y + yVel, 0, Main.windowHeight - Main.monsterSize);
	};
	
	private boolean collide(ArrayList<Monster> reference) {
		for(Monster monster : reference) {
			if(monster.type == Functions.wrap(type + 1, 0, 3)) {
				if(Functions.rectCollide(
					x,
					y,
					x + Main.monsterSize,
					y + Main.monsterSize,
					monster.x,
					monster.y,
					monster.x + Main.monsterSize,
					monster.y + Main.monsterSize
				)) {
					type = monster.type;
					sprite = monster.sprite;
					return true;
				};
				return false;
			};
		};
		return false;
	};
	
	public void draw(Graphics2D g) {
		g.drawImage(sprite, (int)x, (int)y, null);
	};
}
