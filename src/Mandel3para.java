
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.JLabel;

public final class Mandel3para extends JFrame
 implements MouseListener, MouseMotionListener, KeyListener {
  public static int maxCount = 192;
  public static boolean smooth = false;
  public static boolean antialias = false;
  private boolean drag = false;
  private boolean toDrag = false;
  private boolean rect = true, oldRect = true;
  public  static  Color[][] colors;
  public static int pal = 0;
  public static double viewX = 0.0;
  public static double viewY = 0.0;
  public static double zoom = 1.0;

  private int mouseX, mouseY;
  private int dragX, dragY, oldX, oldY;
  
  volatile int processors;
  long time=12345670 ;

  public static final int[][][] colpal = {
    { {0, 10, 20}, {50, 100, 240}, {20, 3, 26}, {230, 60, 20},
      {25, 10, 9}, {230, 170, 0}, {20, 40, 10}, {0, 100, 0},
      {5, 10, 10}, {210, 70, 30}, {90, 0, 50}, {180, 90, 120},
      {0, 20, 40}, {30, 70, 200} },
    { {70, 0, 20}, {100, 0, 100}, {255, 0, 0}, {255, 200, 0} },
    { {40, 70, 10}, {40, 170, 10}, {100, 255, 70}, {255, 255, 255} },
    { {0, 0, 0}, {0, 0, 255}, {0, 255, 255}, {255, 255, 255}, {0, 128, 255} },
    { {0, 0, 0}, {255, 255, 255}, {128, 128, 128} },
  };
 
  public  Mandel3para(){
	  processors = Runtime.getRuntime().availableProcessors();
	  this.init();
  }
  
  public  Mandel3para(int n){
	  processors = n;
	  this.init();
  }
  public void init() {
	
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setSize(500,500);

	addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    // initialize color palettes
    colors = new Color[colpal.length][];
    for (int p = 0; p < colpal.length; p++) {
      colors[p] = new Color[colpal[p].length * 12];
      for (int i = 0; i < colpal[p].length; i++) {
        int[] c1 = colpal[p][i];
        int[] c2 = colpal[p][(i + 1) % colpal[p].length];
        for (int j = 0; j < 12; j++)
          colors[p][i * 12 + j] = new Color(
              (c1[0] * (11 - j) + c2[0] * j) / 11,
              (c1[1] * (11 - j) + c2[1] * j) / 11,
              (c1[2] * (11 - j) + c2[2] * j) / 11);
      }
    }
	  setVisible(true);

  }

  public void paint(Graphics g) {
	
	long time1 =System.currentTimeMillis();
    
	Dimension size = getSize();
    // select-rectangle or offset-line drawing
    if (drag) {
      g.setColor(Color.black);
      g.setXORMode(Color.white);
      if (oldRect) {
        int x = Math.min(mouseX, oldX);
        int y = Math.min(mouseY, oldY);
        int w = Math.max(mouseX, oldX) - x;
        int h = Math.max(mouseY, oldY) - y;
        double r = Math.max((double)w / size.width, (double)h / size.height);
        g.drawRect(x, y, (int)(size.width * r), (int)(size.height * r));
      }
      else
        g.drawLine(mouseX, mouseY, oldX, oldY);
      if (rect) {
        int x = Math.min(mouseX, dragX);
        int y = Math.min(mouseY, dragY);
        int w = Math.max(mouseX, dragX) - x;
        int h = Math.max(mouseY, dragY) - y;
        double r = Math.max((double)w / size.width, (double)h / size.height);
        g.drawRect(x, y, (int)(size.width * r), (int)(size.height * r));
      }
      else
        g.drawLine(mouseX, mouseY, dragX, dragY);
      oldX = dragX;
      oldY = dragY;
      oldRect = rect;
      drag = false;
      return;
    }
    
    //-------------------------------parallelization-------------------------------------------------
	
    
    if(this.processors<=0)
    	this.processors = Runtime.getRuntime().availableProcessors();
    
    Future<Image>[] fw = new Future[processors];
    Callable<Image> []drawers = new Callable[processors];
	ExecutorService executor = Executors.newFixedThreadPool(processors);
	
	
    for(int i = 0 ; i< processors ; i++) {
    	drawers[i] = new Drawer(i*size.width/processors, (i+1)*size.width/processors, size);
    	fw[i] = executor.submit(drawers[i]);
    }
    
    // fractal image drawing
    Image [] tmp = new Image[processors];
    for(int i = 0 ; i< processors ; i++) {
    	try {
			tmp[i] = fw[i].get();

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    for(int i = 0 ; i< processors ; i++) 
	    g.drawImage(tmp[i], i*size.width/processors, 0, null);
    
    this.time = System.currentTimeMillis() - time1;
    
    this.setTitle("\t\t size="+size.width+"x"+size.height+"\t [T]="+processors+"\t time="+time+"ms");

    
    //----------------------------------------------------------------------------------------------
  }

   // methods from MouseListener interface

  public void mousePressed(MouseEvent e) {
    mouseX = dragX = oldX = e.getX();
    mouseY = dragY = oldY = e.getY();
    toDrag = true;
  }

  public void mouseReleased(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
      if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) { // moved
        int width = getSize().width;
        int height = getSize().height;
        viewX += zoom * (mouseX - x) / Math.min(width, height);
        viewY += zoom * (mouseY - y) / Math.min(width, height);
        repaint();
      }
      else if (x != mouseX && y != mouseY) { // zoomed
        int width = getSize().width;
        int height = getSize().height;
        int mx = Math.min(x, mouseX);
        int my = Math.min(y, mouseY);
        viewX += zoom * mx / Math.min(width, height);
        viewY += zoom * my / Math.min(width, height);
        int w = Math.max(x, mouseX) - mx;
        int h = Math.max(y, mouseY) - my;
        double r = Math.max((double)w / width, (double)h / height);
        zoom *= r;
        drag = false;
        repaint();
      }
    }
    else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
      maxCount += maxCount / 4;
      repaint();
    }
    toDrag = false;
  }

  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  // methods from MouseMotionListener interface

  public void mouseDragged(MouseEvent e) {
    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
      dragX = e.getX();
      dragY = e.getY();
      drag = true;
      repaint();
    }
  }
  public void mouseMoved(MouseEvent e) {}

  // methods from KeyListener interface

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // init
      maxCount = 192;
      viewX = viewY = 0.0;
      zoom = 1.0;
      repaint();
    }
    else if (e.getKeyCode() == KeyEvent.VK_O) { // zoom out
      viewX -= 0.5 * zoom;
      viewY -= 0.5 * zoom;
      zoom *= 2.0;
      repaint();
    }
    else if (e.getKeyCode() == KeyEvent.VK_P) { // next palette
      pal = (pal + 1) % colors.length;
      repaint();
    }
    else if (e.getKeyCode() == KeyEvent.VK_S) { // smoothing
      smooth = !smooth;
      repaint();
    }
    else if (e.getKeyCode() == KeyEvent.VK_A) { // antialiasing
      antialias = !antialias;
      repaint();
    }
    else if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // move/zoom mode
      if (rect == true) {
        oldRect = true;
        rect = false;
        if (toDrag) {
          drag = true;
          repaint();
        }
      }
    }
    //----------------------------------ADDED KEYEVENTS--------------------------------------------
    else if (e.getKeyCode() == KeyEvent.VK_I) { //zoom in
        viewX += 0.5 * zoom;
        viewY += 0.5 * zoom;
        zoom /= 2.0;
        repaint();
	}else if (e.getKeyCode() == KeyEvent.VK_T) { // decrement threads number
		this.processors--;
        repaint();
	}else if (e.getKeyCode() == KeyEvent.VK_Y) { // increment threads number
		this.processors++;
        repaint();
	}
    //--------------------------------------------------------------------------------------------
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // move/zoom mode
      if (rect == false) {
        oldRect = false;
        rect = true;
        if (toDrag) {
          drag = true;
          repaint();
        }
      }
    }
  }

  public void keyTyped(KeyEvent e) {}
  
  public static void main(String[] args) {
	Mandel3para a = new Mandel3para();
	//Mandel3para a = new Mandel3para(1);
}
}