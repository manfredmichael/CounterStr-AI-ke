import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Arrays; 
import java.util.List; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class counterstrAIke extends PApplet {

ArrayList<Militan> mils = new ArrayList<Militan>();
ArrayList<Bullet> bullets = new ArrayList<Bullet>();

float t;

EventList eList = new EventList();

public void setup(){
	t=millis();
	
	frameRate(30);
	for (int i = 0; i < 1; i++){
		mils.add(new Sniper(false));
		mils.add(new Sniper(true));
		mils.add(new Militan(false));
		mils.add(new Militan(true));
	}
	// println(degrees(new PVector(1,1).heading() - new PVector(1,-1).heading()));
	// println(degrees(PVector.angleBetween(new PVector(1,0),new PVector(1,1))));
}
public void draw(){
	background(51);

	for(Militan mil : mils){
		mil.takeAction(mils, bullets);
		mil.limit();
	}

	eList.render();

	//update position of bullets and render them
	ArrayList<Bullet> newBullets = new ArrayList<Bullet>(bullets);
	for(Bullet bul : bullets){
		bul.move();
		bul.show();
		for (int i = 0; i < mils.size(); i++) {
			if(mils.get(i).hitBy(bul)){ //die by bullet or age
				if(mils.get(i).team)
					println("In memoriam, a Dark Green Soldier,",mils.get(i).name,"from Generation",mils.get(i).qt.episodeCount,"has been shot. He comitted",mils.get(i).score,"confirmed kills");
				else
					println("In memoriam, a Light Brown Soldier,",mils.get(i).name,"from Generation",mils.get(i).qt.episodeCount,"has been shot. He comitted",mils.get(i).score,"confirmed kills");
				mils.get(i).reset();
				mils.get(i).qt.reward += -10;
				mils.get(i).qt.done = true;
				// mils.set(i, regenerate(mils.get(i), mils));//regenerate mil here
				bul.shooter.scoreKill(); //give score to shooter here
				bul.shooter.qt.reward += 20;
				bul.shooter.qt.done = true;

				newBullets.remove(bul); //remove on contact bullet

				eList.addKill(bul.shooter, mils.get(i));
			}
		}
	}
	bullets = new ArrayList<Bullet>(newBullets);

	for(Militan mil : mils){
		mil.evaluateAction(mils, bullets); //evaluate here
		mil.show();
	}

	for (int i = bullets.size()-1; i >= 0; i--) {
		if(bullets.get(i).isOutside())
			bullets.remove(i);
	}
}

public void mousePressed(){
	showVision = !showVision;
}
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
	public void move(){
		p.add(dir.copy().mult(speed)); //move position according to direction
	}
	public void show(){
		noStroke();
		fill(255);
		ellipse(p.x, p.y, 5, 5);  //!!!Bullet Size is HARD CODED!!!
	}
	public boolean isOutside(){
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
class Qtable{
	float epsilon             = 0.7f;
	final float START_EPSILON = epsilon;
	final float MIN_EPSILON   = 0.05f;
	float learning_rate       = 0.4f;
	final float START_LR      = learning_rate;
	final float DECAY         = 2000;
	final float DISCOUNT      = 0.85f;
	int episodeCount          = 0;

	final int tgtAng = 8; //Target angle +1 for no target
	final int tgtDis = 6; //Target distance +1 for no target
	final int bulAng = 6; //Bullet angle +1 for no approaching bullet
	final int action = 7; //0: idle, 1: shoot, 2: move forward, 3: move to right, 4: move to left, 5: rotate clockwise, 6: rotate counter-clockwise

	//PREVIOUS STEP RECORD FOR EVALUATION
	float reward = 0;
	int state1prev;
	int state2prev;
	int state3prev;
	int actionPrev;
	boolean done = false;

	float [][][][] table = new float[tgtAng][tgtDis][bulAng][action];
	Qtable(){
		for (int i = 0; i < tgtAng; i++) {
			for (int j = 0; j < tgtDis; j++) {
				for (int k = 0; k < bulAng; k++) {
					for (int l = 0; l < action; l++) {
						table[i][j][k][l] = random(-0.01f, 0.01f);
					}
				}
			}
		}
	}

	public int step(int state1, int state2, int state3){
		reward = -0.013f; //reset reward (i want it to get -2 reward every 150 frames which is 5 seconds. -2/150 = -0.013)
		int action;
		if(random(1) > epsilon){
			// argMax(table[1][1][state3]);
			// 	argMax(table[1][state2][1]);
			// 		argMax(table[state1][1][1]);
			try {
				action = argMax(table[state1][state2][state3]);
			} catch(Exception e) {
				action = floor(random(this.action));
			}
		}
		else 
			action = floor(random(this.action));

		state1prev = state1;
		state2prev = state2;
		state3prev = state3;
		actionPrev = action;

		return action;
	}

	public void evaluate(int state1, int state2, int state3){
		try{
			if(!done)
				table[state1prev][state2prev][state3prev][actionPrev] = (1 - learning_rate) * table[state1prev][state2prev][state3prev][actionPrev] + (learning_rate * (reward + DISCOUNT * max(table[state1][state2][state3])));
			else {
				table[state1prev][state2prev][state3prev][actionPrev] = (1 - learning_rate) * table[state1prev][state2prev][state3prev][actionPrev] + (learning_rate * reward);
				episodeCount++;
				done = false;
			}
		} catch(Exception e){
			println("something is wrong in input :(");
		}
		if(epsilon > MIN_EPSILON){
			epsilon -= START_EPSILON / DECAY;
		}
		if(learning_rate > START_LR / DECAY){
			learning_rate -= START_LR / DECAY;
		}
	}
}

public int argMax(float [] num){
	float max = max(num);
	for (int i = 0; i < num.length; i++) {
		if(num[i] == max)
			return i;
	}

	return 5;
}

// if(player.enemyX <= 0 || player.enemyX > 4)
// 			println("enemyX",player.enemyX);
// 		if(player.enemyY <= 0 || player.enemyY > 4)
// 			println("eenemyY",player.enemyY);
// 		if(x <= 0 || x > 8)
// 			println("x",x);
// 		if(y <= 0 || y > 8)
// 			println("y",y);
// 		if(action < 0 || action > 3)
// 			println("action", action);
// 		if(player.enemyX - player.x + w - 1 < 0 || player.enemyX - player.x + w - 1 > 8)
// 			println("a",player.enemyX - player.x + w - 1);
// 		if(player.enemyY - player.y + h - 1 < 0 || player.enemyY - player.y + h - 1 > 8)
// 			println(player.enemyY,player.y,"b",player.enemyY - player.y + h - 1);
class EventList{
	ArrayList<String> events=new ArrayList<String>();
	float time = 0;
	float ytop = 15;  //not constant because list will move vertically
	final float xtop = 15;
	final float ytopDefault = ytop;
	final float timer = 3000;
	EventList(){
	}
	public void render(){
		fill(255,100);
		textSize(14);
		textAlign(LEFT);
		for (int i = 0; i < events.size(); i++) {
			text(events.get(i), xtop, ytop + i * ytopDefault);
		}
		if(events.size() > 0){
			if(millis() > time + timer && events.size() > 0){
				ytop-=2;
				if(ytop <= 0){
					events.remove(0);
					ytop = ytopDefault;
					time = millis();
				}
			}
		} else {
			time = millis();
		}

		if(events.size() >= 10)
			time -= timer;
	}
	public void addMessage(String message){
		events.add(message);
	}
	public void addKill(Militan shooter, Militan dead){
		String message = shooter.name + " killed " + dead.name;
		if(shooter.score % 10 == 0){
			message = message + " for his " + str(shooter.score) + " kill streak!";
		}
		events.add(message);
	}
}
class Matrix {
  float [][] array;
  int row;
  int column;

  Matrix(int row, int column) {
    array=new float[row][column];
    this.row=row;
    this.column=column;
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=round(random(-1, 1));
      }
    }
  }

  Matrix(Matrix other) {
    row=other.row;
    column=other.column;
    array=new float[row][column];
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=other.array[i][j];
      }
    }
  }

  Matrix(float [] input) {
    row=1;
    column=input.length;
    array=new float[row][column];
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=input[j];
      }
    }
  }

  public float get(int i, int j) {
    return array[i][j];
  }

  public float [][] getArray() {
    return array;
  }
  
  public void set(float n){
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=n;
      }
    }
  }

  public Matrix copy() {
    Matrix result=new Matrix(row, column);
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        result.array[i][j]=array[i][j];
      }
    }
    return result;
  }

  public void printMatrix() {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        print(array[i][j]+" ");
      }
      println();
    }
    println();
  }

  public void T() {
    Matrix result=new Matrix(column, row);
    for (int i=0; i<column; i++) {
      for (int j=0; j<row; j++) {
        result.array[i][j]=array[j][i];
      }
    }
    row=result.row;
    column=result.column;
    array=result.array;
  }

  public void add(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]+n;
      }
    }
  } 

  public void mult(float n) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        array[i][j]=array[i][j]*n;
      }
    }
  }

  public void mutate(float mutationRate) {
    for (int i=0; i<row; i++) {
      for (int j=0; j<column; j++) {
        float random=random(1);
        if (random<=mutationRate) {
          array[i][j]+=random(-0.5f, 0.5f);
        }
      }
    }
  }
}

class MatrixMath {
  public Matrix mult(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, b.column);

    if (a.column==b.row) {
      for (int i=0; i<a.row; i++) {
        for (int j=0; j<b.column; j++) {
          result.array[i][j]=0;
          for (int k=0; k<b.row; k++) {
            result.array[i][j]+=a.array[i][k]*b.array[k][j];
          }
        }
      }
    } else {
      println("=========================================");
      println("this matrix column doesnt match other row");
      println("=========================================");
    }

    return result;
  }

  public Matrix add(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, a.column); //not done error mismatch row cathcer
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]+b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix sub(Matrix a, Matrix b) {
    Matrix result=new Matrix(a.row, a.column);
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]-b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix getT(Matrix a) {
    Matrix result=new Matrix(a.column, a.row);
    for (int i=0; i<result.row; i++) {
      for (int j=0; j<result.column; j++) {
        result.array[i][j]=a.array[j][i];
      }
    }
    return result;
  }
  
  public Matrix hadamartProduct(Matrix a,Matrix b){
  Matrix result=new Matrix(a.row, a.column); //not done error mismatch row cathcer
    if ((a.row==b.row)&&(a.column==b.column)) {
      for (int i=0; i<result.row; i++) {
        for (int j=0; j<result.column; j++) {
          result.array[i][j]=a.array[i][j]*b.array[i][j];
        }
      }
    } else {
      println("=========================================");
      println("this matrix column/row doesnt match other column/row");
      println("=========================================");
    }
    return result;
  }

  public Matrix sigmoid(Matrix a) {
    Matrix result=new Matrix(a);
    for (int i=0; i<a.row; i++) {
      for (int j=0; j<a.column; j++) {
        float x=result.array[i][j];
        result.array[i][j]=1/(1+exp(-1*x));

        Double d = new Double(result.array[i][j]);
        if (d.isNaN())
          print("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
      }
    }
    return result;
  }

  public Matrix softmax(Matrix a){
    Matrix result = new Matrix(a);
    double sum    = 0;

    for (int i=0; i<a.row; i++) {
      for (int j=0; j<a.column; j++) {
        float x = result.array[i][j];

        Double d = new Double(x);
        if (d.isNaN())
          x = 0;
        if(d.isInfinite())
          x = 1000;

        sum += exp(x);
        // if (d.isNaN())
        //   print("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
        // if(exp(x) == 0)
        //   print("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
      }
    }   

    for (int i=0; i<a.row; i++) {
      for (int j=0; j<a.column; j++) {
        float x = result.array[i][j];

        Double d = new Double(x);
         if (d.isNaN())
          x = 0;
        if(d.isInfinite())
          x = 1000;

        result.array[i][j] = (float) (exp(x) / sum);
      }
    }

    println(sum);
    if(sum <= 0)
       print("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    return result;
  }
}

MatrixMath Matrix=new MatrixMath();
boolean showVision = true;



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
	int reloadTime = 1000;
	float visionAng = PI*3/4; //Angle of vision limit, the total visible are is 2 times this
	float visionDis = 300;
	Militan(PVector p,boolean team){
		brain = new NeuralNetwork(new int[]{4,6,4});
		qt = new Qtable();

		int i = PApplet.parseInt(random(nameList.size()));
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

		int i = PApplet.parseInt(random(nameList.size()));
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
	public void reset(){
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
	public boolean hitBy(Bullet bul){
		if(bul.gamecode != this.gamecode)
			if(PVector.sub(bul.p,this.p).mag()<size/2+5/2) //check collision by distance (Bullet size is HARD CODED!)
			return true;

		return false;
	}
	public float see(Militan other){  //returns enemy distance, returns 0 if no enemy around
		PVector dis = PVector.sub(other.p, this.p); //distance vector to other Militan
		PVector dir = PVector.fromAngle(ang);
		if(PVector.angleBetween(dis, dir) < visionAng && dis.mag() < visionDis)
			return dis.mag();
		return 0;
	}
	public float see(Bullet bul){  //returns enemy distance, returns 0 if no enemy around
		PVector dis = PVector.sub(bul.p, this.p); //distance vector to other Militan
		PVector dir = PVector.fromAngle(ang);
		if(PVector.angleBetween(dis, dir) < visionAng && dis.mag() < visionDis)
			return dis.mag();
		return 0;
	}
	public void scoreKill(){
		score += 1;
	}
	public void scoreSurvival(){
		if(millis() > survival + 1000){
			score += 0.05f;
			survival = millis();
		}
	}
	public void scoreMovement(){
		if(millis() > survival + 4000){
			// score += 0.4*PVector.sub(pPrev, p).mag()/250;
			if(PVector.sub(pPrev, p).mag() > 200)
				qt.reward += 2;
			pPrev = p.copy();
			movement = millis();
		}
	}
	public void show(){
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
			fill(0xff555346);
			rect(size/2, 0,size*8/7,size/8);
			rect(size/2, 0,size*3/4,size/5);
			fill(0xff535D4A);
			ellipse(0, 0, size, size);
		} else {
			fill(0xff746B5A);
			rect(size/2, 0,size*8/7,size/8);
			rect(size/2, 0,size*3/4,size/5);
			fill(0xffB29D69);
			ellipse(0, 0, size, size);
		}
		popMatrix();
	}
	public void move(){
		p.add(PVector.random2D().setMag(2));
		limit(); // dont let it escape the window
	}
	public void limit(){
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
	public void shoot(ArrayList<Bullet> bulArray){
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
	public void think(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
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
			ang += 0.1f;
		if(output[2] > 0)
			ang -= 0.1f;
		if(output[3] > 0)
			shoot(bulArray);
	}

	public void takeAction(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
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
					state1 = PApplet.parseInt(state);
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
					state2 = PApplet.parseInt(state);
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
							state3 = PApplet.parseInt(state);
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
			ang += 0.2f;
		}
		else if(action == 6){
			ang -= 0.2f;
		}
	}
	public void evaluateAction(ArrayList<Militan> milArray, ArrayList<Bullet> bulArray){
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
					state1 = PApplet.parseInt(state);
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
					state2 = PApplet.parseInt(state);
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

public float degreeFloor(float rad){
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
class NeuralNetwork {
  ArrayList<Matrix> weights=new ArrayList<Matrix>();
  ArrayList<Matrix> biases=new ArrayList<Matrix>();
  ArrayList<Matrix> perceptrons=new ArrayList<Matrix>();
  NeuralNetwork(int [] layers) {
    for (int i=0; i<layers.length-1; i++) {
      int row=layers[i+1];
      int column=layers[i];
      weights.add(new Matrix(row, column));

      row=layers[i+1];
      column=1;
      biases.add(new Matrix(row, column));
    }
  }

  public NeuralNetwork copy() {
    int [] parameter = {0};
    NeuralNetwork clone = new NeuralNetwork(parameter);
    clone.weights.clear();
    clone.biases.clear();
    for (int i = 0; i < weights.size(); i++) {
      clone.weights.add(weights.get(i).copy());
      clone.biases.add(biases.get(i).copy());
    }
    return clone;
  }

  public void mutate(float mutationRate) {
    for (Matrix weight : weights)
      weight.mutate(mutationRate);
    for (Matrix bias : biases)
      bias.mutate(mutationRate);
  }

  public float [] feedforward(float [] input) {
    perceptrons.clear();
    Matrix output=new Matrix(input);
    output.T();
    perceptrons.add(output.copy());
    for (int i=0; i<weights.size(); i++) {
      output=Matrix.mult(weights.get(i), output);

      output=Matrix.add(output, biases.get(i));

      if (i < weights.size() - 1) {
        output=Matrix.sigmoid(output);
        perceptrons.add(output.copy());
      }
    }

    // output = Matrix.softmax(output);
    perceptrons.add(output.copy());
    output.T();
    return output.array[0];
  }

  public void train(float [] inputArray, float [] targetArray) {
    float learningRate=0.5f;
    ArrayList<Matrix> neurons=new ArrayList<Matrix>();
    ArrayList<Matrix> errors=new ArrayList<Matrix>();

    Matrix target=new Matrix(targetArray);
    target.T();
    Matrix output=new Matrix(inputArray);
    output.T();
    neurons.add(output.copy());

    for (int i=0; i<weights.size(); i++) {
      output=Matrix.mult(weights.get(i), output);
      output=Matrix.add(output, biases.get(i));

      if (i < weights.size() - 1) {
        output=Matrix.sigmoid(output);
        neurons.add(output.copy());
      }
    }

    // output = Matrix.softmax(output);
    neurons.add(output.copy());
    errors.add(Matrix.sub(target, output));

    for (int i=weights.size()-1; i>0; i--) {
      Matrix transposedWeight=Matrix.getT(weights.get(i));
      for (int j=0; j<transposedWeight.column; j++) {
        float sumOfColumn = 0;
        for (int k=0; k<transposedWeight.row; k++) {
          sumOfColumn += abs(transposedWeight.array[k][j]);
        }
        for (int k=0; k<transposedWeight.row; k++) {
          if (sumOfColumn>=1)
            transposedWeight.array[k][j]*=(1/sumOfColumn);
        }
      }

      Matrix error=Matrix.mult(transposedWeight, errors.get(0));
      errors.add(0, error);
    }

    for (int i=weights.size()-1; i>=0; i--) {
      Matrix gradient = errors.get(i).copy();

      if (i < weights.size() - 1) {
        Matrix derivatedSigmoid=neurons.get(i+1).copy();
        Matrix inverseMatrix=derivatedSigmoid.copy();
        inverseMatrix.set(1);
        inverseMatrix=Matrix.sub(inverseMatrix, derivatedSigmoid);
        derivatedSigmoid=Matrix.hadamartProduct(derivatedSigmoid, inverseMatrix);
        gradient=Matrix.hadamartProduct(errors.get(i), derivatedSigmoid);
      }

      Matrix slope=Matrix.mult(gradient, Matrix.getT(neurons.get(i)));
      slope.mult(learningRate);

      Matrix weight=weights.get(i).copy();
      weights.remove(i);
      weights.add(i, Matrix.add(weight, slope));

      Matrix bias=biases.get(i).copy();
      biases.remove(i);
      biases.add(i, Matrix.add(bias, gradient));
    }
  }
}

// float scroll = 0;

// class NetBoard {
//   PGraphics board;
//   int margin = 30;
//   float boardX       = width / 4;
//   int boardSize      = width / 2 + margin;
//   float inputX       = margin / 2;
//   float outputX      = boardSize - margin / 2;
//   float size         = outputX - inputX;
//   float resolution   = size / (layers.length - 1);
//   NetBoard() {
//     board = createGraphics(boardSize, height);
//   }
//   void visualizeNN() {
//     image(board, boardX, 0);
//     board.beginDraw();
//     board.background(255);
//     for ( int i = 0; i < nn.perceptrons.size(); i++) {
//       for ( int j = 0; j < layers[i]; j++) {
//         float x      = inputX + resolution * i;
//         float y      = 50 + 40 * (j * 2  + 1 - layers[i]) / 2 + scroll;
//         float value  = nn.perceptrons.get(i).get(j, 0) ;
//         if (i < nn.weights.size()) {
//           for (int k = 0; k < layers[i + 1]; k++) {
//             float xo = inputX + resolution * (i + 1);
//             float yo = 50 + 40 * (k * 2  + 1 - layers[i + 1]) / 2 + scroll;
//             float w  = nn.weights.get(i).get(k, j);
//             if (w>0)
//               board.stroke(0, 255, 0, 128 * abs(w));
//             else
//               board.stroke(255, 0, 0, 128 * abs(w));
//             board.line(x, y, xo, yo);
//           }
//         }
//         board.stroke(0);
//         board.fill(100 + 155 * value);
//         board.ellipse(x, y, 30, 30);
//         board.textAlign(CENTER);
//         if (value > 0.5)
//           board.fill(0);
//         else
//           board.fill(255);
//         board.text(nf(value, 1, 2), x + 1, y + 5);
//       }
//     }
//     board.endDraw();
//     if (mousePressed) {
//       scroll += mouseY- pmouseY;
//     }
//   }
// }

public Militan regenerate(Militan deadMil, ArrayList<Militan> milArray){
	final float EPSILON = 0.25f; //chance of NOT picking the actual best 
	final float mutationRate = 0.01f;

	Militan child = new Militan(deadMil.team);
	float [] milArrayScore = new float[milArray.size()];
	for (int i = 0; i < milArrayScore.length; i++) {
		milArrayScore[i] = milArray.get(i).score;
	}

	if(random(1) < EPSILON)
		child.brain = milArray.get(getIndexOfLargest(milArrayScore, PApplet.parseInt(random(2,3)))).brain.copy();
	else
		child.brain = milArray.get(getIndexOfLargest(milArrayScore, 1)).brain.copy();

	child.brain.mutate(mutationRate);
	return child;
}

public int getIndexOfLargest(float [] array, int num) {
	float [] sorted = sort(array);
	int largest = 0;
	for ( int i = 1; i < array.length; i++ ) {
	  if ( array[i] == sorted[sorted.length - num])
	  	largest = i;
	}
	return largest; // position of the first largest found
}
class Sniper extends Militan{
	Sniper(boolean team){
		super(team);
		reloadTime = 5000;
		visionAng = PI/5; //Angle of vision limit, the total visible are is 2 times this
		visionDis = 500;
		println(super.visionDis);
	}
	Sniper(PVector p, boolean team){
		super(p, team);
	}
	public void show(){
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
			fill(0xff555346);
			rect(size/2, 0,size*2,size/10);
			rect(size/2, 0,size*5/7,size/8);
			rect(size/2, 0,size*2/4,size/5);
			fill(0xff535D4A);
			ellipse(0, 0, size, size);
		} else {
			fill(0xff746B5A);
			rect(size/2, 0,size*2,size/10);
			rect(size/2, 0,size*5/7,size/8);
			rect(size/2, 0,size*2/4,size/5);
			fill(0xffB29D69);
			ellipse(0, 0, size, size);
		}
		popMatrix();
	}
}
  public void settings() { 	size(600, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "counterstrAIke" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
