// package com.zombasoft.bawo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

// HTML Launcher
// <title> My Applet </title> <applet code="test.class"
// Width = 400 height = 400> </applet>

public class jBawo extends Applet implements ActionListener, ItemListener {
	/******************************************************************************
	** Created:  2000 by Zangaphee Chimombo
	** Purpose:  This is Bawo.java, the main wrapper class for jBawo 1.0.
	**		It can be run as either an applet within a browser, or as a
	**		standalone application.
	** Copying:  GNU Public Licence.
	** Changes:
	**		2010-08-24 ZC
	**		Adding standard file headers like this one and class and
	**		method headers. Also taking the opportunity to do a code audit
	**		in order to remind myself of my code and get ideas about
	**		implementing jBawo 2.0 in MaMaL. Possibly.
	**
	**		2012-07-25 ZC
	**		Renamed to jBawo.java
	******************************************************************************/	
	private static final long serialVersionUID = 3491848322878767212L;
	static final int WIDTH = 541; /* ctrl panel width */
	static final int HEIGHT = 121; /* ctrl panel  height */

	static jBawoMove m;
	static jBawoPosition p;
	static DisplayBoard d;

	static String history="New Game\n";
	static int depth=1; /* depth of artificial intelligence search tree */
	static int delay; /* animation delay */
	static boolean pause_status=false;

	static URL eMail;
	static Button newGame,takeBack,pause,bugAlert;
	static Checkbox p1Starts;
	static Choice p1Level,p2Level,animate,playMode;
	static Label message;
	static TextArea messages;

//	public void jBawo() {
//	}

	public void init() {
//	  Font f = new Font("TimesRoman", Font.PLAIN, 12);
//	  setFont(f);
		setBackground(Color.white);

		p=new jBawoPosition();
		p.initState(1);
		p.reSet(true);

		d=new DisplayBoard();
		m=null;

		Panel ctrlPanel=new Panel();
		makeCtrlPanel(ctrlPanel);

		GridBagLayout gbl=new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints c1=new GridBagConstraints();
		c1.fill=GridBagConstraints.NONE;

		message=new Label("jBawo 1.0");
		buildConstraints(c1,0,0,1,1,25,1);
		gbl.setConstraints(message,c1);
		add(message);
		buildConstraints(c1,0,1,1,24,0,24);
		gbl.setConstraints(d,c1);
		add(d);
		buildConstraints(c1,0,25,1,1,0,1);
		gbl.setConstraints(ctrlPanel,c1);
		add(ctrlPanel);

		delay=(animate.getSelectedIndex())*100;

		newGame.addActionListener(this);
		takeBack.addActionListener(this);
		pause.addActionListener(this);
		bugAlert.addActionListener(this);

		p1Level.addItemListener(this);
		p2Level.addItemListener(this);
		p1Starts.addItemListener(this);
		playMode.addItemListener(this);
		animate.addItemListener(this);
	}

	void makeCtrlPanel(Panel ctrlPanel) {
		ctrlPanel.setSize(WIDTH, HEIGHT);
		newGame=new Button("<<");
		takeBack=new Button("<");
		pause=new Button("||");
		bugAlert=new Button("Bug Alert!");
		p1Starts=new Checkbox("Player 1 Starts", null, true);

		GridBagLayout gbl=new GridBagLayout();
		ctrlPanel.setLayout(gbl);
		GridBagConstraints c1=new GridBagConstraints();
		c1.fill=GridBagConstraints.BOTH;

		/* first row */
		buildConstraints(c1,0,0,1,1,25,20);
		gbl.setConstraints(newGame,c1);
		ctrlPanel.add(newGame);
		buildConstraints(c1,1,0,1,1,25,0);
		gbl.setConstraints(takeBack,c1);
		ctrlPanel.add(takeBack);
		buildConstraints(c1,2,0,1,1,25,0);
		gbl.setConstraints(pause,c1);
		ctrlPanel.add(pause);
		buildConstraints(c1,3,0,1,1,25,0);
		gbl.setConstraints(bugAlert,c1);
		ctrlPanel.add(bugAlert);

		/* second row */
		message=new Label("Player 1 (lower board):");
		buildConstraints(c1,0,1,1,1,0,20);
		gbl.setConstraints(message,c1);
		ctrlPanel.add(message);

		messages=new TextArea("To move: press down left mouse button in a valid bowl and drag left or right.",3,40,TextArea.SCROLLBARS_VERTICAL_ONLY);
		messages.setEditable(false);
		buildConstraints(c1,1,1,2,2,0,0);
		gbl.setConstraints(messages,c1);
		ctrlPanel.add(messages);

		message=new Label("Player 2 (upper board):");
		buildConstraints(c1,3,1,1,1,0,0);
		gbl.setConstraints(message,c1);
		ctrlPanel.add(message);

		/* third row */
		p1Level=new Choice();
		p1Level.addItem("human");
		buildConstraints(c1,0,2,1,1,0,20);
		gbl.setConstraints(p1Level,c1);
		ctrlPanel.add(p1Level);

		p2Level=new Choice();
		p2Level.addItem("human");
		p2Level.addItem("random");
		p2Level.addItem("novice");
		p2Level.addItem("intermediate");
		p2Level.addItem("advanced");
		p2Level.addItem("tongaman");
		p2Level.select("novice");
		buildConstraints(c1,3,2,1,1,0,0);
		gbl.setConstraints(p2Level,c1);
		ctrlPanel.add(p2Level);

		/* fourth row */
		playMode=new Choice();
		playMode.addItem("Re-arrange Board");
		playMode.addItem("Yokhoma");
		playMode.addItem("Yawana");
		playMode.select("Yokhoma");
		buildConstraints(c1,0,3,1,1,0,0);
		gbl.setConstraints(playMode,c1);
		ctrlPanel.add(playMode);

		buildConstraints(c1,1,3,1,1,0,0);
		gbl.setConstraints(p1Starts,c1);
		ctrlPanel.add(p1Starts);

		message=new Label("  Animation Delay:");
		message.setAlignment(Label.RIGHT);
		buildConstraints(c1,2,3,1,1,0,20);
		gbl.setConstraints(message,c1);
		ctrlPanel.add(message);

		animate=new Choice();
		animate.addItem("0");
		animate.addItem("100");
		animate.addItem("200");
		animate.addItem("300");
		animate.addItem("400");
		animate.addItem("500");
		animate.select("300");
		buildConstraints(c1,3,3,1,1,0,0);
		gbl.setConstraints(animate,c1);
		ctrlPanel.add(animate);
	}

	void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy) {
		gbc.gridx=gx;
		gbc.gridy=gy;
		gbc.gridwidth=gw;
		gbc.gridheight=gh;
		gbc.weightx=wx;
		gbc.weighty=wy;
	}
	
	public void paint (Graphics g) {
	}
	
	/* ActionListener */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource()==newGame) {
			m=null;
			if (playMode.getSelectedItem()=="Re-arrange Board") {
				playMode.select("Yokhoma");
			}
			else {
				p=new jBawoPosition();
				p.initState(playMode.getSelectedIndex());
			}
			p.reSet(p1Starts.getState());
			delay=(animate.getSelectedIndex()+1)*100;
			d.updateBoard();
			msg("\nNew Game");
		}
		else if (event.getSource()==takeBack) p.takeBack();
		else if (event.getSource()==pause) {
			if (pause_status) {
				pause_status=false;
				pause.setLabel("||");
			}
			else {
				pause_status=true;
				pause.setLabel(">");
			}
		}
		else if (event.getSource()==bugAlert) {
			try {
				eMail=new URL("mailto:zanga@zombasoft.com ?subject=bug alert &body="+history);
			}
			catch (MalformedURLException e) {
				System.out.println("\nMalformed URL... sorry!\n");
				msg("\nMalformed URL... sorry!");
			}
			getAppletContext().showDocument(eMail);
		}
	}

	/* ItemListener */
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource()==p2Level) depth=p2Level.getSelectedIndex();
		if (event.getSource()==p1Starts) {
			if (p1Starts.getState()) {
				msg("\nPlayer 1 begins.");
			}
			else {
				msg("\nPlayer 2 begins");
			}
			msg("\nStart a new game (\"<<\") for change to take effect.");
		}
		if (event.getSource()==playMode) {
			if (playMode.getSelectedItem()=="Re-arrange Board") {
				msg("\nClick or drag 'n' drop. Start a new game (\"<<\") for change to take effect.");
				delay=0;
				p.rearrangeMove();
			}
			else {
				p.updateStatus();
				msg("\nStart a new game (\"<<\") for change(s) to take effect.");
			}
		}
		if (event.getSource()==animate) {
			delay=(animate.getSelectedIndex())*100;
		}
	}

	static synchronized void msg (String s) {
		/* store message s in history and display */
		try {
			Thread.sleep(delay);
		}
		catch (InterruptedException exc) { }
		
		history=history+p.moveNo+": "+s;
		/* force vertical scrolling. inelegant, but works! */
		messages.setCaretPosition(messages.getText().length());
		messages.append(s);
	}
	
	/* Bawo Application */
	public static void main (String[] args) {
		Frame f=new Frame("jBawo");
		jBawo b;
		
		f.add(b=new jBawo());
		f.addWindowListener(new WindowAdapter () {
				public void windowClosing (WindowEvent e) {
					System.exit(0);
				}
			}
		);

		b.init();
		b.start();
		f.setSize(560,400);
		f.setVisible(true);
		/* where is bawoicon.gif??? */
		seticon(f,"bawoicon.gif");
	}

	static void seticon (Frame f, String file) {
		Image i=Toolkit.getDefaultToolkit().getImage(file);
		MediaTracker mt=new MediaTracker(f);
		mt.addImage(i,0);
		try { mt.waitForAll(); } catch (Exception e) {}
		f.setIconImage(i);
	}
}
