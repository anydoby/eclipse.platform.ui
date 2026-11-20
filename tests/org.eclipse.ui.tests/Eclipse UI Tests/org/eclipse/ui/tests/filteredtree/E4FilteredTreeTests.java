/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.filteredtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class E4FilteredTreeTests {
	private Shell shell;
	private FilteredTree filteredTree;
	private TestElement root;

	@Before
	public void setUp() {
		shell = new Shell();
		shell.setLayout(new FillLayout());
		filteredTree = new FilteredTree(shell, SWT.SINGLE | SWT.BORDER, new PatternFilter(), 0);
		root = TestElement.createModel(3, 10);
		TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new TestModelContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(root);
		shell.setSize(400, 500);
	}

	@After
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testQuickSelectionModeSelectsFirstMatch() {
		filteredTree.setQuickSelectionMode(true);
		TestElement leaf = root.getChildAt(4).getChildAt(2).getChildAt(7);

		applyFilter(leaf.toString());

		assertEquals(leaf, filteredTree.getViewer().getStructuredSelection().getFirstElement());
	}

	@Test
	public void testNoSelectionWithoutQuickSelectionMode() {
		TestElement leaf = root.getChildAt(4).getChildAt(2).getChildAt(7);

		applyFilter(leaf.toString());

		assertTrue(filteredTree.getViewer().getStructuredSelection().isEmpty());
	}

	@Test
	public void testQuickSelectionModeWithoutMatchSelectsNothing() {
		filteredTree.setQuickSelectionMode(true);
		TreeViewer viewer = filteredTree.getViewer();

		filteredTree.getFilterControl().setText("no such element");
		assertTrue("Filter was not applied", DisplayHelper.waitForCondition(shell.getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() == 0));

		assertTrue(viewer.getStructuredSelection().isEmpty());
	}

	@Test
	public void testQuickSelectionModeSelectsEqualLabel() {
		useFileTree();

		assertQuickSelection("File", "File");
	}

	@Test
	public void testQuickSelectionModeAfterEditsBeforeRefresh() {
		useFileTree();
		assertQuickSelection("Fil", "Files");

		filteredTree.getFilterControl().setText("Filex");
		assertQuickSelection("File", "File");
	}

	// "Project from File" is the first match for "File", but not the best one
	private void useFileTree() {
		Map<Object, List<String>> children = Map.of( //
				"", List.of("Project from File", "Files", "General"), //
				"General", List.of("File", "Untitled Text File"));
		TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return children.getOrDefault(parentElement, List.of()).toArray();
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return children.containsKey(element);
			}
		});
		viewer.setInput("");
		filteredTree.setQuickSelectionMode(true);
	}

	private void assertQuickSelection(String filter, String expected) {
		TreeViewer viewer = filteredTree.getViewer();
		filteredTree.getFilterControl().setText(filter);
		DisplayHelper.waitForCondition(shell.getDisplay(), 5000,
				() -> expected.equals(viewer.getStructuredSelection().getFirstElement()));
		assertEquals(expected, viewer.getStructuredSelection().getFirstElement());
	}

	private void applyFilter(String text) {
		TreeViewer viewer = filteredTree.getViewer();
		filteredTree.getFilterControl().setText(text);
		assertTrue("Filter was not applied", DisplayHelper.waitForCondition(shell.getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() == 1));
	}
}
