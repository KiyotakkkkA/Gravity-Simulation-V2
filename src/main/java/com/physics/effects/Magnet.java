package com.physics.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import com.physics.model.Ball;
import com.physics.model.Particle;

public class Magnet {
    private boolean isActive = false;
    private final Random random = new Random();
    private Point2D.Float fieldCenter;
    private ArrayList<MagnetParticle> particles = new ArrayList<>();
    
    private static final int PARTICLE_COUNT = 100;
    private static final float FIELD_RADIUS = 200f;
    private static final float FORCE_MULTIPLIER = 10f;
    
    private class MagnetParticle {
        float x, y;
        float angle;
        float radius;
        float alpha = 1.0f;
        Color color;
        
        MagnetParticle() {
            reset();
        }
        
        void reset() {
            angle = random.nextFloat() * (float)(Math.PI * 2);
            radius = random.nextFloat() * FIELD_RADIUS;
            if (fieldCenter != null) {
                x = fieldCenter.x + (float)Math.cos(angle) * radius;
                y = fieldCenter.y + (float)Math.sin(angle) * radius;
            }
            alpha = 0.3f + random.nextFloat() * 0.7f;
            color = new Color(100, 100, 255);
        }
        
        void update() {
            if (fieldCenter == null) return;
            
            angle += 0.05f;
            x = fieldCenter.x + (float)Math.cos(angle) * radius;
            y = fieldCenter.y + (float)Math.sin(angle) * radius;
            alpha *= 0.99f;
            
            if (alpha < 0.1f) {
                reset();
            }
        }
        
        void draw(Graphics2D g2d) {
            Color particleColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(alpha * 255)
            );
            g2d.setColor(particleColor);
            g2d.fillOval((int)x - 2, (int)y - 2, 4, 4);
        }
    }
    
    public void setActive(boolean active, float x, float y) {
        if (active && !isActive) {
            fieldCenter = new Point2D.Float(x, y);
            initializeParticles();
        }
        isActive = active;
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new MagnetParticle());
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void update() {
        if (!isActive) return;
        
        for (MagnetParticle particle : particles) {
            particle.update();
        }
    }
    
    public void draw(Graphics2D g2d) {
        if (!isActive || fieldCenter == null) return;
        
        // Рисуем магнитное поле
        Point2D center = new Point2D.Float(fieldCenter.x, fieldCenter.y);
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(100, 100, 255, 100),
            new Color(100, 100, 255, 30),
            new Color(100, 100, 255, 0)
        };
        RadialGradientPaint paint = new RadialGradientPaint(
            center, FIELD_RADIUS, dist, colors
        );
        g2d.setPaint(paint);
        g2d.fillOval(
            (int)(fieldCenter.x - FIELD_RADIUS),
            (int)(fieldCenter.y - FIELD_RADIUS),
            (int)(FIELD_RADIUS * 2),
            (int)(FIELD_RADIUS * 2)
        );
        
        // Рисуем частицы
        for (MagnetParticle particle : particles) {
            particle.draw(g2d);
        }
    }
    
    public void applyEffect(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isActive || fieldCenter == null) return;
        
        // Применяем магнитные силы к шарам
        for (Ball ball : balls) {
            float dx = ball.getX() - fieldCenter.x;
            float dy = ball.getY() - fieldCenter.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < FIELD_RADIUS) {
                float force = (1.0f - dist / FIELD_RADIUS) * FORCE_MULTIPLIER;
                float angle = (float)Math.atan2(dy, dx);
                // Добавляем вращательное движение
                angle += (float)Math.PI / 2;
                
                ball.addForce(
                    (float)Math.cos(angle) * force,
                    (float)Math.sin(angle) * force
                );
            }
        }
        
        // Применяем магнитные силы к частицам
        for (Particle particle : particles) {
            float dx = particle.getX() - fieldCenter.x;
            float dy = particle.getY() - fieldCenter.y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist < FIELD_RADIUS) {
                float force = (1.0f - dist / FIELD_RADIUS) * FORCE_MULTIPLIER * 0.5f;
                float angle = (float)Math.atan2(dy, dx);
                angle += (float)Math.PI / 2;
                
                particle.addForce(
                    (float)Math.cos(angle) * force,
                    (float)Math.sin(angle) * force
                );
            }
        }
    }
} 