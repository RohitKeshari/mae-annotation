/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.controller.textpanel;

import edu.brandeis.llc.mae.controller.MaeMainController;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by krim on 1/9/2017.
 */
class TextPanelMouseListener extends MouseAdapter {

    private MaeMainController mainController;

    TextPanelMouseListener(MaeMainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            createAndShowContextMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            createAndShowContextMenu(e);
        }
    }

    private void createAndShowContextMenu(MouseEvent e) {
        mainController.createTextContextMenu().show(e.getComponent(), e.getX(), e.getY());

    }

}
