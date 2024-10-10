import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

class JuegoDelTopo extends JFrame implements ActionListener, MouseListener {
    private static final int NUM_HOYOS = 6;
    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 600;
    private static final int DIAMETRO_HOYO = 80;
    private static final int MARGEN = 50;
    private static final int TIEMPO_LIMITE = 60;
    private static final int VELOCIDAD = 500;

    private int[] posicionesX;
    private int[] posicionesY;
    private int topoActual;
    private boolean esTrampa;
    private int golpes;
    private int tiempoRestante;
    private JPanel panelJuego;
    private Timer timer, timerTiempo;
    private Image imagenTopo, imagenMazo, imagenTrampa;
    private Random random;
    private Clip musicaFondo;
    private Clip efectoGolpe;
    private Clip efectoTrampa;
    private Clip ganar;
    private Clip perder;

    public JuegoDelTopo() {
        setTitle("Juego del Topo");
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        random = new Random();
        inicializarPosiciones();

        panelJuego = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                dibujarFondo(g);
                dibujarHoyos(g);
                dibujarTopoOTrampa(g);
                dibujarPuntaje(g);
            }
        };
        panelJuego.addMouseListener(this);
        add(panelJuego);

        cargarImagenes();
        setCursorPersonalizado();
        cargarSonidos();
        iniciarJuego();
        reproducirMusicaFondo();
    }

    private void inicializarPosiciones() {
        posicionesX = new int[NUM_HOYOS];
        posicionesY = new int[NUM_HOYOS];

        int columnas = 3;
        int filas = NUM_HOYOS / columnas;
        int anchoCelda = (ANCHO_VENTANA - 2 * MARGEN) / columnas;
        int altoCelda = (ALTO_VENTANA - 2 * MARGEN) / filas;

        for (int i = 0; i < NUM_HOYOS; i++) {
            int fila = i / columnas;
            int columna = i % columnas;
            posicionesX[i] = MARGEN + columna * anchoCelda + anchoCelda / 2 - DIAMETRO_HOYO / 2;
            posicionesY[i] = MARGEN + fila * altoCelda + altoCelda / 2 - DIAMETRO_HOYO / 2;
        }
    }

    private void cargarImagenes() {
        imagenTopo = new ImageIcon("src/img/topo.png").getImage();
        imagenMazo = new ImageIcon("src/img/mazo.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        imagenTrampa = new ImageIcon("src/img/trampa.png").getImage();
    }

    private void setCursorPersonalizado() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor customCursor = toolkit.createCustomCursor(imagenMazo, new Point(0, 0), "Mazo");
        setCursor(customCursor);
    }

    private void cargarSonidos() {
        try {
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(AudioSystem.getAudioInputStream(new File("src/sound/musica_fondo.wav")));
            efectoGolpe = AudioSystem.getClip();
            efectoGolpe.open(AudioSystem.getAudioInputStream(new File("src/sound/golpe.wav")));
            efectoTrampa = AudioSystem.getClip();
            efectoTrampa.open(AudioSystem.getAudioInputStream(new File("src/sound/trampa.wav")));
            ganar = AudioSystem.getClip();
            ganar.open(AudioSystem.getAudioInputStream(new File("src/sound/ganar.wav")));
            perder = AudioSystem.getClip();
            perder.open(AudioSystem.getAudioInputStream(new File("src/sound/perder.wav")));
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    private void reproducirMusicaFondo() {
        if (musicaFondo != null) {
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void reproducirEfectoGolpe() {
        if (efectoGolpe != null) {
            efectoGolpe.setFramePosition(0);
            efectoGolpe.start();
        }
    }

    private void reproducirEfectoTrampa() {
        if (efectoTrampa != null) {
            efectoTrampa.setFramePosition(0);
            efectoTrampa.start();
        }
    }

    private void reproducirGanar() {
        if (ganar != null) {
            ganar.setFramePosition(0);
            ganar.start();
        }
    }

    private void reproducirPerder() {
        if (perder != null) {
            perder.setFramePosition(0);
            perder.start();
        }
    }

    private void iniciarJuego() {
        golpes = 0;
        tiempoRestante = TIEMPO_LIMITE;
        moverTopoOTrampaAleatoriamente();
        timer = new Timer(VELOCIDAD, this);
        timer.start();

        timerTiempo = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tiempoRestante--;
                if (tiempoRestante <= 0) {
                    timer.stop();
                    timerTiempo.stop();
                    reproducirPerder();
                    JOptionPane.showMessageDialog(null, "¡Se acabó el tiempo! Puntuación final: " + golpes);
                    reiniciarJuego();
                }
                repaint();
            }
        });
        timerTiempo.start();
    }

    private void moverTopoOTrampaAleatoriamente() {
        topoActual = random.nextInt(NUM_HOYOS);
        esTrampa = random.nextBoolean();
        repaint();
    }

    private void dibujarFondo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLACK, 0, ALTO_VENTANA, Color.DARK_GRAY);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(0, 0, ANCHO_VENTANA, ALTO_VENTANA));

        g.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(ANCHO_VENTANA);
            int y = random.nextInt(ALTO_VENTANA);
            g.drawLine(x, y, x, y + 5);
        }

    }

    private void dibujarHoyos(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i < NUM_HOYOS; i++) {
            g.fillOval(posicionesX[i], posicionesY[i], DIAMETRO_HOYO, DIAMETRO_HOYO);
        }
    }

    private void dibujarTopoOTrampa(Graphics g) {
        if (esTrampa) {
            g.drawImage(imagenTrampa, posicionesX[topoActual], posicionesY[topoActual], DIAMETRO_HOYO, DIAMETRO_HOYO, null);
        } else {
            g.drawImage(imagenTopo, posicionesX[topoActual], posicionesY[topoActual], DIAMETRO_HOYO, DIAMETRO_HOYO, null);
        }
    }

    private void dibujarPuntaje(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Golpes: " + golpes, 20, 30);
        g.drawString("Tiempo restante: " + tiempoRestante + "s", 560, 30);
    }

    private void reiniciarJuego() {
        golpes = 0;
        tiempoRestante = TIEMPO_LIMITE;
        moverTopoOTrampaAleatoriamente();
        timer.start();
        timerTiempo.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moverTopoOTrampaAleatoriamente();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (x >= posicionesX[topoActual] && x <= posicionesX[topoActual] + DIAMETRO_HOYO &&
                y >= posicionesY[topoActual] && y <= posicionesY[topoActual] + DIAMETRO_HOYO) {

            if (esTrampa) {
                golpes--;
                reproducirEfectoTrampa();
            } else {
                golpes++;
                reproducirEfectoGolpe();
            }

            if (golpes >= 10) {
                timer.stop();
                timerTiempo.stop();
                reproducirGanar();
                JOptionPane.showMessageDialog(this, "¡Ganaste el juego!");
                reiniciarJuego();
            } else {
                moverTopoOTrampaAleatoriamente();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JuegoDelTopo juego = new JuegoDelTopo();
            juego.setVisible(true);
        });
    }
}
