# Parallelization of the Mandelbrot set generation 
*The parallelization has been done from a pre existing* [source code](http://java.rubikscube.info/s0urce/Mandel3.java)  
## Mandelbrot set [Wiki](https://en.wikipedia.org/wiki/Mandelbrot_set)
Mandelbrot set is the set of values of c in the complex plane for which the orbit of 0 under iteration of the quadratic map Z<sub>n+1</sub> = Z<sub>n</sub>&sup2; + c remains bounded. That is, a complex number c is part of the Mandelbrot set if, when starting with Z<sub>0</sub>= 0 and applying the iteration repeatedly, the absolute value of Z<sub>n+1</sub> remains bounded however large n gets. 
## Parallelization
The Mandelbrot set computation is fully parallelizable, as we check all the possible values for c (Z<sub>n+1</sub> = Z<sub>n</sub>&sup2; + c). There is no dependency between the computation of two different values of c, ideally we can parallelize the computation of all the values. The parallelization of the computation in n threads, each thread will compute a an interval of values defined by a master thread, the results will be returned to the master thread who will regroup all of them in a single image.
<p align="center">
 <img src="https://github.com/Ali-Ouahhabi/MandelbortSet/blob/master/MandelEx2threads.PNG"   align="middle" width="300" height="300" />
 <p align="center">threads computation area</p> 
</p>
