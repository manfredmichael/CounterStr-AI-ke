ArrayList<Militan> mils = new ArrayList<Militan>();
ArrayList<Bullet> bullets = new ArrayList<Bullet>();

float t;

EventList eList = new EventList();

void setup(){
	t=millis();
	fullScreen();
	frameRate(30);
	for (int i = 0; i < 2; i++){
		mils.add(new Gunman(false));
		mils.add(new Gunman(true));
	}
	for (int i = 0; i < 1; i++){
		mils.add(new Sniper(false));
		mils.add(new Sniper(true));
	}
	for (int i = 0; i < 7; i++){
		mils.add(new Militan(false));
		mils.add(new Militan(true));
	}
}
void draw(){
	background(51);

	for(Militan mil : mils){
		mil.takeAction(mils, bullets);
		mil.limit();
		mil.scoreMovement();
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

void mousePressed(){
	showVision = !showVision;
}