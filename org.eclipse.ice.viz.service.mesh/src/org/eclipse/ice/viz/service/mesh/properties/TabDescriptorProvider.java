/*******************************************************************************
 * Copyright (c) 2014 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.viz.service.mesh.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ice.viz.service.mesh.datastructures.BezierEdge;
import org.eclipse.ice.viz.service.mesh.datastructures.Edge;
import org.eclipse.ice.viz.service.mesh.datastructures.Hex;
import org.eclipse.ice.viz.service.mesh.datastructures.IMeshPartVisitor;
import org.eclipse.ice.viz.service.mesh.datastructures.Polygon;
import org.eclipse.ice.viz.service.mesh.datastructures.PolynomialEdge;
import org.eclipse.ice.viz.service.mesh.datastructures.Quad;
import org.eclipse.ice.viz.service.mesh.datastructures.Vertex;
import org.eclipse.ice.viz.service.mesh.datastructures.VizMeshComponent;
import org.eclipse.ice.viz.service.mesh.properties.BoundaryConditionSection.Type;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;

/**
 * This class provides an {@link ITabDescriptorProvider} that can be used to
 * generate dynamic tabs based on the selected {@link IMeshPart}.<br>
 * <br>
 * To use this ITabDescriptorProvider, you need to add the tabDescriptorProvider
 * attribute to the propertyContributor extension point in your plugin.xml file,
 * for example:
 * 
 * <pre>
 * {@code 
 * 	<extension point="org.eclipse.ui.views.properties.tabbed.propertyContributor">
 * 		<propertyContributor 
 * 			contributorId="id.of.class.implementing.ITabbedPropertySheetPageContributor"
 * 			tabDescriptorProvider="org.eclipse.ice.client.widgets.mesh.properties.TabDescriptorProvider">
 * 			<propertyCategory category="MeshElementTreeViewer"/>
 * 		</propertyContributor>
 * 	</extension>
 * }
 * </pre>
 * 
 * @author Jordan H. Deyton
 * 
 */
public class TabDescriptorProvider implements ITabDescriptorProvider,
		IMeshPartVisitor {

	/**
	 * The propertyCategory used by {@link TabDescriptorProvider}. This should
	 * be the same as the category used in the plugin.xml extension point.
	 */
	private static final String CATEGORY = "MeshSelection";

	/**
	 * This filter is used by the framework to determine if an object should
	 * actually create tabbed properties. Currently, this supports all
	 * IMeshParts.
	 */
	private final IFilter filter = new IFilter() {
		@Override
		public boolean select(Object toTest) {
			return (toTest instanceof MeshSelection);
		}
	};

	/**
	 * A List of ITabDescriptors. ITabDescriptors should be added to this List
	 * in the visit operations. It should be returned as an array in
	 * {@link #getTabDescriptors(IWorkbenchPart, ISelection)}.
	 */
	private final List<ITabDescriptor> tabDescriptors = new ArrayList<ITabDescriptor>();

	/**
	 * The mesh for the current selection.
	 */
	private VizMeshComponent mesh;

	/**
	 * Sets and returns {@link #tabDescriptors} based on the current selection.
	 */
	@Override
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part,
			ISelection selection) {

		// Reset the tab descriptors.
		tabDescriptors.clear();

		// Get the IMeshPart from the selection and visit it.
		Assert.isTrue(selection instanceof IStructuredSelection);
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		// TODO Incorporate multiple elements from the selection.

		// Visit the selected IMeshPart if possible.
		if (!structuredSelection.isEmpty()) {
			Object element = structuredSelection.getFirstElement();
			Assert.isTrue(element instanceof MeshSelection);
			MeshSelection meshSelection = (MeshSelection) element;

			// Get the mesh from the selection.
			mesh = meshSelection.mesh;

			// Visit the selected IMeshPart to get the available tabs.
			meshSelection.selectedMeshPart.acceptMeshVisitor(this);
		}

		// Convert the List of ITabDescriptors into an array..
		return tabDescriptors
				.toArray(new ITabDescriptor[tabDescriptors.size()]);
	}

	// ---- Implements IMeshPartVisitor ---- //
	@Override
	public void visit(VizMeshComponent mesh) {
		// Do nothing.
	}

	@Override
	public void visit(Polygon polygon) {

		// IDs used for the tabs.
		final String polygonTabId = "polygon";
		String edgeTabId = "edge";
		String vertexTabId = "vertex";

		AbstractTabDescriptor tabDescriptor;
		List<ISectionDescriptor> sectionDescriptors;
		int size;
		String lastTabId;

		// ---- Create a tab for the Polygon's information. ---- //
		// Create a tab for the Polygon's information.
		final String polygonTabLabel = polygon.getName() + " "
				+ polygon.getId();
		AbstractTabDescriptor polygonTab = new AbstractTabDescriptor() {
			@Override
			public String getCategory() {
				return CATEGORY;
			}

			@Override
			public String getId() {
				return polygonTabId;
			}

			@Override
			public String getLabel() {
				return polygonTabLabel;
			}
		};
		// Update the array of tab descriptors.
		tabDescriptors.add(polygonTab);

		// Re-create the List of ISectionDescriptors.
		sectionDescriptors = new ArrayList<ISectionDescriptor>();

		// Create a SectionDescriptor for the edge's general info section.
		sectionDescriptors.add(new AbstractSectionDescriptor() {
			@Override
			public String getTargetTab() {
				return polygonTabId;
			}

			@Override
			public ISection getSectionClass() {
				return new GeneralInfoSection();
			}

			@Override
			public String getId() {
				return polygonTabId + ".general";
			}

			@Override
			public IFilter getFilter() {
				return filter;
			}
		});

		// Set the section descriptors for the tab.
		polygonTab.setSectionDescriptors(sectionDescriptors);
		// ----------------------------------------------------- //

		// ---- Create tabs for each of the edges. ---- //
		ArrayList<Edge> edges = polygon.getEdges();
		size = edges.size();
		lastTabId = polygonTabId;
		for (int i = 0; i < size; i++) {
			Edge edge = edges.get(i);

			// Set the current tab ID.
			final String tabLabel = edge.getName() + " " + edge.getId();
			final String tabId = edgeTabId + i;
			final String fLastTabId = lastTabId;

			// Create a tab descriptor for the edge.
			tabDescriptor = new AbstractTabDescriptor() {
				@Override
				public String getCategory() {
					return CATEGORY;
				}

				@Override
				public String getId() {
					return tabId;
				}

				@Override
				public String getLabel() {
					return tabLabel;
				}

				@Override
				public String getAfterTab() {
					return fLastTabId;
				}
			};
			tabDescriptors.add(tabDescriptor);

			// Store a reference to the previous tab ID.
			lastTabId = tabId;

			// Re-create the List of ISectionDescriptors.
			sectionDescriptors = new ArrayList<ISectionDescriptor>();

			// Create a section for the edge's general information.
			final int fi = i;
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new EdgeInfoSection(fi);
				}

				@Override
				public String getId() {
					return tabId + ".general";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Create a section for the edge's fluid boundary condition.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new BoundaryConditionSection(Type.Fluid, 0, fi);
				}

				@Override
				public String getId() {
					return tabId + ".fluid";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Create a section for the edge's thermal boundary condition.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new BoundaryConditionSection(Type.Thermal, 0, fi);
				}

				@Override
				public String getId() {
					return tabId + ".thermal";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// TODO Create a section for other boundary conditions for the edge.

			// Set the section descriptors for the tab.
			tabDescriptor.setSectionDescriptors(sectionDescriptors);
		}
		// -------------------------------------------- //

		// ---- Create tabs for each of the vertices. ---- //
		ArrayList<Vertex> vertices = polygon.getVertices();
		size = vertices.size();
		for (int i = 0; i < size; i++) {
			Vertex vertex = vertices.get(i);

			// Set the current tab ID.
			final String tabLabel = vertex.getName() + " " + vertex.getId();
			final String tabId = vertexTabId + i;
			final String fLastTabId = lastTabId;

			// Create a tab descriptor for the edge.
			tabDescriptor = new AbstractTabDescriptor() {
				@Override
				public String getCategory() {
					return CATEGORY;
				}

				@Override
				public String getId() {
					return tabId;
				}

				@Override
				public String getLabel() {
					return tabLabel;
				}

				@Override
				public String getAfterTab() {
					return fLastTabId;
				}
			};
			tabDescriptors.add(tabDescriptor);

			// Store a reference to the previous tab ID.
			lastTabId = tabId;

			// Re-create the List of ISectionDescriptors.
			sectionDescriptors = new ArrayList<ISectionDescriptor>();

			// Create a section for the vertex's general information.
			final int fi = i;
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new VertexInfoSection(fi);
				}

				@Override
				public String getId() {
					return tabId + ".general";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Create a section for the vertex's location.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new VertexSection(fi);
				}

				@Override
				public String getId() {
					return tabId + ".location";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Set the section descriptors for the tab.
			tabDescriptor.setSectionDescriptors(sectionDescriptors);
		}
		// ----------------------------------------------- //

		return;
	}

	@Override
	public void visit(Quad quad) {
		// Re-direct to the standard polygon operation for now.
		visit((Polygon) quad);
	}

	@Override
	public void visit(Hex hex) {
		// Re-direct to the standard polygon operation for now.
		visit((Polygon) hex);
	}

	@Override
	public void visit(Edge edge) {

		// The IDs of the tabs that will be created.
		final String edgeTabId = "edge";
		final String vertexTabId = "vertex";
		final String conditionsTabId = "conditions";

		int edgeId = edge.getId();
		AbstractTabDescriptor tabDescriptor;
		List<ISectionDescriptor> sectionDescriptors;
		String lastTabId;

		// ---- Create a tab for the Edge's information. ---- //
		// Create a tab for the edge.
		final String edgeTabLabel = edge.getName() + " " + edge.getId();
		AbstractTabDescriptor edgeTab = new AbstractTabDescriptor() {
			@Override
			public String getCategory() {
				return CATEGORY;
			}

			@Override
			public String getId() {
				return edgeTabId;
			}

			@Override
			public String getLabel() {
				return edgeTabLabel;
			}
		};
		tabDescriptors.add(edgeTab);

		// Re-create the list of ISectionDescriptors.
		sectionDescriptors = new ArrayList<ISectionDescriptor>();
		// Create a SectionDescriptor for the edge's general info section.
		sectionDescriptors.add(new AbstractSectionDescriptor() {
			@Override
			public String getTargetTab() {
				return edgeTabId;
			}

			@Override
			public ISection getSectionClass() {
				return new GeneralInfoSection();
			}

			@Override
			public String getId() {
				return edgeTabId + ".general";
			}

			@Override
			public IFilter getFilter() {
				return filter;
			}
		});

		// Set the section descriptors for the tab.
		edgeTab.setSectionDescriptors(sectionDescriptors);
		// -------------------------------------------------- //

		// ---- Create tabs for the vertices. ---- //
		lastTabId = edgeTabId;
		for (int i = 0; i < 2; i++) {
			Vertex vertex = mesh.getVertex(edge.getVertexIds()[i]);

			// Set the current tab ID.
			final String tabLabel = vertex.getName() + " " + vertex.getId();
			final String tabId = vertexTabId + i;
			final String fLastTabId = lastTabId;

			// Create a tab descriptor for the edge.
			tabDescriptor = new AbstractTabDescriptor() {
				@Override
				public String getCategory() {
					return CATEGORY;
				}

				@Override
				public String getId() {
					return tabId;
				}

				@Override
				public String getLabel() {
					return tabLabel;
				}

				@Override
				public String getAfterTab() {
					return fLastTabId;
				}
			};
			tabDescriptors.add(tabDescriptor);

			// Store a reference to the previous tab ID.
			lastTabId = tabId;

			// Re-create the List of ISectionDescriptors.
			sectionDescriptors = new ArrayList<ISectionDescriptor>();

			// Create a section for the vertex's general information.
			final int fi = i;
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new VertexInfoSection(fi);
				}

				@Override
				public String getId() {
					return tabId + ".general";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Create a section for the vertex's location.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new VertexSection(fi);
				}

				@Override
				public String getId() {
					return tabId + ".location";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Set the section descriptors for the tab.
			tabDescriptor.setSectionDescriptors(sectionDescriptors);
		}
		// --------------------------------------- //

		// ---- Create tabs for the BoundaryConditions. ---- //
		ArrayList<Polygon> polygons = mesh.getPolygonsFromEdge(edgeId);
		for (int i = 0; i < polygons.size(); i++) {
			Polygon polygon = polygons.get(i);

			// Set the current tab ID.
			final String tabLabel = "Boundary Conditions (" + polygon.getId()
					+ ")";
			final String tabId = conditionsTabId + i;
			final String fLastTabId = lastTabId;

			// Create a tab descriptor for the boundary conditions.
			tabDescriptor = new AbstractTabDescriptor() {
				@Override
				public String getCategory() {
					return CATEGORY;
				}

				@Override
				public String getId() {
					return tabId;
				}

				@Override
				public String getLabel() {
					return tabLabel;
				}

				@Override
				public String getAfterTab() {
					return fLastTabId;
				}
			};
			tabDescriptors.add(tabDescriptor);

			// Store a reference to the previous tab ID.
			lastTabId = tabId;

			// Re-create the List of ISectionDescriptors.
			sectionDescriptors = new ArrayList<ISectionDescriptor>();

			final int fi = i;

			// Create a section for the fluid boundary condition.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new BoundaryConditionSection(Type.Fluid, 0, fi);
				}

				@Override
				public String getId() {
					return tabId + ".fluid";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// Create a section for the thermal boundary condition.
			sectionDescriptors.add(new AbstractSectionDescriptor() {
				@Override
				public String getTargetTab() {
					return tabId;
				}

				@Override
				public ISection getSectionClass() {
					return new BoundaryConditionSection(Type.Thermal, 0, fi);
				}

				@Override
				public String getId() {
					return tabId + ".thermal";
				}

				@Override
				public IFilter getFilter() {
					return filter;
				}
			});
			// TODO Create a section for other boundary conditions on the edge.
			// Set the section descriptors for the tab.
			tabDescriptor.setSectionDescriptors(sectionDescriptors);
		}
		// ------------------------------------------------- //

		return;
	}

	@Override
	public void visit(BezierEdge edge) {
		// Re-direct to the standard edge operation for now.
		visit((Edge) edge);
	}

	@Override
	public void visit(PolynomialEdge edge) {
		// Re-direct to the standard edge operation for now.
		visit((Edge) edge);
	}

	@Override
	public void visit(Vertex vertex) {

		// Create a TabDescriptor for the Vertex. We only need one tab!
		final String tabLabel = vertex.getName() + " " + vertex.getId();
		AbstractTabDescriptor vertexTab = new AbstractTabDescriptor() {
			@Override
			public String getCategory() {
				return CATEGORY;
			}

			@Override
			public String getId() {
				return "vertex";
			}

			@Override
			public String getLabel() {
				return tabLabel;
			}
		};
		// Update the array of tab descriptors.
		tabDescriptors.add(vertexTab);

		// Create a SectionDescriptor for the vertex's general info section.
		AbstractSectionDescriptor generalSection = new AbstractSectionDescriptor() {
			@Override
			public String getTargetTab() {
				return "vertex";
			}

			@Override
			public ISection getSectionClass() {
				return new GeneralInfoSection();
			}

			@Override
			public String getId() {
				return "vertex.general";
			}

			@Override
			public IFilter getFilter() {
				return filter;
			}
		};

		// Create a SectionDescriptor for the vertex's location section.
		AbstractSectionDescriptor locationSection = new AbstractSectionDescriptor() {
			@Override
			public String getTargetTab() {
				return "vertex";
			}

			@Override
			public ISection getSectionClass() {
				return new VertexSection();
			}

			@Override
			public String getId() {
				return "vertex.location";
			}

			@Override
			public IFilter getFilter() {
				return filter;
			}

			@Override
			public String getAfterSection() {
				// Set this section to appear after the "General" section.
				return "vertex.general";
			}
		};

		// Create a List from the SectionDescriptors and set them for the tab.
		List<ISectionDescriptor> sectionDescriptors = new ArrayList<ISectionDescriptor>();
		sectionDescriptors.add(generalSection);
		sectionDescriptors.add(locationSection);
		vertexTab.setSectionDescriptors(sectionDescriptors);

		return;
	}

	@Override
	public void visit(Object object) {
		// Do nothing.
	}
	// ------------------------------------- //

}
