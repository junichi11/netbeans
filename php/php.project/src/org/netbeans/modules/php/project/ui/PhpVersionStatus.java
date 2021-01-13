/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.project.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.awt.StatusLineElementProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@NbBundle.Messages({
    "PhpVersionStatus.version.unknown=Unknown",
})
public final class PhpVersionStatus {

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
    private static final JLabel PHP_VERSION_LABEL = new JLabel(""); // NOI18N
    private static final String UNKNOWN = Bundle.PhpVersionStatus_version_unknown();

    private PhpVersionStatus() {
    }

    static {
        EditorRegistry.addPropertyChangeListener(propertyChangeEvent -> updatePhpVersionComponent());
        PHP_VERSION_LABEL.addMouseListener(new MouseAdapterImpl());
        initMinDimension(getDimensions());
    }

    private static JList<PhpVersion> createPhpVersionList(JTextComponent component, FileObject fileObject) {
        JList<PhpVersion> phpVersionList = new JList<>();
        DefaultListModel<PhpVersion> model = new DefaultListModel<>();

        for (PhpVersion phpVersion : PhpVersion.values()) {
            model.addElement(phpVersion);
        }

        phpVersionList.setModel(model);
        PhpVersion phpVersion = PhpLanguageProperties.forFileObject(fileObject).getPhpVersion();
        phpVersionList.setSelectedValue(phpVersion, true);
        phpVersionList.setBorder(new LineBorder(Color.GRAY, 1));

        phpVersionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                FileObject fileObject = NbEditorUtilities.getFileObject(component.getDocument());
                if (fileObject != null) {
                    Project project = FileOwnerQuery.getOwner(fileObject);
                    if (project instanceof PhpProject) {
                        PhpProjectProperties projectProperties = new PhpProjectProperties((PhpProject) project);
                        projectProperties.setPhpVersion(phpVersionList.getSelectedValue().name());
                        projectProperties.save();
                    }
                }
                showPhpVersion(component.getDocument(), PHP_VERSION_LABEL);
            }
        });
        return phpVersionList;
    }

    private static Popup getPopup(JList<PhpVersion> phpVersionList) {
        Point labelStart = PHP_VERSION_LABEL.getLocationOnScreen();
        int x = Math.min(labelStart.x, labelStart.x + PHP_VERSION_LABEL.getSize().width - phpVersionList.getPreferredSize().width);
        int y = labelStart.y - phpVersionList.getPreferredSize().height;

        final Popup popup = PopupFactory.getSharedInstance().getPopup(PHP_VERSION_LABEL, phpVersionList, x, y);
        final AWTEventListener multicastListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0) {
                    popup.hide();
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(multicastListener, AWTEvent.MOUSE_EVENT_MASK);
        return popup;
    }

    private static Collection<String> getDimensions() {
        Collection<String> dimensions = new ArrayList<>();
        for (PhpVersion phpVersion : PhpVersion.values()) {
            dimensions.add(phpVersion.getDisplayName());
        }
        dimensions.add(UNKNOWN);
        return dimensions;
    }

    private static void updatePhpVersionComponent() {
        assert EventQueue.isDispatchThread();
        final JTextComponent comp = EditorRegistry.focusedComponent();
        if (comp != null) {
            showPhpVersion(comp.getDocument(), PHP_VERSION_LABEL);
        } else {
            PHP_VERSION_LABEL.setToolTipText(""); // NOI18N
            PHP_VERSION_LABEL.setText(""); // NOI18N
            PHP_VERSION_LABEL.setVisible(false);
        }
    }

    private static void showPhpVersion(Document document, JLabel label) {
        FileObject fileObject = NbEditorUtilities.getFileObject(document);
        PhpVersion phpVersion = null;
        if (fileObject != null) {
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (project instanceof PhpProject) {
                phpVersion = PhpLanguageProperties.forFileObject(fileObject).getPhpVersion();
                if (phpVersion != null) {
                    label.setToolTipText(((PhpProject) project).getName());
                    label.setText(phpVersion.getDisplayName());
                    label.setVisible(true);
                }
            }
        }
        if (phpVersion == null) {
            label.setToolTipText(""); // NOI18N
            label.setText(""); // NOI18N
            label.setVisible(false);
        }
    }

    private static void initMinDimension(Iterable<? extends String> maxStrings) {
        FontMetrics fm = PHP_VERSION_LABEL.getFontMetrics(PHP_VERSION_LABEL.getFont());
        int minWidth = 0;
        for (String s : maxStrings) {
            minWidth = Math.max(minWidth, fm.stringWidth(s));
        }
        Border b = PHP_VERSION_LABEL.getBorder();
        Insets ins = (b != null) ? b.getBorderInsets(PHP_VERSION_LABEL) : NULL_INSETS;
        minWidth += ins.left + ins.right;
        int minHeight = fm.getHeight() + ins.top + ins.bottom;
        PHP_VERSION_LABEL.setMinimumSize(new Dimension(minWidth, minHeight));
        PHP_VERSION_LABEL.setPreferredSize(new Dimension(minWidth, minHeight));
    }

    private static Component panelWithSeparator(JLabel cell) {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            private static final long serialVersionUID = 4857471448025818174L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(3, 3); // Y-unimportant -> gridlayout will stretch it
            }
        };
        separator.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(separator, BorderLayout.WEST);
        panel.add(cell);
        return panel;
    }

    //~ Inner classes
    @ServiceProvider(service = StatusLineElementProvider.class)
    public static final class StatusLineElementProviderImpl implements StatusLineElementProvider {

        @Override
        public Component getStatusLineElement() {
            return panelWithSeparator(PHP_VERSION_LABEL);
        }
    }

    private static class MouseAdapterImpl extends MouseAdapter {

        public MouseAdapterImpl() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final JTextComponent component = EditorRegistry.focusedComponent();
            if (component == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            FileObject fileObject = NbEditorUtilities.getFileObject(component.getDocument());
            if (fileObject == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            final JList<PhpVersion> phpVersionList = createPhpVersionList(component, fileObject);
            final Popup popup = getPopup(phpVersionList);
            popup.show();
        }
    }
}
