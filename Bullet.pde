class Bullet{
	Militan shooter;
	PVector p;
	PVector dir;
	int gamecode;
	final float speed = 18;
	Bullet(PVector p, PVector dir,int gamecode, Militan shooter){
		this.shooter = shooter;
		this.gamecode = gamecode;
		this.p = p.copy();
		this.dir = dir.normalize().copy(); //create unit vector out of direction
	}
	void move(){
		p.add(dir.copy().mult(speed)); //move position according to direction
	}
	void show(){
		noStroke();
		fill(255);
		ellipse(p.x, p.y, 5, 5);  //!!!Bullet Size is HARD CODED!!!
	}
	boolean isOutside(){
		if(p.x < -50)
			return true;
		else if (p.x > width + 50)
			return true;
		else if (p.y < -50)
			return true;
		else if (p.y > height + 50)
			return true;

		return false;
	}
}