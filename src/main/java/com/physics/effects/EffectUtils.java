package com.physics.effects;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EffectUtils {
    private static final ExecutorService executor = 
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
    private static final int COLOR_CACHE_SIZE = 1024;
    private static final Color[] colorCache = new Color[COLOR_CACHE_SIZE];
    private static final float[] sinTable = new float[360];
    private static final float[] cosTable = new float[360];
    
    static {
        // Инициализируем таблицы синусов и косинусов
        for (int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            sinTable[i] = (float)Math.sin(angle);
            cosTable[i] = (float)Math.cos(angle);
        }
        
        // Инициализируем кэш цветов
        for (int i = 0; i < COLOR_CACHE_SIZE; i++) {
            float hue = i / (float)COLOR_CACHE_SIZE;
            colorCache[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
        }
    }
    
    public static BufferedImage createCompatibleImage(int width, int height, boolean hasAlpha) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        return gc.createCompatibleImage(width, height, 
            hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    }
    
    public static Color getColorFromCache(float hue) {
        int index = (int)(hue * COLOR_CACHE_SIZE) % COLOR_CACHE_SIZE;
        return colorCache[Math.abs(index)];
    }
    
    public static float sin(float degrees) {
        int index = ((int)degrees % 360);
        if (index < 0) index += 360;
        return sinTable[index];
    }
    
    public static float cos(float degrees) {
        int index = ((int)degrees % 360);
        if (index < 0) index += 360;
        return cosTable[index];
    }
    
    public static <T> void parallelProcess(List<T> items, int minBatchSize, Consumer<T> processor) {
        if (items.size() < minBatchSize) {
            items.forEach(processor);
            return;
        }
        
        try {
            for (T item : items) {
                executor.submit(() -> processor.accept(item));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ExecutorService getExecutor() {
        return executor;
    }
    
    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    public static void processImageParallel(BufferedImage image, ImageProcessor processor) {
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int width = image.getWidth();
        int height = image.getHeight();
        int threadCount = Runtime.getRuntime().availableProcessors();
        
        List<Future<?>> futures = new ArrayList<>();
        int rowsPerThread = height / threadCount;
        
        for (int i = 0; i < threadCount; i++) {
            final int startY = i * rowsPerThread;
            final int endY = (i == threadCount - 1) ? height : (i + 1) * rowsPerThread;
            
            futures.add(executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width + x;
                        pixels[index] = processor.processPixel(x, y, pixels[index]);
                    }
                }
            }));
        }
        
        // Ждем завершения всех потоков
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FunctionalInterface
    public interface ImageProcessor {
        int processPixel(int x, int y, int pixel);
    }
    
    // Вспомогательные методы для работы с цветами в формате int
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    
    public static int getAlpha(int pixel) {
        return (pixel >> 24) & 0xff;
    }
    
    public static int getRed(int pixel) {
        return (pixel >> 16) & 0xff;
    }
    
    public static int getGreen(int pixel) {
        return (pixel >> 8) & 0xff;
    }
    
    public static int getBlue(int pixel) {
        return pixel & 0xff;
    }
    
    public static int blendColors(int color1, int color2, float ratio) {
        int a1 = getAlpha(color1);
        int r1 = getRed(color1);
        int g1 = getGreen(color1);
        int b1 = getBlue(color1);
        
        int a2 = getAlpha(color2);
        int r2 = getRed(color2);
        int g2 = getGreen(color2);
        int b2 = getBlue(color2);
        
        int a = (int)(a1 + (a2 - a1) * ratio);
        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);
        
        return argb(a, r, g, b);
    }
} 