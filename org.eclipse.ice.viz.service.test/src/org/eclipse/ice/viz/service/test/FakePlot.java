/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jordan Deyton - Initial API and implementation and/or initial documentation 
 *   
 *******************************************************************************/
package org.eclipse.ice.viz.service.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ice.viz.service.widgets.PlotGridComposite;
import org.eclipse.ice.viz.service.IPlot;
import org.eclipse.ice.viz.service.ISeries;
import org.eclipse.ice.viz.service.csv.CSVSeries;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A simple {@link IPlot} implementation for testing things that draw
 * {@code IPlot}s, including the {@link PlotGridComposite} and the
 * {@link ICEResourcePage}.
 * 
 * @author Jordan Deyton
 * @author Kasper Gammeltoft - Refactored to work with the new IPlot
 *         implementation using <code> ISeries</code>.
 *
 */
public class FakePlot implements IPlot {

	/**
	 * A list of all child composites created when
	 * {@link #draw(String, String, Composite)} is called.
	 */
	public Composite child;

	/**
	 * The map of dependent series. This will not be populated with anything by
	 * default.
	 */
	public final Map<String, List<ISeries>> depSeries = new HashMap<String, List<ISeries>>();

	private int drawCount;
	
	/**
	 * The independent series for this plot. This is a plain new series by
	 * default.
	 */
	public ISeries indepSeries = new CSVSeries();

	/**
	 * The name or label for this plot
	 */
	public String name = "";

	public FakePlot() {
		drawCount = 0;
	}
	
	/**
	 * Adds the specified series to this plot under the given category
	 * 
	 * @param catagory
	 *            The category that this series falls under
	 * @param seriesToAdd
	 *            The series to add
	 */
	public void addDependentSeries(String catagory, ISeries seriesToAdd) {
		if (depSeries.get(catagory) == null) {
			depSeries.put(catagory, new ArrayList<ISeries>());
		}
		depSeries.get(catagory).add(seriesToAdd);

	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public Composite draw(Composite parent) throws Exception {
		System.err.println("Drawing fake plot... " + drawCount);
		drawCount++;
		child = new Composite(parent, SWT.NONE);
		child.setMenu(parent.getMenu());
		return child;
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public List<String> getCategories() {
		return new ArrayList<String>(depSeries.keySet());
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public URI getDataSource() {
		return null;
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public List<ISeries> getDependentSeries(String category) {
		return depSeries.get(category);
	}

	/**
	 * Gets the number of times that {@link #draw(Composite)} was called.
	 */
	public int getDrawCount() {
		return drawCount;
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public ISeries getIndependentSeries() {
		return indepSeries;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public int getNumberOfAxes() {
		return 0;
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public String getPlotTitle() {
		return name;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public Map<String, String> getProperties() {
		return null;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public String getSourceHost() {
		return null;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public boolean isSourceRemote() {
		return false;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public void redraw() {
		// Nothing TODO- fake plot
		return;
	}

	/**
	 * Removes the specified series from this plot.
	 * 
	 * @param series
	 *            The series to remove
	 */
	public void removeDependantSeries(ISeries series) {
		depSeries.remove(series);
		// If this series is in the list
		if (depSeries.containsValue(series)) {
			// Iterate to find the right key
			for (String key : depSeries.keySet()) {
				// Remove the series for the first category it is in in the map
				if (depSeries.get(key).contains(series)) {
					depSeries.get(key).remove(series);
					break;
				}
			}
		}
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public void setIndependentSeries(ISeries series) {
		indepSeries = series;
		return;
	}

	/*
	 * Implements a method from IPlot.
	 */
	@Override
	public void setPlotTitle(String title) {
		name = title;
		return;
	}

	/*
	 * Implements a method from IVizCanvas.
	 */
	@Override
	public void setProperties(Map<String, String> props) throws Exception {
		// Do nothing.
	}

}
