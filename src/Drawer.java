
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.awt.Component;

public class Drawer implements Callable<Image>{
	
	int from;
	int to; 
	Dimension d;
	public Drawer(int from,int to, Dimension d) {
		// TODO Auto-generated constructor stub
		this.from=from;
		this.to=to;
		this.d=d;
	}
	@Override
	public Image call() throws Exception {
		// TODO Auto-generated method stub
		  Image tmp = new BufferedImage(to - from, d.height, BufferedImage.TYPE_INT_RGB);

		  Graphics g = tmp.getGraphics();

		  for (int y = 0; y < d.height; y++) {
		      for (int x = from; x < to; x++) {
		        double r = Mandel3para.zoom / Math.min(d.width, d.height);
		        double dx = 2.5 * (x * r + Mandel3para.viewX) - 2;
		        double dy = 1.25 - 2.5 * (y * r + Mandel3para.viewY);
		        Color color = color(dx, dy);
		        // computation of average color for antialiasing
		        if (Mandel3para.antialias) {
		          Color c1 = color(dx, dy + 0.5 * r);
		          Color c2 = color(dx + 0.5 * r, dy);
		          Color c3 = color(dx + 0.5 * r, dy + 0.5 * r);
		          int red = (color.getRed() + c1.getRed() + c2.getRed() + c3.getRed()) / 4;
		          int green = (color.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen()) / 4;
		          int blue = (color.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue()) / 4;
		          color = new Color(red, green, blue);
		        }
		        g.setColor(color);
		        g.drawLine(x-from, y, x-from, y); // draws point
		      }
		    }
		 
		  return tmp;
	}

	  // Computes a color for a given point
	  private Color color(double x, double y) {
	    int count = mandel(0.0, 0.0, x, y);
	    int palSize = Mandel3para.colors[Mandel3para.pal].length;
	    Color color = Mandel3para.colors[Mandel3para.pal][count / 256 % palSize];
	    if (Mandel3para.smooth) {
	      Color color2 = Mandel3para.colors[Mandel3para.pal][(count / 256 + palSize - 1) % palSize];
	      int k1 = count % 256;
	      int k2 = 255 - k1;
	      int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
	      int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
	      int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;
	      color = new Color(red, green, blue);
	    }
	    return color;
	  }	
	  // Computes a value for a given complex number
	  private int mandel(double zRe, double zIm, double pRe, double pIm) {
	    double zRe2 = zRe * zRe;
	    double zIm2 = zIm * zIm;
	    double zM2 = 0.0;
	    int count = 0;
	    while (zRe2 + zIm2 < 4.0 && count < Mandel3para.maxCount) {
	      zM2 = zRe2 + zIm2;
	      zIm = 2.0 * zRe * zIm + pIm;
	      zRe = zRe2 - zIm2 + pRe;
	      zRe2 = zRe * zRe;
	      zIm2 = zIm * zIm;
	      count++;
	    }
	    if (count == 0 || count == Mandel3para.maxCount)
	      return 0;
	    // transition smoothing
	    zM2 += 0.000000001;
	    return 256 * count + (int)(255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
	  }

}
