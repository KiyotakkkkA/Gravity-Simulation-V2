package com.physics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.physics.data.Constants;
import com.physics.effects.BlackHole;
import com.physics.effects.Explosion;
import com.physics.effects.GravityWave;
import com.physics.effects.Magnet;
import com.physics.effects.Matrix;
import com.physics.effects.QuantumTunnel;
import com.physics.effects.Rainbow;
import com.physics.effects.SlowMotion;
import com.physics.effects.Split;
import com.physics.effects.Teleport;
import com.physics.effects.TimeFreeze;
import com.physics.effects.TimeReversal;
import com.physics.effects.TimeVortex;
import com.physics.model.Ball;
import com.physics.model.Particle;
import com.physics.ui.InfoPanel;

public class PhysicsSimulation extends JFrame {
    private final ArrayList<Ball> balls = new ArrayList<>(100);
    private final ArrayList<Particle> particles = new ArrayList<>(500);
    private final JPanel canvas;
    private double timeScale = 1.0;  // Масштаб времени
    private static final double TIME_STEP = 0.1;  // Шаг изменения времени
    private double gravity = 0.3;
    private double spaceWarp = 1.0;  // Искривление пространства
    private static final int FPS = 120;
    private double energyField = 1.0;  // Энергетическое поле (влияет на упругость)
    private static final double ENERGY_STEP = 0.1;
    private Point mousePosition = new Point(0, 0);
    private static final int WARP_RADIUS = 150;  // Радиус действия искривления
    private final TimeFreeze timeFreeze = new TimeFreeze();  // Эффект остановки времени
    private final BlackHole blackHole = new BlackHole();  // Эффект чёрной дыры
    private final Explosion explosion = new Explosion();  // Эффект взрывов
    private final double backgroundDistortion = 0.0;  // Искажение фона
    private Point tooltipPosition = null;       // Позиция для всплывающей подсказки
    private String tooltipText = null;          // Текст подсказки

    private InfoPanel infoPanel;  // Меняем тип на InfoPanel
    private final TimeReversal timeReversal = new TimeReversal();  // Добавляем эффект обращения времени
    private final Matrix matrix = new Matrix();  // Эффект матрицы
    private final TimeVortex timeVortex = new TimeVortex();  // Эффект временного вихря
    private final GravityWave gravityWave = new GravityWave();  // Эффект гравитационных волн
    private final Rainbow rainbow = new Rainbow();  // Эффект радуги
    private final QuantumTunnel quantumTunnel = new QuantumTunnel();  // Эффект квантового туннелирования

    // Кэшируем часто используемые значения
    private int canvasWidth;
    private int canvasHeight;
    private final Rectangle canvasBounds = new Rectangle();
    private final Point centerPoint = new Point();
    
    // Буферы для избежания создания новых объектов
    private final ArrayList<Ball> ballsToRemove = new ArrayList<>();
    private final ArrayList<Particle> newParticles = new ArrayList<>();

    private int mouseX, mouseY;

    // Добавляем новые эффекты как поля класса
    private Teleport teleport;
    private Split split;
    private Magnet magnet;
    private SlowMotion slowMotion;

    public PhysicsSimulation() {
        setTitle("Физическая Симуляция");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        setSize(bounds.width, bounds.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);  // Убираем рамку окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Инициализируем эффекты
        teleport = new Teleport();
        split = new Split();
        magnet = new Magnet();
        slowMotion = new SlowMotion();
        
        // Создаем InfoPanel
        infoPanel = new InfoPanel(teleport, split, magnet, slowMotion);
        
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Рисуем динамический фон
                drawBackground(g2d);
                
                // Рисуем искривление пространства
                drawSpaceWarp(g2d);
                
                // Рисуем частицы
                for (Particle particle : particles) {
                    particle.draw(g2d);
                }
                
                // Рисуем шары
                for (Ball ball : balls) {
                    ball.draw(g2d);
                }
                
                // Рисуем подсказку в последнюю очередь
                drawTooltip(g2d);
                
                // Отрисовываем новые эффекты
                teleport.draw(g2d);
                magnet.draw(g2d);
                slowMotion.draw(g2d);
                
                // Сбрасываем позицию подсказки
                tooltipPosition = null;
                tooltipText = null;
            }
        };
        
        // Обновляем обработчик клавиш
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (!timeReversal.isReversing()) {
                            timeScale = Math.min(timeScale + TIME_STEP, Constants.MAX_TIME_SCALE);
                            infoPanel.setTimeScale(timeScale);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (!timeReversal.isReversing()) {
                            timeScale = Math.max(timeScale - TIME_STEP, Constants.MIN_TIME_SCALE);
                            infoPanel.setTimeScale(timeScale);
                        }
                        break;
                    case KeyEvent.VK_W:
                        gravity = Math.min(gravity + TIME_STEP, Constants.MAX_GRAVITY);
                        infoPanel.setGravity(gravity);
                        break;
                    case KeyEvent.VK_S:
                        gravity = Math.max(gravity - TIME_STEP, Constants.MIN_GRAVITY);
                        infoPanel.setGravity(gravity);
                        break;
                    case KeyEvent.VK_A:
                        spaceWarp = Math.max(spaceWarp - TIME_STEP, Constants.MIN_WARP);
                        infoPanel.setSpaceWarp(spaceWarp);
                        break;
                    case KeyEvent.VK_D:
                        spaceWarp = Math.min(spaceWarp + TIME_STEP, Constants.MAX_WARP);
                        infoPanel.setSpaceWarp(spaceWarp);
                        break;
                    case KeyEvent.VK_E:
                        energyField = Math.min(energyField + ENERGY_STEP, Constants.MAX_ENERGY);
                        infoPanel.setEnergyField(energyField);
                        break;
                    case KeyEvent.VK_C:
                        energyField = Math.max(energyField - ENERGY_STEP, Constants.MIN_ENERGY);
                        infoPanel.setEnergyField(energyField);
                        break;
                    case KeyEvent.VK_Q:
                        timeReversal.setReversing(true);
                        break;
                    case KeyEvent.VK_H:
                        infoPanel.setShowHelp(!infoPanel.isShowHelp());
                        break;
                    case KeyEvent.VK_F:
                        timeFreeze.setActive(!timeFreeze.isActive());
                        infoPanel.setTimeFreeze(timeFreeze.isActive());
                        break;
                    case KeyEvent.VK_B:
                        blackHole.setActive(!blackHole.isActive());
                        infoPanel.setBlackHoleMode(blackHole.isActive());
                        break;
                    case KeyEvent.VK_V:
                        timeVortex.setActive(!timeVortex.isActive());
                        infoPanel.setTimeVortexMode(timeVortex.isActive());
                        break;
                    case KeyEvent.VK_L:
                        rainbow.setActive(!rainbow.isActive());
                        infoPanel.setRainbowMode(rainbow.isActive());
                        break;
                    case KeyEvent.VK_X:
                        quantumTunnel.setActive(!quantumTunnel.isActive());
                        infoPanel.setQuantumTunneling(quantumTunnel.isActive());
                        break;
                    case KeyEvent.VK_Z:
                        explosion.setActive(!explosion.isActive());
                        infoPanel.setExplosionMode(explosion.isActive());
                        break;
                    case KeyEvent.VK_M:
                        matrix.setActive(!matrix.isActive());
                        infoPanel.setMatrixMode(matrix.isActive());
                        break;
                    case KeyEvent.VK_G:
                        gravityWave.setActive(!gravityWave.isActive(), mouseX, mouseY);
                        infoPanel.setGravityWaves(gravityWave.isActive());
                        break;
                    case KeyEvent.VK_T:
                        teleport.setActive(!teleport.isActive(), canvas.getWidth()/2, canvas.getHeight()/2);
                        infoPanel.setTeleportMode(teleport.isActive());
                        break;
                    case KeyEvent.VK_Y:
                        split.setActive(!split.isActive());
                        infoPanel.setSplitMode(split.isActive());
                        break;
                    case KeyEvent.VK_U:
                        magnet.setActive(!magnet.isActive(), canvas.getWidth()/2, canvas.getHeight()/2);
                        infoPanel.setMagnetMode(magnet.isActive());
                        break;
                    case KeyEvent.VK_I:
                        slowMotion.setActive(!slowMotion.isActive(), 
                            canvas.getWidth()/2, 
                            canvas.getHeight()/2);
                        infoPanel.setSlowMode(slowMotion.isActive());
                        break;
                }
                infoPanel.repaint();
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    timeReversal.setReversing(false);
                }
            }
        });
        
        // Добавляем отслеживание мыши для инфо-панели
        infoPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                infoPanel.setMousePosition(e.getPoint());
            }
        });

        // При выходе мыши с панели сбрасываем позицию
        infoPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                infoPanel.setMousePosition(null);
            }
        });
        
        setFocusable(true);
        canvas.setBackground(Color.WHITE);
        
        // Используем BorderLayout для размещения компонентов
        setLayout(new BorderLayout());
        
        add(infoPanel, BorderLayout.NORTH);  // Теперь только infoPanel
        add(canvas, BorderLayout.CENTER);
        
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Левая кнопка мыши - создаем шар
                    balls.add(new Ball(e.getX(), e.getY(), 20));
                } else if (e.getButton() == MouseEvent.BUTTON3 && explosion.isActive()) {
                    // Правая кнопка мыши - создаем взрыв
                    explosion.createExplosion(e.getX(), e.getY(), balls, particles);
                }
            }
        });
        
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!infoPanel.getBounds().contains(e.getPoint())) {
                    mousePosition = e.getPoint();
                    canvas.repaint();
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePosition = e.getPoint();
            }
        });
        
        Timer timer = new Timer(1000 / FPS, e -> {
            if (!timeReversal.isReversing()) {
                updateForward();
            } else {
                timeReversal.applyReversal(balls, particles);
            }
            canvas.repaint();
            infoPanel.repaint();
        });
        timer.start();
    }

    private void updateForward() {
        if (!timeFreeze.shouldUpdatePhysics()) return;

        // Обновляем размеры канваса только при необходимости
        updateCanvasDimensions();

        // Сохраняем состояние для обращения времени
        timeReversal.saveState(balls, particles);

        // Применяем эффекты перед обычным обновлением
        if (blackHole.isActive()) {
            blackHole.applyEffect(balls, canvasWidth, canvasHeight);
        }
        
        // Обновляем временной вихрь
        timeVortex.setSpaceWarp(spaceWarp);
        timeVortex.update();
        if (timeVortex.isActive()) {
            timeVortex.applyEffect(balls, mousePosition);
        }

        // Обновляем матричный эффект
        if (matrix.isActive()) {
            matrix.update();
        }
        
        // Обновляем гравитационные волны
        if (gravityWave.isActive()) {
            gravityWave.update();
            gravityWave.applyEffect(balls, particles);
        }

        // Обновляем радужный эффект
        if (rainbow.isActive()) {
            rainbow.update();
            rainbow.applyEffect(balls);
        }

        // Обновляем эффект квантового туннелирования
        if (quantumTunnel.isActive()) {
            quantumTunnel.update();
            for (Ball ball : balls) {
                quantumTunnel.checkTunneling(ball, canvasWidth, canvasHeight);
                quantumTunnel.applyTunneling(ball);
            }
        }

        // Обновляем новые эффекты
        teleport.update();
        split.update();
        magnet.update();
        slowMotion.update();
        
        // Применяем эффекты к объектам
        teleport.applyEffect(balls, particles);
        split.applyEffect(balls, particles);
        magnet.applyEffect(balls, particles);
        slowMotion.applyEffect(balls, particles);

        // Оптимизированное обновление частиц
        updateParticles();
        
        // Оптимизированное обновление шаров
        updateBalls();
        
        // Очищаем буферы
        ballsToRemove.clear();
        newParticles.clear();
    }

    private void updateCanvasDimensions() {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        canvasBounds.setSize(canvasWidth, canvasHeight);
        centerPoint.setLocation(canvasWidth / 2, canvasHeight / 2);
    }

    private void updateParticles() {
        for (Particle p : particles) {
            if (spaceWarp != 1.0) {
                applySpaceWarp(p);
            }
            p.update(canvasWidth, canvasHeight, timeScale, gravity);
        }
    }

    private void updateBalls() {
        for (Ball ball : balls) {
            if (spaceWarp != 1.0) {
                applySpaceWarp(ball);
            }
            if (ball.update(canvasWidth, canvasHeight, timeScale, gravity)) {
                ballsToRemove.add(ball);
                newParticles.addAll(ball.createParticles());
            }
        }
        
        // Пакетное удаление шаров и добавление частиц
        if (!ballsToRemove.isEmpty()) {
            balls.removeAll(ballsToRemove);
            particles.addAll(newParticles);
        }
    }

    private void drawSpaceWarp(Graphics2D g2d) {
        // Рисуем эффект остановки времени
        timeFreeze.draw(g2d, getWidth(), getHeight());
        
        // Рисуем эффект чёрной дыры
        blackHole.draw(g2d, getWidth(), getHeight());
        
        // Рисуем временной вихрь
        timeVortex.draw(g2d, mousePosition);
        
        if (spaceWarp == 1.0 || mousePosition == null) return;
        
        // Создаем градиент для визуализации искривления
        Color warpColor = spaceWarp > 1.0 ? 
            new Color(255, 100, 100, 50) :  // Красный для отталкивания
            new Color(100, 100, 255, 50);   // Синий для притяжения
            
        float alpha = Math.abs((float)(spaceWarp - 1.0)) * 0.5f;
        alpha = Math.min(alpha, 0.7f);
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            mousePosition,
            WARP_RADIUS,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{
                new Color(warpColor.getRed(), warpColor.getGreen(), warpColor.getBlue(), (int)(255 * alpha)),
                new Color(warpColor.getRed(), warpColor.getGreen(), warpColor.getBlue(), (int)(100 * alpha)),
                new Color(warpColor.getRed(), warpColor.getGreen(), warpColor.getBlue(), 0)
            }
        );
        
        g2d.setPaint(gradient);
        g2d.fillOval(
            mousePosition.x - WARP_RADIUS,
            mousePosition.y - WARP_RADIUS,
            WARP_RADIUS * 2,
            WARP_RADIUS * 2
        );
        
        // Рисуем линии искривления
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(warpColor.getRed(), warpColor.getGreen(), warpColor.getBlue(), 100));
        
        int lines = 16;
        double angleStep = Math.PI * 2 / lines;
        for (int i = 0; i < lines; i++) {
            double angle = i * angleStep;
            int x1 = mousePosition.x + (int)(Math.cos(angle) * WARP_RADIUS * 0.3);
            int y1 = mousePosition.y + (int)(Math.sin(angle) * WARP_RADIUS * 0.3);
            int x2 = mousePosition.x + (int)(Math.cos(angle) * WARP_RADIUS * (spaceWarp > 1.0 ? 1.2 : 0.8));
            int y2 = mousePosition.y + (int)(Math.sin(angle) * WARP_RADIUS * (spaceWarp > 1.0 ? 1.2 : 0.8));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
    
    private void applySpaceWarp(Ball ball) {
        if (mousePosition == null) return;
        
        double dx = ball.getX() - mousePosition.x;
        double dy = ball.getY() - mousePosition.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < WARP_RADIUS) {
            float force = (float)((1.0 - distance / WARP_RADIUS) * (spaceWarp - 1.0) * 0.5);
            double angle = Math.atan2(dy, dx);
            ball.addForce((float)(Math.cos(angle) * force), (float)(Math.sin(angle) * force));
        }
    }
    
    private void applySpaceWarp(Particle particle) {
        if (mousePosition == null) return;
        
        double dx = particle.getX() - mousePosition.x;
        double dy = particle.getY() - mousePosition.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < WARP_RADIUS) {
            float force = (float)((1.0 - distance / WARP_RADIUS) * (spaceWarp - 1.0) * 0.3);
            double angle = Math.atan2(dy, dx);
            particle.addForce((float)(Math.cos(angle) * force), (float)(Math.sin(angle) * force));
        }
    }

    private void drawTooltip(Graphics2D g2d) {
        if (tooltipPosition != null && tooltipText != null) {
            FontMetrics fm = g2d.getFontMetrics();
            int padding = 10;
            int tooltipWidth = fm.stringWidth(tooltipText) + padding * 2;
            int tooltipHeight = fm.getHeight() + padding * 2;

            // Проверяем, не выходит ли подсказка за пределы экрана
            if (tooltipPosition.x + tooltipWidth > getWidth()) {
                tooltipPosition.x = tooltipPosition.x - tooltipWidth - 20;
            }

            // Фон подсказки с градиентом
            GradientPaint gradientBg = new GradientPaint(
                tooltipPosition.x, tooltipPosition.y,
                new Color(0, 0, 0, 230),
                tooltipPosition.x + tooltipWidth, tooltipPosition.y + tooltipHeight,
                new Color(20, 20, 40, 230)
            );
            g2d.setPaint(gradientBg);
            g2d.fillRoundRect(tooltipPosition.x, tooltipPosition.y,
                            tooltipWidth, tooltipHeight, 10, 10);

            // Рамка подсказки
            g2d.setColor(new Color(100, 100, 255, 100));
            g2d.drawRoundRect(tooltipPosition.x, tooltipPosition.y,
                            tooltipWidth, tooltipHeight, 10, 10);

            // Текст подсказки
            g2d.setColor(Color.WHITE);
            g2d.drawString(tooltipText,
                          tooltipPosition.x + padding,
                          tooltipPosition.y + padding + fm.getAscent());
        }
    }

    private void drawBackground(Graphics2D g2d) {
        // Кэшируем размеры
        int w = canvasWidth;
        int h = canvasHeight;
        
        // Создаем базовый градиент фона только если нужно
        if (gravity > 0) {
            g2d.setPaint(new GradientPaint(
                0, 0, new Color(0, 0, 50),
                0, h, new Color(0, 0, (int)(100 + 155 * backgroundDistortion))
            ));
        } else {
            g2d.setPaint(new GradientPaint(
                0, 0, new Color(50, 0, 0),
                0, h, new Color((int)(100 + 155 * backgroundDistortion), 0, 0)
            ));
        }
        
        g2d.fillRect(0, 0, w, h);

        // Рисуем матричный эффект
        if (matrix.isActive()) {
            matrix.draw(g2d, w, h);
        }

        // Рисуем гравитационные волны
        if (gravityWave.isActive()) {
            gravityWave.draw(g2d, w, h);
        }

        // Рисуем радужный эффект
        if (rainbow.isActive()) {
            rainbow.draw(g2d, w, h);
        }

        // Рисуем эффект квантового туннелирования
        if (quantumTunnel.isActive()) {
            quantumTunnel.draw(g2d, w, h);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PhysicsSimulation simulation = new PhysicsSimulation();
            simulation.setVisible(true);
        });
    }
}