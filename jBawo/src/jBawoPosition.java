// package com.zombasoft.bawo;

class jBawoPosition extends jBawoBoard {
	/******************************************************************************
	** Created:  2000 by Zangaphee Chimombo
	** Purpose:  This is Position.java. It extends com.zombasoft.bawo.Board. It
	**		instantiates a game.
	** Copying:  GNU Public Licence.
	** Changes:
	**		2010-08-26 ZC
	**		Adding standard file headers like this one and class and
	**		method headers. Also taking the opportunity to do a code audit
	**		in order to remind myself of my code and get ideas about
	**		implementing jBawo 2.0 in MaMaL. Possibly.
	**
	**		2012-07-25 ZC
	**		Added to jBawo. Renamed to jBawoPosition.java
	******************************************************************************/
	byte prevState1 [];
	byte prevState2 [];
	boolean prevStatus1 [];
	boolean prevStatus2 [];
	int moveNo; /* move about to be made */
	int takeBackNo; /* moveNo of last takeBack */

	jBawoPosition() {
		prevState1=new byte[33];
		prevState2=new byte[33];
		prevStatus1=new boolean[2];
		prevStatus2=new boolean[2];
	}

// i think initial board state should be in Board.java
	void initState(int modeIndex) {
		for (int i=0;i<33;i++) {
			if (modeIndex==1) /* Yokhoma */ {
				switch (i) {
					case 32: state[i] = 36; // this should depend on INIT_TOKENS
						break;
					case 4:
					case 20: state[i] = 10; // this should depend on INIT_TOKENS
						break;
					case 5:
					case 6:
					case 21:
					case 22: state[i] = 2;
						break;
					default: state[i] = 0;
				}
				for (int j=0;j<2;j++) status[j]=true;
			}
			else if (modeIndex==2) /* Yawana */ {
				switch (i) {
					case 32: state[i] = 0;
						break;
					default: state[i] = 2;
				}
				for (int j=0;j<2;j++) status[j]=false;
			}
		}
	}

	void reSet(boolean p1Starts) {
		int i;
		
		for (i=0;i<33;i++) {
			prevState2[i] = -1;
			prevState1[i] = -1;
		}
		for (i=0;i<2;i++) {
			prevStatus1[i]=false;
			prevStatus2[i]=false;
		}
		moveNo=0;
		takeBackNo=0;
		value=eValuate();
		gameOver=false;
		contCapture=false;
		if (p1Starts) side=0;
		else side=1;
		updateMove(0);
		if (p1Starts) jBawo.m=null;
		else {
			if (jBawo.p2Level.getSelectedItem()!="human") {
				jBawo.m=new jBawoMove();
			}
	}
	}

	void history() {
		if (!(contCapture || contTakata)) {
			moveNo++;
			for (int i=0;i<33;i++) {
				prevState2[i] = prevState1[i];
				prevState1[i] = state[i];
			}
			for (int i=0;i<2;i++) {
				prevStatus2[i]=prevStatus1[i];
				prevStatus1[i]=status[i];
			}
		}
	}

	synchronized void animate(int index) {
		if (index==-1) return;
		// Bawo.bowls[index].changeBackGround(java.awt.Color.lightGray);
		jBawo.d.updateBowl(index,java.awt.Color.lightGray);
		do {
			try {
				Thread.sleep(jBawo.delay);
			}
			catch (InterruptedException exc) { }
		} while (jBawo.pause_status);
		// Bawo.bowls[index].changeBackGround(java.awt.Color.white);
		jBawo.d.updateBowl(index,java.awt.Color.white);
	}

	void messages() {
		if (gameOver) {
			if (value[0]>0) jBawo.msg("\nPlayer 1 wins...");
			else jBawo.msg("\nPlayer 2 wins...");
			jBawo.msg("\nClick \"<<\" to start a new game");
		}
		else if (!(contCapture || contTakata)) {
			if (side==0) jBawo.msg("\nPlayer 2's turn...");
			else jBawo.msg("\nPlayer 1's turn...");
		}
		else if (contCapture || contTakata) {
			if (side==1) jBawo.msg("\nPlayer 2's turn...");
			else jBawo.msg("\nPlayer 1's turn...");
			jBawo.msg(" Stop OR Continue?");
		}
	}

//	boolean moveAlive()
//	{
//		if (Bawo.m==null) return false;
//		else return true;
//	}

	void takeBack() {
		/* take back last two moves (computer's and player's) */
		if (jBawo.m!=null && jBawo.m.isAlive()) return;
		if (moveNo>takeBackNo+1 && !gameOver) {
			moveNo=moveNo-2;
			takeBackNo=moveNo;
			for (int i=0;i<2;i++) {
				status[i]=prevStatus2[i];
				prevStatus2[i]=false;
				prevStatus1[i]=false;
			}
			for (int i=0;i<33;i++) {
				state[i]=prevState2[i];
				prevState2[i] = -1;
				prevState1[i] = -1;
				jBawo.d.updateBowl(i,java.awt.Color.white);
			}
			updateMove(0);
			jBawo.msg("\nTake Back");
		}
	}

	void rearrangeMove() {
	/* updates move array before a Re-arrange Board playMode */
		for (int i=0;i<33;i++) move[i]=0;
	}

	void updateStatus() {
		/* updates status array after a Re-arrange Board playMode */
		/* kinda precludes the case where nyumba has enough tokens */
		/* but status is false ('cos nyumba was previously moved etc) */
		/* there is a simple way around this: call updateStatus from */
		/* moveTokens and addToken instead of Bawo.itemStateChanged! */
		/* and preserving previous status... */
		if (state[32]>0) {
			if (state[4]>=INIT_TOKENS) status[0]=true;
			else status[0]=false;
			if (state[20]>=INIT_TOKENS) status[1]=true;
			else status[1]=false;
		}
	else for (int i=0;i<2;i++) status[i]=false;
	}

	void moveTokens(int srcBowl,int destBowl) {
		/* called during Re-arrange Board playMode */
		if (srcBowl==destBowl || destBowl==-1) {
			addToken(srcBowl);
			return;
		}
		state[destBowl]+=state[srcBowl];
		state[srcBowl]=0;
		animate(srcBowl);
		animate(destBowl);
	}

	void addToken(int index) {
		/* called during Re-arrange Board playMode */
		if (state[32]<1) return;
		state[32]--;
		animate(32);
		state[index]++;
		animate(index);
	}
}
