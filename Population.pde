Militan regenerate(Militan deadMil, ArrayList<Militan> milArray){
	final float EPSILON = 0.25; //chance of NOT picking the actual best 
	final float mutationRate = 0.01;

	Militan child = new Militan(deadMil.team);
	float [] milArrayScore = new float[milArray.size()];
	for (int i = 0; i < milArrayScore.length; i++) {
		milArrayScore[i] = milArray.get(i).score;
	}

	if(random(1) < EPSILON)
		child.brain = milArray.get(getIndexOfLargest(milArrayScore, int(random(2,3)))).brain.copy();
	else
		child.brain = milArray.get(getIndexOfLargest(milArrayScore, 1)).brain.copy();

	child.brain.mutate(mutationRate);
	return child;
}

int getIndexOfLargest(float [] array, int num) {
	float [] sorted = sort(array);
	int largest = 0;
	for ( int i = 1; i < array.length; i++ ) {
	  if ( array[i] == sorted[sorted.length - num])
	  	largest = i;
	}
	return largest; // position of the first largest found
}