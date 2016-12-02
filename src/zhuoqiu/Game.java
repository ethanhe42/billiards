package zhuoqiu;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class Game extends JPanel implements MouseListener,
                                             MouseMotionListener,
                                             Runnable {

	//This is Professional Pool table dimensions 4.5' x 9' = 54" x 108" * 8pixels/inch = 432x864 pixels.
	public static final int SIZEY = 432;
	public static final int SIZEX = 864;


    Vector<Ball> balls; // collection of the circles
    Vector<Ball> jinqiuBalls; // collection of the circles
    
	int baiqiuidx = -1;

    Menu mymenu;   		// to reference shuffle window
    Yinqing  yinqing;    		// the engine that drives the game
    
    public boolean ready;   // must be true to go again
    public boolean scratched = false;
    public boolean movingQ = true;

    Vector<Qiudai> qiudais;	//Collection of pockets on the table;
   	private Thread t;

	public static final double CoF = 120;


    int mubiaoqiu = -1;
    boolean zhixiangqiu = false;
    HashMap<String, Integer> queueLine;

    /*******************************************************
     * constructor. sets up the panel stuff, adds
     * mouse listenners, starts game engine, etc
     *
     * @param shuffle
     ******************************************************/
    public Game(Menu mymenu) {
        //Always call super
        super();

        //Makes the moving display smoother
        setDoubleBuffered(true);

        //Default bg colour
        setBackground(new Color (50, 100, 50));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        //Add mouse listeners
        addMouseListener(this);
        addMouseMotionListener(this);

        //Reference to bouncer
        this.mymenu = mymenu;

		//Initialize the circles.
        balls = new Vector<Ball>();
        
        // Initialize engine
        yinqing = new Yinqing( this );

        createBalls();
        yinqing.setCircles( balls );

        createQiudais();

        //Start the Engine
        Thread e = new Thread( yinqing );
		e.setPriority(Thread.NORM_PRIORITY);
        e.start();

        // start thread
        Thread t = new Thread(this);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();

        // ready!
        ready = true;
    }

    public void newGame() {
		//init circles.
        createBalls();
        yinqing.setCircles( balls );
        movingQ = true;

    }

    public void readdQueueBall() {
		boolean foundQueueBall = false;
		for ( Ball c : balls ) {
			if ( c.name.equals("Queue Ball") ) foundQueueBall = true;
		}

		if ( !foundQueueBall ) {
			double cx =  800+yinqing.TABLE_OFFSET_X;
			double cy = SIZEY/2+yinqing.TABLE_OFFSET_Y;

			System.out.println("Adding que ball at (x,y): " + cx + "," + cy);

			Ball b = new Ball(0, "Que Ball", Color.WHITE, cx, cy, 0, 0, 30);
			balls.add( b );
			baiqiuidx = findBaiqiuIdx( balls );
		}
		else {
			System.out.println("Queue Ball is already on the table!!");
		}
	}

	public int findBaiqiuIdx( Vector<Ball> balls ) {
		Ball b;
		int idx = -1;
		for ( int i = 0 ; i < balls.size(); i++ ) {
			b = (Ball)balls.elementAt(i);
			if ( b.name.equals("Que Ball") ) idx = i;
		}
		if ( idx == -1 ) {
			System.out.println("没有白球!!!!");
			System.exit(0);
		}
		return idx;
	}

	public void createQiudais() {
		qiudais = new Vector<Qiudai>();
		Qiudai p;

		p = new Qiudai("Top Left",     Qiudai.STANDARD_COLOR, 86, 86, 48);
		qiudais.add( p );

		p = new Qiudai("Top Mid",      Qiudai.STANDARD_COLOR, 508, 76, 48);
		qiudais.add( p );

		p = new Qiudai("Top Right",    Qiudai.STANDARD_COLOR, 930, 86, 48);
		qiudais.add( p );

		p = new Qiudai("Bottom Left",  Qiudai.STANDARD_COLOR, 86, 496, 48);
		qiudais.add( p );

		p = new Qiudai("Bottom Mid",   Qiudai.STANDARD_COLOR, 508, 506, 48);
		qiudais.add( p );

		p = new Qiudai("Bottom Right", Qiudai.STANDARD_COLOR, 930, 496, 48);
		qiudais.add( p );

	}


	/*************************************************
	*
	*
	*
	**************************************************/
	public void createBalls() {
		balls = new Vector<Ball>();
    	jinqiuBalls = new Vector<Ball>();

		Ball ball;

		double midy = SIZEY/2 + Yinqing.TABLE_OFFSET_Y-15;
		double firstRow = 150 + Yinqing.TABLE_OFFSET_X;

		// name, Color color, double x, double y, int speed, double direction, int size) {

		ball = new Ball(0, "Que Ball", Color.WHITE, (SIZEX*.75)+Yinqing.TABLE_OFFSET_X, midy, 0, 0, 30);
		balls.add( ball );

		//First Row (going down)
		ball = new Ball(1, "One Ball", new Color(255, 255, 102),firstRow, midy-62, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(2, "Two Ball", new Color( 51, 51, 255), firstRow, midy-31, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(3, "Three Ball", new Color(204, 0, 51), firstRow, midy, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(4, "Four Ball", new Color(255, 0, 153), firstRow, midy+31, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(5, "Five Ball", new Color(255, 102, 0), firstRow, midy+62, 0, 0, 30);
		balls.add( ball );

		//Second Row
		ball = new Ball(6, "Six Ball", new Color(51, 255, 0),  firstRow+31, midy-47, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(7, "Seven Ball", new Color(102, 0, 0), firstRow+31, midy-16, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(8, "Eight Ball", Color.BLACK,          firstRow+31, midy+16, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(9, "Nine Ball", new Color(204, 204, 0),firstRow+31, midy+47, 0, 0, 30);
		balls.add( ball );


		//Third Row
		ball = new Ball(10, "Ten Ball", new Color(153, 0, 255), firstRow+62, midy-31, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(11, "Eleven Ball", new Color(153, 0, 0), firstRow+62, midy, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(12, "Twelve Ball", new Color(255, 0, 153), firstRow+62, midy+31, 0, 0, 30);
		balls.add( ball );
		//Forth Row
		ball = new Ball(13, "Thirteen Ball", new Color(153, 255, 153), firstRow+93, midy-16, 0, 0, 30);
		balls.add( ball );
		ball = new Ball(14, "Fourteen Ball", new Color(0, 0, 153), firstRow+93, midy+16, 0, 0, 30);
		balls.add( ball );

		//Fifth Row
		ball = new Ball(15, "Fifteen Ball", new Color(0, 0, 255), firstRow+124, midy, 0, 0, 30);
		balls.add( ball );


		baiqiuidx = findBaiqiuIdx( balls );

		for ( int x = 0 ; x < balls.size(); x++ ) {
			yinqing.calcFriction( ball);
		}

	}

    private void paintBalls(Graphics g) {
        try {
            for (Ball c : balls)
                paintBall(g, c);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            paintBalls(g); // retry so the disks never not get painted
        }
    }

    private void paintBall( Graphics g, Ball b ) {
        if (b == null) return;
        int fontSize = 10;
        int dx = (int)b.x+8;
        int dy = (int)b.y+8;
        
        if ( b.ballNumber > 0 && b.ballNumber < 9 ) {
            g.setColor( b.color );
            g.fillOval((int)b.x - 1, (int)b.y - 1, 30, 30 );

            g.setColor( Color.WHITE );
            g.fillOval(dx, dy, 12, 12 );

            char[] num = { Character.forDigit(b.ballNumber, 10) }; 
            Font font = new Font("Courier New", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor( Color.BLACK );
            g.drawChars(num, 0, num.length, (int)StrictMath.round(b.x+12), (int)StrictMath.round(b.y+17) );
        }
        else if ( b.ballNumber > 9 ) {
            g.setColor( Color.WHITE );
            g.fillOval((int)b.x - 1, (int)b.y - 1, 30, 30 );
            
            g.setColor( b.color );
            int x = (int)b.x + 11;
            int y = (int)b.y;
            
            g.drawLine(x+11, y+1, x+11, y+27);
            g.drawLine(x+10, y+1, x+10, y+27);
            g.drawLine(x+9,  y,   x+9, y+28);
            g.drawLine(x+8,  y,   x+8, y+28);
            
            g.fillRect(x, y, 8, 29);
            
            g.drawLine(x-1, y,   x-1, y+28);
            g.drawLine(x-2, y,   x-2, y+28);
            g.drawLine(x-3, y+1, x-3, y+27);
            g.drawLine(x-4, y+1, x-4, y+27);

            g.setColor( Color.WHITE );
            g.fillOval(dx, dy, 12, 12 );

            int bi = b.ballNumber-10;
            char[] num = { '1', Character.forDigit(bi, 10) }; 
            Font font = new Font("Courier New", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor( Color.BLACK );
            g.drawChars(num, 0, num.length, (int)StrictMath.round(b.x+8), (int)StrictMath.round(b.y+17) );
        }
        else if ( b.ballNumber == 9 ) {
            g.setColor( Color.WHITE );
            g.fillOval((int)b.x - 1, (int)b.y - 1, 30, 30 );
            
            g.setColor( b.color );
            int x = (int)b.x + 11;
            int y = (int)b.y;
            
            g.drawLine(x+11, y+1, x+11, y+27);
            g.drawLine(x+10, y+1, x+10, y+27);
            g.drawLine(x+9,  y,   x+9, y+28);
            g.drawLine(x+8,  y,   x+8, y+28);
            
            g.fillRect(x, y, 8, 29);
            
            g.drawLine(x-1, y,   x-1, y+28);
            g.drawLine(x-2, y,   x-2, y+28);
            g.drawLine(x-3, y+1, x-3, y+27);
            g.drawLine(x-4, y+1, x-4, y+27);

            g.setColor( Color.WHITE );
            g.fillOval(dx, dy, 12, 12 );

            char[] num = { Character.forDigit(b.ballNumber, 10) }; 
            Font font = new Font("Courier New", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor( Color.BLACK );
            g.drawChars(num, 0, num.length, (int)StrictMath.round(b.x+12), (int)StrictMath.round(b.y+17) );
        }
        else if ( b.ballNumber == 0 ) {
            g.setColor( Color.WHITE );
            g.fillOval((int)b.x - 1, (int)b.y - 1, 30, 30 );
        }
    }

	private void paintQiudais( Graphics g) {
		if ( qiudais == null ) createQiudais();
		try {
			for (Qiudai p : qiudais)
				paintQiudai(g, p);
			}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			paintQiudais(g); // retry so the disks never not get painted
        }
	}

	private void paintQiudai( Graphics g, Qiudai p) {
		if ( p == null ) return;
		g.setColor( p.color );
		g.fillOval(  p.x, p.y, p.size, p.size );
	}

	private void paintTray( Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		String trayTitle = "The Fallen Balls";
		char[] titleChars = trayTitle.toCharArray();
		//String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		
		Font font = new Font("Comic Sans MS", Font.BOLD, 20 );
		g2.setFont(font);
		g2.drawChars(titleChars, 0, titleChars.length , 450, 590);
		g2.setColor( Color.GRAY );
		g2.fill3DRect(299, 598, 450, 33, true);
		g2.setColor( Color.BLACK );
		g2.draw3DRect(298, 597, 451, 34, true);
	
		paintFallen( g );
	}
	
	public void paintFallen( Graphics g ) {
		try {
			for (Ball f : jinqiuBalls)
				paintBall(g, f);
		}
		catch (Exception ex) {
			System.out.println("ERROR IN paintFallen!: " + ex.getMessage());
		}
	}

	public void addToFallen( Ball c ) {
		System.out.println("Adding fallen ball: " + c.name );
		jinqiuBalls.add( c );
		int fallenCount = jinqiuBalls.size();
		c.x = 270+(fallenCount*31);
		c.y = 600;
	}
	
    private void paintQueueLine( Graphics g) {
		g.setColor( Color.WHITE );
		if ( queueLine == null ) return;
		g.drawLine( ((Integer)queueLine.get("x1")).intValue(),
					((Integer)queueLine.get("y1")).intValue(),
					((Integer)queueLine.get("x2")).intValue(),
					((Integer)queueLine.get("y2")).intValue());
	}


	private void paintTable( Graphics g ) {
		g.setColor(Color.black);
		//System.out.println("Painting table outline: ");
		g.drawRect(	yinqing.TABLE_OFFSET_X-30, yinqing.TABLE_OFFSET_Y-30, SIZEX+60, SIZEY+60);
		g.drawRect(	yinqing.TABLE_OFFSET_X, yinqing.TABLE_OFFSET_Y, SIZEX, SIZEY);
		
		int x25  = (int)StrictMath.round((SIZEX/4)   + yinqing.TABLE_OFFSET_X);
		int x75  = (int)StrictMath.round((SIZEX/4*3) + yinqing.TABLE_OFFSET_X);
		int midy = (int)StrictMath.round((SIZEY/2)   + yinqing.TABLE_OFFSET_Y);
		g.fillOval( x25-5, midy-5, 10, 10);
		g.fillOval( x75-5, midy-5, 10, 10);
		g.setColor( Color.white );
		g.fillOval( x25-2, midy-2, 4, 4);
		g.fillOval( x75-2, midy-2, 4, 4);

		paintQiudais( g );
		paintTray( g );
	}

    @Override
    public void paint(Graphics g) {
        // paint real panel stuff
        super.paint(g);

        //Make the table
        paintTable( g );

        // paint the disks
        paintBalls(g);
        if ( zhixiangqiu ) paintQueueLine( g );
    }

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
		if ( !ready ) {
			return;
		}

		System.out.println("I clicked at " + e.getPoint());

		//for some reason I need to subtract 10 pixels from each to get relative coords.
		int x = e.getPoint().x - 15;
		int y = e.getPoint().y - 15;

		//See if that point in inside a circle
		Ball k;
		for ( int j = 0 ; j < balls.size(); j++ ) {
			k = (Ball)balls.elementAt(j);
			int kx = (int)StrictMath.round(k.x);
			int ky = (int)StrictMath.round(k.y);
			int xDif = Math.abs( kx - x );
			int yDif = Math.abs( ky - y );
			int radius = (int)StrictMath.round(k.size/2);


			if ( xDif <= radius  && yDif <= radius ) {
				//System.out.println("I clicked inside: " + k.name + "!!!");
				if ( j == baiqiuidx && !movingQ ) zhixiangqiu = true;
				mubiaoqiu = j;
				k.beingDragged = true;
			}
		}
    }

    public void mouseDragged(MouseEvent e) {
		if ( !ready ) {
			return;
		}

		Ball queBall = (Ball)balls.elementAt( baiqiuidx );

		int mx = e.getX();
		int my = e.getY();
		if ( zhixiangqiu ) {
			queueLine = new HashMap<String, Integer>();
			queueLine.put("x1", (int)StrictMath.round(mx));
			queueLine.put("y1", (int)StrictMath.round(my));

			if ( yinqing.aimHelp ) {
				int bx = (int)StrictMath.round(queBall.x)+15;
				int by = (int)StrictMath.round(queBall.y)+15;
				double dx = bx - mx;
				double dy = by - my;
				double i = 1000 / ( Math.sqrt( dx*dx + dy*dy ) );
				double ex = ( dx*i) + mx;
				double ey = ( dy*i) + my;

				queueLine.put("x2", (int)StrictMath.round(ex));
				queueLine.put("y2", (int)StrictMath.round(ey));
			}
			else {
				queueLine.put("x2", (int)StrictMath.round(queBall.x)+15);
				queueLine.put("y2", (int)StrictMath.round(queBall.y)+15);
			}
		}
		else if ( movingQ ) {
			queBall.x = mx;
			queBall.y = my;
		}
    }

    public void mouseReleased(MouseEvent e) {
		if ( !ready ) {
			return;
		}

		Ball queBall = (Ball)balls.elementAt( baiqiuidx );

		System.out.println("Released the mouse at: (" + e.getX() + "," + e.getY() + ")!! aiming? " + zhixiangqiu);
		queueLine = null;

		if ( zhixiangqiu ) {
			double x1 = e.getX();
			double y1 = e.getY();
			double x2 = queBall.x+15;
			double y2 = queBall.y+15;


			//Calculate the Queball velocity based on the disance and angle
			//of the mouse at release from the center of the que ball.
			// The speed has to be a number between 0 and 10.

			double dx = (x2 - x1)/50;
			double dy = (y2 - y1)/50;
			System.out.println("X Distance=" + dx);
			System.out.println("Y Distance=" + dy);
			
			double k = 1; //( Math.abs(dx) > Math.abs(dy) ) ? 5/Math.abs(dx) : 5/Math.abs(dy);
			dx = dx * k;
			dy = dy * k;

			ready = false;

			queBall.dx = dx;
			queBall.dy = dy;

			System.out.println("Queue ball speed is: " + queBall.dx + ", " + queBall.dy);
			//movement = ( queBall.dx == 0 && queBall.dy == 0 ) ? false : true;

			yinqing.calcFriction( queBall );

			//System.out.println( q.toString() );
			zhixiangqiu = false;
			SoundEffect.QUE.play();
		}
		else if ( movingQ ) {
			movingQ = false;
			//queBall.x = e.getX()-15;
			//queBall.y = e.getY()-15;

			Ball j;
			for ( int a = 0 ; a < balls.size(); a++ ) {
				j = (Ball)balls.elementAt( a );

				if ( a != baiqiuidx ) {
					// fix possible overlapping
					double dx = queBall.x - j.x;
					double dy = queBall.y - j.y;
					double d2 = (dx*dx) + (dy*dy);
					double circleSize2 = (j.size/2 + j.size/2) * (j.size/2 + j.size/2);

				}
			}
		}
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {
		mymenu.setStatus( "Pointer is at: (" + e.getX() + "," + e.getY() + ")" );
		if ( movingQ ) {
			Ball queBall = (Ball)balls.elementAt( baiqiuidx );
			int mx = e.getX();
			int my = e.getY();

			double newX = queBall.x;
			double newY = queBall.y;
			
			double xRight = SIZEX + yinqing.TABLE_OFFSET_X - 15;
			double xLeft  = (SIZEX*0.75) + yinqing.TABLE_OFFSET_X;
			
			double yBottom = SIZEY + yinqing.TABLE_OFFSET_Y-15;
			double yTop    = yinqing.TABLE_OFFSET_Y+15;

			if ( mx > xRight ) newX = xRight;
			else if ( mx < xLeft  ) newX = xLeft;
			else newX = mx;
				
			if ( my > yBottom ) newY = yBottom; 
			else if ( my < yTop ) newY = yTop;
			else newY = my;
			
			queBall.x = newX-15;
			queBall.y = newY-15;
		}
	}

	public void run() {
        // repaint every 9 ms (~100 fps)
        while (true) {
            repaint();
            try {
                Thread.sleep(9, 1 );
            }
            catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}