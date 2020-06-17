boolean showVision = true;

import java.util.Arrays;
import java.util.List;
String [] names = {"john wick", "cj", "leutinant", "rambo", "jackie chan", "bones", "wazowski", "rickroll", "hitler", "derek", "brute", "trump", "kim", "vladimir", "trevor", "ryan", "wiranto", "noobgamer69", "obama", "floor gang"};
ArrayList<String> nameList = new ArrayList<String>(Arrays.asList(names));
class Militan{
	PVector p;
	boolean team; //defines militan team side
	float ang;
	int gamecode;
	String name = "";
	NeuralNetwork brain;
	Qtable qt;

	PVector pPrev;
	float score = 0;
	int reload = 0;
	int survival = 0;
	int movement = 0;
	int birthTime;

	//parameter
	final int size = 25;
	final int reloadTime = 1000;
	final float visionAng = PI*3/4; //Angle of vision limit, the total visible are is 2 times this
	final float visionDis = 300;
	Militan(PVector p,boolean team){
		brain = new NeuralNetwork(new int[]{4,6,4});
		qt = new Qtable();

		int i = int(random(nameList.size()));
		name = nameList.get(i);
		nameList.remove(i);

		this.p = p.copy();
		pPrev  = p.copy();
		this.team = team;
		birthTime = millis();
		ang = radians(random(360));

		if(team)
			gamecode = 1;
		else  
			gamecode = 2;
	}
	Militan(boolean team){
		brain = new NeuralNetwork(new int[]{4,6,4});
		qt = new Qtable();

		int i = int(random(nameList.size()));
		name = nameList.get(i);
		nameList.remove(i);

		this.p = new PVector(random(width), random(height));
		pPrev  = p.copy();
		this.team = team;
		birthTime = millis();
		ang = radians(random(360));

		if(team)
			gamecode = 1;
		else  
			gamecode = 2;
	}
	void reset(){
		//Resets militan states : position, prev-positon, birthTime, angular direction
		this.p = new PVector(random(width), random(height));
		pPrev  = p.copy();
		birthTime = millis();
		ang = radians(random(360));
		score = 0;
		reload = 0;
		survival = 0;
		movement = 0;
	}
	boolean hitBy(Bullet bul){
		if(bul.gamecode != this.gamecode)
			if(PVector.sub(bul.p,this.p).mag()<size/2+5/2) //check collision by distance (Bullet size is HARD CODED!)
			return true;

		return false;
	}
	float see(Militan other){  //returns enemy distance, returns 0 if no enemy around
		PVector dis = PVector.sub(other.p, this.p); //distance vector to other Militan
		PVector dir = PVector.fromAngle(ang);
		if(PVector.angleBetween(dis, dir) < visionAng && dis.mag() < visionDis)
			return dis.mag();
		return 0;
	}
	float see(Bullet bul){  //returns enemy distance, returns 0 if no enemy around
		PVector dis = PVector.sub(bul.p, this.p); //distance vector to other Militan
		PVector dir = PVector.fromAngle(ang);
		if(PVector.angleBetween(dis, dir) < visionAng && dis.mag() < visionDis)
			return dis.mag();
		return 0;
	}
	void scoreKill(){
		score += 1;
	}
	void scoreSurvival(){
		if(millis() > survival + 1000){
			score += 0.05;
			survival = millis();
		}
	}
	void scoreMovement(){
		if(millis() > survival + 4000){
			// score += 0.4*PVector.sub(pPrev, p).mag()/250;
			if(PVector.sub(pPrev, p).mag() > 200)
				qt.reward += 2;
			pPrev = p.copy();
			movement = millis();
		}
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
			rect(size/2, 0,size*8/7,size/8);
			rect(size/2, 0,size*3/4,size/5);
			fill(#535D4A);
			ellipse(0, 0, size, size);
		} else {
			fill(#746B5A);
			rect(size/2, 0,size*8/7,size/8);
			rect(size/2, 0,size*3/4,size/5);
			fill(#B29D69);
			ellipse(0, 0, size, size);
		}
		popMatrix();
	}
	void move(){
		p.add(PVector.random2D().setMag(2));
		limit(); // dont let it escape the window
	}
	void limit(){
		if(p.x + size/2 < 0)
			p.x = width + size/2;
		if(p.x - size/2 > width)
			p.x = -size/2;
		if(p.y - size/2 < 0)
			p.y = size/2;
		if(p.y + size/2 > height)
			p.y = height - size/2;
	}
	//ACTIONS :
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
	//The intellegence starts here
	void think(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
		float [] input = new float[4];
		Militan onTarget = this; //initialize it just to prevent error
		float onTargetDis = visionDis;
		boolean enemySpotted = false; 
		for(Militan mil : milArray){
			if(mil.team != this.team){ //pick closes enemy as target
				if(see(mil) !=  0){
					if(see(mil) < onTargetDis)
						onTarget = mil;
						onTargetDis = see(mil);
						enemySpotted = true;
				}
			}
		}

		/*		INPUT DICTIONAY
			input[0] ==> 1 if there is enemy spotted, 0 if otherwise
			input[1] ==> normalized enemy relative distance from this Militan
			input[2] ==> normalized enemy relative angle from this Militan
			input[3] == 1 if there is bullet spotted, 0 if otherwise

				OUTPUT DICTIONARY
			output[0] ==> positive number means go forward, negative number means stay
			output[1] ==> positive number means rotate clockwise, negative number means NOT rotating clokwise
			output[2] ==> positive number means rotate counter-clockwise, negative number means NOT rotating counter clockwise
			output[3] ==> positive number means shoot, negative number means NOT shoot
		*/

		if(enemySpotted){
			input[0] = 1;
			input[1] = see(onTarget) / visionDis;
			input[2] = degrees(onTarget.p.heading() - ang) / visionAng;
		} else {
			input[0] = 0;
			input[1] = 0;
			input[2] = 0;
		}
		input[3] = 0;
		outerLoop:
		for(Bullet b : bulArray){
			if(this.gamecode != b.gamecode){
				if(see(b) != 0){
					input[3] = 1;
					break outerLoop;
				}
			}
		}

		float [] output = brain.feedforward(input);
		if(output[0] > 0)
			p.add(PVector.fromAngle(ang).mult(4));
		if(output[1] > 0)
			ang += 0.1;
		if(output[2] > 0)
			ang -= 0.1;
		if(output[3] > 0)
			shoot(bulArray);
	}

	void takeAction(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
		int state1 = -1;	//-1 to catch error
		int state2 = -1;
		int state3;
		Militan onTarget = this; //initialize it just to prevent error
		float onTargetDis = visionDis;
		boolean enemySpotted = false; 
		for(Militan mil : milArray){
			if(mil.team != this.team){ //pick closes enemy as target
				if(see(mil) !=  0){
					if(see(mil) < onTargetDis)
						onTarget = mil;
						onTargetDis = see(mil);
						enemySpotted = true;
				}
			}
		}

		if(enemySpotted) {
			for (float i = -visionAng, state=1; i < visionAng; i += visionAng * 2 / (qt.tgtAng - 1)) {   //Dividing target angle to 5 different type of state for state1
				float angleOffset = degreeFloor(PVector.sub(onTarget.p, this.p).heading() - ang);
				if(angleOffset >= degrees(i) && angleOffset <= degrees(i + visionAng * 2 / 5))
					state1 = int(state);
				state++;
			}
			// if(state1 == -1){
			// 	println(degreeFloor(PVector.sub(onTarget.p, this.p).heading() - ang), see(onTarget), degrees(PVector.sub(onTarget.p, this.p).heading()), degreeFloor(ang), degrees(-visionAng), degrees(visionAng));
			// 	fill(random(255));
			// 	stroke(255);
			// 	ellipse(p.x, p.y, 45, 45);
			// 	ellipse(onTarget.p.x, onTarget.p.y, 45, 45);
			// 	PVector a = PVector.fromAngle(ang).mult(100).add(p);
			// 	line(p.x, p.y, a.x, a.y);
			// }
			for (float i = 0, state=1; i < visionDis; i += visionDis / (qt.tgtDis - 1)) {   //Dividing target distance to 5 different type of state for state2
				if(onTargetDis >= i && onTargetDis <= i + visionDis / 5)
					state2 = int(state);
				state++;
			}
		} else {
			state1 = 0;
			state2 = 0;
		}


		state3 = 0;
		outerLoop:
		for(Bullet b : bulArray){
			if(this.gamecode != b.gamecode){
				if(see(b) != 0){
					for (float i = -visionAng, state=1; i < visionAng; i += visionAng * 2 / (qt.bulAng - 1)) {   //Dividing target angle to 5 different type of state for state1
						float angleOffset = degreeFloor(PVector.sub(b.p, this.p).heading() - ang);
						if(angleOffset >= degrees(i) && angleOffset <= degrees(i + visionAng * 2 / 5))
							state3 = int(state);
						state++;
					}
					break outerLoop;
				}
			}
		}

		

		//take action
		int action = qt.step(state1, state2, state3);
		if(action == 0){

		}
		else if(action == 1){
			shoot(bulArray);
		}
		else if(action == 2){
			p.add(PVector.fromAngle(ang).mult(4));
		}
		else if(action == 3){
			p.add(PVector.fromAngle(ang).rotate(PI/2).mult(4));
		}
		else if(action == 4){
			p.add(PVector.fromAngle(ang).rotate(-PI/2).mult(4));
		}
		else if(action == 5){
			ang += 0.2;
		}
		else if(action == 6){
			ang -= 0.2;
		}
	}
	void evaluateAction(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
		int state1 = -1;	//-1 to catch error
		int state2 = -1;
		int state3;
		Militan onTarget = this; //initialize it just to prevent error
		float onTargetDis = visionDis;
		boolean enemySpotted = false; 
		for(Militan mil : milArray){
			if(mil.team != this.team){ //pick closes enemy as target
				if(see(mil) !=  0){
					if(see(mil) < onTargetDis)
						onTarget = mil;
						onTargetDis = see(mil);
						enemySpotted = true;
				}
			}
		}

		if(enemySpotted) {
			for (float i = -visionAng, state=1; i < visionAng; i += visionAng * 2 / 5) {   //Dividing target angle to 5 different type of state for state1
				float angleOffset = degreeFloor(PVector.sub(onTarget.p, this.p).heading() - ang);
				if(angleOffset >= degrees(i) && angleOffset <= degrees(i + visionAng * 2 / 5))
					state1 = int(state);
				state++;
			}
			// if(state1 == -1){
			// 	println(degreeFloor(PVector.sub(onTarget.p, this.p).heading() - ang), see(onTarget), degrees(PVector.sub(onTarget.p, this.p).heading()), degreeFloor(ang), degrees(-visionAng), degrees(visionAng));
			// 	fill(random(255));
			// 	stroke(255);
			// 	ellipse(p.x, p.y, 45, 45);
			// 	ellipse(onTarget.p.x, onTarget.p.y, 45, 45);
			// 	PVector a = PVector.fromAngle(ang).mult(100).add(p);
			// 	line(p.x, p.y, a.x, a.y);
			// }
			for (float i = 0, state=1; i < visionDis; i += visionDis / 5) {   //Dividing target distance to 5 different type of state for state2
				if(onTargetDis >= i && onTargetDis <= i + visionDis / 5)
					state2 = int(state);
				state++;
			}
		} else {
			state1 = 0;
			state2 = 0;
		}


		state3 = 0;
		outerLoop:
		for(Bullet b : bulArray){
			if(this.gamecode != b.gamecode){
				if(see(b) != 0){
					state3 = 1;
					break outerLoop;
				}
			}
		}
		qt.evaluate(state1, state2, state3);
	}
}

float degreeFloor(float rad){
	float degree = degrees(rad);
	while(degree > 360)
		degree -= 360;
	while(degree < -360)
		degree += 360;
	if(degree < -180)
		degree = 360 + degree;
	if(degree > 180)
		degree = -360 + degree;

	return degree;
}


//old action
// if(action == 0){
// 	ang += 0.1;
// 	shoot(bulArray);
// }
// else if(action == 1){
// 	ang += 0.1;
// }
// else if(action == 2){
// 	shoot(bulArray);
// }
// else if(action == 3){
	
// }
// else if(action == 4){
// 	ang -= 0.1;
// 	shoot(bulArray);
// }
// else if(action == 5){
// 	ang -= 0.1;
// }
// else if(action == 6){
// 	p.add(PVector.fromAngle(ang).mult(4));
// 	ang += 0.1;
// 	shoot(bulArray);
// }
// else if(action == 7){
// 	p.add(PVector.fromAngle(ang).mult(4));
// 	ang += 0.1;
// }
// else if(action == 8){
// 	p.add(PVector.fromAngle(ang).mult(4));
// 	shoot(bulArray);
// }
// else if(action == 9){
// 	p.add(PVector.fromAngle(ang).mult(4));
// }
// else if(action == 10){
// 	p.add(PVector.fromAngle(ang).mult(4));
// 	ang -= 0.1;
// 	shoot(bulArray);
// }
// else if(action == 11){
// 	p.add(PVector.fromAngle(ang).mult(4));
// 	ang -= 0.1;
// }