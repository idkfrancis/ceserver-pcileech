/*
 * This file is part of ceserver-pcileech by Isabella Flores
 *
 * Copyright © 2021-2022 Isabella Flores
 *
 * It is licensed to you under the terms of the
 * GNU Affero General Public License, Version 3.0.
 * Please see the file LICENSE for more information.
 */

package iflores.ceserver.pcileech;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static iflores.ceserver.pcileech.Constants.DEFAULT_PORT_NUMBER;

public class MainFrame extends JFrame implements RunningServerListener {

    private static final AtomicReference<RunningServer> _server = new AtomicReference<>();
    private final JPanel _settingsPanel;
    private final JButton _startStopButton;
    private final JTextArea _outputArea;

    public MainFrame(Settings settings) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    RunningServer server = _server.get();
                    if (server != null) {
                        try {
                            server.shutdownNowAndWait();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                })
        );

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        _settingsPanel = new JPanel(new GridBagLayout());
        _settingsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(_settingsPanel, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        try {
            JLabel label = new JLabel("<html>If you like this software, <a href=''><font color='blue'>you should take a look at our other project</font></a>, Vamos!</html>");
//            label.setForeground(Color.BLUE);
            label.setFont(new Font(Font.SERIF, Font.BOLD, 18));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            bottomPanel.add(label, BorderLayout.NORTH);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        openWebpage(new URL("https://github.com/isabellaflores/vamos/blob/master/README.md"));
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        GridBagConstraintsBuilder column0 = new GridBagConstraintsBuilder()
                .gridx(0)
                .fill(GridBagConstraints.BOTH)
                .weightx(0.0)
                .insets(new Insets(2, 2, 2, 2))
                .anchor(GridBagConstraints.EAST);
        GridBagConstraintsBuilder column1 = new GridBagConstraintsBuilder()
                .gridx(1)
                .fill(GridBagConstraints.BOTH)
                .weightx(1.0)
                .insets(new Insets(2, 2, 2, 2))
                .anchor(GridBagConstraints.WEST);

        _settingsPanel.add(new JLabel("MemProcFS.exe Location:", SwingConstants.RIGHT), column0);

        JTextField memprocFsPathTextField = new JTextField(30);
        memprocFsPathTextField.setText(settings.getMemProcFsExePath());
        memprocFsPathTextField.setEditable(false);
        JButton browseButton = new JButton("Browse...");
        JPanel memprocFsPathPanel = new JPanel(new BorderLayout());
        memprocFsPathPanel.add(memprocFsPathTextField, BorderLayout.CENTER);
        memprocFsPathPanel.add(browseButton, BorderLayout.EAST);
        _settingsPanel.add(memprocFsPathPanel, column1);

        _settingsPanel.add(new JLabel("Arguments to PCILeech:", SwingConstants.RIGHT), column0);

        JTextField argsTextField = new JTextField(30);
        argsTextField.setText(settings.getPciLeechArguments());
        _settingsPanel.add(argsTextField, column1);

        _settingsPanel.add(new JLabel("Listen Port:", SwingConstants.RIGHT), column0);

        JSpinner portNumberSpinner = new JSpinner(
                new SpinnerNumberModel(settings.getPortNumber(), 1, 65535, 1)
        );
        JPanel portNumberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        portNumberPanel.add(portNumberSpinner);
        JLabel portNumberDefaultLabel = new JLabel("(Default is " + DEFAULT_PORT_NUMBER + ")", SwingConstants.LEFT);
        portNumberDefaultLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        portNumberPanel.add(portNumberDefaultLabel);
        _settingsPanel.add(portNumberPanel, column1);

        _startStopButton = new JButton();
        _startStopButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        _startStopButton.setFocusable(false);
        buttonPanel.add(_startStopButton);

        _outputArea = new JTextArea(10, 30);
        _outputArea.setEditable(false);
        _outputArea.setAutoscrolls(true);
        add(new JScrollPane(_outputArea), BorderLayout.CENTER);

        browseButton.addActionListener(e -> {
            File result = openFileDialog_MemProcFsExe();
            if (result != null) {
                settings.setMemProcFsExePath(result.getAbsolutePath());
                memprocFsPathTextField.setText(settings.getMemProcFsExePath());
                settings.save();
            }
        });

        _startStopButton.addActionListener(
                e -> startServer(settings)
        );

        argsTextField.getDocument().addDocumentListener(new DocumentChangeListener(() -> {
            settings.setPciLeechArguments(argsTextField.getText());
            settings.save();
        }));

        portNumberSpinner.addChangeListener(e -> {
            settings.setPortNumber((Integer) portNumberSpinner.getValue());
            settings.save();
        });

        updateServerState();
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        if (settings.getMemProcFsExePath() != null && ! settings.getMemProcFsExePath().isEmpty()) {
            startServer(settings);
        }
    }

    private void startServer(Settings settings) {
        RunningServer server = _server.get();
        if (server == null) {
            _outputArea.setText("");
            _startStopButton.setText("Stop Server");
            ProcessBuilder pb = new ProcessBuilder(
                    System.getProperty("java.home") + "\\bin\\java.exe",
                    "-classpath",
                    System.getProperty("java.class.path"),
                    ServerMain.class.getName(),
                    String.valueOf(settings.getPortNumber()),
                    settings.getMemProcFsExePath(),
                    settings.getPciLeechArguments().trim()
            );
            pb.redirectInput(ProcessBuilder.Redirect.PIPE);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectErrorStream(true);
            try {
                Process p = pb.start();
                server = new RunningServer(p);
                _server.set(server);
                server.addListener(this);
                server.start();
                updateServerState();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            server.shutdownNow();
            _startStopButton.setEnabled(false);
        }
    }

    private static void enableContainer(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                enableContainer((Container) component, enabled);
            }
            component.setEnabled(enabled);
        }
    }

    private void updateServerState() {
        if (_server.get() == null) {
            enableContainer(_settingsPanel, true);
            _startStopButton.setText("Start Server");
            _startStopButton.setForeground(Color.WHITE);
            _startStopButton.setBackground(Color.GREEN.darker());
        } else {
            enableContainer(_settingsPanel, false);
            _startStopButton.setText("Stop Server");
            _startStopButton.setForeground(Color.WHITE);
            _startStopButton.setBackground(Color.RED.darker());
        }
    }

    private File openFileDialog_MemProcFsExe() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().equalsIgnoreCase("MemProcFS.exe");
                    }

                    @Override
                    public String getDescription() {
                        return "MemProcFS.exe";
                    }
                }
        );
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    @Override
    public void charsRead(String chars) {
        _outputArea.append(chars);
    }

    @Override
    public void closed(Integer exitCode) {
        _server.set(null);
        if (!_outputArea.getText().isEmpty() && !_outputArea.getText().endsWith("\n")) {
            _outputArea.append("\n");
        }
        if (exitCode == null) {
            _outputArea.append("*** Server died unexpectedly\n");
        } else if (exitCode == 0) {
            _outputArea.append("*** Server shut down normally\n");
        } else {
            _outputArea.append("*** Server died with exit code " + exitCode + "\n");
        }
        updateServerState();
        _startStopButton.setEnabled(true);
    }

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
