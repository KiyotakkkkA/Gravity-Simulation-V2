package com.physics.effects;

import java.awt.Color;
import java.util.ArrayList;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class Explosion {
    private boolean isActive = false;
    private static final int EFFECT_RADIUS = 300;
    private static final double FORCE_MULTIPLIER = 10.0;
    private static final int PARTICLE_COUNT = 30;

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public void createExplosion(int x, int y, ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive) return;

        // Создаем частицы взрыва
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 5 + Math.random() * 10;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(x, y, (float) vx, (float) vy, 100, 
                new Color((int)(Math.random() * 255), 100, 50), 5));
        }
        
        // Воздействуем на ближайшие шары
        for (Ball ball : balls) {
            double dx = ball.getX() - x;
            double dy = ball.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < EFFECT_RADIUS) {
                double force = (1.0 - distance / EFFECT_RADIUS) * FORCE_MULTIPLIER;
                double angle = Math.atan2(dy, dx);
                ball.addForce((float) (Math.cos(angle) * force), (float) (Math.sin(angle) * force));
            }
        }
    }
} 