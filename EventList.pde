class EventList{
	ArrayList<String> events=new ArrayList<String>();
	float time = 0;
	float ytop = 15;  //not constant because list will move vertically
	final float xtop = 15;
	final float ytopDefault = ytop;
	final float timer = 3000;
	EventList(){
	}
	void render(){
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
	void addMessage(String message){
		events.add(message);
	}
	void addKill(Militan shooter, Militan dead){
		String message = shooter.name + " killed " + dead.name;
		if(shooter.score % 10 == 0){
			message = message + " for his " + str(shooter.score) + " kill streak!";
		}
		events.add(message);
	}
}