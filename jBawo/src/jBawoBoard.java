// package com.zombasoft.bawo;

class jBawoBoard {
	/******************************************************************************
	** Created:  2000 by Zangaphee Chimombo
	** Purpose:  This is Board.java. It contains the Bawo Game Engine and Artificial
	**		Intelligence (minimax search with alpha-beta pruning).
	** Copying:  GNU Public Licence.
	** Changes:
	**		2010-08-24 ZC
	**		Adding standard file headers like this one and class and
	**		method headers. Also taking the opportunity to do a code audit
	**		in order to remind myself of my code and get ideas about
	**		implementing jBawo 2.0 in MaMaL. Possibly.
	**
	**		2012-07-25 ZC
	**		Added to jBawo. Renamed to jBawoBoard.java
	******************************************************************************/
	/* initial number of tokens in nyumba (yokhoma mode) */
	static final int INIT_TOKENS = 10;
/* The ranks (4) and files (8) of the board are related to the array
   indices of state[] and move[] as follows:

   24 25 26 27 28 29 30 31
   23 22 21 20 19 18 17 16
   0  1  2  3  4  5  6  7
   15 14 13 12 11 10 9  8

   32 contains the stock tokens
*/
	byte state [];
	byte move [];

	/* next side to move. contains 0 or 1 */
	byte side; /* 0=lower, default human; 1=upper, default computer */
/* certain special moves only apply to the nyumba: */
/* 1. stop or continue; 2. kutapa m'nyumba; */
/* under certain conditions the nyumba loses this special status: */
/* 1. when the the yokhoma stage ends; 2. when the #tokens falls */
/* below INIT_TOKENS; */
/* status[] keeps track of whether nyumba still has its special properties. */
	boolean status[];

/* gameOver is true if next side to move has no tokens in its front rank,
   or if all its bowls are empty or possess singleton tokens. */
	boolean gameOver;
/* contCapture, default false, specifies an extended move. i.e. when a move
   could've ended in the nyumba, but the player has chosen to contCapture! */
	boolean contCapture=false;
	boolean contTakata;

/* the value of the current board as returned by the eValuate() method is the
	number of tokens belonging to the side to move next. */
	int value [];
	private static final int LIM=Integer.MAX_VALUE;

	jBawoBoard () {
		state=new byte[33];
		move=new byte[33];
		value=new int[2];
		status=new boolean[2];
		side=0;
		gameOver=false;
		contCapture=false;
	}

	void copy(jBawoBoard old) {
		/* called only in bestMove */
		state=new byte[33];
		move=new byte[33];
		value=new int[2];
		status=new boolean[2];

		int i;
		for (i=0;i<33;i++) state[i]=old.state[i];
		for (i=0;i<2;i++) status[i]=old.status[i];
		side=old.side;
		gameOver=old.gameOver;
		contCapture=old.contCapture;
		contTakata=old.contTakata;
	}

	final void updateMove(int d1r) {
		/**********************************************************************
		** Created:  UNKNOWN by Zangaphee Chimombo
		** Based On:
		** Purpose:  
				determine move[] for current state[]
				-1 - no valid move
				0 (00) - used for contCapture type moves to denote stop
				1 (01) - right move valid
				2 (10) - left move valid
				3 (11) - both left and right moves valid (or valid sow)
		** Replaces:
		** Status:   
		** Inputs:
		** Outputs:
		** Returns:
		** Uses:
		** Comment: would the codes above (00, 01, 10 and 11) now have MaMaL
			equivalents? 

		What in Mbona's name is contTakata?
		** Requires:
		** Used by:
		** Assumptions:
		** Changes:
		**		   2010-08-26  by ZC
		**		   Fixed bug #3040085
		**
		**		   2010-11-14  by ZC
		**		   Fixed bug #3085082.
		**********************************************************************/
		int i,index,dir,n,dest,mtaji=-1,mtajiCount=0;
		byte mkatiTokens=0;
		byte s=side;
		boolean takata;

		if (gameOver) for (i=0;i<32;i++) move[i]=-1;
		else do {
			s=(byte) ((s+1)%2);
			takata=true; /* assume true and then look for captures */
			mkatiTokens=0;
			for (i=0;i<16;i++) {
				index=s*16+i;
				move[index]=-1;
		/* 2010-08-26 ZC */
		/* first condition now only applies in mtaji stage */
		/* fixes bug #3040085 */
		if (state[index]>15 && state[32]==0 || s==side && contTakata)
					takata=true; /* redundant! */
				else if (contCapture && s==side) takata=false;
				else if (state[32]==0) { /* mtaji stage */
					for (dir=-1;dir<2;dir+=2) {
						n=state[index];
						dest=(index+n*dir)%16;
						if (dest<0) dest+=16;
						if (n>1 && dest<8 && state[dest+s*16]>0 && state[7-dest+((s+1)%2)*16]>0) { /* capture==true */
							if (move[index]==-1) move[index]=0; /* necessary for following +='s to work! */
							switch (dir) {
								case -1: move[index]+=2;
								break;
								case 1: move[index]+=1;
								break;
								default: move[index]=-1;
							}
							takata=false;
							mtajiCount++;
							mtaji=7-dest+((s+1)%2)*16; /* ;-) */
						}
					}
				}
				else {   /* yokhoma stage */
					if ((i<8) && (state[index]>0) && (state[7-i+((s+1)%2)*16]>0)) {
						if (i<2) move[index]=1;
						else if (i>5) move[index]=2;
						else move[index]=3;
						takata=false;
					}
				}
				if (i<8 && state[index]>mkatiTokens && !(status[s] && i==4))
					mkatiTokens=state[index];
			}
			if (takata==true && !contTakata) {
				for (i=0;i<16;i++) {
					index=s*16+i;
			if (state[32]==0) /* mtaji stage */ {
						if (state[index]>1) {
/* hierarchy of takata options (mtaji stage): */
/*
1. (highest priority) do not takata opponent's only mtaji
2. takata zamkati
3. (lowest priority) takata any
it is assumed that Nyumba loses its status in mtaji stage
*/
							if (mtajiCount!=1 && mkatiTokens<2 && i>7
				 || mtajiCount!=1 && i<8
							 || mtajiCount==1 && mtaji!=index ) {
								move[index]=3;
				}
							else move[index]=-1; /* redundant! */
						}
						else move[index]=-1; /* redundant! */
					}
					else /* yokhoma stage */ {
						if (state[index]>0) {
/* hierarchy of takata options (yokhoma stage): */
/*
1. (highest priority) takata >1 zamkati (except Nyumba)
2. takata any zamkati (except Nyumba)
3. (lowest priority) takata Nyumba

during yokhoma stage, one can not takata ya kunja.
*/
							if (i==4 && status[s] && mkatiTokens==0
				 || i<8 && mkatiTokens==1 && !(i==4 && status[s])
							 || state[index]>1 && i<8 && !(i==4 && status[s])) {
								move[index]=3;
							}
						}
						else move[index]=-1; /* also redundant! */
					}
				}
			}
		} while(s!=side);
		/* contCapture updateMoves should really just be a one-liner, somewhere... */
		if (contCapture) move[side*16+4]=(byte) ((-d1r+3)/2);
		if (contTakata) move[side*16+4+d1r*2]=(byte) d1r;
	}

	void history() {
		/* this method is overidden in class position */
	}

	void animate(int index) {
		/* this method is overidden in class position */
	}

	void messages() {
		/* this method is overidden in class position */
	}

	boolean moveAlive() {
		/* this method is overidden in class position */
		return true;
	}

	final void updateState(int index, int dir) {
		/**********************************************************************
		** Created:  UNKNOWN by Zangaphee Chimombo
		** Based On:
		** Purpose:  
				perform move starting at index towards dir. update state[] array.
		** Replaces:
		** Status:   
		** Inputs:
		** Outputs:
		** Returns:
		** Uses:
		** Comment:
		** Requires:
		** Used by:
		** Assumptions:
		** Changes:
		**********************************************************************/
		int currPtr; /* 0..15 */
		byte n=0;
		boolean capture, takata=false, first=true;
		if (dir==0) {
			value=eValuate();
			contCapture=false;
			contTakata=false;
			messages();
			updateMove(dir);
			if (side==0) side=1;
			else side=0;
			return;
		}
		capture=false;
		history();
		currPtr=index;
		if (!contCapture && state[32]>0) /* yokhoma move */ {
			state[32]--;
		animate(32);
			state[currPtr+16*side]++;
		animate(currPtr+16*side);
			first=false;
			if (currPtr<8 && state[7-currPtr+((side+1)%2)*16]>0) /* capture move */ {
				capture=true;
				n=state[7-currPtr+((side+1)%2)*16];
				state[7-currPtr+((side+1)%2)*16]=0;
				animate(7-currPtr+((side+1)%2)*16);
				if (dir>0) currPtr=0;
				else currPtr=7;
			}
			else /* takata move */ {
				takata=true;
				if (status[side] && move[side*16+4]==3) {
					/* only nyumba has tokens. takata 2 tokens from nyumba */
					/* this move is not possible in mtaji stage */
					n=2;
					state[currPtr+16*side]=(byte) (state[currPtr+16*side]-2);
					animate(currPtr+16*side);
					currPtr+=dir; /* no need to wrap <= currPtr=4 */
					capture=true; /* not really: need to avoid 1st if-blok in loop! */
				}
			}
		}
		do /* repeat until state[currPtr+16*side]==1 */ {
			if (contCapture || !capture) {
				n=state[currPtr+16*side];
				state[currPtr+16*side]=0;
				animate(currPtr+16*side);
				currPtr+=dir;
				if (currPtr<0) currPtr=15;
				if (currPtr>15) currPtr=0;
			}
			//if (contCapture) first=false;
			contCapture=false;
			capture=false;
			while(n!=0) {
				state[currPtr+16*side]++;
				animate(currPtr+16*side);
				n--;
				if (n!=0) currPtr+=dir;
				if (currPtr<0) currPtr=15;
				if (currPtr>15) currPtr=0;
			}
			if (!takata && currPtr<8 && state[23-currPtr-16*side]>0 && state[currPtr+16*side]!=1) {
				capture=true;
				n=state[7-currPtr+16-16*side];
				state[7-currPtr+16-16*side]=0;
				animate(23-currPtr-16*side);
				switch (currPtr) {
					case 0:
					case 1: currPtr=0;
						if (dir==-1) dir=1;
					break;
					case 6:
					case 7: currPtr=7;
						if (dir==1) dir=-1;
					break;
					default: currPtr=(byte) ((-1*dir+1)*7/2);
					break;
				}
			}
			if (first && !capture) takata=true; /* necessary 4 mtaji stage! */
			first=false; /* first time round loop! */
			value=eValuate(); /* sets gameOver if true */
			if ( (state[32]<1) || status[0] && state[4]<INIT_TOKENS)
				status[0]=false;
			if ( (state[32]<1) || status[1] && state[20]<INIT_TOKENS)
				status[1]=false;
			if (status[side] && currPtr==4 && !takata)
				contCapture=true;
			if (status[side] && takata && index==4 && (currPtr==6 || currPtr==2))
		/* 6/8/11. zc. bug #3387140. commenting out next line. */
				//contTakata=true;
			/* NOTE: if capture, then state[currPtr+16*side]<2 is legit! */
			if (!moveAlive()) return;
		} while(!(currPtr==4 && status[side]) && (state[currPtr+16*side]>1 || capture) && !gameOver && !contCapture);
		messages();
		updateMove(dir);
		if (!(contCapture || contTakata) && side==0) side=1;
		else if (!(contCapture || contTakata)) side=0;
		animate(-1);
	}

	int[] eValuate() {
		/* returns 0 or 1 if game over */
		/* 0 if front rank is empty */
		/* 1 if bowls have 1 or less tokens */
		int largest, val[];
		int i, index, s;
		val =new int[2];

		for (s=0;s<2;s++) {
			val[s]=0;
			largest=0;
			/* front rank first */
			for (i=0;i<8;i++) {
				index=s*16+i;
				val[s]+=state[index];
				if (largest<state[index]) largest=state[index];
			}
			if (val[s]>0) {
				for (i=8;i<16;i++) {
					index=s*16+i;
					val[s]+=state[index];
					if (largest<state[index]) largest=state[index];
				}
			}
			if ((val[s]==0) || ((largest==1) && (state[32]==0))) {
				gameOver=true;
				val[s]=-255*65536; /* 255*2^16=0x00ff0000 */
			}
		}
		return val;
	}

	final int[] bestMove(int depth) {
		/* return move for current side. calls recursive version */
		return bestMove(-LIM,LIM,depth,(byte) 1);
	}

	final int[] bestMove(int alpha, int beta, int depth, byte rootSide) {
		/* minimax search with alpha-beta pruning return score, index, dir
		   score not needed at root. latter two not needed within tree.
		   inefficiency 1: repeats tree search for contCapture moves!
		*/
		int i, sid[], dir, temp[], numMoves=0;
		sid=new int[3]; /* score, index, direction */
		temp=new int[3];
		temp[0]=0;

		if (gameOver) {
			sid[0]=value[rootSide]-value[(rootSide+1)%2];
			return sid;
		}
		/* institute random choice between moves of otherwise equal score */
		for (i=0;i<16;i++)
			if (move[i+16*side]>0)
				for (dir=-1;dir<2;dir++)
					if (move[i+16*side]>1 && dir==-1 || move[i+16*side]!=2 && dir==1 || contCapture && dir==0) {
						numMoves++; /* count moves */
						sid[0]=(int) (256*Math.random()); /* 0x000000** */
						/* the 1st term below is very necessary! */
						if (temp[0]==0 || temp[0]<sid[0]) {
							temp[0]=sid[0];
							sid[1]=i;
							sid[2]=dir;
						}
					}
		if (numMoves<1) jBawo.msg("error!");
		if (depth==0) {
			if (jBawo.depth>0) {
				/* 0x0000**00 + 0x000000** */
				sid[0]=(value[rootSide]-value[(rootSide+1)%2])*256 + sid[0];
			}
			return sid;
		}
		sid[0]=LIM*((int) Math.pow(-1,side));
		/* LIM			 =2^31 - 1 */
// maybe gameOver shd b > LIM?
		/* gameOver		=0x00ff0000 */
		/* value_diff	  =0x0000**00 */
		/* random		  =0x000000** */

	/* if root has only one move return it */
		if (numMoves==1 && jBawo.depth==depth) return sid;
		if (numMoves<2) jBawo.msg("error!");
		/* go through successors of node */
		for (i=0;i<16;i++)
			if (move[i+16*side]>0)
				for (dir=-1;dir<2;dir++)
					if (move[i+16*side]>1 && dir==-1 || move[i+16*side]!=2 && dir==1 || contCapture && dir==0) {
						jBawoBoard scratch=new jBawoBoard();
						scratch.copy(this);
						do {
							scratch.updateState(i, dir);
							temp=scratch.bestMove(alpha, beta, depth-1, rootSide); /* returns a sid! */
							if ((side==1 && (temp[0]>sid[0])) || (side==0 && (temp[0]<sid[0]))) {
								sid[0]=temp[0];
								sid[1]=i;
								sid[2]=dir;
								if ((side==1) && (sid[0]>alpha)) alpha=sid[0];
								if ((side==0) && (sid[0]<beta)) beta=sid[0];
							}
							/* pruning */
							if (alpha>beta) {
								return sid;
							}
						} while(scratch.contCapture);
					}
		return sid;
	}

	final String displayNode(String temp) {
		int i;

		for (i=24;i<32;i++) temp=temp+state[i]+" ";
		temp+="\n";
		for (i=23;i>15;i--) temp=temp+state[i]+" ";
		temp+="\n";
		for (i=0;i<8;i++) temp=temp+state[i]+" ";
		temp+="\n";
		for (i=15;i>7;i--) temp=temp+state[i]+" ";
		temp+="\n"+state[32]+"\n\n";
// move array?
		return temp;
	}

	final String displayGameTree(int depth, byte s) {
		/* 21/02/2010. Zanga Chimombo. */
		/* s is player side: 1 (computer) or 0 (human) */
		int i, dir;
		String temp="\n";
		temp+=displayNode(temp);

		if (gameOver) {
			return temp+"("+String.valueOf(value[s]-value[(s+1)%2])+")\n\n";
		}
		if (depth==0) {
			return temp+"("+String.valueOf(value[s]-value[(s+1)%2])+")\n\n";
		}
// square brackets?
		for (i=0;i<16;i++)
			if (move[i+16*side]>0)
				for (dir=-1;dir<2;dir++)
					if (move[i+16*side]>1 && dir==-1 || move[i+16*side]!=2 && dir==1 || contCapture && dir==0) {
						jBawoBoard scratch=new jBawoBoard();
						scratch.copy(this);
						do {
							scratch.updateState(i, dir);
							temp=temp+scratch.displayGameTree(depth-1,s);
						} while(scratch.contCapture);
					}
		return temp+")\n";
	}
}
