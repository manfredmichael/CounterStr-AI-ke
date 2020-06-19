class Gunman extends Militan{
	boolean shooting = false;
	Gunman(boolean team){
		super(team);
		reloadTime = 400;
		visionAng = PI*3/5; //Angle of vision limit, the total visible are is 2 times this
		visionDis = 180;
		println(super.visionDis);
	}
	Gunman(PVector p, boolean team){
		super(p, team);
		reloadTime = 400;
		visionAng = PI*3/5; //Angle of vision limit, the total visible are is 2 times this
		visionDis = 180;
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
			rect(size/2, 0,size*12/8,size/5);
			rect(size/2, 0,size*1/4,size/4);
			fill(#535D4A);
			ellipse(0, 0, size, size);
		} else {
			fill(#746B5A);
			rect(size/2, 0,size*12/8,size/5);
			rect(size/2, 0,size*1/4,size/4);
			fill(#B29D69);
			ellipse(0, 0, size, size);
		}
		popMatrix();
	}
	void shoot(ArrayList<Bullet> bulArray){
		if(millis() > reload + reloadTime){
			//add bullet at the tip of the gun
			PVector shootDir = PVector.fromAngle(ang).setMag(size*8/7); //size*8/7 is the tip of the gun bruh, dont get confused :p
			bulArray.add(new Bullet(p.copy().add(shootDir), PVector.fromAngle(ang), gamecode, this)); //giving gun tip position, gun direction, gun gamecode(team side), and the shooter object for scoring
			// score -= 0.1; //do not tolerate random shooter >:(
			qt.reward -= 1;
			reload = millis();
		}
	}
}

