package org.protege.editor.search.ui;

import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class LuceneUiUtils {

    public static Color
            MATTE_BORDER_COLOR = new Color(220, 220, 220);

    public static final Border
            MATTE_BORDER = new MatteBorder(1, 1, 1, 1, MATTE_BORDER_COLOR),
            EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    public static final String
            CLOSE_ICON_FILENAME = "close.png",
            BACK_ICON_FILENAME = "back.png",
            FORWARD_ICON_FILENAME = "forward.png";

    public static Icon getIcon(String filename, int width, int height) {
        BufferedImage icon = null;
        ClassLoader classLoader = LuceneUiUtils.class.getClassLoader();
        try {
            icon = ImageIO.read(checkNotNull(classLoader.getResource(filename)));
        } catch (IOException e) {
            ErrorLogPanel.showErrorDialog(e);
        }
        Image img = icon.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static List<OWLEntity> getProperties(OWLEditorKit editorKit) {
        List<OWLEntity> entities = new ArrayList<>();
        OWLOntology ont = editorKit.getModelManager().getActiveOntology();
        entities.addAll(ont.getAnnotationPropertiesInSignature());
        entities.addAll(ont.getObjectPropertiesInSignature());
        entities.addAll(ont.getDataPropertiesInSignature());
        return entities;
    }
}
