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

package edu.brandeis.llc.mae.controller;

import edu.brandeis.llc.mae.MaeHotKeys;
import edu.brandeis.llc.mae.MaeStrings;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.database.MaeDriverI;
import edu.brandeis.llc.mae.io.AnnotationLoader;
import edu.brandeis.llc.mae.io.MaeFileWriter;
import edu.brandeis.llc.mae.io.MaeIOException;
import edu.brandeis.llc.mae.model.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * All popups and supplement sub windows are managed within this class
 */
class DialogController {
    private MaeMainController mainController;
    private JFileChooser fileChooser;

    DialogController(MaeMainController mainController) {
        this.mainController = mainController;

        this.fileChooser = new JFileChooser(getMainController().getLastWorkingDirectory()) {
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this
                            ,"We found the file! Do you want to overwrite?"
                            ,MaeStrings.WARN_POPUP_TITLE,
                            JOptionPane.YES_NO_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        default:
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    }

    JFileChooser getFileChooser() {
        return fileChooser;
    }

    MaeMainController getMainController() {
        return mainController;
    }

    int showWarning(String warnMessage) {
        return JOptionPane.showConfirmDialog(null, warnMessage, MaeStrings.WARN_POPUP_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    boolean popupMessageWithToggle(String message) {
        JCheckBox toggleCheck = new JCheckBox("Do not show this again");
        toggleCheck.setSelected(false);
        Object[] params = {message, toggleCheck};
        JOptionPane.showMessageDialog(null, params, MaeStrings.INFO_POPUP_TITLE, JOptionPane.PLAIN_MESSAGE);
        return toggleCheck.isSelected();
    }

    void popupMessage(String message) {
        JOptionPane.showMessageDialog(null, message, MaeStrings.INFO_POPUP_TITLE, JOptionPane.PLAIN_MESSAGE);
    }

    void showError(Exception e) {
        // TODO: 1/1/2016 maybe can implement "send error log to dev" button
        String errorTitle = e.getClass().getName();
        String errorMessage = e.getMessage();
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.WARNING_MESSAGE);

    }

    void showError(String message, Exception e) {
        String errorTitle = e.getClass().getName();
        String errorMessage = String.format("%s: %s", message, e.getMessage());
        JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.WARNING_MESSAGE);

    }

    void showError(String message) {
        JOptionPane.showMessageDialog(null, message, MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);

    }

    File showFileChooseDialogAndSelect(String defaultName, boolean saveFile) {
        File curDir = fileChooser.getCurrentDirectory();
        if (defaultName.length() > 0) {
            fileChooser.setSelectedFile(new File(defaultName));
        }

        if (saveFile) {
            fileChooser.setCurrentDirectory(new File(getMainController().getSaveDirectory()));
            int response = fileChooser.showSaveDialog(null);
            fileChooser.setCurrentDirectory(curDir);
            if (response == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        } else {
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        }
        return null;
    }

    void setAsArgumentDialog(String argumentTid) throws MaeDBException {
        if (getMainController().getDriver().getAllLinkTagsOfAllTypes().size() == 0) {
            showWarning("No link tags are found.");
        } else {
            SetArgumentOptionPanel options = new SetArgumentOptionPanel();
            int result = JOptionPane.showConfirmDialog(getMainController().getMainWindow(),
                    options,
                    String.format("Setting %s an argument of", argumentTid),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                LinkTag linker = options.getSelectedLinkTag();
                ArgumentType argType = options.getSelectedArgumentType();
                getMainController().surgicallyUpdateCell(linker, argType.getName() + MaeStrings.ARG_IDCOL_SUF, argumentTid);
            }
        }

    }

    LinkTag createLinkDialog(TagType linkType, List<ExtentTag> candidates) throws MaeDBException {
        CreateLinkOptionPanel options = new CreateLinkOptionPanel(linkType, candidates);
        int result = JOptionPane.showConfirmDialog(getMainController().getMainWindow(),
                options,
                String.format("Create a new link: %s ", linkType.getName()),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Map<ArgumentType, ExtentTag> arguments = options.getSelectedArguments();
            LinkTag linker = (LinkTag) getMainController().createTagFromMenu(linkType);
            for (ArgumentType argType : arguments.keySet()) {
                String argTypeName = argType.getName();
                String argTid = arguments.get(argType).getTid();
                if (getMainController().isAdjudicating()) {
                    getMainController().addArgument(linker, argType, argTid);
                    getMainController().adjudicationStatUpdate();
                } else {
                    getMainController().surgicallyUpdateCell(linker, argTypeName + MaeStrings.ARG_IDCOL_SUF, argTid);
                }
            }
            return linker;

        }
        return null;
    }

    boolean showIncompleteTagsWarning(Set<Tag> incomplete, boolean simplyWarn) {
        if (incomplete == null || incomplete.size() < 1) {
            return true;
        }
        UnderspecifiedTagsWarningOptionPanel options = new UnderspecifiedTagsWarningOptionPanel(incomplete, simplyWarn);
        options.setVisible(true);
        switch (options.getResponse()) {
            case JOptionPane.YES_OPTION:
                options.dispose();
                return true;
            case JOptionPane.CANCEL_OPTION:
                getMainController().selectTagAndTable(options.getSelectedTag());
            default:
                options.dispose();
                return false;
        }
    }

    public File showStartAdjudicationDialog() throws MaeControlException, MaeDBException, MaeIOException {
        Object[] options = {MaeStrings.START_ADJUD_NEW_GS_OPTION,
                MaeStrings.START_ADJUD_LOAD_GS_OPTION,
                MaeStrings.CANCEL};
        int response = JOptionPane.showOptionDialog(null,
                MaeStrings.START_ADJUD_MSG,
                MaeStrings.ADJUD_DIALOG_TITLE,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        switch (response) {
            case JOptionPane.YES_OPTION:
                return getNewGoldstandardFile();
            case JOptionPane.NO_OPTION:
                return getExistingGoldstandardFile();
            default:
                return null;
        }
    }

    File getExistingGoldstandardFile() throws MaeIOException, MaeDBException {
        File existingGS = showFileChooseDialogAndSelect(MaeStrings.DEF_GS_FILE, false);
        AnnotationLoader xmlLoader = new AnnotationLoader(getMainController().getDriver());
        if (existingGS == null) {
            showError("Cannot open the file: " + existingGS.getName());
            return null;
        } else if (!xmlLoader.isFileMatchesCurrentWork(existingGS)) {
            showError("Primary text do not match");
            return null;
        }
        return existingGS;
    }

    File getNewGoldstandardFile() throws MaeIOException, MaeDBException {
        File newGS = showFileChooseDialogAndSelect(MaeStrings.DEF_GS_FILE, true);
        if (newGS != null) {
            MaeFileWriter.writeTextToEmptyXML(getMainController().getDriver().getPrimaryText(),
                    getMainController().getDriver().getTaskName(), newGS);
            return newGS;
        }
        return null;
    }

    class UnderspecifiedTagsWarningOptionPanel extends JDialog {
        private final String tidSep = " - ";
        JList<String> incompleteTags;
        int response;

        UnderspecifiedTagsWarningOptionPanel(Set<Tag> incomplete, boolean simplyWarn) {
            super(getMainController().getMainWindow(), MaeStrings.UNDERSPEC_TITLE, true);
            setSize(100, 200);

            final JButton yes = makeYesButton();
            final JButton no = makeNoButton();
            final JButton see = makeSeeButton();
            see.setEnabled(false);


            String[] incompleteTagsDetail = getMissingDetails(incomplete);
            incompleteTags = new JList<>(incompleteTagsDetail);
            addListenersToList(see);
            JPanel buttons = new JPanel(new FlowLayout());
            buttons.add(yes);
            if (!simplyWarn) buttons.add(no);
            buttons.add(see);

            String tag = incomplete.size() == 1? "tag" : "tags";
            add(new JLabel(String.format(MaeStrings.UNDERSPEC_MSG, incomplete.size(), tag),
                            SwingConstants.CENTER),
                    BorderLayout.NORTH);
            add(new JScrollPane(incompleteTags), BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
            setLocationRelativeTo(getMainController().getMainWindow());
            pack();
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    response = JOptionPane.NO_OPTION;

                }
            });
            KeyStroke stroke   = MaeHotKeys.ksESC;

            getRootPane().registerKeyboardAction(e -> no(),
                    stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().setDefaultButton(yes);

        }

        private void yes() {
            response = JOptionPane.YES_OPTION;
            setVisible(false);
        }

        private void no() {
            response = JOptionPane.NO_OPTION;
            setVisible(false);
        }
        private void see() {
            response = JOptionPane.CANCEL_OPTION;
            setVisible(false);
        }

        private void addListenersToList(final JButton see) {
            incompleteTags.addListSelectionListener(e -> {
                see.setEnabled(true);
                see.requestFocusInWindow();
            });
            incompleteTags.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getClickCount() == 2) {
                        see();
                    }
                }
            });
        }

        private String[] getMissingDetails(Set<Tag> incomplete) {
            String[] incompleteTagsDetail = new String[incomplete.size()];
            int i = 0;
            for (Tag tag : incomplete) {
                incompleteTagsDetail[i++] = String.format("%s%smissing: %s",
                        tag.getId(),
                        tidSep,
                        tag.getUnderspec());
            }
            return incompleteTagsDetail;
        }

        private JButton makeYesButton() {
            return makeButton("Yes", MaeHotKeys.mnYES_BUTTON, JOptionPane.YES_OPTION);
        }

        private JButton makeNoButton() {
            return makeButton("No", MaeHotKeys.mnNO_BUTTON, JOptionPane.NO_OPTION);
        }

        private JButton makeSeeButton() {
            return makeButton("See", MaeHotKeys.mnSEE_BUTTON, JOptionPane.CANCEL_OPTION);
        }

        private JButton makeButton(String label, int mnemonic, final int responsevalue) {
            final JButton button = new JButton(label);
            button.setMnemonic(mnemonic);
            button.addActionListener(e -> {
                response = responsevalue;
                setVisible(false);
            });
            return button;
        }

        public Tag getSelectedTag() {
            String tid = incompleteTags.getSelectedValue().split(tidSep)[0];
            return getMainController().getTagByTid(tid);
        }

        public int getResponse() {
            return response;
        }
    }


    class SetArgumentOptionPanel extends JPanel {

        final MaeDriverI driver = getMainController().getDriver();
        final JComboBox<TagType> linkTypes = new JComboBox<>();
        final JComboBox<ArgumentType> argTypes = new JComboBox<>();
        final JComboBox<LinkTag> linkTags = new JComboBox<>();

        SetArgumentOptionPanel() throws MaeDBException {
            super(new GridLayout(6, 1));

            argTypes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TagType type = (TagType) linkTypes.getSelectedItem();
                    if (argTypes.getSelectedItem() != null) {
                        String argTypeName = ((ArgumentType) argTypes.getSelectedItem()).getName();
                        try {
                            List<LinkTag> prioritized = getPrioritizedLinkTags(type, argTypeName);

                            linkTags.removeAllItems();
                            for (LinkTag link : prioritized) {
                                linkTags.addItem(link);
                            }

                        } catch (MaeDBException ex) {
                            getMainController().showError(ex);
                        }
                    }

                }

                List<LinkTag> getPrioritizedLinkTags(TagType type, String argTypeName) throws MaeDBException {
                    List<LinkTag> prioritized = new ArrayList<>();
                    for (LinkTag link : driver.getAllLinkTagsOfType(type)) {
                        if (link.getArgumentByTypeName(argTypeName) == null) {
                            prioritized.add(0, link);
                        } else {
                            prioritized.add(link);
                        }
                    }
                    return prioritized;
                }
            });

            linkTypes.addActionListener(e -> {
                TagType type = (TagType) linkTypes.getSelectedItem();

                try {
                    argTypes.removeAllItems();
                    for (ArgumentType argType : driver.getArgumentTypesOfLinkTagType(type)) {
                        argTypes.addItem(argType);
                    }
                } catch (MaeDBException ex) {
                    getMainController().showError(ex);
                }

            });

            for (TagType type : driver.getLinkTagTypes()) {
                if (driver.getAllLinkTagsOfType(type).size() > 0) linkTypes.addItem(type);
            }

            add(new JLabel(MaeStrings.SETARG_SEL_TAGTYPE));
            add(linkTypes);
            add(new JLabel(MaeStrings.SETARG_SEL_ARGTYPE));
            add(argTypes);
            add(new JLabel(MaeStrings.SETARG_SEL_TAG));
            add(linkTags);

        }

        LinkTag getSelectedLinkTag() {
            return (LinkTag) linkTags.getSelectedItem();

        }
        ArgumentType getSelectedArgumentType() {
            return (ArgumentType) argTypes.getSelectedItem();

        }

    }

    class CreateLinkOptionPanel extends JPanel {

        private Map<ArgumentType, ExtentTag> argumentsMap;
        // selected arguments should not be null or empty by now (checked in menu controller)
        final Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);


        CreateLinkOptionPanel(TagType linkType, List<ExtentTag> argumentCandidates) throws MaeDBException {
            super();

            List<ArgumentType> argTypes = new ArrayList<>(linkType.getArgumentTypes());
            setLayout(new GridLayout(1, argTypes.size()));

            argumentsMap = new LinkedHashMap<>();

            int typeNum = 0;
            for (final ArgumentType type : argTypes) {
                final JComboBox<ExtentTag> candidates = new JComboBox<>();
                candidates.setFont(MaeStrings.UNICODE_FONT);
                candidates.addItem(null);
                for (ExtentTag tag : argumentCandidates) {
                    candidates.addItem(tag);
                }
                candidates.addActionListener(event ->
                        argumentsMap.put(type, (ExtentTag) candidates.getSelectedItem()));
                candidates.setSelectedIndex((typeNum++ % argumentCandidates.size()) + 1);
                JPanel comboPanel = new JPanel();
                comboPanel.add(candidates);
                TitledBorder titledBorder = BorderFactory.createTitledBorder(
                        etched, type.getName());
                titledBorder.setTitleJustification(TitledBorder.CENTER);
                comboPanel.setBorder(titledBorder);
                add(comboPanel);
            }

        }

        Map<ArgumentType, ExtentTag> getSelectedArguments() {
            return argumentsMap;

        }
    }


}
