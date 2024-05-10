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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class GraphPane extends JFrame {
	private static final long serialVersionUID = 1L;

	private Graph modelGraph;
	private MultiGraph visGraph;

	public GraphPane(Graph graph, MultiGraph g, SwingViewer viewer, View view) {
		this.modelGraph = graph;
		this.visGraph = g;
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
				for (Edge edge : modelGraph.getEdges()) {
					Vertex out = edge.getVertex(Direction.OUT);
					Vertex in = edge.getVertex(Direction.IN);
					visGraph.addNode(out.toString());
					visGraph.addNode(in.toString());
					visGraph.addEdge(out.toString() + "|" + in.toString(), out.toString(), in.toString(), true);
				}
			}
		});
		connectMenu.add(connectMenuItem);
		menuBar.add(connectMenu);
		setJMenuBar(menuBar);
		setTitle("Chronoweb - TPVis");
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
}
