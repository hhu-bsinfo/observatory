package de.hhu.bsinfo.observatory.benchmark.plot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends JFrame {

    private final File resultDirectory;

    public MainWindow(File resultDirectory) throws IOException {
        this.resultDirectory = resultDirectory;

        InputStream iconStream = MainWindow.class.getClassLoader().getResourceAsStream("icon.png");

        if(iconStream != null) {
            setIconImage(ImageIO.read(iconStream));
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Observatory");
        setSize(800, 600);

        setupMenu();
        setupTabs();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> {
            MainWindow.this.setVisible(false);
            MainWindow.this.dispose();
        });

        fileMenu.add(closeItem);

        setJMenuBar(menuBar);
    }

    private void setupTabs() throws IOException {
        JTabbedPane tabbedPane = new JTabbedPane();

        File[] directories = resultDirectory.listFiles();

        if(directories == null) {
            return;
        }

        for(File directory : directories) {
            if(directory.isDirectory()) {
                tabbedPane.addTab(directory.getName(), new PlotPanel(directory));
            }
        }

        add(tabbedPane);
    }
}
