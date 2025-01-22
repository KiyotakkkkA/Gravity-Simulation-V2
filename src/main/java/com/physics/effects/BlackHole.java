package com.physics.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.physics.model.Ball;

public class BlackHole {
    private final ExecutorService executor = EffectUtils.getExecutor();
    private boolean isActive = false;
    private double phase = 0.0;
    private static final Random random = new Random();
    private final ArrayList<AccretionParticle> accretionDisk = new ArrayList<>();
    
    private static final int PARTICLE_COUNT = 500;
    private static final double PHASE_SPEED = 0.03;
    private static final int BLACK_HOLE_RADIUS = 30;
    private static final int EVENT_HORIZON_RADIUS = 100;
    private static final int INFLUENCE_RADIUS = 400;
    private static final double DISK_ROTATION_SPEED = 0.01;
    private double diskRotation = 0.0;
    
    // Кэшируем часто используемые значения
    private final Point2D.Float centerPoint = new Point2D.Float();
    private final float[] sineTable = new float[360];
    private final float[] cosineTable = new float[360];
    private final Color[] lineColors = new Color[360];
    private final Path2D.Float[] distortionPaths = new Path2D.Float[120];  // Уменьшаем количество линий
    private final Color[] particleColors = new Color[256];  // Кэш для цветов частиц
    
    // Кэшируем градиенты
    private RadialGradientPaint horizonGradient;
    private final Color[] glowColors = new Color[8];
    private final RadialGradientPaint[] glowGradients = new RadialGradientPaint[8];
    
    // Буферы для рендеринга
    private BufferedImage distortionBuffer;
    private BufferedImage accretionBuffer;
    private BufferedImage horizonBuffer;
    
    private class AccretionParticle {
        double angle;
        double radius;
        double speed;
        float hue;
        float alpha;
        double distortion;
        double verticalOffset;
        
        AccretionParticle() {
            respawn();
        }
        
        void update() {
            angle += speed + DISK_ROTATION_SPEED;
            if (angle > Math.PI * 2) {
                angle -= Math.PI * 2;
            }
            
            double distanceRatio = (radius - BLACK_HOLE_RADIUS) / 
                                 (double)(EVENT_HORIZON_RADIUS - BLACK_HOLE_RADIUS);
            speed = 0.02 + (1.0 - distanceRatio) * 0.3;
            
            distortion = Math.sin(angle * 3 + phase) * (1.0 - distanceRatio) * 40 +
                        Math.cos(angle * 2 - phase) * (1.0 - distanceRatio) * 20;
            
            verticalOffset = Math.sin(angle * 2 + phase) * (1.0 - distanceRatio) * 30 +
                           Math.cos(angle * 3 - phase * 0.5) * (1.0 - distanceRatio) * 15;
            
            alpha = 0.4f + (float)(1.0 - distanceRatio) * 0.6f;
            
            float baseHue = 0.6f + (float)(1.0 - distanceRatio) * 0.4f;
            hue = (baseHue + (float)(Math.sin(phase * 0.7 + angle * 2) * 0.2f)) % 1.0f;
            
            radius -= 0.3 * (1.0 - distanceRatio);
            
            if (radius < BLACK_HOLE_RADIUS) {
                respawn();
            }
        }
        
        void respawn() {
            radius = EVENT_HORIZON_RADIUS + random.nextDouble() * 
                    (INFLUENCE_RADIUS - EVENT_HORIZON_RADIUS);
            angle = random.nextDouble() * Math.PI * 2;
            speed = 0.02 + random.nextDouble() * 0.03;
            hue = 0.6f + random.nextFloat() * 0.4f;
            alpha = 0.3f + random.nextFloat() * 0.4f;
            distortion = 0;
            verticalOffset = 0;
        }
    }
    
    public BlackHole() {
        // Предварительно вычисляем тригонометрические значения
        for (int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            sineTable[i] = (float)Math.sin(angle);
            cosineTable[i] = (float)Math.cos(angle);
        }
        
        // Инициализируем пути для искажения
        for (int i = 0; i < distortionPaths.length; i++) {
            distortionPaths[i] = new Path2D.Float();
        }
        
        // Предварительно создаем цвета для частиц
        for (int i = 0; i < particleColors.length; i++) {
            float hue = 0.6f + (i / (float)particleColors.length) * 0.4f;
            particleColors[i] = Color.getHSBColor(hue, 0.8f, 1.0f);
        }
        
        // Инициализируем цвета свечения
        for (int i = 0; i < glowColors.length; i++) {
            glowColors[i] = new Color(0, 0, 100, 50);
        }
        
        // Инициализируем буферы с нулевым размером
        distortionBuffer = EffectUtils.createCompatibleImage(1, 1, true);
        accretionBuffer = EffectUtils.createCompatibleImage(1, 1, true);
        horizonBuffer = EffectUtils.createCompatibleImage(1, 1, true);
    }
    
    public void setActive(boolean active) {
        isActive = active;
        if (active && accretionDisk.isEmpty()) {
            initializeParticles();
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    private void initializeParticles() {
        accretionDisk.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            accretionDisk.add(new AccretionParticle());
        }
    }
    
    public void update() {
        if (!isActive) return;
        
        phase += PHASE_SPEED;
        diskRotation += DISK_ROTATION_SPEED;
        
        // Параллельное обновление частиц
        List<List<AccretionParticle>> batches = new ArrayList<>();
        int batchSize = PARTICLE_COUNT / Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < accretionDisk.size(); i += batchSize) {
            batches.add(accretionDisk.subList(i, 
                Math.min(i + batchSize, accretionDisk.size())));
        }
        
        EffectUtils.parallelProcess(batches, 1, batch -> {
            for (AccretionParticle particle : batch) {
                particle.update();
            }
        });
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (!isActive) return;
        
        ensureBufferSize(width, height);
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Очищаем буферы
        Graphics2D distortionG2d = distortionBuffer.createGraphics();
        Graphics2D accretionG2d = accretionBuffer.createGraphics();
        Graphics2D horizonG2d = horizonBuffer.createGraphics();
        
        try {
            distortionG2d.setBackground(new Color(0, 0, 0, 0));
            accretionG2d.setBackground(new Color(0, 0, 0, 0));
            horizonG2d.setBackground(new Color(0, 0, 0, 0));
            
            distortionG2d.clearRect(0, 0, width, height);
            accretionG2d.clearRect(0, 0, width, height);
            horizonG2d.clearRect(0, 0, width, height);
            
            // Рендерим каждый слой параллельно
            Future<?> distortionFuture = executor.submit(() -> 
                drawSpaceDistortion(distortionG2d, centerX, centerY));
            Future<?> accretionFuture = executor.submit(() -> 
                drawAccretionDisk(accretionG2d, centerX, centerY));
            Future<?> horizonFuture = executor.submit(() -> {
                drawEventHorizon(horizonG2d, centerX, centerY);
                drawBlackHoleCore(horizonG2d, centerX, centerY);
            });
            
            // Ждем завершения рендеринга
            try {
                distortionFuture.get();
                accretionFuture.get();
                horizonFuture.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Композитинг слоев
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(distortionBuffer, 0, 0, null);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.drawImage(accretionBuffer, 0, 0, null);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.drawImage(horizonBuffer, 0, 0, null);
            
        } finally {
            distortionG2d.dispose();
            accretionG2d.dispose();
            horizonG2d.dispose();
        }
    }
    
    private void ensureBufferSize(int width, int height) {
        if (distortionBuffer.getWidth() != width || distortionBuffer.getHeight() != height) {
            distortionBuffer = EffectUtils.createCompatibleImage(width, height, true);
            accretionBuffer = EffectUtils.createCompatibleImage(width, height, true);
            horizonBuffer = EffectUtils.createCompatibleImage(width, height, true);
        }
    }
    
    private void updateGradients(int centerX, int centerY) {
        centerPoint.setLocation(centerX, centerY);
        
        // Обновляем градиент горизонта событий только при необходимости
        if (horizonGradient == null || horizonGradient.getCenterPoint().getX() != centerX || 
            horizonGradient.getCenterPoint().getY() != centerY) {
            
            float[] fractions = {0.0f, 0.5f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 50, 100),
                new Color(0, 0, 0, 200),
                new Color(0, 0, 0, 255)
            };
            
            horizonGradient = new RadialGradientPaint(
                centerPoint, EVENT_HORIZON_RADIUS, fractions, colors, CycleMethod.NO_CYCLE
            );
            
            // Обновляем градиенты свечения
            for (int i = 0; i < glowGradients.length; i++) {
                double glowAngle = i * Math.PI / 4;
                float glowX = centerX + (float)(Math.cos(glowAngle) * EVENT_HORIZON_RADIUS * 0.8);
                float glowY = centerY + (float)(Math.sin(glowAngle) * EVENT_HORIZON_RADIUS * 0.8);
                
                glowGradients[i] = new RadialGradientPaint(
                    glowX, glowY, EVENT_HORIZON_RADIUS / 2,
                    new float[]{0.0f, 1.0f},
                    new Color[]{glowColors[i], new Color(0, 0, 0, 0)}
                );
            }
        }
    }
    
    private void drawSpaceDistortion(Graphics2D g2d, int centerX, int centerY) {
        // Обновляем пути искажения только каждый второй кадр
        if ((int)(phase * 100) % 2 == 0) {
            // Параллельное обновление путей искажения
            int pathsPerThread = distortionPaths.length / Runtime.getRuntime().availableProcessors();
            List<List<Integer>> pathBatches = new ArrayList<>();
            for (int i = 0; i < distortionPaths.length; i += pathsPerThread) {
                final int start = i;
                final int end = Math.min(i + pathsPerThread, distortionPaths.length);
                pathBatches.add(java.util.stream.IntStream.range(start, end)
                    .boxed().collect(java.util.stream.Collectors.toList()));
            }
            
            EffectUtils.parallelProcess(pathBatches, 1, batch -> {
                for (int i : batch) {
                    updateDistortionPath(i, centerX, centerY);
                }
            });
        }
        
        // Отрисовка путей
        for (int i = 0; i < distortionPaths.length; i++) {
            float alpha = 0.15f + EffectUtils.sin(i * 3) * 0.1f;
            alpha *= 1.0f + (float)Math.sin(phase * 0.7) * 0.3f;
            
            Color baseColor = EffectUtils.getColorFromCache((float)i / distortionPaths.length);
            g2d.setColor(new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                (int)(255 * alpha)
            ));
            
            g2d.draw(distortionPaths[i]);
        }
    }
    
    private void updateDistortionPath(int index, int centerX, int centerY) {
        Path2D.Float path = distortionPaths[index];
        if (path == null) {
            path = new Path2D.Float();
            distortionPaths[index] = path;
        }
        path.reset();
        
        try {
            double angle = (index * 3) * Math.PI / 180 + diskRotation;
            float startX = centerX + EffectUtils.cos(index * 3) * EVENT_HORIZON_RADIUS;
            float startY = centerY + EffectUtils.sin(index * 3) * EVENT_HORIZON_RADIUS;
            
            if (!Float.isNaN(startX) && !Float.isNaN(startY)) {
                path.moveTo(startX, startY);
                
                for (double r = EVENT_HORIZON_RADIUS; r <= INFLUENCE_RADIUS; r += 4) {
                    double distanceRatio = (r - EVENT_HORIZON_RADIUS) / (INFLUENCE_RADIUS - EVENT_HORIZON_RADIUS);
                    double warpStrength = 1.0 - distanceRatio;
                    
                    double distortionAngle = angle + 
                        Math.sin(r * 0.03 + phase) * warpStrength +
                        Math.cos(r * 0.02 - phase * 0.7) * 0.5 * warpStrength;
                    
                    float x = centerX + (float)(Math.cos(distortionAngle) * r);
                    float y = centerY + (float)(Math.sin(distortionAngle) * r);
                    
                    if (!Float.isNaN(x) && !Float.isNaN(y)) {
                        path.lineTo(x, y);
                    }
                }
            }
        } catch (Exception e) {
            // В случае ошибки создаем пустой путь
            path.reset();
            path.moveTo(centerX, centerY);
            path.lineTo(centerX, centerY);
        }
    }
    
    private void drawAccretionDisk(Graphics2D g2d, int centerX, int centerY) {
        // Сортировка частиц по Z-индексу
        accretionDisk.sort((p1, p2) -> Double.compare(p2.verticalOffset, p1.verticalOffset));
        
        // Разбиваем частицы на батчи для параллельной отрисовки
        int particlesPerBatch = accretionDisk.size() / Runtime.getRuntime().availableProcessors();
        List<List<AccretionParticle>> batches = new ArrayList<>();
        for (int i = 0; i < accretionDisk.size(); i += particlesPerBatch) {
            batches.add(accretionDisk.subList(i, 
                Math.min(i + particlesPerBatch, accretionDisk.size())));
        }
        
        // Параллельная отрисовка частиц
        EffectUtils.parallelProcess(batches, 1, batch -> {
            Graphics2D batchG2d = (Graphics2D)g2d.create();
            try {
                for (AccretionParticle particle : batch) {
                    drawParticle(batchG2d, particle, centerX, centerY);
                }
            } finally {
                batchG2d.dispose();
            }
        });
    }
    
    private void drawParticle(Graphics2D g2d, AccretionParticle particle, int centerX, int centerY) {
        Color baseColor = EffectUtils.getColorFromCache(particle.hue);
        
        float glowSize = 8 + (float)(Math.sin(phase + particle.angle) + 1) * 3;
        glowSize *= (1.0f - (float)(particle.radius - BLACK_HOLE_RADIUS) / 
                    (float)(EVENT_HORIZON_RADIUS - BLACK_HOLE_RADIUS)) * 1.5f;
        
        double distortedAngle = particle.angle + 
            Math.sin(particle.angle * 2 + phase) * 0.3 *
            (1.0 - (particle.radius - BLACK_HOLE_RADIUS) / 
             (double)(EVENT_HORIZON_RADIUS - BLACK_HOLE_RADIUS));
        
        float x = (float)(centerX + Math.cos(distortedAngle) * 
                (particle.radius + particle.distortion));
        float y = (float)(centerY + Math.sin(distortedAngle) * 
                (particle.radius + particle.distortion) * 
                (0.7 + Math.cos(particle.angle) * 0.3) +
                particle.verticalOffset);
        
        drawParticleGlow(g2d, x, y, glowSize, baseColor, particle.alpha);
    }
    
    private void drawParticleGlow(Graphics2D g2d, float x, float y, float size, Color baseColor, float alpha) {
        // Внешнее свечение
        g2d.setColor(new Color(
            baseColor.getRed(),
            baseColor.getGreen(),
            baseColor.getBlue(),
            (int)(50 * alpha)
        ));
        
        float outerSize = size * 3;
        g2d.fillOval(
            (int)(x - outerSize/2),
            (int)(y - outerSize/2),
            (int)outerSize,
            (int)outerSize
        );
        
        // Основное свечение
        g2d.setColor(new Color(
            baseColor.getRed(),
            baseColor.getGreen(),
            baseColor.getBlue(),
            (int)(100 * alpha)
        ));
        
        float middleSize = size * 2;
        g2d.fillOval(
            (int)(x - middleSize/2),
            (int)(y - middleSize/2),
            (int)middleSize,
            (int)middleSize
        );
        
        // Ядро частицы
        g2d.setColor(new Color(
            baseColor.getRed(),
            baseColor.getGreen(),
            baseColor.getBlue(),
            (int)(255 * alpha)
        ));
        
        float coreSize = size * 0.5f;
        g2d.fillOval(
            (int)(x - coreSize/2),
            (int)(y - coreSize/2),
            (int)coreSize,
            (int)coreSize
        );
    }
    
    private void drawEventHorizon(Graphics2D g2d, int centerX, int centerY) {
        updateGradients(centerX, centerY);
        
        g2d.setPaint(horizonGradient);
        g2d.fillOval(
            centerX - EVENT_HORIZON_RADIUS,
            centerY - EVENT_HORIZON_RADIUS,
            EVENT_HORIZON_RADIUS * 2,
            EVENT_HORIZON_RADIUS * 2
        );
        
        // Рисуем свечение с использованием кэшированных градиентов
        for (int i = 0; i < glowGradients.length; i++) {
            g2d.setPaint(glowGradients[i]);
            g2d.fillOval(
                centerX - EVENT_HORIZON_RADIUS,
                centerY - EVENT_HORIZON_RADIUS,
                EVENT_HORIZON_RADIUS * 2,
                EVENT_HORIZON_RADIUS * 2
            );
        }
    }
    
    private void drawBlackHoleCore(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(Color.BLACK);
        g2d.fillOval(
            centerX - BLACK_HOLE_RADIUS,
            centerY - BLACK_HOLE_RADIUS,
            BLACK_HOLE_RADIUS * 2,
            BLACK_HOLE_RADIUS * 2
        );
    }
    
    public void applyEffect(ArrayList<Ball> balls, int width, int height) {
        if (!isActive) return;
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        for (Ball ball : balls) {
            double dx = ball.getX() - centerX;
            double dy = ball.getY() - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INFLUENCE_RADIUS) {
                double force = (1.0 - distance / INFLUENCE_RADIUS) * 3.0;
                
                double angle = Math.atan2(dy, dx);
                double tangentialForce = force * (0.5 + Math.sin(phase) * 0.2);
                
                force *= 1.0 + Math.sin(phase * 0.5) * 0.2;
                
                ball.addForce(
                    (float) (-dx / distance * force - Math.sin(angle) * tangentialForce),
                    (float) (-dy / distance * force + Math.cos(angle) * tangentialForce)
                );
                
                if (distance < EVENT_HORIZON_RADIUS * 1.2) {
                    ball.addForce(
                        (float) (-dx / distance * force * 2),
                        (float) (-dy / distance * force * 2)
                    );
                }
            }
        }
    }
} 