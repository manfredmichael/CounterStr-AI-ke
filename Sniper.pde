class Sniper extends Militan{
	Sniper(boolean team){
		super(team);
		reloadTime = 5000;
		visionAng = PI/7; //Angle of vision limit, the total visible are is 2 times this
		visionDis = 600;
		println(super.visionDis);
	}
	Sniper(PVector p, boolean team){
		super(p, team);
		reloadTime = 5000;
		visionAng = PI/7; //Angle of vision limit, the total visible are is 2 times this
		visionDis = 600;
	}
	void show(){
		//Draw depending on militan's team
		fill(255);
		textAlign(CENTER, CENTER);
		textSize(size / 2);
		text(name, p.x, p.y - 30);

		pushMatrix();
		rectMode(CENTER);
		stroke(0);
		translate(p.x, p.y);
		rotate(ang);
		if(showVision){
				noFill();
				arc(0, 0, visionDis * 2, visionDis * 2, -visionAng, visionAng, PIE); //to show visible area
		}
		if(team){
			fill(#555346);
			rect(size/2, 0,size*2,size/10);
			rect(size/2 + size, 0,size/3,size/7);
			rect(size/2, 0,size*5/7,size/8);
			rect(size/2, 0,size*2/4,size/5);
			fill(#535D4A);
			ellipse(0, 0, size, size);
		} else {
			fill(#746B5A);
			rect(size/2, 0,size*2,size/10);
			rect(size/2 + size, 0,size/3,size/7);
			rect(size/2, 0,size*5/7,size/8);
			rect(size/2, 0,size*2/4,size/5);
			fill(#B29D69);
			ellipse(0, 0, size, size);
		}
		popMatrix();
	}
}