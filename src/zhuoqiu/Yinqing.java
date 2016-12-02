package zhuoqiu;

import java.awt.*;
import java.util.*;

/**
 * the engine that drives the game
 *
 * calculates placement, collisions, friction, etc of the circle
 *
 Issues:	When one ball is on the table with the Que ball, the aim is off.
 			The timing is off as less and less balls are on the table.
 */
public class Yinqing implements Runnable {

    Game table;	       // reference to table
    Vector<Ball> balls;
	public static final int TABLE_OFFSET_X = 100;
	public static final int TABLE_OFFSET_Y = 100;


    // options
    boolean friction = true, aimHelp  = true;


    // score keeping
    int move = 0,  // keep track of what "move" it is
        round = 1; // keep track of what round its on

    /*****************************************
     * Constuctor. pass it the table
     * this also starts new thread
     *
     * @param table the table the circles are on
     ****************************************/
    public Yinqing( Game table ) {
        this.table = table;
    }


	public void setCircles( Vector<Ball> balls ) {
		this.balls = balls;
	}



    /********************************************************************************
     * calculates the velocitys of 2 disks after they collide
     *
     * if they do not collide it will just retrun false
     *
     * works by saying the vector of the velocitiy of disk j due to i after the
     * collision is a similar triangle to the vector of the displacement from
     * disk i to j and the vector of disk i due to i is perpendicular to j
     *
     * the same consept is applied again to the disks but due to j. then the
     * final velocities of disks i and j are these velocities added together
     *
     * i like math :D
     *
     * @param i first circle
     * @param j second circle
     * @return true if they collide
     *******************************************************************************/
   	public static boolean calcCollision(Ball i, Ball j ) {
   		double dx = i.x - j.x;
   		double dy = i.y - j.y; 

   		double d2 =  dx * dx + dy * dy;
   		//double d2 = i.center.distanceSq(j.center);
   		
   		if ( d2 < 900 )  {
   			double kii, kji, kij, kjj;
   			kji = (dx * i.dx + dy * i.dy) / d2; // k of j due to i
   			kii = (dx * i.dy - dy * i.dx) / d2; // k of i due to i
   			kij = (dx * j.dx + dy * j.dy) / d2; // k of i due to j
   			kjj = (dx * j.dy - dy * j.dx) / d2; // k of j due to j

   			// set velocity of i
   			i.dy = kij * dy + kii * dx;
   			i.dx = kij * dx - kii * dy;

   			// set velocity of j
   			j.dy = kji * dy + kjj * dx;
   			j.dx = kji * dx - kjj * dy;

			SoundEffect.HIT.play();
   			return true;
   		}
   		return false;
   	}



	/*********************************************************************
	 * calculates the components of the friction on a disk
	 *
	 * friction is propertional to normal force, which is propertional
	 * to acceleration.
	 *
	 * k is the ratio between velocity and acceleration. this is used
	 * because friction always opposes motion so the acceleration is a
	 * similar triangle to the velocity. the friction has to be calculated
	 * this way otherwise it would change the direction of the sliding disks
	 *
	 *
	 * @param c the Circle
	 **********************************************************************/
	public void calcFriction( Ball c) {
		double k = (c.dx * c.dx + c.dy * c.dy);

		//System.out.println("Arena Friction is: " + Arena.CoF);
		k = Math.sqrt(k) * Game.CoF;

		//System.out.println("k is : " + k );
		// dividng by zero is bad
		if (k == 0) return;

		// set ddx and ddy
		c.ddy = -c.dy / k;
		c.ddx = -c.dx / k;
    }


    /**************************************************************
     * applys the force of friction on the sliding disk
     *
     * when dx and ddx have the same sign, it means they have changed to the
     * same direction. friction always opposes motion so when this happens,
     * it means the disk has been stopped due to friction.
     *
     * @param c the Circle
     *************************************************************/
    private void doFriction( Ball c ) {
        c.dy += c.ddy;
        c.dx += c.ddx;
        // if dx and ddx now have the same sign then stop
        if (c.dx > 0 == c.ddx > 0) {
            c.dx = 0;
            checkReady();
        }
        if (c.dy > 0 == c.ddy > 0) {
            c.dy = 0;
            checkReady();
        }
    }


	/**************************************************************
	*
	*
	*
	***************************************************************/
	private boolean ifQiudai( Ball c ) {

		if ( table.movingQ ) return false;
		
		for ( Qiudai p : table.qiudais ) {
			double pocketRadius = p.size/2;
			double circleRadius = c.size/2;

			double px = p.x+pocketRadius;
			double py = p.y+pocketRadius;

			double cx = c.x+circleRadius;
			double cy = c.y+circleRadius;

			double dx = cx-px;
			double dy = cy-py;
			double distance = Math.sqrt((dx*dx) + (dy*dy));
			if ( distance < p.size/2 ) {
				SoundEffect.SINK.play();
				checkReady();
				return true;
			}
		}
		return false;
	}


	/**************************************************************
	*
	*
	*
	***************************************************************/
	private void ifBounds( Ball c) {
		double xRight = Game.SIZEX + TABLE_OFFSET_X - 30;
		double yBottom = Game.SIZEY + TABLE_OFFSET_Y - 30;

		if (  c.x > xRight ) {
			c.dx  = -Math.abs( c.dx );
			c.ddx = Math.abs( c.ddx );
		}
		else if ( c.x < TABLE_OFFSET_X ) {
			c.dx  = Math.abs( c.dx );
			c.ddx = -Math.abs( c.ddx );
		}

		if ( c.y < TABLE_OFFSET_Y ) {
			c.dy  = Math.abs( c.dy );
			c.ddy = -Math.abs( c.ddy );
		}
		else if ( c.y > yBottom ) {
			c.dy  = -Math.abs( c.dy );
			c.ddy = Math.abs( c.ddy );
		}
	}



    /****************************************************************
     * calculates the new position of each disk
     *
     * works by adding ddx to dx and ddy to dy. then applys friction,
     * checks bounds, calcs collisions, fixes overlapping, etc etc
     *
     * returns boolean indicating still movement on board.
     ****************************************************************/
    public void calcPositions() {
        //try {
			//System.out.println("Calc Positions");
            int a, b;
            Ball i, j;

            for  ( a = 0 ; a < balls.size() ; a++ ) {
                i = (Ball)balls.elementAt( a );
                //System.out.println("Looking at the " + i.name);

                // check if circle is moving
                if (i.dy != 0 || i.dx != 0) {
                    i.y += i.dy;
                    i.x += i.dx;

                    if ( friction ) doFriction(i);
                }

				//check for the ball falling in a pocket
				if ( ifQiudai(i) ) {
					//System.out.println("A = " + a);
					//System.out.println("queBallIndex = " + table.queBallIndex );
					i.dx = 0;
					i.dy = 0;
					balls.remove(a);
					if ( a == table.baiqiuidx ) {
						table.scratched = true;
						System.out.println("You scratched the Que Ball!!");
						table.baiqiuidx = -1;
					}
					else {
						//Remove ball
						if ( !table.scratched ) {
							table.baiqiuidx = table.findBaiqiuIdx( balls );
						}
						System.out.println("The " + i.name + " fell in a hole!");
						table.addToFallen(i);
					}
				}
				else {
					// check bounces
					ifBounds(i);

					// check if circle has collided with any other circle after it
					// It's like this so circles dont collide twice
					for ( b = a+1; b < balls.size() ; b++ ) {
						j = (Ball)balls.elementAt( b );
						//See if they collided
						if (calcCollision(i, j)) {
							// Calculate new friction for both circles
							calcFriction(i);
							calcFriction(j);
						}
					}
				}
            }

            checkReady();

        //}
        //catch (Exception ex) {
        //    System.out.println("ERROR!!!  - " + ex.getMessage());
        //}
    }


	/*****************************************************
	*
	*
	*
	******************************************************/
	private void checkReady() {
		for (Ball c : balls)
			if (c.dx != 0 || c.dy != 0)
				return;

		if ( !table.ready ) System.out.println("We are now ready!!");
		table.ready = true;
		if ( table.scratched ) {
			table.mymenu.setStatus( "You scratched!!  You can move the Queue Ball." );
			table.readdQueueBall();
			table.movingQ = true;
			table.scratched = false;
		}
	}



    /************************************************************
     * toggles friction
     ***********************************************************/
    public void toggleAimHelp() {
        aimHelp = !aimHelp;
    }

    /************************************************************
     * toggles friction
     ***********************************************************/
    public void toggleFriction() {
        friction = !friction;
    }


    /***********************************************************
	 * calculates positions every 6 ms
	 *
	 * works by looping endlessly and timming how long it took
	 * to calc positions and subtract that time from 6 ms and
	 * waits that long before looping again. it has to be done
	 * this way or else calculation times would effect velocity
	 **********************************************************/
	public void run() {
		long time;

		while (true) {
			time = System.currentTimeMillis();
			calcPositions();
			time = System.currentTimeMillis() - time;

			// sleep for 6 seconds in total. subtract calculations time
			time = 6 - time;

			// so sleep is never passed a negitive time
			if (time < 0)
				time = 0;

			try {
				Thread.sleep(time, 1);
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

}