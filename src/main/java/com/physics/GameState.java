package com.physics;

import java.awt.*;
import java.util.ArrayList;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class GameState {
    private static class BallState {
        double x, y, vx, vy, radius;
        Color color;
        
        BallState(double x, double y, double vx, double vy, double radius, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
        }
        
        Ball toBall() {
            Ball ball = new Ball((float)x, (float)y, (float)radius);
            ball.setVelocityX((float)vx);
            ball.setVelocityY((float)vy);
            ball.setColor(color);
            return ball;
        }
    }
    
    private static class ParticleState {
        double x, y, vx, vy;
        int lifetime, size;
        Color color;
        
        ParticleState(double x, double y, double vx, double vy, int lifetime, Color color, int size) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.lifetime = lifetime;
            this.color = color;
            this.size = size;
        }
        
        Particle toParticle() {
            return new Particle((float)x, (float)y, (float)vx, (float)vy, lifetime, color, (int)size);
        }
    }
    
    private ArrayList<BallState> balls = new ArrayList<>();
    private ArrayList<ParticleState> particles = new ArrayList<>();
    
    public void addBall(double x, double y, double vx, double vy, double radius, Color color) {
        balls.add(new BallState(x, y, vx, vy, radius, color));
    }
    
    public void addParticle(double x, double y, double vx, double vy, int lifetime, Color color, int size) {
        particles.add(new ParticleState(x, y, vx, vy, lifetime, color, size));
    }
    
    public ArrayList<Ball> getBalls() {
        ArrayList<Ball> result = new ArrayList<>();
        for (BallState state : balls) {
            result.add(state.toBall());
        }
        return result;
    }
    
    public ArrayList<Particle> getParticles() {
        ArrayList<Particle> result = new ArrayList<>();
        for (ParticleState state : particles) {
            result.add(state.toParticle());
        }
        return result;
    }
} 