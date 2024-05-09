package org.dfpl.chronograph.chronoweb.visualization;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;

public class GraphPane extends JFrame {
	private static final long serialVersionUID = 1L;

	public GraphPane(MultiGraph g, SwingViewer viewer, View view) {
		Container container = getContentPane();
		JComponent graphView = (JComponent) view;
		container.add(graphView);
		viewer.enableAutoLayout();

		JMenuBar menuBar = new JMenuBar();
		JMenu connectMenu = new JMenu("Connect");
		JMenuItem connectMenuItem = new JMenuItem("connect");
		connectMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e);
			}
		});
		connectMenu.add(connectMenuItem);
		menuBar.add(connectMenu);
		setJMenuBar(menuBar);
		setTitle("Chronoweb - Graph Pane");
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
}
