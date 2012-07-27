// package com.zombasoft.bawo;

import java.awt.*;
import java.awt.event.*;

class DisplayBoard extends Canvas implements MouseListener, MouseMotionListener {
	/******************************************************************************
	** Created:  2000 by Zangaphee Chimombo
	** Purpose:  This is DisplayBoard.java. It displays a 8 by 4 matrix of bowls
	**		representing the Bawo board; plus a stock bowl. It also allows
	**		the user to make moves selections using the mouse and animates
	**		moves. This is the main game UI apart from the control panel
	**		in Bawo.java.
	**
	**		DisplayBoard.java can readily morph into MaMaL-UI.java!
	** Copying:  GNU Public Licence.
	** Changes:
	**		2010-08-24 ZC
	**		Adding standard file headers like this one and class and
	**		method headers. Also taking the opportunity to do a code audit
	**		in order to remind myself of my code and get ideas about
	**		implementing jBawo 2.0 in MaMaL. Possibly.
	**
	**		2012-07-25 ZC
	**		Added to jBawo
	******************************************************************************/
	private static final long serialVersionUID = 5951144632163550805L;
	private static final int TEXTOFFSET = 3; /* text offset */
	private static final int SPACING = 2; /* Bowl spacing */
	private static final int DIA = 60; /* Bowl diameter */
	private static final int WIDTH = 541; /* Board width */
	private static final int HEIGHT = 241; /* Board height */

	DisplayBoard() {
		setSize(WIDTH, HEIGHT);
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	void paintBoardOutline(Graphics g) {
		g.setColor(Color.black);
		g.drawLine(DIA/2,0,WIDTH-1-DIA/2,0); /* north */
		g.drawLine(WIDTH-1,DIA,WIDTH-1,HEIGHT-1-DIA); /* east */
		g.drawLine(DIA/2,HEIGHT-1,WIDTH-1-DIA/2,HEIGHT-1); /* south */
		g.drawLine(0,DIA/2,0,HEIGHT-1-DIA/2); /* west */
		
		g.drawArc(0,0,DIA,DIA,90,90); /* northwest */
		g.drawArc(0,HEIGHT-1-DIA,DIA,DIA,180,90); /* southwest */
		g.drawArc(WIDTH-1-DIA,0,DIA,2*DIA,0,90); /* northeast */
		g.drawArc(WIDTH-1-DIA,HEIGHT-1-2*DIA,DIA,2*DIA,270,90); /* southeast */
	}

	void paintBowl(Graphics g, int index, Color bgColour) {
		int h=DIA*index2i(index); /* horizontal offset */
		int v=DIA*index2j(index); /* vertical offset */
		g.setColor(bgColour);
		if (index==4 || index==20) {
			g.fillOval(h+SPACING,v+SPACING,DIA-2*SPACING,DIA-2*SPACING);
			g.fillRect(h+SPACING,v+SPACING,DIA/2,DIA/2);
			g.fillRect(h+DIA/2,v+DIA/2,DIA/2-SPACING,DIA/2-SPACING);
		}
		else if (index==32) {
			g.fillRect(h+SPACING,v+SPACING-DIA,DIA/2-SPACING,DIA-SPACING);
			g.fillRect(h+SPACING+DIA/2-SPACING,v,DIA/2-SPACING,DIA-SPACING);
			g.fillArc(h+SPACING,v+SPACING-DIA,DIA-2*SPACING,2*DIA-2*SPACING,0,90);
			g.fillArc(h+SPACING,v+SPACING-DIA,DIA-2*SPACING,2*DIA-2*SPACING,180,90);
		}
		else g.fillOval(h+SPACING,v+SPACING,DIA-2*SPACING,DIA-2*SPACING);
		g.setColor(Color.black);
		if (index==4 || index==20) {
			g.drawArc(h+SPACING,v+SPACING,DIA-2*SPACING,DIA-2*SPACING,0,90);
			g.drawArc(h+SPACING,v+SPACING,DIA-2*SPACING,DIA-2*SPACING,180,90);
			g.drawLine(h+SPACING,v+SPACING,h+DIA/2,v+SPACING);
			g.drawLine(h+SPACING,v+SPACING,h+SPACING,v+DIA/2);
			g.drawLine(h+DIA-SPACING,v+DIA/2,h+DIA-SPACING,v+DIA-SPACING);
			g.drawLine(h+DIA/2,v+DIA-SPACING,h+DIA-SPACING,v+DIA-SPACING);
		}
		else if (index==32) {
			g.drawLine(h+SPACING,v+SPACING-DIA,h+SPACING,v);
			g.drawLine(h+SPACING,v+SPACING-DIA,h+SPACING+DIA/2-SPACING,v+SPACING-DIA);
			g.drawLine(h+SPACING+DIA-2*SPACING,v,h+SPACING+DIA-2*SPACING,v-SPACING+DIA);
			g.drawLine(h+SPACING+DIA/2-SPACING,v-SPACING+DIA,h+SPACING+DIA-2*SPACING,v-SPACING+DIA);
			g.drawArc(h+SPACING,v+SPACING-DIA,DIA-2*SPACING,2*DIA-2*SPACING,0,90);
			g.drawArc(h+SPACING,v+SPACING-DIA,DIA-2*SPACING,2*DIA-2*SPACING,180,90);
		}
		else g.drawOval(h+SPACING,v+SPACING,DIA-2*SPACING,DIA-2*SPACING);
	}

	public void updateBowl(int index, Color bgColour) {
		if (index<0) return;
		Graphics g=getGraphics();
		if (g==null) return;
		paintBowl(g, index, bgColour);
		paintTokens(g, index, jBawo.p.state[index]);
	}

	void paintTokens(Graphics g, int index, int nTokens) {
		int h=DIA*index2i(index); /* horizontal offset */
		int v=DIA*index2j(index); /* vertical offset */
		g.setColor(Color.black);
		if (nTokens<1) return;
		if (index>31) g.drawString(""+nTokens,h+DIA/2-TEXTOFFSET,v+TEXTOFFSET);
		else g.drawString(""+nTokens,h+DIA/2-TEXTOFFSET,v+DIA/2+TEXTOFFSET);
	}

	public void updateBoard() {
		Graphics g=getGraphics();
		if (g==null) return;

		for (int i=0; i<33; i++) {
			paintBowl(g, i, Color.white);
			paintTokens(g, i, jBawo.p.state[i]);
		}
	}

	public void update(Graphics g) {
		for (int i=0; i<33; i++) {
			paintBowl(g, i, Color.white);
			paintTokens(g, i, jBawo.p.state[i]);
		}
	}

	public void paint(Graphics g) {
		paintBoardOutline(g);
		for (int i=0; i<33; i++) {
			paintBowl(g, i, Color.white);
			paintTokens(g, i, jBawo.p.state[i]);
		}
	}

	void changeBackGround(Color newBG) {
		// applies to single bowls!
		// DisplayBoard doesn't store single-bowl stuff!
		// bgColour=newBG;
		repaint();
	}

	int index2j(int index) {
		if (index<8 || index==32) return 2;
		else if (index<16) return 3;
		else if (index<24) return 1;
		else if (index<32) return 0;
		else return -1;
	}

	int index2i(int index) {
		if (index<8) return index;
		else if (index<16) return 15-index;
		else if (index<24) return 23-index;
		else if (index<32) return index-24;
		else if (index<33) return 8;
		else return -1;
	}

	int ij2index(int i, int j) {
		int index=-1;

		if (i<8) {
			switch (j) {
				case 0: index=24+i;
				break;
				case 1: index=23-i;
				break;
				case 2: index=i;
				break;
				case 3: index=15-i;
				break;
			}
		}
		else if (i==8) index=32;
		return index;
	}

	/* MouseListener methods */
	public void mousePressed(MouseEvent event) {
		int x=event.getX();
		int y=event.getY();
		int index=ij2index((x-x%DIA)/DIA, (y-y%DIA)/DIA);
		if (jBawo.m==null && jBawo.p.move[index]>-1
				&& (jBawo.p.side==0 && index<16 || jBawo.p.side==1 && index>15
				|| jBawo.playMode.getSelectedIndex()==0)) {
			jBawo.m = new jBawoMove(index);
		}
	}

	public void mouseReleased(MouseEvent event) {
		if (jBawo.m==null) return;
		jBawo.m.mouseReleased();
	}

	public void mouseClicked(MouseEvent event) {
//		mousePressed(event);
//		mouseReleased(event);
//		the above doesn't work: click counts as two!
	}

	public void mouseEntered(MouseEvent event) { }

	public void mouseExited(MouseEvent event) { }

	/* MouseMotionListener methods */
	public void mouseMoved(MouseEvent event) {
		if (jBawo.m!=null) return;
		int x=event.getX();
		int y=event.getY();
		int index=ij2index((x-x%DIA)/DIA, (y-y%DIA)/DIA);
		// Bawo.msg(" "+Bawo.p.move[index]+","+Bawo.p.state[index]);
		/* send index to Move class */
		jBawoMove.mouseMoved(index);
	}

	public void mouseDragged(MouseEvent event) {
		int x=event.getX();
		int y=event.getY();
		int index=ij2index((x-x%DIA)/DIA, (y-y%DIA)/DIA);
		// Bawo.msg(""+index);
		/* send index to Move class */
		jBawo.m.mouseDragged(index);
	}
}

