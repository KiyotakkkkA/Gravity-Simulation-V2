package com.physics.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import com.physics.data.Constants;
import com.physics.effects.Magnet;
import com.physics.effects.SlowMotion;
import com.physics.effects.Split;
import com.physics.effects.Teleport;

public class InfoPanel extends JPanel {
    private Point mousePosition = null;
    private Point tooltipPosition = null;
    private String tooltipText = null;
    private boolean showHelp = false;

    // Эффекты
    private Teleport teleport;
    private Split split;
    private Magnet magnet;
    private SlowMotion slowMotion;

    private boolean teleportMode = false;
    private boolean splitMode = false;
    private boolean magnetMode = false;
    private boolean slowMode = false;

    public InfoPanel(Teleport teleport, Split split, Magnet magnet, SlowMotion slowMotion) {
        this.teleport = teleport;
        this.split = split;
        this.magnet = magnet;
        this.slowMotion = slowMotion;
        setPreferredSize(new Dimension(800, Constants.INFO_PANEL_HEIGHT));
        setBackground(new Color(240, 240, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Рисуем градиентный фон панели
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(0, getHeight());
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {
            new Color(30, 30, 40),
            new Color(20, 20, 30)
        };
        LinearGradientPaint bgGradient = new LinearGradientPaint(start, end, dist, colors);
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Добавляем тонкую линию-разделитель снизу панели
        g2d.setColor(new Color(100, 100, 255, 50));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        
        drawInfo(g2d);
    }

    private void drawInfo(Graphics2D g2d) {
        // Рисуем статусы параметров с указанием клавиш
        drawParameterStatus(g2d, "Время", "↑/↓", getTimeScale(), Constants.MIN_TIME_SCALE, Constants.MAX_TIME_SCALE, 10, 10);
        drawParameterStatus(g2d, "Гравитация", "W/S", getGravity(), Constants.MIN_GRAVITY, Constants.MAX_GRAVITY, 160, 10);
        drawParameterStatus(g2d, "Искривление", "A/D", getSpaceWarp(), Constants.MIN_WARP, Constants.MAX_WARP, 310, 10);
        drawParameterStatus(g2d, "Энергия", "E/C", getEnergyField(), Constants.MIN_ENERGY, Constants.MAX_ENERGY, 460, 10);
        drawParameterStatus(g2d, "Плотность", "O/P", getParticleDensity(), Constants.MIN_DENSITY, Constants.MAX_DENSITY, 610, 10);
        
        // Рисуем индикаторы активных эффектов с указанием клавиш
        int y = 60;
        drawEffectStatus(g2d, "Заморозка", "F", isTimeFreeze(), 10, y);
        drawEffectStatus(g2d, "Чёрная дыра", "B", isBlackHoleMode(), 160, y);
        drawEffectStatus(g2d, "Врем. вихрь", "V", isTimeVortexMode(), 310, y);
        drawEffectStatus(g2d, "Радужный", "L", isRainbowMode(), 460, y);
        drawEffectStatus(g2d, "Квант. туннель", "X", isQuantumTunneling(), 610, y);
        
        y += 30;
        drawEffectStatus(g2d, "Взрывы", "Z", isExplosionMode(), 10, y);
        drawEffectStatus(g2d, "Матрица", "M", isMatrixMode(), 160, y);
        drawEffectStatus(g2d, "Грав. волны", "G", isGravityWaves(), 310, y);
        drawEffectStatus(g2d, "Магнетизм", "U", isMagnetMode(), 460, y);
        drawEffectStatus(g2d, "Замедление", "I", isSlowMode(), 610, y);

        y += 30;
        drawEffectStatus(g2d, "Телепорт", "T", isTeleportMode(), 10, y);
        drawEffectStatus(g2d, "Разделение", "Y", isSplitMode(), 160, y);
        
        // Добавляем подсказку для выхода
        g2d.setColor(new Color(150, 150, 200));
        g2d.drawString("ESC - Выход", getWidth() - 100, 20);
        
        if (showHelp) {
            drawHelp(g2d);
        }

        if (tooltipPosition != null && tooltipText != null) {
            drawTooltip(g2d);
        }
    }

    private void drawParameterStatus(Graphics2D g2d, String name, String key, 
                                   double value, double min, double max, int x, int y) {
        int width = 140;
        int height = 40;
        
        // Проверяем, находится ли мышь над параметром
        boolean isHovered = false;
        if (mousePosition != null) {
            Rectangle paramBounds = new Rectangle(x, y, width, height);
            if (paramBounds.contains(mousePosition)) {
                isHovered = true;
                // Подсветка при наведении
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g2d.setColor(new Color(100, 100, 255));
                g2d.fillRoundRect(x, y, width, height, 15, 15);
                g2d.setComposite(AlphaComposite.SrcOver);
                
                // Устанавливаем позицию и текст подсказки
                tooltipPosition = new Point(x + width + 10, y);
                tooltipText = Constants.PARAMETER_DESCRIPTIONS.get(name);
            }
        }

        // Фон параметра
        g2d.setColor(new Color(0, 0, 0, isHovered ? 80 : 60));
        g2d.fillRoundRect(x, y, width, height, 15, 15);
        
        // Рамка
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(100, 100, 255, isHovered ? 100 : 50));
        g2d.drawRoundRect(x, y, width, height, 15, 15);
        
        // Название и клавиша
        g2d.setColor(new Color(200, 200, 255));
        g2d.drawString(name, x + 5, y + 15);
        g2d.setColor(new Color(150, 150, 200));
        g2d.drawString("[" + key + "] ", x + width - g2d.getFontMetrics().stringWidth("[" + key + "]") - 5, y + 15);
        
        // Значение
        String valueStr = String.format("%.2f", value);
        g2d.setColor(new Color(150, 150, 255));
        g2d.drawString(valueStr, x + width - g2d.getFontMetrics().stringWidth(valueStr) - 5, y + 35);
        
        // Прогресс-бар
        int barWidth = width - 10;
        int barHeight = 4;
        int barX = x + 5;
        int barY = y + 25;
        
        // Фон прогресс-бара
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);
        
        // Заполнение прогресс-бара
        float progress = (float)((value - min) / (max - min));
        Point2D barStart = new Point2D.Float(barX, barY);
        Point2D barEnd = new Point2D.Float(barX + barWidth, barY);
        
        Color startColor, endColor;
        if (value < 0) {
            startColor = new Color(255, 50, 50);
            endColor = new Color(255, 100, 100);
        } else if (value > 0) {
            startColor = new Color(50, 100, 255);
            endColor = new Color(100, 150, 255);
        } else {
            startColor = new Color(150, 150, 150);
            endColor = new Color(200, 200, 200);
        }
        
        LinearGradientPaint gradient = new LinearGradientPaint(
            barStart, barEnd, new float[]{0.0f, 1.0f},
            new Color[]{startColor, endColor}
        );
        
        g2d.setPaint(gradient);
        g2d.fillRoundRect(barX, barY, (int)(barWidth * progress), barHeight, 2, 2);
        
        // Блик на прогресс-баре
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(barX, barY, (int)(barWidth * progress), barHeight/2, 1, 1);
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    private void drawEffectStatus(Graphics2D g2d, String name, String key, boolean active, int x, int y) {
        int width = 140;
        int height = 30;
        
        boolean isHovered = false;
        if (mousePosition != null) {
            Rectangle effectBounds = new Rectangle(x, y, width, height);
            if (effectBounds.contains(mousePosition)) {
                isHovered = true;
                tooltipPosition = new Point(x + width + 10, y);
                tooltipText = Constants.EFFECT_DESCRIPTIONS.get(name);
            }
        }
        
        // Фон эффекта
        Color bgColor = active ? new Color(50, 50, 150, 80) : new Color(0, 0, 0, 60);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        
        // Рамка
        g2d.setStroke(new BasicStroke(1.0f));
        Color borderColor = active ? 
            new Color(100, 100, 255, isHovered ? 200 : 150) : 
            new Color(100, 100, 255, isHovered ? 100 : 50);
        g2d.setColor(borderColor);
        g2d.drawRoundRect(x, y, width, height, 10, 10);
        
        // Название эффекта
        g2d.setColor(active ? new Color(150, 150, 255) : new Color(150, 150, 200));
        g2d.drawString(name, x + 5, y + 20);
        
        // Клавиша
        g2d.setColor(new Color(100, 100, 150));
        String keyText = "[" + key + "]";
        g2d.drawString(keyText, x + width - g2d.getFontMetrics().stringWidth(keyText) - 5, y + 20);
        
        // Индикатор активности
        if (active) {
            int indicatorSize = 6;
            g2d.setColor(new Color(100, 255, 100));
            g2d.fillOval(x + width - indicatorSize - 20, y + height/2 - indicatorSize/2, 
                        indicatorSize, indicatorSize);
            
            // Свечение индикатора
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(new Color(100, 255, 100));
            g2d.fillOval(x + width - indicatorSize - 21, y + height/2 - indicatorSize/2 - 1, 
                        indicatorSize + 2, indicatorSize + 2);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }

    private void drawHelp(Graphics2D g2d) {
        String[] help = {
            "Управление:",
            "↑/↓ - Скорость времени",
            "W/S - Гравитация",
            "A/D - Искривление",
            "Q - Обратить время",
            "R - Сброс физики",
            "ПРОБЕЛ - Инверсия гравитации",
            "F - Заморозка времени",
            "B - Режим чёрной дыры",
            "V - Временной вихрь",
            "E/C - Энергия поля",
            "T - Телепортация",
            "L - Радужный режим",
            "P - Эффекты частиц",
            "X - Квантовое туннелирование",
            "Z - Режим взрывов",
            "M - Матричный режим",
            "G - Гравитационные волны",
            "Y - Разделение шаров",
            "U - Магнитное поле",
            "I - Замедление времени",
            "ЛКМ - Создать шар",
            "ПКМ - Создать взрыв"
        };

        // Полупрозрачный фон для помощи
        g2d.setColor(new Color(0, 0, 0, 200));
        int helpWidth = 300;
        int helpHeight = help.length * 20 + 10;
        g2d.fillRoundRect(10, Constants.INFO_PANEL_HEIGHT + 10, helpWidth, helpHeight, 15, 15);

        // Текст помощи
        g2d.setColor(Color.WHITE);
        int y = Constants.INFO_PANEL_HEIGHT + 30;
        for (String line : help) {
            g2d.drawString(line, 20, y);
            y += 20;
        }
    }

    private void drawTooltip(Graphics2D g2d) {
        // Настраиваем шрифт и получаем метрики для расчета размеров
        g2d.setFont(g2d.getFont().deriveFont(12.0f));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Разбиваем текст на строки по 40 символов
        String[] words = tooltipText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        for (String word : words) {
            if (currentLine.length() + word.length() > 40) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        // Вычисляем размеры подсказки
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        int tooltipWidth = maxWidth + 20;
        int tooltipHeight = lines.size() * fm.getHeight() + 10;
        
        // Вычисляем позицию подсказки
        int x = tooltipPosition.x;
        int y = tooltipPosition.y;
        
        // Проверяем и корректируем позицию, чтобы подсказка не выходила за экран
        if (x + tooltipWidth > getWidth()) {
            x = getWidth() - tooltipWidth - 5;
        }
        
        // Если подсказка выходит за нижнюю границу, показываем её выше курсора
        if (y + tooltipHeight > getHeight()) {
            y = y - tooltipHeight - 5;
        }
        
        // Если после коррекции подсказка всё ещё выходит за верхнюю границу,
        // показываем её справа от курсора
        if (y < 0) {
            y = tooltipPosition.y;
            x = tooltipPosition.x + 20;
        }
        
        // Рисуем фон подсказки с тенью
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(x + 2, y + 2, tooltipWidth, tooltipHeight, 10, 10);
        
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRoundRect(x, y, tooltipWidth, tooltipHeight, 10, 10);
        
        // Добавляем рамку
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawRoundRect(x, y, tooltipWidth, tooltipHeight, 10, 10);
        
        // Рисуем текст
        g2d.setColor(Color.WHITE);
        int textY = y + fm.getAscent() + 5;
        for (String line : lines) {
            g2d.drawString(line, x + 10, textY);
            textY += fm.getHeight();
        }
    }

    // Геттеры и сеттеры для всех необходимых параметров
    private double timeScale = 1.0;
    private double gravity = 0.3;
    private double spaceWarp = 1.0;
    private double energyField = 1.0;
    private double particleDensity = 1.0;
    private boolean timeFreeze = false;
    private boolean blackHoleMode = false;
    private boolean timeVortexMode = false;
    private boolean rainbowMode = false;
    private boolean quantumTunneling = false;
    private boolean explosionMode = false;
    private boolean matrixMode = false;
    private boolean gravityWaves = false;
    private boolean kingCrimsonActive = false;

    // Геттеры
    public double getTimeScale() { return timeScale; }
    public double getGravity() { return gravity; }
    public double getSpaceWarp() { return spaceWarp; }
    public double getEnergyField() { return energyField; }
    public double getParticleDensity() { return particleDensity; }
    public boolean isTimeFreeze() { return timeFreeze; }
    public boolean isBlackHoleMode() { return blackHoleMode; }
    public boolean isTimeVortexMode() { return timeVortexMode; }
    public boolean isRainbowMode() { return rainbowMode; }
    public boolean isQuantumTunneling() { return quantumTunneling; }
    public boolean isExplosionMode() { return explosionMode; }
    public boolean isMatrixMode() { return matrixMode; }
    public boolean isGravityWaves() { return gravityWaves; }
    public boolean isKingCrimsonActive() { return kingCrimsonActive; }

    // Сеттеры
    public void setTimeScale(double value) { timeScale = value; }
    public void setGravity(double value) { gravity = value; }
    public void setSpaceWarp(double value) { spaceWarp = value; }
    public void setEnergyField(double value) { energyField = value; }
    public void setParticleDensity(double value) { particleDensity = value; }
    public void setTimeFreeze(boolean value) { timeFreeze = value; }
    public void setBlackHoleMode(boolean value) { blackHoleMode = value; }
    public void setTimeVortexMode(boolean value) { timeVortexMode = value; }
    public void setRainbowMode(boolean value) { rainbowMode = value; }
    public void setQuantumTunneling(boolean value) { quantumTunneling = value; }
    public void setExplosionMode(boolean value) { explosionMode = value; }
    public void setMatrixMode(boolean value) { matrixMode = value; }
    public void setGravityWaves(boolean value) { gravityWaves = value; }
    public void setKingCrimsonActive(boolean value) { kingCrimsonActive = value; }

    public void setMousePosition(Point p) {
        mousePosition = p;
        if (p == null) {
            tooltipPosition = null;
            tooltipText = null;
        }
        repaint();
    }

    public void setShowHelp(boolean show) {
        showHelp = show;
        repaint();
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isTeleportMode() { 
        return teleport != null && teleport.isActive();
    }
    
    public boolean isSplitMode() { 
        return split != null && split.isActive();
    }
    
    public boolean isMagnetMode() { 
        return magnet != null && magnet.isActive();
    }
    
    public boolean isSlowMode() { 
        return slowMotion != null && slowMotion.isActive();
    }

    public void setTeleportMode(boolean active) {
        teleportMode = active;
        repaint();
    }
    
    public void setSplitMode(boolean active) {
        splitMode = active;
        repaint();
    }
    
    public void setMagnetMode(boolean active) {
        magnetMode = active;
        repaint();
    }
    
    public void setSlowMode(boolean active) {
        slowMode = active;
        repaint();
    }
} 