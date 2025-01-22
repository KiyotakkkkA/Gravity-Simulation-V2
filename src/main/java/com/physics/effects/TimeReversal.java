package com.physics.effects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.physics.GameState;
import com.physics.model.Ball;
import com.physics.model.Particle;

public class TimeReversal {
    private final ExecutorService executor = EffectUtils.getExecutor();
    private boolean isActive = false;
    private final Random random = new Random();
    private final List<ReversalParticle> particles = new ArrayList<>(200);
    private double phase = 0.0;
    
    // Буферы для рендеринга
    private BufferedImage particleBuffer;
    private BufferedImage trailBuffer;
    private BufferedImage glowBuffer;
    
    private static final float PARTICLE_SPEED = 2.0f;
    private static final float PHASE_SPEED = 0.02f;
    private static final int INFLUENCE_RADIUS = 300;
    private static final int TRAIL_COUNT = 10;
    private static final int POINTS_PER_TRAIL = 100;
    
    private boolean isReversing = false;
    private List<GameState> stateHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 300; // 5 секунд при 60 FPS
    
    private class ReversalParticle {
        float x, y;
        float angle;
        float radius;
        float speed;
        float alpha;
        Color color;
        
        ReversalParticle() {
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
            float hue = 0.8f + random.nextFloat() * 0.2f;  // Оттенки фиолетового
            color = EffectUtils.getColorFromCache(hue);
        }
    }
    
    public TimeReversal() {
        initializeParticles();
    }
    
    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < 200; i++) {
            particles.add(new ReversalParticle());
        }
    }
    
    private void ensureBufferSize(int width, int height) {
        if (particleBuffer == null || particleBuffer.getWidth() != width || particleBuffer.getHeight() != height) {
            particleBuffer = EffectUtils.createCompatibleImage(width, height, true);
            trailBuffer = EffectUtils.createCompatibleImage(width, height, true);
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
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        
        // Параллельное обновление частиц
        List<List<ReversalParticle>> batches = new ArrayList<>();
        int batchSize = particles.size() / Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < particles.size(); i += batchSize) {
            batches.add(particles.subList(i, 
                Math.min(i + batchSize, particles.size())));
        }
        
        EffectUtils.parallelProcess(batches, 1, batch -> {
            for (ReversalParticle particle : batch) {
                particle.update();
            }
        });
    }
    
    public void draw(Graphics2D g2d, int mouseX, int mouseY, int width, int height) {
        if (!isActive) return;
        
        ensureBufferSize(width, height);
        
        // Очищаем буферы
        Graphics2D particleG2d = particleBuffer.createGraphics();
        Graphics2D trailG2d = trailBuffer.createGraphics();
        Graphics2D glowG2d = glowBuffer.createGraphics();
        
        try {
            particleG2d.setBackground(new Color(0, 0, 0, 0));
            trailG2d.setBackground(new Color(0, 0, 0, 0));
            glowG2d.setBackground(new Color(0, 0, 0, 0));
            
            particleG2d.clearRect(0, 0, width, height);
            trailG2d.clearRect(0, 0, width, height);
            glowG2d.clearRect(0, 0, width, height);
            
            // Рендерим каждый слой параллельно
            Future<?> particleFuture = executor.submit(() -> 
                drawParticles(particleG2d, mouseX, mouseY));
            Future<?> trailFuture = executor.submit(() -> 
                drawTrails(trailG2d, mouseX, mouseY));
            Future<?> glowFuture = executor.submit(() -> 
                drawGlow(glowG2d, mouseX, mouseY));
            
            // Ждем завершения рендеринга
            try {
                particleFuture.get();
                trailFuture.get();
                glowFuture.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Композитинг слоев
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(glowBuffer, 0, 0, null);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.drawImage(trailBuffer, 0, 0, null);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.drawImage(particleBuffer, 0, 0, null);
            
        } finally {
            particleG2d.dispose();
            trailG2d.dispose();
            glowG2d.dispose();
        }
    }
    
    private void drawParticles(Graphics2D g2d, int centerX, int centerY) {
        // Параллельная отрисовка частиц
        List<List<ReversalParticle>> batches = new ArrayList<>();
        int batchSize = particles.size() / Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < particles.size(); i += batchSize) {
            batches.add(particles.subList(i, 
                Math.min(i + batchSize, particles.size())));
        }
        
        EffectUtils.parallelProcess(batches, 1, batch -> {
            Graphics2D batchG2d = (Graphics2D)g2d.create();
            try {
                for (ReversalParticle particle : batch) {
                    float size = 4 + (float)(Math.sin(phase + particle.angle * 0.1) + 1) * 2;
                    float distanceRatio = particle.radius / INFLUENCE_RADIUS;
                    float alpha = particle.alpha * (1.0f - distanceRatio);
                    
                    batchG2d.setColor(new Color(
                        particle.color.getRed(),
                        particle.color.getGreen(),
                        particle.color.getBlue(),
                        (int)(255 * alpha)
                    ));
                    
                    batchG2d.fillOval(
                        (int)(centerX + particle.x - size/2),
                        (int)(centerY + particle.y - size/2),
                        (int)size, (int)size
                    );
                }
            } finally {
                batchG2d.dispose();
            }
        });
    }
    
    private void drawTrails(Graphics2D g2d, int centerX, int centerY) {
        g2d.setStroke(new BasicStroke(2.0f));
        
        for (int t = 0; t < TRAIL_COUNT; t++) {
            Path2D.Float path = new Path2D.Float();
            float trailPhase = (float)phase + (float)t / TRAIL_COUNT;
            float baseAngle = trailPhase * 360;
            
            for (int i = 0; i <= POINTS_PER_TRAIL; i++) {
                float t2 = i / (float)POINTS_PER_TRAIL;
                float radius = t2 * INFLUENCE_RADIUS;
                float angle = baseAngle + t2 * 720;  // Двойная спираль
                
                float x = centerX + (float)(Math.cos(Math.toRadians(angle)) * radius);
                float y = centerY + (float)(Math.sin(Math.toRadians(angle)) * radius);
                
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            
            float alpha = 0.7f - (t / (float)TRAIL_COUNT) * 0.5f;
            alpha *= 0.7f + (float)(Math.sin(phase * 5 + t) + 1) * 0.15f;
            
            g2d.setColor(new Color(0.8f, 0.4f, 1.0f, alpha));  // Фиолетовый цвет
            g2d.draw(path);
        }
    }
    
    private void drawGlow(Graphics2D g2d, int centerX, int centerY) {
        float intensity = 0.3f + (float)(Math.sin(phase * 3) + 1) * 0.1f;
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            centerX, centerY, INFLUENCE_RADIUS,
            new float[] { 0.0f, 0.7f, 1.0f },
            new Color[] {
                new Color(0.8f, 0.4f, 1.0f, intensity),  // Фиолетовый
                new Color(0.8f, 0.4f, 1.0f, intensity * 0.4f),
                new Color(0.8f, 0.4f, 1.0f, 0)
            }
        );
        
        g2d.setPaint(gradient);
        g2d.fillOval(centerX - INFLUENCE_RADIUS, centerY - INFLUENCE_RADIUS,
            INFLUENCE_RADIUS * 2, INFLUENCE_RADIUS * 2);
    }
    
    public void applyEffect(List<Ball> balls, int mouseX, int mouseY) {
        if (!isActive) return;
        
        for (Ball ball : balls) {
            double dx = ball.getX() - mouseX;
            double dy = ball.getY() - mouseY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INFLUENCE_RADIUS) {
                // Инвертируем скорость шара
                ball.setVelocityX(-ball.getVelocityX());
                ball.setVelocityY(-ball.getVelocityY());
                
                // Добавляем вращательное движение
                double angle = Math.atan2(dy, dx);
                double force = 0.1 * (1.0 - distance / INFLUENCE_RADIUS);
                
                double vx = Math.cos(angle + Math.PI/2) * force;  // Поворот на 90 градусов
                double vy = Math.sin(angle + Math.PI/2) * force;
                
                ball.setVelocityX((float) (ball.getVelocityX() + vx));
                ball.setVelocityY((float) (ball.getVelocityY() + vy));
            }
        }
    }

    public boolean isReversing() {
        return isReversing;
    }

    public void setReversing(boolean reversing) {
        isReversing = reversing;
    }

    public void saveState(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (!isReversing) {
            GameState currentState = new GameState();
            for (Ball ball : balls) {
                currentState.addBall(ball.getX(), ball.getY(), ball.getVelocityX(), ball.getVelocityY(), 
                                   ball.getRadius(), new Color(ball.getColor().getRGB()));
            }
            for (Particle particle : particles) {
                currentState.addParticle(particle.getX(), particle.getY(), particle.getVelocityX(), particle.getVelocityY(),
                                       (int)particle.getLifetime(), new Color(particle.getColor().getRGB()), 
                                       (int)particle.getSize());
            }
            
            stateHistory.add(currentState);
            if (stateHistory.size() > MAX_HISTORY_SIZE) {
                stateHistory.remove(0);
            }
        }
    }

    public void applyReversal(ArrayList<Ball> balls, ArrayList<Particle> particles) {
        if (isReversing && !stateHistory.isEmpty()) {
            GameState previousState = stateHistory.get(stateHistory.size() - 1);
            stateHistory.remove(stateHistory.size() - 1);
            
            balls.clear();
            particles.clear();
            balls.addAll(previousState.getBalls());
            particles.addAll(previousState.getParticles());
        }
    }
} 