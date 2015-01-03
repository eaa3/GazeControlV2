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

import javax.security.sasl.RealmCallback;
import uk.ac.bham.view.GCFrame;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;


class ParticleComp implements Comparator<Particle>
{

	@Override
	public int compare(Particle arg0, Particle arg1) {

		return Double.compare(arg0.getWeight(), arg1.getWeight());
	}

}
public class ParticleFilterOld implements VisualEntity{

	private Vector<Particle> particles;
	private Vector<Particle> resampledParticles;
	private int n_particles;

	private Vector2 m, var;
	private Vector2 trueCenter;
	
	private Random r;
	public int currentNoise;

	private ParticleFilterOld(int n_particles)
	{
		this.particles = new Vector<Particle>();
		this.resampledParticles = new Vector<Particle>();
		this.n_particles = n_particles;
		
		this.m = this.var = null;
		
		this.r = new Random();
	}

	public ParticleFilterOld(int n_particles, Vector2 p0)
	{
		this.particles = new Vector<Particle>();
		this.resampledParticles = new Vector<Particle>();
		this.n_particles = n_particles;
		
		this.initialize(p0.getX(), p0.getY());
		
		this.m = this.var = null;
		
		this.r = new Random();

	}

	public void initialize(double x0, double y0)
	{
		this.trueCenter = new Vector2(x0,y0);
		this.particles.clear();
		this.resampledParticles.clear();

		for(int i = 0; i < n_particles; i ++)
		{
			this.particles.add(new Particle(x0, y0));
			this.resampledParticles.add(this.particles.get(i).getCpy());
		}
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
		double unc_x = var.getX();
		double unc_y = var.getY();
		double vx = - unc_x/2;
		double vy = - unc_y/2;
		
		g2d.setColor(Color.ORANGE);
		//Ellipse representing the uncertainty degree of the this object position (update unc_x and unc_y after updating the particle filter)
		g2d.drawOval((int)(m.getX() + vx),(int)(m.getY() + vy), (int)unc_x, (int)unc_y);

		g2d.setColor(Color.ORANGE);
		g2d.fillOval((int)m.getX(), (int)m.getY(), 5, 5);
		
		g2d.setColor(Color.BLUE);
		g2d.fillOval((int)this.getTrueCenter().getX(), (int)this.getTrueCenter().getY(), 5, 5);
		

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


	public synchronized Vector2 mean()
	{
		if( this.m == null )
		{
			this.m = new Vector2();
			double sum = 0;
			for(Particle p : this.particles)
			{
				sum += p.getWeight();
				m.add(p.getPos());
				//m.add(p.getPosition().getX()*p.getWeight(),p.getPosition().getY()*p.getWeight());
			}
			//m.mult(1.0/sum);
			
			//m.add(0.0001,0.0001);
			m.mult(1.0/this.particles.size());
			//System.out.println(m);
		}


		return m;		
	}

	public synchronized Vector2 variance()
	{
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

			var.mult(1.0f/this.particles.size());
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

		//Perform action
		data = this.actionFunc(data, r, Config.ACTION_NOISE);

		//Perform measurement after action
		Vector2 z = this.measurementFunc(data.getCpy(), r, Config.OBS_NOISE);

		Vector2 p = null;


		double sum = 0.0;
		for(int i = 0; i < this.particles.size(); i++)
		{
			p = this.particles.get(i).getPos();

			//Perform action on hypothesis
			p = this.actionFunc(p, r, Config.ACTION_NOISE);

			
			double weight =  ObservationModelFunc(z,p);
			
			this.particles.get(i).setWeight(weight);
			//this.particles.get(i).setPos(p);

			sum += weight;			
		}


		//Sort
		Collections.sort(this.particles,new ParticleComp());

		for(int i = 0; i < this.particles.size(); i++)
		{			
			Particle pt = resample(this.particles,sum);
			resampledParticles.get(i).setTo(pt);	
		}

		this.swap();
		var = null; m = null;

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
		return ps.get(i);
	}

	public void forget()
	{
		Random r = new Random();
		double rx, ry;
		for(Particle p : this.particles)
		{
			rx = r.nextGaussian()*Math.sqrt(1);
			ry = r.nextGaussian()*Math.sqrt(1);
			
			p.getPos().add(new Vector2(rx,ry));
		}

		var = null; m = null;
	}

	public Vector<Particle> getParticles()
	{
		return this.particles;
	}


	public ParticleFilterOld getCpy()
	{
		ParticleFilterOld cpy = new ParticleFilterOld(this.n_particles);

		Vector<Particle> particles = cpy.getParticles();
		Vector<Particle> resampledParticles = cpy.getResampledParticles();
		for(int i = 0; i < this.n_particles; i++)
		{
			particles.add(this.particles.get(i).getCpy());
			//particles.get(i).setColor(Color.GREEN);
			resampledParticles.add(this.particles.get(i).getCpy());
			
		}
		
		cpy.setTrueCenter(this.getTrueCenter());
		
		return cpy;
	}
	
	public Vector<Particle> getResampledParticles()
	{
		return this.resampledParticles;
	}

	public Vector2 actionFunc(Vector2 x,Random r, double NOISE)
	{
		Vector2 noise = new Vector2(Math.sqrt(NOISE)*r.nextGaussian(),Math.sqrt(NOISE)*r.nextGaussian());

		x.add(noise);

		return x;
	}

	public Vector2 measurementFunc(Vector2 x, Random r, double NOISE)
	{
		Vector2 noise = new Vector2(Math.sqrt(NOISE)*r.nextGaussian(),Math.sqrt(NOISE)*r.nextGaussian());
		
		
		//Measurement function option1
		x.setX(noise.getX() + (x.getX()*x.getX())/600);
		x.setY(noise.getY() + ((x.getY())*x.getY())/600);
		
		//Measurement function option2
		//x.setX(noise.getX() + (Math.log(x.getX())*x.getX())/40);
		//x.setY(noise.getY() + (Math.log(x.getY())*x.getY())/40);
		
		


		return x;
	}
	
	public double ObservationModelFunc(Vector2 z, Vector2 p)
	{
		double det = 2*2;//GCFrame.OBS_NOISE*GCFrame.OBS_NOISE;		
		
		//Perform measurement on hypothesis
		Vector2 z_h = this.measurementFunc(p.getCpy(), r, 0);


		Vector2 diff = z.subCpy(z_h);
		Vector2 diff_t = diff.multCpy(1.0/2);
		//System.out.println("DOT: " + diff.dot(diff_t)/2);
		//System.out.println("DIFF: " + diff.toString() + " DIFF_T: " + diff_t.toString());

		diff.mult(0.5);
		double dot = diff.dot(diff_t.mult(0.5));
		double exp = Math.exp(-dot);		
		
		
		double weight = (exp/(Math.sqrt(Math.pow(2*Math.PI, 2)*det)));
		
		
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
		//super.reinit(x0, y0);
		this.trueCenter.setX(x0);
		this.trueCenter.setY(y0);

		for(int i = 0; i < n_particles; i ++)
		{
			this.particles.get(i).reinit(this.trueCenter.getX(),this.trueCenter.getY());
		}
		this.m = this.var = null;
	}
	
	
	public void swap()
	{
		Vector<Particle> tmp_particles = this.particles;
		this.particles = this.resampledParticles;
		this.resampledParticles = tmp_particles;
	}

	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.mean();
	}

}
