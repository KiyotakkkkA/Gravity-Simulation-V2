package com.physics.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import com.physics.model.Ball;

public class TimeVortex {
    private final ExecutorService executor = EffectUtils.getExecutor();
    private boolean isActive = false;
    private final Random random = new Random();
    private final List<VortexParticle> particles = new ArrayList<>(200);
    private double phase = 0.0;
    private double spaceWarp = 1.0;
    
    // Буферы для рендеринга
    private BufferedImage particleBuffer;
    private BufferedImage spiralBuffer;
    private BufferedImage glowBuffer;
    
    private static final float PARTICLE_SPEED = 2.0f;
    private static final float PHASE_SPEED = 0.02f;
    private static final int INFLUENCE_RADIUS = 200;
    private static final int SPIRAL_COUNT = 8;
    private static final int POINTS_PER_SPIRAL = 100;
    
    private class VortexParticle {
        float x, y;
        float angle;
        float radius;
        float speed;
        float alpha;
        Color color;
        
        VortexParticle() {
            reset();
        }
        
        void reset() {
            angle = random.nextFloat() * 360;
            radius = random.nextFloat() * INFLUENCE_RADIUS;
            speed = PARTICLE_SPEED + random.nextFloat() * 2.0f;
            alpha = 0.7f + random.nextFloat() * 0.3f;
            updatePosition();
            updateColor();
        }
        
        void update() {
            radius += speed;
            angle += speed * 2;  // Частицы вращаются быстрее
            
            if (radius > INFLUENCE_RADIUS) {
                reset();
                radius = 0;
            }
            
            updatePosition();
            updateColor();
        }
        
        void updatePosition() {
            x = (float)(Math.cos(Math.toRadians(angle)) * radius);
            y = (float)(Math.sin(Math.toRadians(angle)) * radius);
        }
        
        void updateColor() {
            float hue = (spaceWarp > 0) ? 0.6f : 0.0f;  // Синий или красный
            color = EffectUtils.getColorFromCache(hue + random.nextFloat() * 0.2f);
        }
        
        void draw(Graphics2D g2d, float sx, float sy) {
            float x = sx +(float)(Math.cos(Math.toRadians(angle)) * radius);
            float y = sy +(float)(Math.sin(Math.toRadians(angle)) * radius);
            
            float size = 4 + (float)(Math.sin(phase + angle * 0.1) + 1) * 2;
            
            g2d.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(255 * alpha)
            ));
            
            g2d.fillOval((int)(x - size/2), (int)(y - size/2), 
                (int)size, (int)size);
        }
    }
    
    public TimeVortex() {
        initializeParticles();
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < 200; i++) {
            particles.add(new VortexParticle());
        }
    }
    
    private void ensureBufferSize(int width, int height) {
        if (particleBuffer == null || particleBuffer.getWidth() != width || particleBuffer.getHeight() != height) {
            particleBuffer = EffectUtils.createCompatibleImage(width, height, true);
            spiralBuffer = EffectUtils.createCompatibleImage(width, height, true);
            glowBuffer = EffectUtils.createCompatibleImage(width, height, true);
        }
    }
    
    public void setActive(boolean active) {
        if (active && !isActive) {
            initializeParticles();
        }
        isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setSpaceWarp(double warp) {
        this.spaceWarp = warp;
    }
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        
        // Параллельное обновление частиц
        List<List<VortexParticle>> batches = new ArrayList<>();
        int batchSize = particles.size() / Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < particles.size(); i += batchSize) {
            batches.add(particles.subList(i, 
                Math.min(i + batchSize, particles.size())));
        }
        
        EffectUtils.parallelProcess(batches, 1, batch -> {
            for (VortexParticle particle : batch) {
                particle.update();
            }
        });
    }
    
    public void draw(Graphics2D g2d, Point center) {
        if (!isActive) return;
        
        // Отрисовка эффекта временного вихря
        int x = center.x;
        int y = center.y;
        
        // Рисуем основной вихрь
        drawVortex(g2d, x, y);
        
        // Рисуем частицы
        for (VortexParticle particle : particles) {
            particle.draw(g2d, x, y);
        }
    }
    
    private void drawVortex(Graphics2D g2d, int centerX, int centerY) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Рисуем основные спирали
        drawSpirals(g2d, centerX, centerY);
        
        // Рисуем искажающие линии
        drawDistortionLines(g2d, centerX, centerY);
        
        // Рисуем энергетические кольца
        drawEnergyRings(g2d, centerX, centerY);
        
        // Рисуем вихревые частицы
        drawVortexParticles(g2d, centerX, centerY);
    }
    
    private void drawSpirals(Graphics2D g2d, int centerX, int centerY) {
        g2d.setStroke(new BasicStroke(2.0f));
        
        for (int s = 0; s < SPIRAL_COUNT; s++) {
            Path2D.Float path = new Path2D.Float();
            float baseAngle = (360.0f / SPIRAL_COUNT) * s + (float)(phase * 50);
            
            float startX = centerX + (float)(Math.cos(Math.toRadians(baseAngle)) * 10);
            float startY = centerY + (float)(Math.sin(Math.toRadians(baseAngle)) * 10);
            path.moveTo(startX, startY);
            
            for (int i = 1; i <= POINTS_PER_SPIRAL; i++) {
                float t = i / (float)POINTS_PER_SPIRAL;
                float radius = 10 + t * INFLUENCE_RADIUS;
                float angle = baseAngle + (spaceWarp > 0 ? t : -t) * 720 * (float)Math.abs(spaceWarp);
                
                // Добавляем волнистость
                float wave = (float)(Math.sin(t * 10 + phase * 5) * 10);
                radius += wave;
                
                float x = centerX + (float)(Math.cos(Math.toRadians(angle)) * radius);
                float y = centerY + (float)(Math.sin(Math.toRadians(angle)) * radius);
                
                path.lineTo(x, y);
            }
            
            float alpha = 0.3f + (float)(Math.sin(phase * 2 + s) + 1) * 0.2f;
            Color spiralColor = spaceWarp > 0 ? 
                new Color(0.0f, 0.5f, 1.0f, alpha) :  // Голубой
                new Color(1.0f, 0.3f, 0.0f, alpha);   // Оранжевый
                
            g2d.setColor(spiralColor);
            g2d.draw(path);
        }
    }
    
    private void drawDistortionLines(Graphics2D g2d, int centerX, int centerY) {
        g2d.setStroke(new BasicStroke(1.0f));
        int lines = 36;
        double angleStep = Math.PI * 2 / lines;
        
        for (int i = 0; i < lines; i++) {
            double angle = i * angleStep + phase;
            float distortion = (float)(Math.sin(angle * 3 + phase * 2) * 20);
            
            float x1 = centerX + (float)(Math.cos(angle) * (INFLUENCE_RADIUS * 0.3));
            float y1 = centerY + (float)(Math.sin(angle) * (INFLUENCE_RADIUS * 0.3));
            float x2 = centerX + (float)(Math.cos(angle + distortion * 0.02) * INFLUENCE_RADIUS);
            float y2 = centerY + (float)(Math.sin(angle + distortion * 0.02) * INFLUENCE_RADIUS);
            
            float alpha = 0.2f + (float)(Math.sin(phase * 3 + i) + 1) * 0.1f;
            g2d.setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
            g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
        }
    }
    
    private void drawEnergyRings(Graphics2D g2d, int centerX, int centerY) {
        int rings = 3;
        float baseRadius = INFLUENCE_RADIUS * 0.5f;
        
        for (int i = 0; i < rings; i++) {
            float t = (float)((phase * 0.5 + i / (float)rings) % 1.0);
            float radius = baseRadius + t * INFLUENCE_RADIUS * 0.5f;
            float alpha = (1.0f - t) * 0.5f;
            
            Color ringColor = spaceWarp > 0 ?
                new Color(0.0f, 0.7f, 1.0f, alpha) :
                new Color(1.0f, 0.5f, 0.0f, alpha);
                
            g2d.setColor(ringColor);
            g2d.setStroke(new BasicStroke(3.0f));
            
            // Рисуем искаженное кольцо
            Path2D.Float path = new Path2D.Float();
            int points = 60;
            for (int j = 0; j <= points; j++) {
                float angle = (float)(j * Math.PI * 2 / points);
                float distortion = (float)(Math.sin(angle * 6 + phase * 5) * 10);
                float r = radius + distortion;
                
                float x = centerX + (float)(Math.cos(angle) * r);
                float y = centerY + (float)(Math.sin(angle) * r);
                
                if (j == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            path.closePath();
            g2d.draw(path);
        }
    }
    
    private void drawVortexParticles(Graphics2D g2d, int centerX, int centerY) {
        for (VortexParticle particle : particles) {
            float size = 4 + (float)(Math.sin(phase + particle.angle * 0.1) + 1) * 2;
            float distanceRatio = particle.radius / INFLUENCE_RADIUS;
            float alpha = particle.alpha * (1.0f - distanceRatio);
            
            // Создаем свечение вокруг частицы
            RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Float(centerX + particle.x, centerY + particle.y),
                size * 2,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{
                    new Color(particle.color.getRed(), particle.color.getGreen(), particle.color.getBlue(), (int)(255 * alpha)),
                    new Color(particle.color.getRed(), particle.color.getGreen(), particle.color.getBlue(), (int)(127 * alpha)),
                    new Color(particle.color.getRed(), particle.color.getGreen(), particle.color.getBlue(), 0)
                }
            );
            
            g2d.setPaint(gradient);
            g2d.fillOval(
                (int)(centerX + particle.x - size),
                (int)(centerY + particle.y - size),
                (int)(size * 2), (int)(size * 2)
            );
        }
    }
    
    public void applyEffect(ArrayList<Ball> balls, Point center) {
        if (!isActive) return;
        
        for (Ball ball : balls) {
            double dx = ball.getX() - center.x;
            double dy = ball.getY() - center.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INFLUENCE_RADIUS) {
                double factor = 1.0 - distance / INFLUENCE_RADIUS;
                double angle = Math.atan2(dy, dx);
                double speed = Math.sqrt(ball.getVelocityX() * ball.getVelocityX() + ball.getVelocityY() * ball.getVelocityY());
                
                angle += factor * spaceWarp * 0.1;
                
                ball.setVelocityX((float) (speed * Math.cos(angle)));
                ball.setVelocityY((float) (speed * Math.sin(angle)));
            }
        }
    }
} 