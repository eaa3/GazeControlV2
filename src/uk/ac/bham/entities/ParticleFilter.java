package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;






public class ParticleFilter implements VisualEntity{

	private Vector<Particle> particles;
	private ParticleFilter imaginaryPf;
	private Vector2 imaginaryMean;
	public double weights[];

	private int n_particles;

	private Vector2 m, var;
	private Vector2 trueCenter;

	private Random r;

	public double currentNoise;

	private ParticleFilter(int n_particles)
	{
		this.particles = new Vector<Particle>(n_particles);
		this.n_particles = n_particles;
		this.weights = new double[n_particles];

		this.m = this.var = null;

		this.r = new Random();
	}

	public ParticleFilter(int n_particles, Vector2 p0)
	{
		this.particles = new Vector<Particle>();
		this.n_particles = n_particles;
		this.weights = new double[n_particles];

		this.trueCenter = p0;

		this.initialize(p0.getX(), p0.getY());


		this.r = new Random();

	}

	public void initialize(double x0, double y0)
	{
		this.particles.clear();

		for(int i = 0; i < n_particles; i ++)
		{
			this.particles.add(new Particle(x0, y0));
		}

		this.m = this.var = null;
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		Graphics2D g2d =  (Graphics2D) g.create();

		for(Particle p : this.particles)
		{
			p.paint(g);
		}



		Vector2 m = this.mean();
		Vector2 var = this.variance();

		if( m!=null && var !=null )
		{
			double unc_x = var.getX();
			double unc_y = var.getY();
			double vx = - unc_x/2;
			double vy = - unc_y/2;

			g2d.setColor(Color.ORANGE);
			//Ellipse representing the uncertainty degree of the this object position (update unc_x and unc_y after updating the particle filter)
			g2d.drawOval((int)(m.getX() + vx),(int)(m.getY() + vy), (int)unc_x, (int)unc_y);

			g2d.setColor(Color.ORANGE);
			g2d.fillOval((int)m.getX(), (int)m.getY(), 5, 5);
		}


		g2d.dispose();
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 1;
	}


	public Vector2 mean()
	{
		synchronized (this) {
			if( this.m == null )
			{
				this.m = new Vector2();
				double sum = 0;
				for(Particle p : this.particles)
				{
					//sum += (p.getWeight()+1);
					m.add(p.getPos());
					//m.add(p.getPos().getX()*(p.getWeight()+1),p.getPos().getY()*(p.getWeight()+1));
				}
				//m.mult(1.0/sum);
				m.div(this.particles.size());

			}
		}

		return m;		
	}

	public Vector2 variance()
	{
		synchronized (this) {
			if( var == null ) {
				Vector2 m = this.mean();
				this.var = new Vector2();

				double sqr_diff_x = 0;
				double sqr_diff_y = 0;
				for(Particle p : this.particles)
				{
					sqr_diff_x = (p.getPos().getX() - m.getX());
					sqr_diff_x *= sqr_diff_x;
					var.setX( var.getX() + sqr_diff_x);

					sqr_diff_y = (p.getPos().getY() - m.getY());
					sqr_diff_y *= sqr_diff_y;
					var.setY( var.getY() + sqr_diff_y);
				}

				var.div(this.particles.size());
			}
		}

		return var;
	}

	public double overallUnc()
	{
		double unc = 0;
		Vector2 m = this.mean();
		for(Particle p : this.particles)
		{
			unc += p.getPos().dist(m);
		}

		unc /= this.particles.size();

		return unc;

	}

	public void translate(Vector2 other)
	{
		for(Particle p : this.particles)
		{
			p.getPos().add(other);
		}

		this.m = null; this.var = null;
	}




	public void updateEstimates(Vector2 data)
	{

		//		 for i=(1:N_HIPOS),
		//			        
		//			        % Just performs the same random action on the ith hipothesis
		//			        hipos_update{i} = hipos{i};%+ sqrt(ACTION_NOISE)*randn(2,1);
		//			        
		//			        % Now performs a measurement on the ith hipothesis
		//			        h = hipos_update{i} .^ 2;
		//			        z_update{i} = h/20 ;
		//			        
		//			  
		//			        weight(i) = 1/sqrt((2*pi)^2*det(K)) * exp(-(z - z_update{i})'*inv(K)*(z - z_update{i})/2);
		//			  
		//			    end		

		synchronized (this) {


			//Perform action
			data = this.addNoise(data, r, currentNoise);

			//Perform measurement after action
			//Vector2 z = this.measurementFunc(data.getCpy(), r, Config.OBS_NOISE);

			Vector2 p = null;


			double sum = 0.0;
			for(int i = 0; i < this.particles.size(); i++)
			{
				p = this.particles.get(i).getPos().getCpy();

				//Perform action on hypothesis
				p = this.addNoise(p, r, currentNoise);
				//p = this.MotionModelFunc(p);


				double weight = ObservationModelFunc(data,p);
				weights[i] = weight;

				this.particles.get(i).setWeight(weight);
				this.particles.get(i).setPos(p);

				sum += weight;


			}

			//Sort
			//Collections.sort(this.particles,new ParticleComp());

			Vector<Particle> resampledParticles = new Vector<Particle>(this.particles.size());
			for(int i = 0; i < this.particles.size(); i++)
			{

				Particle pt = Math.random() >= 0.5? whichParticle(Math.random(), sum)/*resample(this.particles,sum)*/ : this.particles.get(i).getCpy();
				resampledParticles.add(pt);

			}


			this.particles = resampledParticles;
			var = null; m = null;

		}

	}

	public Particle whichParticle(double randPt, double sumW)
	{
		int index;
		double minLimit, maxLimit;

		minLimit = 0;
		maxLimit = weights[0] / sumW;
		for (index = 0; index < n_particles; index++)
		{
			//cout << "minLimit: " << minLimit << "   maxLimit: " << maxLimit << endl;
			// If the random point is in this range return the index
			if (randPt >= minLimit && randPt <= maxLimit)
			{
				return this.particles.get(index).getCpy();
			}

			if (index+1 == n_particles-1)
			{
				return this.particles.get(index+1).getCpy();
			}

			minLimit = maxLimit;
			maxLimit += weights[index+1] / sumW;
		}
		return this.particles.get(0).getCpy();
	}

	public Particle resample(Vector<Particle> ps, double limit)
	{
		double r = Math.random()*limit;
		double sum = 0.0;
		int i = 0;
		for( i = 0; i < ps.size(); i++)
		{
			sum += ps.get(i).getWeight();

			if( r < sum )
			{

				break;
			}

		}

		if( i == ps.size() ) i--;


		//System.out.println("Resampled particle pos: " + ps.get(i).getPosition().toString() + " Weight: " + weights[i]);
		return ps.get(i).getCpy();
	}

	public void forget()
	{
		synchronized (this) {

			Random r = new Random();
			double rx, ry;
			Vector2 noise = new Vector2(Math.sqrt(Config.FORGET_NOISE)*r.nextGaussian(),Math.sqrt(Config.FORGET_NOISE)*r.nextGaussian());

			for(Particle p : this.particles)
			{
				p.getPos().add(noise);
			}

			var = null; m = null;
		}
	}

	public Vector<Particle> getParticles()
	{
		return this.particles;
	}


	public ParticleFilter getCpy()
	{
		ParticleFilter cpy = null;

		synchronized (this) {


			cpy = new ParticleFilter(this.n_particles);
			cpy.setTrueCenter(this.getTrueCenter());


			Vector<Particle> cpy_particles = cpy.getParticles();

			for(int i = 0; i < this.n_particles; i++)
			{
				cpy_particles.add(this.particles.get(i).getCpy());
				cpy_particles.get(i).setColor(Color.GREEN);
			}

		}

		return cpy;
	}

	public Vector2 addNoise(Vector2 x, Random r, double NOISE)
	{
		Vector2 noise = new Vector2(Math.sqrt(NOISE)*r.nextGaussian(),Math.sqrt(NOISE)*r.nextGaussian());

		x.add(noise);

		return x;
	}



	//public Vector2 

	public Vector2 measurementFunc(Vector2 x, Random r, double NOISE)
	{
		Vector2 noise = new Vector2(Math.sqrt(NOISE)*r.nextGaussian(),Math.sqrt(NOISE)*r.nextGaussian());

		//Measurement function option0

		//x.setX((x.getX()*x.getX())/320.0);
		//x.setY(((x.getY())*x.getY())/240.0);

		//Measurement function option1

		//x.setX(noise.getX() + (x.getX()*x.getX())/300.0);
		//x.setY(noise.getY() + ((x.getY())*x.getY())/300.0);


		//Measurement function option2
		//x.setX(noise.getX() + (Math.log(x.getX())*x.getX())/100);
		//x.setY(noise.getY() + (Math.log(x.getY())*x.getY())/100);

		//Measurement function option3

		//x.setX(noise.getX() + Math.log(x.getX()));
		//x.setY(noise.getY() + Math.log(x.getY()));



		//this.addNoise(x, r, NOISE);
		return x;
	}


	public Vector2 MotionModelFunc(Vector2 x)
	{
		return this.addNoise(x, r, Config.ACTION_NOISE);
	}

	// The probability density function (PDF) is the importance factor
	double computePDF(double partX, double partY, double obsX, double obsY, double sigmaX, double sigmaY, double sigmaXY)
	{
		double Q, rho, stdx, stdy, pdf;
		double mean[] = new double[2];
		// The particle is the mean of the distribution
		mean[0] = partX;
		mean[1] = partY;

		// Sigma is given by getCholeskySigmaMatrix() method executed in the resampling method

		stdx = Math.sqrt(sigmaX);
		stdy = Math.sqrt(sigmaY);
		rho = sigmaXY / (stdx * stdy);

		Q = (Math.pow(obsX-mean[0], 2) / sigmaX) + (Math.pow(obsY - mean[1], 2) / sigmaY) - ( (2 * rho * (obsX-mean[0]) * (obsY-mean[1])) / (stdx * stdy) );
		Q = ( -1 / (2 * (1 - Math.pow(rho, 2))) ) * Q;
		Q = Math.exp(Q);
		pdf = (1 / ( 2 * 3.141592 * stdx * stdy * Math.sqrt(1 - Math.pow(rho, 2)))) * Q;

		return pdf;
	}
	public double ObservationModelFunc(Vector2 z, Vector2 p)
	{
		double det = Config.OBS_NOISE*Config.OBS_NOISE;		

		//Perform measurement on hypothesis
		Vector2 z_h = this.measurementFunc(p.getCpy(), r, 0);


		Vector2 diff = z.subCpy(z_h);
		Vector2 diff_t = diff.divCpy(Config.OBS_NOISE);
		//System.out.println("DOT: " + diff.dot(diff_t)/2);
		//System.out.println("DIFF: " + diff.toString() + " DIFF_T: " + diff_t.toString());

		diff.mult(0.5);
		double dot = diff.dot(diff_t.div(2));
		double exp = Math.exp(-dot);		


		double weight = (exp/(Math.sqrt(4*Math.PI*Math.PI*det)));


		return weight;
	}

	public Vector2 getTrueCenter()
	{
		return this.trueCenter;
	}

	public void setTrueCenter(Vector2 trueCenter)
	{
		this.trueCenter = trueCenter;
	}

	public void reinit(double x0, double y0)
	{
		this.trueCenter.setX(x0);
		this.trueCenter.setY(y0);

		for(int i = 0; i < n_particles; i ++)
		{
			this.particles.get(i).reinit(this.trueCenter.getX(),this.trueCenter.getY());
		}
		this.m = this.var = null;
	}

	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.mean();
	}


	public void initImaginaryPf()
	{
		this.imaginaryPf = null;
		this.imaginaryPf = this.getCpy();

		//this.imaginaryObs = new 
		this.imaginaryMean = this.particles.get((int)(this.particles.size()*Math.random())).getPos().getCpy();



	}

	public void imaginaryUpdate()
	{

		//this.initImaginaryPf();
		/*PerceptualMotorSystem pms = EntityPool.robot.getPMS();
		Vector2 currentEyePos = pms.getCurrentEyePos();
		Vector2 y_axis = pms.transform(new Vector2(0,-100).add(pms.getEyePos())).sub(currentEyePos);
		Vector2 vtmp = this.imaginaryMean.getCpy();
		vtmp.sub(currentEyePos);


		double ang = Math.abs(vtmp.degreesAngle(y_axis));*/
		//Imaginary observation
		this.imaginaryPf.currentNoise = 0;//Config.noiseOffset + ((ang*ang)/Config.ROBOT_HALF_FOV)*Config.ACUITY_NOISE;

		this.imaginaryPf.updateEstimates(this.imaginaryMean);


	}


	public ParticleFilter getImaginaryPf()
	{
		return this.imaginaryPf;
	}

	public void clearImagination()
	{
		this.imaginaryPf = null;
	}

}
