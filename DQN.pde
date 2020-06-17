class Qtable{
	float epsilon             = 0.7;
	final float START_EPSILON = epsilon;
	final float MIN_EPSILON   = 0.05;
	float learning_rate       = 0.4;
	final float START_LR      = learning_rate;
	final float DECAY         = 2000;
	final float DISCOUNT      = 0.85;
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
						table[i][j][k][l] = random(-0.01, 0.01);
					}
				}
			}
		}
	}

	int step(int state1, int state2, int state3){
		reward = -0.013; //reset reward (i want it to get -2 reward every 150 frames which is 5 seconds. -2/150 = -0.013)
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

	void evaluate(int state1, int state2, int state3){
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

int argMax(float [] num){
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